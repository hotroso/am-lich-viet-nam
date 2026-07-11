#!/bin/bash
###############################################################################
# Script cài đặt PWA Âm Lịch Việt Nam
# Domain: amlich.hotrogiaiphapso.info
# Server: Ubuntu + Apache2
# Thư mục: /var/www/html/sites/sunbabe/public_html/amlich.hotrogiaiphapso.info
###############################################################################

set -e

# === CẤU HÌNH ===
DOMAIN="amlich.hotrogiaiphapso.info"
WEB_ROOT="/var/www/html/sites/sunbabe/public_html/amlich.hotrogiaiphapso.info"
APACHE_CONF="/etc/apache2/sites-available/${DOMAIN}.conf"
REPO_SOURCE="$(cd "$(dirname "$0")/.." && pwd)"

# Màu sắc cho output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# === KIỂM TRA QUYỀN ROOT ===
if [ "$EUID" -ne 0 ]; then
    log_error "Vui lòng chạy script với quyền root (sudo)"
    exit 1
fi

echo "=============================================="
echo "  CÀI ĐẶT PWA ÂM LỊCH VIỆT NAM"
echo "  Domain: ${DOMAIN}"
echo "=============================================="
echo ""

# === BƯỚC 1: CÀI ĐẶT CÁC GÓI CẦN THIẾT ===
log_info "Bước 1: Kiểm tra và cài đặt các gói cần thiết..."

apt-get update -qq

# Cài Apache2 nếu chưa có
if ! command -v apache2 &> /dev/null; then
    log_info "Cài đặt Apache2..."
    apt-get install -y apache2
fi

# Cài certbot cho SSL
if ! command -v certbot &> /dev/null; then
    log_info "Cài đặt Certbot cho SSL..."
    apt-get install -y certbot python3-certbot-apache
fi

# === BƯỚC 2: BẬT CÁC MODULE APACHE CẦN THIẾT ===
log_info "Bước 2: Bật các module Apache cần thiết..."

a2enmod rewrite
a2enmod headers
a2enmod ssl
a2enmod expires
a2enmod deflate

# === BƯỚC 3: TẠO THƯ MỤC WEB ===
log_info "Bước 3: Tạo thư mục web root..."

mkdir -p "${WEB_ROOT}"

# === BƯỚC 4: COPY SOURCE CODE ===
log_info "Bước 4: Copy source code vào web root..."

# Copy tất cả file từ thư mục dự án (trừ thư mục deploy)
rsync -av --exclude='deploy' --exclude='node_modules' --exclude='.git' \
    "${REPO_SOURCE}/" "${WEB_ROOT}/"

# Phân quyền
chown -R www-data:www-data "${WEB_ROOT}"
chmod -R 755 "${WEB_ROOT}"

log_info "Source code đã được copy vào ${WEB_ROOT}"

# === BƯỚC 5: TẠO CẤU HÌNH APACHE VIRTUALHOST ===
log_info "Bước 5: Tạo cấu hình Apache VirtualHost..."

cat > "${APACHE_CONF}" << 'VHOST_EOF'
<VirtualHost *:80>
    ServerName amlich.hotrogiaiphapso.info
    DocumentRoot /var/www/html/sites/sunbabe/public_html/amlich.hotrogiaiphapso.info

    # Redirect tất cả HTTP sang HTTPS
    RewriteEngine On
    RewriteCond %{HTTPS} off
    RewriteRule ^(.*)$ https://%{HTTP_HOST}%{REQUEST_URI} [L,R=301]
</VirtualHost>

<VirtualHost *:443>
    ServerName amlich.hotrogiaiphapso.info
    DocumentRoot /var/www/html/sites/sunbabe/public_html/amlich.hotrogiaiphapso.info

    # SSL sẽ được cấu hình bởi Certbot
    # SSLEngine on
    # SSLCertificateFile /etc/letsencrypt/live/amlich.hotrogiaiphapso.info/fullchain.pem
    # SSLCertificateKeyFile /etc/letsencrypt/live/amlich.hotrogiaiphapso.info/privkey.pem

    <Directory /var/www/html/sites/sunbabe/public_html/amlich.hotrogiaiphapso.info>
        Options -Indexes +FollowSymLinks
        AllowOverride All
        Require all granted
    </Directory>

    # === HEADERS BẢO MẬT ===
    Header always set X-Content-Type-Options "nosniff"
    Header always set X-Frame-Options "SAMEORIGIN"
    Header always set X-XSS-Protection "1; mode=block"
    Header always set Referrer-Policy "strict-origin-when-cross-origin"

    # === CACHE CHO PWA ===
    # Service Worker - không cache
    <FilesMatch "sw\.js$">
        Header set Cache-Control "no-cache, no-store, must-revalidate"
        Header set Pragma "no-cache"
        Header set Expires "0"
    </FilesMatch>

    # manifest.json - cache ngắn
    <FilesMatch "manifest\.json$">
        Header set Cache-Control "public, max-age=86400"
    </FilesMatch>

    # Static assets - cache dài
    <FilesMatch "\.(css|js|svg|png|jpg|jpeg|gif|ico|woff2?)$">
        Header set Cache-Control "public, max-age=2592000, immutable"
    </FilesMatch>

    # HTML - cache ngắn
    <FilesMatch "\.html$">
        Header set Cache-Control "public, max-age=3600"
    </FilesMatch>

    # === NÉN GZIP ===
    <IfModule mod_deflate.c>
        AddOutputFilterByType DEFLATE text/html
        AddOutputFilterByType DEFLATE text/css
        AddOutputFilterByType DEFLATE text/javascript
        AddOutputFilterByType DEFLATE application/javascript
        AddOutputFilterByType DEFLATE application/json
        AddOutputFilterByType DEFLATE image/svg+xml
    </IfModule>

    # === MIME TYPES CHO PWA ===
    AddType application/manifest+json .json
    AddType text/cache-manifest .appcache

    # === LOG ===
    ErrorLog ${APACHE_LOG_DIR}/amlich-error.log
    CustomLog ${APACHE_LOG_DIR}/amlich-access.log combined
