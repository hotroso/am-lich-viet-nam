/**
 * Notification Manager for PWA
 * Handles push notification permission, scheduling, and daily checks
 */

const NotificationManager = (() => {
    const CHECK_INTERVAL = 60 * 60 * 1000; // Check every hour
    let checkTimer = null;

    /**
     * Check if notifications are supported.
     */
    function isSupported() {
        return 'Notification' in window && 'serviceWorker' in navigator;
    }

    /**
     * Get current permission status.
     */
    function getPermission() {
        if (!isSupported()) return 'unsupported';
        return Notification.permission; // 'granted', 'denied', 'default'
    }

    /**
     * Request notification permission.
     */
    async function requestPermission() {
        if (!isSupported()) return 'unsupported';
        const result = await Notification.requestPermission();
        if (result === 'granted') {
            startDailyCheck();
        }
        return result;
    }

    /**
     * Show a notification via Service Worker.
     */
    async function showNotification(title, body, options = {}) {
        if (getPermission() !== 'granted') return false;

        const registration = await navigator.serviceWorker.ready;
        registration.active.postMessage({
            type: 'SHOW_NOTIFICATION',
            title,
            body,
            tag: options.tag || `event-${Date.now()}`,
            eventId: options.eventId
        });
        return true;
    }

    /**
     * Check today's events and fire notifications.
     */
    async function checkTodayEvents() {
        if (getPermission() !== 'granted') return;

        const now = new Date();
        const today = {
            day: now.getDate(),
            month: now.getMonth() + 1,
            year: now.getFullYear()
        };

        const lunar = VietCalendar.solarToLunar(today.day, today.month, today.year);
        const events = await EventsDB.getEnabledEvents();
        const currentHour = now.getHours();
        const currentMinute = now.getMinutes();

        for (const evt of events) {
            // Calculate target solar date for this event
            let targetLunarDay = evt.lunarDay;
            let targetLunarMonth = evt.lunarMonth;
            let shouldNotify = false;

            // Check if today matches (considering remindDaysBefore)
            if (evt.remindDaysBefore && evt.remindDaysBefore > 0) {
                // Calculate the actual event date in solar
                const eventSolar = VietCalendar.lunarToSolar(
                    evt.lunarDay, evt.lunarMonth,
                    evt.lunarYear || lunar.year, 0
                );
                const eventDate = new Date(eventSolar.year, eventSolar.month - 1, eventSolar.day);
                const reminderDate = new Date(eventDate);
                reminderDate.setDate(reminderDate.getDate() - evt.remindDaysBefore);

                if (reminderDate.getDate() === today.day &&
                    reminderDate.getMonth() + 1 === today.month &&
                    reminderDate.getFullYear() === today.year) {
                    shouldNotify = true;
                }
            } else {
                // Direct match
                if (evt.repeat === 'MONTHLY' && lunar.day === evt.lunarDay) {
                    shouldNotify = true;
                } else if (evt.repeat === 'YEARLY' && lunar.day === evt.lunarDay && lunar.month === evt.lunarMonth) {
                    shouldNotify = true;
                } else if (evt.repeat === 'ONCE' && lunar.day === evt.lunarDay &&
                           lunar.month === evt.lunarMonth && lunar.year === (evt.lunarYear || lunar.year)) {
                    shouldNotify = true;
                }
            }

            if (shouldNotify) {
                const remindHour = evt.remindHour || 7;
                const remindMinute = evt.remindMinute || 0;

                // Check if it's time to notify (within the current hour)
                if (currentHour === remindHour && Math.abs(currentMinute - remindMinute) < 30) {
                    // Check if already notified today
                    const notifiedKey = `notified_${evt.id}_${today.day}_${today.month}_${today.year}`;
                    if (!localStorage.getItem(notifiedKey)) {
                        const jdn = VietCalendar.jdFromDate(today.day, today.month, today.year);
                        const gioHoangDao = CanChi.getGioHoangDao(jdn);

                        const body = buildNotificationBody(evt, lunar, gioHoangDao);
                        await showNotification(
                            `📅 ${evt.title}`,
                            body,
                            { tag: `event-${evt.id}`, eventId: evt.id }
                        );
                        localStorage.setItem(notifiedKey, '1');
                    }
                }
            }
        }

        // Clean old notification keys (older than 7 days)
        cleanOldNotificationKeys();
    }

    /**
     * Build notification body text.
     */
    function buildNotificationBody(evt, lunar, gioHoangDao) {
        const parts = [];
        parts.push(`Ngày ${lunar.day} tháng ${lunar.month} âm lịch`);
        if (gioHoangDao) {
            const firstGio = gioHoangDao.split(',')[0];
            parts.push(`Giờ tốt tiếp theo: ${firstGio}`);
        }
        if (evt.note) {
            parts.push(evt.note);
        }
        return parts.join('\n');
    }

    /**
     * Clean notification keys older than 7 days.
     */
    function cleanOldNotificationKeys() {
        const keysToRemove = [];
        for (let i = 0; i < localStorage.length; i++) {
            const key = localStorage.key(i);
            if (key && key.startsWith('notified_')) {
                const parts = key.split('_');
                if (parts.length >= 5) {
                    const day = parseInt(parts[parts.length - 3]);
                    const month = parseInt(parts[parts.length - 2]);
                    const year = parseInt(parts[parts.length - 1]);
                    const notifDate = new Date(year, month - 1, day);
                    const now = new Date();
                    const diff = (now - notifDate) / (1000 * 60 * 60 * 24);
                    if (diff > 7) {
                        keysToRemove.push(key);
                    }
                }
            }
        }
        keysToRemove.forEach(k => localStorage.removeItem(k));
    }

    /**
     * Start periodic check for event notifications.
     */
    function startDailyCheck() {
        if (checkTimer) clearInterval(checkTimer);
        // Check immediately
        checkTodayEvents();
        // Then check every hour
        checkTimer = setInterval(checkTodayEvents, CHECK_INTERVAL);
    }

    /**
     * Stop periodic check.
     */
    function stopDailyCheck() {
        if (checkTimer) {
            clearInterval(checkTimer);
            checkTimer = null;
        }
    }

    /**
     * Register periodic sync (if supported).
     */
    async function registerPeriodicSync() {
        if ('periodicSync' in (await navigator.serviceWorker.ready)) {
            try {
                await (await navigator.serviceWorker.ready).periodicSync.register('check-events', {
                    minInterval: 12 * 60 * 60 * 1000 // 12 hours
                });
            } catch (e) {
                // Periodic sync not available, rely on setInterval
                console.log('Periodic sync not available, using interval fallback');
            }
        }
    }

    /**
     * Initialize notification system.
     */
    function init() {
        if (getPermission() === 'granted') {
            startDailyCheck();
            registerPeriodicSync();
        }

        // Listen for SW messages
        if ('serviceWorker' in navigator) {
            navigator.serviceWorker.addEventListener('message', (event) => {
                if (event.data && event.data.type === 'CHECK_EVENTS') {
                    checkTodayEvents();
                }
            });
        }
    }

    return {
        isSupported,
        getPermission,
        requestPermission,
        showNotification,
        checkTodayEvents,
        startDailyCheck,
        stopDailyCheck,
        init
    };
})();
