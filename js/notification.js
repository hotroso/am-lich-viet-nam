/**
 * Notification Manager for PWA
 * Handles push notification permission, scheduling, and daily checks
 */

const NotificationManager = (() => {
    const CHECK_INTERVAL = 60 * 60 * 1000; // Check every hour
    let checkTimer = null;

    /**
     * Check if notifications are supported.
     * Safari iOS chỉ hỗ trợ Web Push từ iOS 16.4+ và khi đã Add to Home Screen.
     */
    function isSupported() {
        // Check basic support
        if (!('Notification' in window)) return false;
        if (!('serviceWorker' in navigator)) return false;

        // On iOS Safari, notifications only work in standalone mode (Add to Home Screen)
        // and only on iOS 16.4+
        if (isIOSSafari() && !isStandaloneMode()) {
            return false;
        }

        return true;
    }

    /**
     * Detect iOS Safari
     */
    function isIOSSafari() {
        const ua = window.navigator.userAgent;
        const isIOS = /iPad|iPhone|iPod/.test(ua) ||
            (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1);
        return isIOS;
    }

    /**
     * Check if running as standalone PWA (Add to Home Screen)
     */
    function isStandaloneMode() {
        return window.navigator.standalone === true ||
            window.matchMedia('(display-mode: standalone)').matches;
    }

    /**
     * Get current permission status.
     * Returns special status for iOS Safari conditions.
     */
    function getPermission() {
        if (!('Notification' in window) || !('serviceWorker' in navigator)) {
            return 'unsupported';
        }

        // iOS Safari but not standalone - needs Add to Home Screen first
        if (isIOSSafari() && !isStandaloneMode()) {
            return 'ios-needs-homescreen';
        }

        return Notification.permission; // 'granted', 'denied', 'default'
    }

    /**
     * Request notification permission.
     * On iOS Safari, must be called from a user gesture in standalone mode.
     */
    async function requestPermission() {
        if (!('Notification' in window)) return 'unsupported';

        // iOS not in standalone mode
        if (isIOSSafari() && !isStandaloneMode()) {
            return 'ios-needs-homescreen';
        }

        try {
            const result = await Notification.requestPermission();
            if (result === 'granted') {
                startDailyCheck();
            }
            return result;
        } catch (e) {
            // Safari older versions may throw on Notification.requestPermission
            console.warn('Notification permission request failed:', e);
            return 'unsupported';
        }
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

                // Check if it's time to notify
                // Match exact hour and within 5-minute window of the target minute
                // OR if we missed it (app was suspended), fire if we're past the time but same hour
                const isExactTime = currentHour === remindHour && currentMinute >= remindMinute && currentMinute <= remindMinute + 5;
                // Also handle case where app was suspended and resumed after target time (same day)
                const isPastTime = (currentHour > remindHour) || (currentHour === remindHour && currentMinute > remindMinute + 5);

                if (isExactTime || isPastTime) {
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
     * Safari/iOS does NOT support Periodic Background Sync.
     * Falls back to setInterval (already handled in startDailyCheck).
     */
    async function registerPeriodicSync() {
        try {
            const registration = await navigator.serviceWorker.ready;
            if ('periodicSync' in registration) {
                await registration.periodicSync.register('check-events', {
                    minInterval: 12 * 60 * 60 * 1000 // 12 hours
                });
            }
        } catch (e) {
            // Periodic sync not available (Safari, Firefox, etc.)
            // Rely on setInterval fallback in startDailyCheck
            console.log('Periodic sync not available, using interval fallback');
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

        // iOS Safari: when app comes back from background, check immediately
        // visibilitychange fires when user switches back to the PWA
        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState === 'visible' && getPermission() === 'granted') {
                checkTodayEvents();
            }
        });

        // Also check on focus (belt and suspenders for iOS)
        window.addEventListener('focus', () => {
            if (getPermission() === 'granted') {
                checkTodayEvents();
            }
        });
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
