/**
 * Service Worker - Âm Lịch Việt Nam PWA
 * Handles: caching, offline, push notifications
 */

const CACHE_NAME = 'amlich-v1';
const STATIC_ASSETS = [
    '/',
    '/index.html',
    '/css/style.css',
    '/js/lunar-calendar.js',
    '/js/canchi.js',
    '/js/events-db.js',
    '/js/notification.js',
    '/js/app.js',
    '/manifest.json',
    '/icons/icon-192.svg'
];

// Install - cache static assets
self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) => {
            return cache.addAll(STATIC_ASSETS);
        })
    );
    self.skipWaiting();
});

// Activate - clean old caches
self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((cacheNames) => {
            return Promise.all(
                cacheNames
                    .filter((name) => name !== CACHE_NAME)
                    .map((name) => caches.delete(name))
            );
        })
    );
    self.clients.claim();
});

// Fetch - Cache First, then Network
self.addEventListener('fetch', (event) => {
    event.respondWith(
        caches.match(event.request).then((cachedResponse) => {
            if (cachedResponse) {
                return cachedResponse;
            }
            return fetch(event.request).then((response) => {
                if (!response || response.status !== 200 || response.type !== 'basic') {
                    return response;
                }
                const responseToCache = response.clone();
                caches.open(CACHE_NAME).then((cache) => {
                    cache.put(event.request, responseToCache);
                });
                return response;
            });
        }).catch(() => {
            // Offline fallback
            if (event.request.destination === 'document') {
                return caches.match('/index.html');
            }
        })
    );
});

// Push Notification
self.addEventListener('push', (event) => {
    let data = { title: 'Âm Lịch Việt Nam', body: 'Bạn có sự kiện hôm nay!' };
    if (event.data) {
        try {
            data = event.data.json();
        } catch (e) {
            data.body = event.data.text();
        }
    }

    const options = {
        body: data.body,
        icon: '/icons/icon-192.svg',
        badge: '/icons/icon-192.svg',
        vibrate: [200, 100, 200],
        tag: data.tag || 'amlich-notification',
        renotify: true,
        data: {
            url: data.url || '/',
            eventId: data.eventId
        },
        actions: [
            { action: 'open', title: 'Xem chi tiết' },
            { action: 'dismiss', title: 'Bỏ qua' }
        ]
    };

    event.waitUntil(
        self.registration.showNotification(data.title, options)
    );
});

// Notification click
self.addEventListener('notificationclick', (event) => {
    event.notification.close();

    if (event.action === 'dismiss') return;

    event.waitUntil(
        clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
            for (const client of clientList) {
                if (client.url.includes('/index.html') && 'focus' in client) {
                    return client.focus();
                }
            }
            return clients.openWindow(event.notification.data.url || '/');
        })
    );
});

// Periodic sync for daily notification check
self.addEventListener('periodicsync', (event) => {
    if (event.tag === 'check-events') {
        event.waitUntil(checkTodayEvents());
    }
});

// Check today's events and show notification
async function checkTodayEvents() {
    // This will be triggered by the periodic sync or message from main app
    const clients_list = await self.clients.matchAll();
    for (const client of clients_list) {
        client.postMessage({ type: 'CHECK_EVENTS' });
    }
}

// Listen for messages from the main app
self.addEventListener('message', (event) => {
    if (event.data && event.data.type === 'SHOW_NOTIFICATION') {
        const { title, body, tag, eventId } = event.data;
        self.registration.showNotification(title, {
            body: body,
            icon: '/icons/icon-192.svg',
            badge: '/icons/icon-192.svg',
            vibrate: [200, 100, 200],
            tag: tag || 'amlich-event',
            renotify: true,
            data: { url: '/', eventId }
        });
    }

    if (event.data && event.data.type === 'SKIP_WAITING') {
        self.skipWaiting();
    }
});
