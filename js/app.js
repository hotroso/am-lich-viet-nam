/**
 * Main App Controller - Âm Lịch Việt Nam PWA
 * Layout matching Android app design
 */

const App = (() => {
    // State
    let currentYear, currentMonth; // Solar month being viewed
    let selectedDate = null; // { day, month, year }
    let editingEventId = null;
    let deferredPrompt = null;
    let allUserEvents = [];
    let clockInterval = null;

    const WEEKDAYS = ['Chủ nhật', 'Thứ hai', 'Thứ ba', 'Thứ tư', 'Thứ năm', 'Thứ sáu', 'Thứ bảy'];
    const MONTHS_SHORT = ['', 'Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
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
        startClock();
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

    // ============ CLOCK ============
    function startClock() {
        updateClock();
        clockInterval = setInterval(updateClock, 1000);
    }

    function updateClock() {
        const el = document.getElementById('info-clock');
        if (el) {
            const now = new Date();
            el.textContent = now.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        }
    }

    // ============ PWA INSTALL ============
    function setupInstallPrompt() {
        window.addEventListener('beforeinstallprompt', (e) => {
            e.preventDefault();
            deferredPrompt = e;
            document.getElementById('nav-install').style.display = 'block';
        });
        if (isIOSSafari() && !isStandalone()) {
            document.getElementById('nav-install').style.display = 'block';
            showIOSInstallBanner();
        }
    }

    function isIOSSafari() {
        const ua = window.navigator.userAgent;
        const isIOS = /iPad|iPhone|iPod/.test(ua) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1);
        return isIOS && /WebKit/.test(ua) && !/CriOS/.test(ua) && !/FxiOS/.test(ua);
    }

    function isStandalone() {
        return window.navigator.standalone === true || window.matchMedia('(display-mode: standalone)').matches;
    }

    function showIOSInstallBanner() {
        const dismissed = localStorage.getItem('ios-install-dismissed');
        if (dismissed && Date.now() - parseInt(dismissed) < 7 * 24 * 60 * 60 * 1000) return;
        setTimeout(() => {
            const banner = document.getElementById('ios-install-banner');
            if (banner) {
                banner.classList.remove('hidden');
                document.getElementById('btn-close-ios-banner').addEventListener('click', () => {
                    banner.classList.add('hidden');
                    localStorage.setItem('ios-install-dismissed', String(Date.now()));
                });
            }
        }, 30000);
    }

    async function promptInstall() {
        if (deferredPrompt) {
            deferredPrompt.prompt();
            const { outcome } = await deferredPrompt.userChoice;
            if (outcome === 'accepted') showToast('Đã cài đặt ứng dụng!');
            deferredPrompt = null;
            document.getElementById('nav-install').style.display = 'none';
        } else if (isIOSSafari()) {
            document.getElementById('ios-install-banner').classList.remove('hidden');
        } else {
            showToast('Dùng menu trình duyệt để cài đặt ứng dụng');
        }
    }

    // ============ EVENT LISTENERS ============
    function setupEventListeners() {
        // Navigation
        document.getElementById('btn-prev-month').addEventListener('click', () => navigateMonth(-1));
        document.getElementById('btn-next-month').addEventListener('click', () => navigateMonth(1));
        document.getElementById('btn-today').addEventListener('click', goToToday);
        document.getElementById('btn-menu').addEventListener('click', openSidebar);
        document.getElementById('sidebar-overlay').addEventListener('click', closeSidebar);
        document.getElementById('btn-day-detail').addEventListener('click', showDateDetail);
        document.getElementById('info-day-rating').addEventListener('click', showDateDetail);

        // Year label click
        document.getElementById('year-label').addEventListener('click', toggleYearPicker);
        document.getElementById('month-label').addEventListener('click', toggleMonthPicker);
        document.getElementById('btn-year-prev').addEventListener('click', () => navigateYearPicker(-12));
        document.getElementById('btn-year-next').addEventListener('click', () => navigateYearPicker(12));

        // Close pickers on outside click
        document.addEventListener('click', (e) => {
            const monthPicker = document.getElementById('month-picker');
            const yearPicker = document.getElementById('year-picker');
            const monthLabel = document.getElementById('month-label');
            const yearLabel = document.getElementById('year-label');
            if (!monthPicker.contains(e.target) && e.target !== monthLabel) {
                monthPicker.classList.add('hidden');
                monthLabel.classList.remove('active');
            }
            if (!yearPicker.contains(e.target) && e.target !== yearLabel && !e.target.closest('.picker-year-nav')) {
                yearPicker.classList.add('hidden');
                yearLabel.classList.remove('active');
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
        document.getElementById('btn-close-detail').addEventListener('click', () => toggleModal('modal-detail', false));
        document.getElementById('btn-close-event-modal').addEventListener('click', closeEventModal);
        document.getElementById('btn-close-event-list').addEventListener('click', () => toggleModal('modal-event-list', false));
        document.getElementById('btn-close-convert').addEventListener('click', () => toggleModal('modal-convert', false));
        document.getElementById('btn-close-upcoming').addEventListener('click', () => toggleModal('modal-upcoming', false));
        document.getElementById('btn-close-notification').addEventListener('click', () => toggleModal('modal-notification', false));
        document.getElementById('btn-close-about').addEventListener('click', () => toggleModal('modal-about', false));
        document.getElementById('btn-share-detail').addEventListener('click', shareDetail);

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

        // Swipe on calendar
        setupSwipeGestures();

        // Keyboard
        document.addEventListener('keydown', (e) => {
            if (e.key === 'ArrowLeft') navigateMonth(-1);
            if (e.key === 'ArrowRight') navigateMonth(1);
            if (e.key === 'Escape') closeAllModals();
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
            if (Math.abs(diff) > 80) {
                navigateMonth(diff > 0 ? -1 : 1);
            }
        }, { passive: true });
    }

    // ============ CALENDAR RENDERING ============
    async function loadUserEvents() {
        allUserEvents = await EventsDB.getAllEvents();
    }

    function renderCalendar() {
        // Update month/year labels
        document.getElementById('month-label').textContent = MONTHS_SHORT[currentMonth];
        document.getElementById('year-label').textContent = currentYear;
        document.getElementById('header-title').textContent = `Tháng ${currentMonth}, ${currentYear}`;

        const grid = document.getElementById('calendar-grid');
        grid.innerHTML = '';

        // Android app uses Monday as first day of week
        // getDay(): 0=Sun, 1=Mon... We need Mon=0
        const firstDayOfMonth = new Date(currentYear, currentMonth - 1, 1).getDay();
        // Shift: Mon=0, Tue=1, ..., Sun=6
        const startOffset = firstDayOfMonth === 0 ? 6 : firstDayOfMonth - 1;
        const daysInMonth = new Date(currentYear, currentMonth, 0).getDate();
        const daysInPrevMonth = new Date(currentYear, currentMonth - 1, 0).getDate();

        const today = new Date();

        // Previous month fill
        for (let i = startOffset - 1; i >= 0; i--) {
            const day = daysInPrevMonth - i;
            const prevMonth = currentMonth === 1 ? 12 : currentMonth - 1;
            const prevYear = currentMonth === 1 ? currentYear - 1 : currentYear;
            const cell = createCalendarCell(day, prevMonth, prevYear, true);
            grid.appendChild(cell);
        }

        // Current month
        for (let day = 1; day <= daysInMonth; day++) {
            const cell = createCalendarCell(day, currentMonth, currentYear, false);
            grid.appendChild(cell);
        }

        // Next month fill (to 42 cells)
        const totalCells = grid.children.length;
        const remaining = (totalCells <= 35 ? 35 : 42) - totalCells;
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
        // getDay: 0=Sun
        const dayOfWeek = new Date(year, month - 1, day).getDay();

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

        // Lunar day (blue text like Android)
        const lunarEl = document.createElement('span');
        lunarEl.className = 'lunar-day';
        if (lunar.day === 1) {
            lunarEl.textContent = `${lunar.day}/${lunar.month}`;
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

        cell.addEventListener('click', () => {
            selectedDate = { day, month, year };
            renderCalendar();
            updateDayInfo();
        });

        return cell;
    }

    // ============ DAY INFO (Top section like Android) ============
    async function updateDayInfo() {
        if (!selectedDate) return;
        const { day, month, year } = selectedDate;
        const lunar = VietCalendar.solarToLunar(day, month, year);
        const info = CanChi.getFullInfo(lunar);
        const jdn = lunar.julianDay;
        const dayOfWeek = new Date(year, month - 1, day).getDay();

        // Big day number
        document.getElementById('info-big-day').textContent = day;

        // Day of week
        document.getElementById('info-weekday').textContent = WEEKDAYS[dayOfWeek];

        // Lunar left column
        const leapStr = lunar.isLeapMonth ? ' (nhuận)' : '';
        document.getElementById('info-lunar-month').textContent = `Tháng ${info.tenThangAm}${leapStr}`;
        document.getElementById('info-lunar-day').textContent = lunar.day;
        document.getElementById('info-lunar-year').textContent = `Năm ${CanChi.canChiNam(lunar.year)}`;

        // Special day color (mùng 1, 15)
        const lunarDayEl = document.getElementById('info-lunar-day');
        if (lunar.day === 1 || lunar.day === 15) {
            lunarDayEl.style.color = '#df2020';
        } else {
            lunarDayEl.style.color = '';
        }

        // Can Chi right column
        document.getElementById('info-canchi-thang').textContent = `Tháng: ${info.thang}`;
        document.getElementById('info-canchi-ngay').textContent = `Ngày: ${info.ngay}`;
        document.getElementById('info-canchi-gio').textContent = `Giờ: ${info.gio}`;
        document.getElementById('info-tiet-khi').textContent = `Tiết: ${info.tietKhi}`;

        // Giờ Hoàng Đạo
        document.getElementById('info-gio-hoang-dao').textContent = `Giờ Hoàng Đạo: ${info.gioHoangDao}`;

        // Day rating badge
        const advice = info.dayAdvice;
        const ratingEl = document.getElementById('info-day-rating');
        const ratingEmojis = { 'VERY_GOOD': '🌟', 'GOOD': '👍', 'NORMAL': '➖', 'BAD': '⚠️', 'VERY_BAD': '❌' };
        const ratingClasses = { 'VERY_GOOD': 'very-good', 'GOOD': 'good', 'NORMAL': 'normal', 'BAD': 'bad', 'VERY_BAD': 'very-bad' };
        ratingEl.textContent = `${ratingEmojis[advice.rating]} ${advice.ratingLabel} — Trực ${advice.truc}  ▸ Xem chi tiết`;
        ratingEl.className = 'day-rating-badge ' + ratingClasses[advice.rating];

        // Events
        const { holidays, userEvents } = await EventsDB.getAllEventsForDay(day, month, lunar.day, lunar.month);
        const eventsEl = document.getElementById('info-events');
        const allEvts = [...holidays.map(h => h.name), ...userEvents.map(e => `📌 ${e.title}`)];
        if (allEvts.length > 0) {
            eventsEl.textContent = allEvts.join('\n');
            eventsEl.style.display = 'block';
            eventsEl.style.whiteSpace = 'pre-line';
        } else {
            eventsEl.style.display = 'none';
        }
    }

    // ============ DATE DETAIL (like Android's WebView detail) ============
    function showDateDetail() {
        if (!selectedDate) return;
        const { day, month, year } = selectedDate;
        const lunar = VietCalendar.solarToLunar(day, month, year);
        const info = CanChi.getFullInfo(lunar);
        const jdn = lunar.julianDay;
        const advice = info.dayAdvice;
        const dayOfWeek = new Date(year, month - 1, day).getDay();

        let html = '';
        html += `<h2>${WEEKDAYS[dayOfWeek]}, ${day}/${month}/${year}</h2>`;
        html += `<div class="detail-hoang-dao" data-hd="${info.hoangDao.isHoangDao ? 1 : 0}">${info.hoangDao.label}</div>`;
        html += `<p><b>Âm lịch:</b> Ngày ${lunar.day} tháng ${info.tenThangAm}${lunar.isLeapMonth ? ' (nhuận)' : ''} năm ${CanChi.canChiNam(lunar.year)}</p>`;
        html += `<p><b>Can Chi ngày:</b> ${info.ngay}</p>`;
        html += `<p><b>Can Chi tháng:</b> ${info.thang}</p>`;
        html += `<p><b>Can Chi giờ:</b> ${info.gio}</p>`;
        html += `<p><b>Giờ Hoàng Đạo:</b> ${info.gioHoangDao}</p>`;
        html += `<p><b>Ngũ hành:</b> ${info.nguHanhNapAm}</p>`;
        html += `<p><b>Tiết khí:</b> ${info.tietKhi}</p>`;
        html += `<p><b>Trực:</b> ${info.truc} — ${info.trucDetail}</p>`;

        // Advice
        html += `<div class="advice-section"><h4>✅ Nên làm</h4><div class="advice-tags">`;
        advice.nenLam.forEach(t => { html += `<span class="advice-tag good">${t}</span>`; });
        html += `</div></div>`;
        html += `<div class="advice-section"><h4>❌ Không nên</h4><div class="advice-tags">`;
        advice.khongNen.forEach(t => { html += `<span class="advice-tag bad">${t}</span>`; });
        html += `</div></div>`;

        // Direction table
        const canIndex = (jdn + 9) % 10;
        const taiThan = CanChi.THIEN_CAN[canIndex] ? ['Đông Nam','Đông','Bắc','Bắc','ĐB','Tây','TN','TN','Nam','ĐN'][canIndex] : '';
        const hyThan = ['ĐB','TB','TN','Nam','ĐN','ĐB','TB','TN','Nam','ĐN'][canIndex] || '';
        const hacThan = ['TN','TB','ĐN','ĐB','Nam','TN','TB','ĐN','ĐB','Nam'][canIndex] || '';

        html += `<p><b>Hướng xuất hành:</b></p>`;
        html += `<table><thead><tr><th>Tài thần</th><th>Hỷ thần</th><th>Hạc thần</th></tr></thead>`;
        html += `<tbody><tr><td>${taiThan}</td><td>${hyThan}</td><td>${hacThan}</td></tr></tbody></table>`;

        document.getElementById('detail-body').innerHTML = html;
        toggleModal('modal-detail', true);
    }

    function shareDetail() {
        if (!selectedDate) return;
        const { day, month, year } = selectedDate;
        const lunar = VietCalendar.solarToLunar(day, month, year);
        const info = CanChi.getFullInfo(lunar);
        const text = `📅 ${day}/${month}/${year}\n🌙 Âm lịch: ${lunar.day}/${lunar.month}\n🔮 ${info.ngay}\n⏰ Giờ tốt: ${info.gioHoangDao}`;

        if (navigator.share) {
            navigator.share({ title: 'Âm Lịch Việt Nam', text });
        } else {
            navigator.clipboard.writeText(text).then(() => showToast('Đã copy!'));
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
        renderCalendar();
        updateDayInfo();
    }

    function showYearPicker() {
        // replaced by toggleYearPicker
    }

    // ============ MONTH/YEAR PICKERS (dropdown grid) ============
    let yearPickerBase = 0;

    function toggleMonthPicker() {
        const picker = document.getElementById('month-picker');
        const yearPicker = document.getElementById('year-picker');
        const monthLabel = document.getElementById('month-label');
        const yearLabel = document.getElementById('year-label');

        yearPicker.classList.add('hidden');
        yearLabel.classList.remove('active');

        if (picker.classList.contains('hidden')) {
            renderMonthPicker();
            picker.classList.remove('hidden');
            positionPicker(picker, monthLabel);
            monthLabel.classList.add('active');
        } else {
            picker.classList.add('hidden');
            monthLabel.classList.remove('active');
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

    function toggleYearPicker() {
        const picker = document.getElementById('year-picker');
        const monthPicker = document.getElementById('month-picker');
        const yearLabel = document.getElementById('year-label');
        const monthLabel = document.getElementById('month-label');

        monthPicker.classList.add('hidden');
        monthLabel.classList.remove('active');

        if (picker.classList.contains('hidden')) {
            yearPickerBase = currentYear - (currentYear % 12);
            renderYearPicker();
            picker.classList.remove('hidden');
            positionPicker(picker, yearLabel);
            yearLabel.classList.add('active');
        } else {
            picker.classList.add('hidden');
            yearLabel.classList.remove('active');
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
        document.getElementById('month-label').classList.remove('active');
        document.getElementById('year-label').classList.remove('active');
    }

    function positionPicker(picker, anchor) {
        const rect = anchor.getBoundingClientRect();
        picker.style.top = (rect.bottom + 4) + 'px';
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
        if (show) modal.classList.remove('hidden');
        else modal.classList.add('hidden');
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

        if (selectedDate) {
            const lunar = VietCalendar.solarToLunar(selectedDate.day, selectedDate.month, selectedDate.year);
            document.getElementById('event-lunar-day').value = lunar.day;
            document.getElementById('event-lunar-month').value = lunar.month;
        }

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

        setChipValue('event-type-group', evt.eventType || 'KHAC');
        setChipValue('event-repeat-group', evt.repeat || 'YEARLY');
        setChipValue('event-remind-group', String(evt.remindDaysBefore || 0));
        toggleModal('modal-event', true);
    }

    function setChipValue(groupId, value) {
        document.getElementById(groupId).querySelectorAll('.chip').forEach(c => {
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
                item.querySelector('.event-toggle').addEventListener('click', async (ev) => {
                    ev.stopPropagation();
                    evt.isEnabled = !evt.isEnabled;
                    await EventsDB.updateEvent(evt);
                    ev.target.className = `event-toggle ${evt.isEnabled ? 'on' : 'off'}`;
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
        if (!day || !month || !year || year < 1900 || year > 2100) { resultEl.textContent = ''; return; }
        try {
            const lunar = VietCalendar.solarToLunar(day, month, year);
            const leapStr = lunar.isLeapMonth ? ' (nhuận)' : '';
            resultEl.textContent = `Ngày ${lunar.day} tháng ${lunar.month}${leapStr} năm ${lunar.year} (${CanChi.canChiNam(lunar.year)})`;
        } catch (e) { resultEl.textContent = 'Ngày không hợp lệ'; }
    }

    function handleLunarToSolar() {
        const day = parseInt(document.getElementById('convert-lunar-day').value);
        const month = parseInt(document.getElementById('convert-lunar-month').value);
        const year = parseInt(document.getElementById('convert-lunar-year').value);
        const resultEl = document.getElementById('convert-result-solar');
        if (!day || !month || !year || year < 1900 || year > 2100) { resultEl.textContent = ''; return; }
        try {
            const solar = VietCalendar.lunarToSolar(day, month, year, 0);
            const dow = new Date(solar.year, solar.month - 1, solar.day).getDay();
            resultEl.textContent = `${WEEKDAYS[dow]}, ${solar.day}/${solar.month}/${solar.year}`;
        } catch (e) { resultEl.textContent = 'Ngày không hợp lệ'; }
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
            statusEl.textContent = '✅ Thông báo đã được bật.';
            btnEnable.style.display = 'none';
        } else if (permission === 'denied') {
            statusEl.className = 'notification-status denied';
            statusEl.textContent = '❌ Thông báo đã bị chặn. Vào cài đặt trình duyệt để bật lại.';
            btnEnable.style.display = 'none';
        } else if (permission === 'unsupported') {
            statusEl.className = 'notification-status denied';
            statusEl.textContent = '⚠️ Trình duyệt không hỗ trợ thông báo.';
            btnEnable.style.display = 'none';
        } else if (permission === 'ios-needs-homescreen') {
            statusEl.className = 'notification-status default';
            statusEl.innerHTML = '⚠️ Trên iOS, cần <strong>thêm vào Màn hình chính</strong> trước khi bật thông báo.';
            btnEnable.style.display = 'none';
        } else {
            statusEl.className = 'notification-status default';
            statusEl.textContent = '⚠️ Thông báo chưa được bật.';
            btnEnable.style.display = 'block';
        }
        toggleModal('modal-notification', true);
    }

    async function handleEnableNotification() {
        const result = await NotificationManager.requestPermission();
        if (result === 'granted') {
            showToast('Đã bật thông báo!');
            openNotificationModal();
        } else {
            showToast('Không thể bật thông báo');
        }
    }

    // ============ ABOUT ============
    function showAbout() {
        toggleModal('modal-about', true);
        generateQRCode();
    }

    /**
     * Generate QR Code on canvas (minimal implementation, no library)
     * Uses a simple approach: draw QR via third-party image or manual encoding
     */
    function generateQRCode() {
        const canvas = document.getElementById('qr-canvas');
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        const url = 'https://amlich.hotrogiaiphapso.info/';

        // Use Google Charts API for QR (lightweight, no dependency)
        const img = new Image();
        img.crossOrigin = 'anonymous';
        img.onload = () => {
            ctx.clearRect(0, 0, 180, 180);
            ctx.drawImage(img, 0, 0, 180, 180);
        };
        img.onerror = () => {
            // Fallback: draw a placeholder
            ctx.fillStyle = '#f0f0f0';
            ctx.fillRect(0, 0, 180, 180);
            ctx.fillStyle = '#333';
            ctx.font = '12px sans-serif';
            ctx.textAlign = 'center';
            ctx.fillText('QR Code', 90, 85);
            ctx.fillText(url, 90, 105);
        };
        img.src = `https://chart.googleapis.com/chart?cht=qr&chs=180x180&chl=${encodeURIComponent(url)}&choe=UTF-8`;
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

document.addEventListener('DOMContentLoaded', App.init);
