# Hướng dẫn Deploy - Âm Lịch PWA

## Thông tin

| Mục | Giá trị |
|-----|---------|
| Domain | `amlich.hotrogiaiphapso.info` |
| Web Root | `/var/www/html/sites/sunbabe/public_html/amlich.hotrogiaiphapso.info` |
| Server | Ubuntu + Apache2 |
| SSL | Let's Encrypt (auto-renew) |

## Yêu cầu trước khi cài đặt

1. Server Ubuntu (18.04+) với quyền root/sudo
2. DNS đã trỏ `amlich.hotrogiaiphapso.info` về IP server
3. Apache2 đã cài hoặc script sẽ tự cài

## Cài đặt lần đầu

```bash
# Upload thư mục dự án lên server, ví dụ:
scp -r . user@server:/tmp/am_lich_web/

# SSH vào server
ssh user@server

# Chạy script cài đặt
cd /tmp/am_lich_web/deploy
sudo chmod +x setup.sh
sudo ./setup.sh
```

## Cập nhật code (deploy phiên bản mới)

```bash
# Upload code mới lên server
scp -r . user@server:/tmp/am_lich_web/

# Chạy script update
cd /tmp/am_lich_web/deploy
sudo chmod +x update.sh
sudo ./update.sh
```

## Cấu trúc file trên server

```
/var/www/html/sites/sunbabe/public_html/amlich.hotrogiaiphapso.info/
├── index.html
├── manifest.json
├── sw.js
├── .htaccess
├── css/
│   └── style.css
├── js/
│   ├── app.js
│   ├── canchi.js
│   ├── events-db.js
│   ├── lunar-calendar.js
│   └── notification.js
└── icons/
    └── icon-192.svg
```

## Lệnh hữu ích

```bash
# Kiểm tra trạng thái Apache
sudo systemctl status apache2

# Xem log lỗi
sudo tail -f /var/log/apache2/amlich-error.log

# Xem log truy cập
sudo tail -f /var/log/apache2/amlich-access.log

# Reload Apache sau khi sửa config
sudo systemctl reload apache2

# Kiểm tra SSL
sudo certbot certificates

# Gia hạn SSL (test)
sudo certbot renew --dry-run

# Test cấu hình Apache
sudo apache2ctl configtest
```

## Ghi chú

- **Không cần Node.js/PM2** vì đây là PWA tĩnh, Apache serve trực tiếp
- Service Worker (`sw.js`) được set `no-cache` để luôn cập nhật
- SSL tự động gia hạn qua certbot timer
- Backup tự động khi chạy `update.sh`, giữ 7 ngày gần nhất