</VirtualHost>
VHOST_EOF

log_info "Cấu hình VirtualHost đã được tạo."

# === BƯỚC 6: TẠO FILE .HTACCESS ===
log_info "Bước 6: Tạo file .htaccess..."

cat > "${WEB_ROOT}/.htaccess" << 'HTACCESS_EOF'
# Bật RewriteEngine
RewriteEngine On

# Chuyển hướng HTTP sang HTTPS
RewriteCond %{HTTPS} off
RewriteRule ^(.*)$ https://%{HTTP_HOST}%{REQUEST_URI} [L,R=301]

# SPA fallback - chuyển tất cả request về index.html
# (trừ file tồn tại thực)
RewriteCond %{REQUEST_FILENAME} !-f
RewriteCond %{REQUEST_FILENAME} !-d
RewriteRule ^(.*)$ /index.html [L]

# Bảo mật - chặn truy cập file ẩn
<FilesMatch "^\.">
    Require all denied
</FilesMatch>

# Cache headers
<IfModule mod_expires.c>
    ExpiresActive On
    ExpiresByType text/html "access plus 1 hour"
    ExpiresByType text/css "access plus 1 month"
    ExpiresByType application/javascript "access plus 1 month"
    ExpiresByType image/svg+xml "access plus 1 month"
    ExpiresByType image/png "access plus 1 month"
    ExpiresByType application/json "access plus 1 day"
</IfModule>
HTACCESS_EOF

chown www-data:www-data "${WEB_ROOT}/.htaccess"

# === BƯỚC 7: KÍCH HOẠT SITE ===
log_info "Bước 7: Kích hoạt site..."

a2ensite "${DOMAIN}.conf"

# Kiểm tra cú pháp Apache
if apache2ctl configtest 2>&1 | grep -q "Syntax OK"; then
    log_info "Cấu hình Apache hợp lệ."
else
    log_error "Lỗi cấu hình Apache! Kiểm tra lại."
    apache2ctl configtest
    exit 1
fi

# Reload Apache
systemctl reload apache2
log_info "Apache đã được reload."

# === BƯỚC 8: CÀI ĐẶT SSL (Let's Encrypt) ===
log_info "Bước 8: Cài đặt SSL Certificate..."
echo ""
log_warn "Đảm bảo DNS của ${DOMAIN} đã trỏ về IP server này!"
echo ""

read -p "Bạn muốn cài SSL ngay bây giờ? (y/n): " INSTALL_SSL

if [ "$INSTALL_SSL" = "y" ] || [ "$INSTALL_SSL" = "Y" ]; then
    certbot --apache -d "${DOMAIN}" --non-interactive --agree-tos --redirect \
        --email admin@hotrogiaiphapso.info || {
        log_warn "Không thể cài SSL tự động. Bạn có thể chạy lại sau:"
        log_warn "  sudo certbot --apache -d ${DOMAIN}"
    }
else
    log_info "Bỏ qua cài SSL. Chạy lệnh sau khi sẵn sàng:"
    log_info "  sudo certbot --apache -d ${DOMAIN}"
fi

# === BƯỚC 9: CÀI ĐẶT AUTO-RENEW SSL ===
log_info "Bước 9: Kiểm tra auto-renew SSL..."

if systemctl is-active --quiet certbot.timer 2>/dev/null; then
    log_info "Certbot timer đã active - SSL sẽ tự gia hạn."
elif crontab -l 2>/dev/null | grep -q certbot || [ -f /etc/cron.d/certbot ]; then
    log_info "Certbot cron job đã tồn tại - SSL sẽ tự gia hạn."
else
    # Thêm cron job renew 2 lần/ngày
    log_info "Tạo cron job auto-renew SSL..."
    echo "0 0,12 * * * root certbot renew --quiet --deploy-hook 'systemctl reload apache2'" > /etc/cron.d/certbot-amlich
    chmod 644 /etc/cron.d/certbot-amlich
    log_info "Đã tạo cron job auto-renew tại /etc/cron.d/certbot-amlich"
fi

# === HOÀN TẤT ===
echo ""
echo "=============================================="
echo "  CÀI ĐẶT HOÀN TẤT!"
echo "=============================================="
echo ""
log_info "Domain: https://${DOMAIN}"
log_info "Web Root: ${WEB_ROOT}"
log_info "Apache Config: ${APACHE_CONF}"
log_info "Log files:"
log_info "  - Error: /var/log/apache2/amlich-error.log"
log_info "  - Access: /var/log/apache2/amlich-access.log"
echo ""
log_info "Các lệnh hữu ích:"
echo "  - Kiểm tra trạng thái: sudo systemctl status apache2"
echo "  - Xem log lỗi: sudo tail -f /var/log/apache2/amlich-error.log"
echo "  - Reload Apache: sudo systemctl reload apache2"
echo "  - Renew SSL: sudo certbot renew --dry-run"
echo ""
