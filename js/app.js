/**
 * Main App Controller - Âm Lịch Việt Nam PWA
 */

const App = (() => {
    // State
    let currentYear, currentMonth; // Solar month being viewed
    let selectedDate = null; // { day, month, year }
    let editingEventId = null;
    let deferredPrompt = null; // PWA install prompt
    let allUserEvents = []; // Cache
    let yearPickerBase = 0; // Base year for year picker grid

    const WEEKDAYS = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];
    const MONTHS = ['', 'Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
                    'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'];

    // ============ INIT ============
    function init() {
        const today = new Date();
        currentYear = today.getFullYear();
        currentMonth = today.getMonth() + 1;
        selectedDate = { day: today.getDate(), month: currentMonth, year: currentYear };

        registerServiceWorker();
        setupEventListeners();
        loadUserEvents().then(() => {
            renderCalendar();
            updateDayInfo();
        });
        NotificationManager.init();
        setupInstallPrompt();
    }

    // ============ SERVICE WORKER ============
    async function registerServiceWorker() {
        if ('serviceWorker' in navigator) {
            try {
                const reg = await navigator.serviceWorker.register('/sw.js');
                console.log('SW registered:', reg.scope);
            } catch (e) {
                console.log('SW registration failed:', e);
            }
        }
    }

    // ============ PWA INSTALL ============
    function setupInstallPrompt() {
        window.addEventListener('beforeinstallprompt', (e) => {
            e.preventDefault();
            deferredPrompt = e;
            document.getElementById('nav-install').style.display = 'block';
        });
    }

    async function promptInstall() {
        if (!deferredPrompt) return;
        deferredPrompt.prompt();
        const { outcome } = await deferredPrompt.userChoice;
        if (outcome === 'accepted') {
            showToast('Đã cài đặt ứng dụng!');
        }
        deferredPrompt = null;
        document.getElementById('nav-install').style.display = 'none';
    }

    // ============ EVENT LISTENERS ============
    function setupEventListeners() {
        // Navigation
        document.getElementById('btn-prev-month').addEventListener('click', () => navigateMonth(-1));
        document.getElementById('btn-next-month').addEventListener('click', () => navigateMonth(1));
        document.getElementById('btn-today').addEventListener('click', goToToday);
        document.getElementById('btn-menu').addEventListener('click', openSidebar);
        document.getElementById('btn-close-sidebar').addEventListener('click', closeSidebar);
        document.getElementById('sidebar-overlay').addEventListener('click', closeSidebar);

        // Month/Year pickers
        document.getElementById('header-month').addEventListener('click', toggleMonthPicker);
        document.getElementById('header-year').addEventListener('click', toggleYearPicker);
        document.getElementById('btn-year-prev').addEventListener('click', () => navigateYearPicker(-12));
        document.getElementById('btn-year-next').addEventListener('click', () => navigateYearPicker(12));

        // Close pickers when clicking outside
        document.addEventListener('click', (e) => {
            const monthPicker = document.getElementById('month-picker');
            const yearPicker = document.getElementById('year-picker');
            const headerMonth = document.getElementById('header-month');
            const headerYear = document.getElementById('header-year');
            if (!monthPicker.contains(e.target) && e.target !== headerMonth) {
                monthPicker.classList.add('hidden');
                headerMonth.classList.remove('active');
            }
            if (!yearPicker.contains(e.target) && e.target !== headerYear &&
                !e.target.closest('.picker-year-nav')) {
                yearPicker.classList.add('hidden');
                headerYear.classList.remove('active');
            }
        });

        // Sidebar nav
        document.getElementById('nav-events').addEventListener('click', (e) => { e.preventDefault(); closeSidebar(); openEventList(); });
        document.getElementById('nav-convert').addEventListener('click', (e) => { e.preventDefault(); closeSidebar(); openConvertModal(); });
        document.getElementById('nav-upcoming').addEventListener('click', (e) => { e.preventDefault(); closeSidebar(); openUpcomingModal(); });
        document.getElementById('nav-notification').addEventListener('click', (e) => { e.preventDefault(); closeSidebar(); openNotificationModal(); });
        document.getElementById('nav-install').addEventListener('click', (e) => { e.preventDefault(); closeSidebar(); promptInstall(); });
        document.getElementById('nav-about').addEventListener('click', (e) => { e.preventDefault(); closeSidebar(); showAbout(); });

        // Modals close
        document.getElementById('btn-close-event-modal').addEventListener('click', closeEventModal);
        document.getElementById('btn-close-event-list').addEventListener('click', () => toggleModal('modal-event-list', false));
        document.getElementById('btn-close-convert').addEventListener('click', () => toggleModal('modal-convert', false));
        document.getElementById('btn-close-upcoming').addEventListener('click', () => toggleModal('modal-upcoming', false));
        document.getElementById('btn-close-notification').addEventListener('click', () => toggleModal('modal-notification', false));

        // Event form
        document.getElementById('form-event').addEventListener('submit', handleEventSubmit);
        document.getElementById('btn-delete-event').addEventListener('click', handleEventDelete);
        document.getElementById('btn-add-event').addEventListener('click', () => { toggleModal('modal-event-list', false); openAddEvent(); });

        // Chip groups
        setupChipGroups();

        // Convert inputs
        ['convert-solar-day', 'convert-solar-month', 'convert-solar-year'].forEach(id => {
            document.getElementById(id).addEventListener('input', handleSolarToLunar);
        });
        ['convert-lunar-day', 'convert-lunar-month', 'convert-lunar-year'].forEach(id => {
            document.getElementById(id).addEventListener('input', handleLunarToSolar);
        });

        // Notification
        document.getElementById('btn-enable-notification').addEventListener('click', handleEnableNotification);

        // Swipe gestures
        setupSwipeGestures();

        // Keyboard
        document.addEventListener('keydown', (e) => {
            if (e.key === 'ArrowLeft') navigateMonth(-1);
            if (e.key === 'ArrowRight') navigateMonth(1);
            if (e.key === 'Escape') { closeAllPickers(); closeAllModals(); }
        });
    }

    function setupChipGroups() {
        document.querySelectorAll('.chip-group').forEach(group => {
            group.addEventListener('click', (e) => {
                if (e.target.classList.contains('chip')) {
                    group.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
                    e.target.classList.add('active');
                }
            });
        });
    }

    function setupSwipeGestures() {
        let touchStartX = 0;
        const grid = document.getElementById('calendar-grid');
        grid.addEventListener('touchstart', (e) => { touchStartX = e.changedTouches[0].screenX; }, { passive: true });
        grid.addEventListener('touchend', (e) => {
            const diff = e.changedTouches[0].screenX - touchStartX;
            if (Math.abs(diff) > 60) {
                navigateMonth(diff > 0 ? -1 : 1);
            }
        }, { passive: true });
    }

    // ============ CALENDAR RENDERING ============
    async function loadUserEvents() {
        allUserEvents = await EventsDB.getAllEvents();
    }

    function renderCalendar() {
        // Header
        document.getElementById('header-month').textContent = MONTHS[currentMonth];
        document.getElementById('header-year').textContent = currentYear;

        const grid = document.getElementById('calendar-grid');
        grid.innerHTML = '';

        // First day of month (0=Sun, 6=Sat)
        const firstDay = new Date(currentYear, currentMonth - 1, 1).getDay();
        const daysInMonth = new Date(currentYear, currentMonth, 0).getDate();
        const daysInPrevMonth = new Date(currentYear, currentMonth - 1, 0).getDate();

        const today = new Date();
        const todayStr = `${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`;

        // Previous month days
        for (let i = firstDay - 1; i >= 0; i--) {
            const day = daysInPrevMonth - i;
            const prevMonth = currentMonth === 1 ? 12 : currentMonth - 1;
            const prevYear = currentMonth === 1 ? currentYear - 1 : currentYear;
            const cell = createCalendarCell(day, prevMonth, prevYear, true);
            grid.appendChild(cell);
        }

        // Current month days
        for (let day = 1; day <= daysInMonth; day++) {
            const cell = createCalendarCell(day, currentMonth, currentYear, false);
            grid.appendChild(cell);
        }

        // Next month days (fill remaining cells to 42)
        const totalCells = grid.children.length;
        const remaining = 42 - totalCells;
        for (let i = 1; i <= remaining; i++) {
            const nextMonth = currentMonth === 12 ? 1 : currentMonth + 1;
            const nextYear = currentMonth === 12 ? currentYear + 1 : currentYear;
            const cell = createCalendarCell(i, nextMonth, nextYear, true);
            grid.appendChild(cell);
        }
    }

    function createCalendarCell(day, month, year, isOutside) {
        const cell = document.createElement('div');
        cell.className = 'calendar-cell';
        if (isOutside) cell.classList.add('outside');

        const lunar = VietCalendar.solarToLunar(day, month, year);
        const dayOfWeek = new Date(year, month - 1, day).getDay();

        // Day of week styling
        if (dayOfWeek === 0) cell.classList.add('sunday');
        if (dayOfWeek === 6) cell.classList.add('saturday');

        // Today
        const today = new Date();
        if (day === today.getDate() && month === today.getMonth() + 1 && year === today.getFullYear()) {
            cell.classList.add('today');
        }

        // Selected
        if (selectedDate && day === selectedDate.day && month === selectedDate.month && year === selectedDate.year) {
            cell.classList.add('selected');
        }

        // Solar day
        const solarEl = document.createElement('span');
        solarEl.className = 'solar-day';
        solarEl.textContent = day;
        cell.appendChild(solarEl);

        // Lunar day
        const lunarEl = document.createElement('span');
        lunarEl.className = 'lunar-day';
        if (lunar.day === 1) {
            lunarEl.textContent = `${lunar.day}/${lunar.month}`;
            lunarEl.style.color = '#e65100';
        } else {
            lunarEl.textContent = lunar.day;
        }
        cell.appendChild(lunarEl);

        // Event dot
        const hasHoliday = EventsDB.getHolidaysForSolarDate(day, month).length > 0 ||
                           EventsDB.getHolidaysForLunarDate(lunar.day, lunar.month).length > 0;
        const hasUserEvt = allUserEvents.some(e => {
            if (e.repeat === 'MONTHLY') return e.lunarDay === lunar.day;
            return e.lunarDay === lunar.day && e.lunarMonth === lunar.month;
        });

        if (hasHoliday || hasUserEvt) {
            const dot = document.createElement('span');
            dot.className = 'event-dot';
            cell.appendChild(dot);
        }

        // Click handler
        cell.addEventListener('click', () => {
            selectedDate = { day, month, year };
            renderCalendar();
            updateDayInfo();
        });

        return cell;
    }

    // ============ DAY INFO ============
    async function updateDayInfo() {
        if (!selectedDate) return;
        const { day, month, year } = selectedDate;
        const lunar = VietCalendar.solarToLunar(day, month, year);
        const info = CanChi.getFullInfo(lunar);
        const jdn = lunar.julianDay;
        const dayOfWeek = new Date(year, month - 1, day).getDay();

        // Solar info
        document.getElementById('info-weekday').textContent = WEEKDAYS[dayOfWeek];
        document.getElementById('info-solar-day').textContent = day;
        document.getElementById('info-solar-month-year').textContent = `Tháng ${month}, ${year}`;

        // Rating badge
        const badge = document.getElementById('info-rating-badge');
        const advice = info.dayAdvice;
        badge.textContent = advice.ratingLabel;
        badge.className = 'rating-badge';
        const ratingClass = {
            'VERY_GOOD': 'very-good', 'GOOD': 'good', 'NORMAL': 'normal',
            'BAD': 'bad', 'VERY_BAD': 'very-bad'
        };
        badge.classList.add(ratingClass[advice.rating]);

        // Lunar info
        const leapStr = lunar.isLeapMonth ? ' (nhuận)' : '';
        document.getElementById('info-lunar-date').textContent =
            `Ngày ${lunar.day} tháng ${info.tenThangAm}${leapStr} năm ${CanChi.canChiNam(lunar.year)}`;

        document.getElementById('info-canchi-ngay').textContent = info.ngay;
        document.getElementById('info-canchi-thang').textContent = info.thang;
        document.getElementById('info-canchi-nam').textContent = info.nam;
        document.getElementById('info-tiet-khi').textContent = info.tietKhi;
        document.getElementById('info-gio-hoang-dao').textContent = info.gioHoangDao;
        document.getElementById('info-truc').textContent = `${info.truc} - ${info.trucDetail}`;
        document.getElementById('info-ngu-hanh').textContent = info.nguHanhNapAm;
        document.getElementById('info-hoang-dao').textContent = info.hoangDao.label;

        // Day advice
        const nenLamEl = document.getElementById('info-nen-lam');
        nenLamEl.innerHTML = advice.nenLam.map(t => `<span class="advice-tag">${t}</span>`).join('');
        const khongNenEl = document.getElementById('info-khong-nen');
        khongNenEl.innerHTML = advice.khongNen.map(t => `<span class="advice-tag">${t}</span>`).join('');

        // Events
        const { holidays, userEvents } = await EventsDB.getAllEventsForDay(day, month, lunar.day, lunar.month);
        const eventsSection = document.getElementById('day-events');
        const eventsList = document.getElementById('info-events-list');

        if (holidays.length > 0 || userEvents.length > 0) {
            eventsSection.style.display = 'block';
            eventsList.innerHTML = '';
            [...holidays, ...userEvents].forEach(evt => {
                const item = document.createElement('div');
                item.className = 'event-item';
                item.textContent = evt.name || evt.title;
                eventsList.appendChild(item);
            });
        } else {
            eventsSection.style.display = 'none';
        }
    }

    // ============ NAVIGATION ============
    function navigateMonth(delta) {
        currentMonth += delta;
        if (currentMonth > 12) { currentMonth = 1; currentYear++; }
        if (currentMonth < 1) { currentMonth = 12; currentYear--; }
        closeAllPickers();
        renderCalendar();
    }

    function goToToday() {
        const today = new Date();
        currentYear = today.getFullYear();
        currentMonth = today.getMonth() + 1;
        selectedDate = { day: today.getDate(), month: currentMonth, year: currentYear };
        closeAllPickers();
        renderCalendar();
        updateDayInfo();
    }

    // ============ MONTH PICKER ============
    function toggleMonthPicker() {
        const picker = document.getElementById('month-picker');
        const yearPicker = document.getElementById('year-picker');
        const headerMonth = document.getElementById('header-month');
        const headerYear = document.getElementById('header-year');

        // Close year picker if open
        yearPicker.classList.add('hidden');
        headerYear.classList.remove('active');

        if (picker.classList.contains('hidden')) {
            renderMonthPicker();
            picker.classList.remove('hidden');
            headerMonth.classList.add('active');
        } else {
            picker.classList.add('hidden');
            headerMonth.classList.remove('active');
        }
    }

    function renderMonthPicker() {
        const grid = document.getElementById('month-picker-grid');
        grid.innerHTML = '';
        const todayMonth = new Date().getMonth() + 1;
        const todayYear = new Date().getFullYear();

        for (let m = 1; m <= 12; m++) {
            const btn = document.createElement('button');
            btn.className = 'picker-cell';
            btn.textContent = `Tháng ${m}`;
            if (m === currentMonth) btn.classList.add('current');
            if (m === todayMonth && currentYear === todayYear) btn.classList.add('today-marker');
            btn.addEventListener('click', () => {
                currentMonth = m;
                closeAllPickers();
                renderCalendar();
            });
            grid.appendChild(btn);
        }
    }

    // ============ YEAR PICKER ============
    function toggleYearPicker() {
        const picker = document.getElementById('year-picker');
        const monthPicker = document.getElementById('month-picker');
        const headerYear = document.getElementById('header-year');
        const headerMonth = document.getElementById('header-month');

        // Close month picker if open
        monthPicker.classList.add('hidden');
        headerMonth.classList.remove('active');

        if (picker.classList.contains('hidden')) {
            yearPickerBase = currentYear - (currentYear % 12);
            renderYearPicker();
            picker.classList.remove('hidden');
            headerYear.classList.add('active');
        } else {
            picker.classList.add('hidden');
            headerYear.classList.remove('active');
        }
    }

    function renderYearPicker() {
        const grid = document.getElementById('year-picker-grid');
        grid.innerHTML = '';
        const todayYear = new Date().getFullYear();
        const rangeStart = yearPickerBase;
        const rangeEnd = yearPickerBase + 11;

        document.getElementById('year-picker-range').textContent = `${rangeStart} - ${rangeEnd}`;

        for (let y = rangeStart; y <= rangeEnd; y++) {
            const btn = document.createElement('button');
            btn.className = 'picker-cell';
            btn.textContent = y;
            if (y === currentYear) btn.classList.add('current');
            if (y === todayYear) btn.classList.add('today-marker');
            btn.addEventListener('click', () => {
                currentYear = y;
                closeAllPickers();
                renderCalendar();
            });
            grid.appendChild(btn);
        }
    }

    function navigateYearPicker(delta) {
        yearPickerBase += delta;
        renderYearPicker();
    }

    function closeAllPickers() {
        document.getElementById('month-picker').classList.add('hidden');
        document.getElementById('year-picker').classList.add('hidden');
        document.getElementById('header-month').classList.remove('active');
        document.getElementById('header-year').classList.remove('active');
    }

    // ============ SIDEBAR ============
    function openSidebar() {
        document.getElementById('sidebar').classList.remove('hidden');
        document.getElementById('sidebar-overlay').classList.remove('hidden');
    }

    function closeSidebar() {
        document.getElementById('sidebar').classList.add('hidden');
        document.getElementById('sidebar-overlay').classList.add('hidden');
    }

    // ============ MODALS ============
    function toggleModal(id, show) {
        const modal = document.getElementById(id);
        if (show) {
            modal.classList.remove('hidden');
        } else {
            modal.classList.add('hidden');
        }
    }

    function closeAllModals() {
        document.querySelectorAll('.modal').forEach(m => m.classList.add('hidden'));
        closeSidebar();
    }

    // ============ EVENT MANAGEMENT ============
    function openAddEvent() {
        editingEventId = null;
        document.getElementById('modal-event-title').textContent = 'Thêm sự kiện';
        document.getElementById('form-event').reset();
        document.getElementById('btn-delete-event').style.display = 'none';
        document.getElementById('event-remind-time').value = '07:00';

        // Pre-fill with selected lunar date
        if (selectedDate) {
            const lunar = VietCalendar.solarToLunar(selectedDate.day, selectedDate.month, selectedDate.year);
            document.getElementById('event-lunar-day').value = lunar.day;
            document.getElementById('event-lunar-month').value = lunar.month;
        }

        // Reset chips
        document.querySelectorAll('.chip-group .chip').forEach(c => c.classList.remove('active'));
        document.querySelector('#event-type-group [data-value="GIO"]').classList.add('active');
        document.querySelector('#event-repeat-group [data-value="YEARLY"]').classList.add('active');
        document.querySelector('#event-remind-group [data-value="0"]').classList.add('active');

        toggleModal('modal-event', true);
    }

    async function openEditEvent(id) {
        const evt = await EventsDB.getEvent(id);
        if (!evt) return;

        editingEventId = id;
        document.getElementById('modal-event-title').textContent = 'Sửa sự kiện';
        document.getElementById('event-title').value = evt.title || '';
        document.getElementById('event-note').value = evt.note || '';
        document.getElementById('event-lunar-day').value = evt.lunarDay;
        document.getElementById('event-lunar-month').value = evt.lunarMonth;
        document.getElementById('event-lunar-year').value = evt.lunarYear || 0;
        document.getElementById('event-remind-time').value =
            `${String(evt.remindHour || 7).padStart(2, '0')}:${String(evt.remindMinute || 0).padStart(2, '0')}`;
        document.getElementById('btn-delete-event').style.display = 'block';

        // Set chips
        setChipValue('event-type-group', evt.eventType || 'KHAC');
        setChipValue('event-repeat-group', evt.repeat || 'YEARLY');
        setChipValue('event-remind-group', String(evt.remindDaysBefore || 0));

        toggleModal('modal-event', true);
    }

    function setChipValue(groupId, value) {
        const group = document.getElementById(groupId);
        group.querySelectorAll('.chip').forEach(c => {
            c.classList.toggle('active', c.dataset.value === value);
        });
    }

    function getChipValue(groupId) {
        const active = document.querySelector(`#${groupId} .chip.active`);
        return active ? active.dataset.value : null;
    }

    async function handleEventSubmit(e) {
        e.preventDefault();
        const title = document.getElementById('event-title').value.trim();
        if (!title) return;

        const time = document.getElementById('event-remind-time').value.split(':');
        const eventData = {
            title,
            note: document.getElementById('event-note').value.trim(),
            lunarDay: parseInt(document.getElementById('event-lunar-day').value),
            lunarMonth: parseInt(document.getElementById('event-lunar-month').value),
            lunarYear: parseInt(document.getElementById('event-lunar-year').value) || 0,
            eventType: getChipValue('event-type-group') || 'KHAC',
            repeat: getChipValue('event-repeat-group') || 'YEARLY',
            remindDaysBefore: parseInt(getChipValue('event-remind-group')) || 0,
            remindHour: parseInt(time[0]) || 7,
            remindMinute: parseInt(time[1]) || 0,
            isEnabled: true
        };

        if (editingEventId) {
            eventData.id = editingEventId;
            eventData.createdAt = (await EventsDB.getEvent(editingEventId)).createdAt;
            await EventsDB.updateEvent(eventData);
            showToast('Đã cập nhật sự kiện');
        } else {
            await EventsDB.addEvent(eventData);
            showToast('Đã thêm sự kiện');
        }

        closeEventModal();
        await loadUserEvents();
        renderCalendar();
        updateDayInfo();
    }

    async function handleEventDelete() {
        if (!editingEventId) return;
        if (confirm('Bạn có chắc muốn xóa sự kiện này?')) {
            await EventsDB.deleteEvent(editingEventId);
            showToast('Đã xóa sự kiện');
            closeEventModal();
            await loadUserEvents();
            renderCalendar();
            updateDayInfo();
        }
    }

    function closeEventModal() {
        toggleModal('modal-event', false);
        editingEventId = null;
    }

    async function openEventList() {
        const events = await EventsDB.getAllEvents();
        const container = document.getElementById('event-list-container');
        container.innerHTML = '';

        if (events.length === 0) {
            container.innerHTML = '<p style="text-align:center;color:#999;padding:20px">Chưa có sự kiện nào</p>';
        } else {
            events.forEach(evt => {
                const icons = { GIO: '🪦', SINH_NHAT: '🎂', LE: '🎉', KHAC: '📌' };
                const item = document.createElement('div');
                item.className = 'event-list-item';
                item.innerHTML = `
                    <span class="event-icon">${icons[evt.eventType] || '📌'}</span>
                    <div class="event-details">
                        <div class="event-name">${evt.title}</div>
                        <div class="event-date">${evt.lunarDay}/${evt.lunarMonth} âm lịch - ${evt.repeat === 'YEARLY' ? 'Hàng năm' : evt.repeat === 'MONTHLY' ? 'Hàng tháng' : 'Một lần'}</div>
                    </div>
                    <button class="event-toggle ${evt.isEnabled ? 'on' : 'off'}" data-id="${evt.id}"></button>
                `;
                item.querySelector('.event-details').addEventListener('click', () => {
                    toggleModal('modal-event-list', false);
                    openEditEvent(evt.id);
                });
                item.querySelector('.event-toggle').addEventListener('click', async (e) => {
                    e.stopPropagation();
                    evt.isEnabled = !evt.isEnabled;
                    await EventsDB.updateEvent(evt);
                    e.target.className = `event-toggle ${evt.isEnabled ? 'on' : 'off'}`;
                });
                container.appendChild(item);
            });
        }

        toggleModal('modal-event-list', true);
    }

    // ============ DATE CONVERT ============
    function openConvertModal() {
        if (selectedDate) {
            document.getElementById('convert-solar-day').value = selectedDate.day;
            document.getElementById('convert-solar-month').value = selectedDate.month;
            document.getElementById('convert-solar-year').value = selectedDate.year;
            handleSolarToLunar();
        }
        toggleModal('modal-convert', true);
    }

    function handleSolarToLunar() {
        const day = parseInt(document.getElementById('convert-solar-day').value);
        const month = parseInt(document.getElementById('convert-solar-month').value);
        const year = parseInt(document.getElementById('convert-solar-year').value);
        const resultEl = document.getElementById('convert-result-lunar');

        if (!day || !month || !year || year < 1900 || year > 2100) {
            resultEl.textContent = '';
            return;
        }

        try {
            const lunar = VietCalendar.solarToLunar(day, month, year);
            const leapStr = lunar.isLeapMonth ? ' (nhuận)' : '';
            resultEl.textContent = `Ngày ${lunar.day} tháng ${lunar.month}${leapStr} năm ${lunar.year} (${CanChi.canChiNam(lunar.year)})`;
        } catch (e) {
            resultEl.textContent = 'Ngày không hợp lệ';
        }
    }

    function handleLunarToSolar() {
        const day = parseInt(document.getElementById('convert-lunar-day').value);
        const month = parseInt(document.getElementById('convert-lunar-month').value);
        const year = parseInt(document.getElementById('convert-lunar-year').value);
        const resultEl = document.getElementById('convert-result-solar');

        if (!day || !month || !year || year < 1900 || year > 2100) {
            resultEl.textContent = '';
            return;
        }

        try {
            const solar = VietCalendar.lunarToSolar(day, month, year, 0);
            const dow = new Date(solar.year, solar.month - 1, solar.day).getDay();
            resultEl.textContent = `${WEEKDAYS[dow]}, ${solar.day}/${solar.month}/${solar.year}`;
        } catch (e) {
            resultEl.textContent = 'Ngày không hợp lệ';
        }
    }

    // ============ UPCOMING EVENTS ============
    async function openUpcomingModal() {
        const upcoming = await EventsDB.getUpcomingEvents(90);
        const container = document.getElementById('upcoming-list');
        container.innerHTML = '';

        if (upcoming.length === 0) {
            container.innerHTML = '<p style="text-align:center;color:#999;padding:20px">Không có sự kiện nào trong 90 ngày tới</p>';
        } else {
            upcoming.forEach(item => {
                let urgencyClass;
                if (item.daysUntil === 0) urgencyClass = 'urgent-today';
                else if (item.daysUntil <= 3) urgencyClass = 'urgent-soon';
                else if (item.daysUntil <= 7) urgencyClass = 'urgent-week';
                else urgencyClass = 'urgent-later';

                const el = document.createElement('div');
                el.className = `upcoming-item ${urgencyClass}`;
                el.innerHTML = `
                    <div class="upcoming-days">${item.daysUntil === 0 ? '🔔' : item.daysUntil + ' ngày'}</div>
                    <div class="upcoming-info">
                        <div class="upcoming-name">${item.title}</div>
                        <div class="upcoming-date">${item.solarDate.day}/${item.solarDate.month}/${item.solarDate.year}</div>
                    </div>
                `;
                container.appendChild(el);
            });
        }

        toggleModal('modal-upcoming', true);
    }

    // ============ NOTIFICATIONS ============
    function openNotificationModal() {
        const statusEl = document.getElementById('notification-status');
        const btnEnable = document.getElementById('btn-enable-notification');
        const permission = NotificationManager.getPermission();

        if (permission === 'granted') {
            statusEl.className = 'notification-status granted';
            statusEl.textContent = '✅ Thông báo đã được bật. Bạn sẽ nhận nhắc nhở về các sự kiện âm lịch.';
            btnEnable.style.display = 'none';
        } else if (permission === 'denied') {
            statusEl.className = 'notification-status denied';
            statusEl.textContent = '❌ Thông báo đã bị chặn. Vui lòng vào cài đặt trình duyệt để bật lại.';
            btnEnable.style.display = 'none';
        } else if (permission === 'unsupported') {
            statusEl.className = 'notification-status denied';
            statusEl.textContent = '⚠️ Trình duyệt không hỗ trợ thông báo.';
            btnEnable.style.display = 'none';
        } else {
            statusEl.className = 'notification-status default';
            statusEl.textContent = '⚠️ Thông báo chưa được bật. Bật để nhận nhắc nhở sự kiện âm lịch.';
            btnEnable.style.display = 'block';
        }

        toggleModal('modal-notification', true);
    }

    async function handleEnableNotification() {
        const result = await NotificationManager.requestPermission();
        if (result === 'granted') {
            showToast('Đã bật thông báo!');
            openNotificationModal(); // Refresh status
        } else {
            showToast('Không thể bật thông báo');
        }
    }

    // ============ ABOUT ============
    function showAbout() {
        alert('Âm Lịch Việt Nam v2.0 (PWA)\n\nỨng dụng xem ngày âm lịch, Can Chi, Giờ Hoàng Đạo, Tiết Khí.\n\nThuật toán: Hồ Ngọc Đức\nPhát triển: hotroso');
    }

    // ============ TOAST ============
    function showToast(message) {
        const toast = document.getElementById('toast');
        toast.textContent = message;
        toast.classList.remove('hidden');
        setTimeout(() => { toast.classList.add('hidden'); }, 3000);
    }

    // ============ START ============
    return { init };
})();

// Boot
document.addEventListener('DOMContentLoaded', App.init);
