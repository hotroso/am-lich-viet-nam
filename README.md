# Âm Lịch Việt Nam - PWA

Progressive Web App xem ngày Âm lịch Việt Nam với đầy đủ tính năng thông báo.

## Tính năng

- 📅 Xem lịch tháng với ngày âm/dương song song
- 🔮 Can Chi (Ngày/Tháng/Năm), Tiết Khí, Giờ Hoàng Đạo
- ⭐ Đánh giá ngày (Đại cát/Tốt/Bình thường/Không tốt/Xấu)
- ✅ Nên làm / ❌ Không nên (theo Trực)
- 📋 Quản lý sự kiện âm lịch (Giỗ, Sinh nhật, Lễ)
- 🔔 Push Notification nhắc nhở sự kiện
- 🔄 Chuyển đổi ngày Dương ↔ Âm
- 📆 Xem sự kiện sắp tới (90 ngày)
- 📲 Cài đặt như app native (PWA installable)
- 🌐 Hoạt động offline

## Chạy local

```bash
cd web-app
npx serve . -l 3000
```

Mở trình duyệt: http://localhost:3000

## PWA Requirements

Để PWA hoạt động đầy đủ (installable + notifications):
1. Phải serve qua HTTPS (hoặc localhost cho dev)
2. Phải có Service Worker đăng ký thành công
3. Phải có manifest.json hợp lệ
4. Người dùng phải cấp quyền Notification

## Cấu trúc

```
web-app/
├── index.html          — UI chính (single page)
├── manifest.json       — PWA manifest
├── sw.js               — Service Worker (cache + push)
├── package.json        — npm scripts
├── css/
│   └── style.css       — Toàn bộ CSS
├── js/
│   ├── lunar-calendar.js  — Thuật toán Hồ Ngọc Đức (port từ Kotlin)
│   ├── canchi.js          — Can Chi, Tiết Khí, Trực, Hoàng Đạo
│   ├── events-db.js       — IndexedDB CRUD + ngày lễ VN
│   ├── notification.js    — Push notification manager
│   └── app.js             — App controller chính
└── icons/
    └── icon-192.svg       — App icon
```

## Thuật toán

Port 100% từ VietCalendar.kt (thuật toán Hồ Ngọc Đức) sang JavaScript:
- Solar ↔ Lunar conversion
- Julian Day Number calculations
- New Moon computation
- Sun Longitude (Tiết Khí)
- Leap month detection

## Notification Flow

1. User bật notification → browser cấp quyền
2. App kiểm tra mỗi giờ xem có event cần nhắc không
3. So khớp ngày âm lịch hiện tại với events đã lưu
4. Gửi notification qua Service Worker
5. Hỗ trợ Periodic Background Sync (nếu browser cho phép)

## Deploy

Có thể deploy lên bất kỳ static hosting nào:
- GitHub Pages
- Netlify
- Vercel
- Firebase Hosting
- Cloudflare Pages

Yêu cầu duy nhất: HTTPS (cho Service Worker + Notifications).
