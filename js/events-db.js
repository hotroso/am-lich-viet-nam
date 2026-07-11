/**
 * Events Database using IndexedDB
 * Manages lunar calendar events with CRUD operations
 */

const EventsDB = (() => {
    const DB_NAME = 'AmLichVietNamDB';
    const DB_VERSION = 1;
    const STORE_NAME = 'events';
    let db = null;

    // Vietnamese holidays (same as events.json)
    const HOLIDAYS = [
        { name: 'Tết Dương lịch', solarDay: 1, solarMonth: 1 },
        { name: 'Ngày lễ Tình yêu - Valentine', solarDay: 14, solarMonth: 2 },
        { name: 'Ngày thành lập Đảng CS Việt Nam', solarDay: 3, solarMonth: 2 },
        { name: 'Ngày thầy thuốc Việt Nam', solarDay: 27, solarMonth: 2 },
        { name: 'Ngày quốc tế Phụ nữ', solarDay: 8, solarMonth: 3 },
        { name: 'Ngày thành lập Đoàn TNCS HCM', solarDay: 26, solarMonth: 3 },
        { name: 'Ngày giải phóng miền Nam', solarDay: 30, solarMonth: 4 },
        { name: 'Ngày quốc tế lao động', solarDay: 1, solarMonth: 5 },
        { name: 'Chiến thắng Điện Biên Phủ', solarDay: 7, solarMonth: 5 },
        { name: 'Ngày sinh Chủ tịch Hồ Chí Minh', solarDay: 19, solarMonth: 5 },
        { name: 'Ngày Quốc tế thiếu nhi', solarDay: 1, solarMonth: 6 },
        { name: 'Ngày gia đình Việt Nam', solarDay: 28, solarMonth: 6 },
        { name: 'Ngày thương binh, liệt sĩ', solarDay: 27, solarMonth: 7 },
        { name: 'Quốc khánh Việt Nam', solarDay: 2, solarMonth: 9 },
        { name: 'Ngày Phụ nữ Việt Nam', solarDay: 20, solarMonth: 10 },
        { name: 'Ngày nhà giáo Việt Nam', solarDay: 20, solarMonth: 11 },
        { name: 'Ngày thành lập QĐND Việt Nam', solarDay: 22, solarMonth: 12 },
        { name: 'Giáng sinh', solarDay: 24, solarMonth: 12 },
        // Lunar holidays
        { name: 'Tết Nguyên Đán', lunarDay: 1, lunarMonth: 1 },
        { name: 'Tết Nguyên Đán', lunarDay: 2, lunarMonth: 1 },
        { name: 'Tết Nguyên Đán', lunarDay: 3, lunarMonth: 1 },
        { name: 'Giỗ Tổ Hùng Vương', lunarDay: 10, lunarMonth: 3 },
        { name: 'Ngày lễ Phật Đản', lunarDay: 15, lunarMonth: 4 },
        { name: 'Tết Đoan Ngọ', lunarDay: 5, lunarMonth: 5 },
        { name: 'Lễ Vu Lan báo hiếu', lunarDay: 15, lunarMonth: 7 },
        { name: 'Tết Trung Thu', lunarDay: 15, lunarMonth: 8 },
        { name: 'Ông Táo chầu trời', lunarDay: 23, lunarMonth: 12 }
    ];

    /**
     * Open/initialize the database.
     */
    function open() {
        return new Promise((resolve, reject) => {
            if (db) { resolve(db); return; }

            const request = indexedDB.open(DB_NAME, DB_VERSION);

            request.onupgradeneeded = (event) => {
                const database = event.target.result;
                if (!database.objectStoreNames.contains(STORE_NAME)) {
                    const store = database.createObjectStore(STORE_NAME, { keyPath: 'id', autoIncrement: true });
                    store.createIndex('lunarMonth', 'lunarMonth', { unique: false });
                    store.createIndex('lunarDay', 'lunarDay', { unique: false });
                    store.createIndex('isEnabled', 'isEnabled', { unique: false });
                }
            };

            request.onsuccess = (event) => {
                db = event.target.result;
                resolve(db);
            };

            request.onerror = (event) => {
                reject(event.target.error);
            };
        });
    }

    /**
     * Add a new event.
     */
    async function addEvent(eventData) {
        const database = await open();
        return new Promise((resolve, reject) => {
            const tx = database.transaction(STORE_NAME, 'readwrite');
            const store = tx.objectStore(STORE_NAME);
            const event = {
                ...eventData,
                createdAt: Date.now(),
                isEnabled: eventData.isEnabled !== undefined ? eventData.isEnabled : true
            };
            const request = store.add(event);
            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    /**
     * Update an existing event.
     */
    async function updateEvent(eventData) {
        const database = await open();
        return new Promise((resolve, reject) => {
            const tx = database.transaction(STORE_NAME, 'readwrite');
            const store = tx.objectStore(STORE_NAME);
            const request = store.put(eventData);
            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    /**
     * Delete an event by ID.
     */
    async function deleteEvent(id) {
        const database = await open();
        return new Promise((resolve, reject) => {
            const tx = database.transaction(STORE_NAME, 'readwrite');
            const store = tx.objectStore(STORE_NAME);
            const request = store.delete(id);
            request.onsuccess = () => resolve();
            request.onerror = () => reject(request.error);
        });
    }

    /**
     * Get all events.
     */
    async function getAllEvents() {
        const database = await open();
        return new Promise((resolve, reject) => {
            const tx = database.transaction(STORE_NAME, 'readonly');
            const store = tx.objectStore(STORE_NAME);
            const request = store.getAll();
            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    /**
     * Get a single event by ID.
     */
    async function getEvent(id) {
        const database = await open();
        return new Promise((resolve, reject) => {
            const tx = database.transaction(STORE_NAME, 'readonly');
            const store = tx.objectStore(STORE_NAME);
            const request = store.get(id);
            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    /**
     * Get enabled events.
     */
    async function getEnabledEvents() {
        const events = await getAllEvents();
        return events.filter(e => e.isEnabled);
    }

    /**
     * Get events for a specific lunar date.
     */
    async function getEventsForLunarDate(lunarDay, lunarMonth) {
        const events = await getAllEvents();
        return events.filter(e => {
            if (e.repeat === 'MONTHLY') {
                return e.lunarDay === lunarDay;
            }
            return e.lunarDay === lunarDay && e.lunarMonth === lunarMonth;
        });
    }

    /**
     * Get holidays for a solar date.
     */
    function getHolidaysForSolarDate(solarDay, solarMonth) {
        return HOLIDAYS.filter(h => h.solarDay === solarDay && h.solarMonth === solarMonth);
    }

    /**
     * Get holidays for a lunar date.
     */
    function getHolidaysForLunarDate(lunarDay, lunarMonth) {
        return HOLIDAYS.filter(h => h.lunarDay === lunarDay && h.lunarMonth === lunarMonth);
    }

    /**
     * Get all events/holidays for a given day (solar + lunar).
     */
    async function getAllEventsForDay(solarDay, solarMonth, lunarDay, lunarMonth) {
        const solarHolidays = getHolidaysForSolarDate(solarDay, solarMonth);
        const lunarHolidays = getHolidaysForLunarDate(lunarDay, lunarMonth);
        const userEvents = await getEventsForLunarDate(lunarDay, lunarMonth);
        return {
            holidays: [...solarHolidays, ...lunarHolidays],
            userEvents
        };
    }

    /**
     * Check if a lunar date has any user events.
     */
    async function hasUserEvent(lunarDay, lunarMonth) {
        const events = await getAllEvents();
        return events.some(e => {
            if (e.repeat === 'MONTHLY') return e.lunarDay === lunarDay;
            return e.lunarDay === lunarDay && e.lunarMonth === lunarMonth;
        });
    }

    /**
     * Get upcoming events (next N days).
     */
    async function getUpcomingEvents(days = 90) {
        const events = await getAllEvents();
        const today = new Date();
        const upcoming = [];

        for (let i = 0; i <= days; i++) {
            const date = new Date(today);
            date.setDate(date.getDate() + i);
            const sDay = date.getDate();
            const sMonth = date.getMonth() + 1;
            const sYear = date.getFullYear();

            const lunar = VietCalendar.solarToLunar(sDay, sMonth, sYear);

            // Check user events
            for (const evt of events) {
                let match = false;
                if (evt.repeat === 'MONTHLY' && evt.lunarDay === lunar.day) {
                    match = true;
                } else if (evt.repeat === 'YEARLY' && evt.lunarDay === lunar.day && evt.lunarMonth === lunar.month) {
                    match = true;
                } else if (evt.repeat === 'ONCE' && evt.lunarDay === lunar.day && evt.lunarMonth === lunar.month && evt.lunarYear === lunar.year) {
                    match = true;
                }

                if (match) {
                    upcoming.push({
                        ...evt,
                        solarDate: { day: sDay, month: sMonth, year: sYear },
                        daysUntil: i
                    });
                }
            }

            // Check lunar holidays
            const lunarH = HOLIDAYS.filter(h => h.lunarDay === lunar.day && h.lunarMonth === lunar.month);
            for (const h of lunarH) {
                upcoming.push({
                    title: h.name,
                    eventType: 'LE',
                    solarDate: { day: sDay, month: sMonth, year: sYear },
                    daysUntil: i,
                    isHoliday: true
                });
            }
        }

        return upcoming;
    }

    return {
        open,
        addEvent,
        updateEvent,
        deleteEvent,
        getAllEvents,
        getEvent,
        getEnabledEvents,
        getEventsForLunarDate,
        getHolidaysForSolarDate,
        getHolidaysForLunarDate,
        getAllEventsForDay,
        hasUserEvent,
        getUpcomingEvents,
        HOLIDAYS
    };
})();
