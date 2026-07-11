#!/bin/bash
###############################################################################
# Script cập nhật code PWA Âm Lịch Việt Nam
# Dùng khi muốn deploy phiên bản mới
###############################################################################

set -e

DOMAIN="amlich.hotrogiaiphapso.info"
WEB_ROOT="/var/www/html/sites/sunbabe/public_html/amlich.hotrogiaiphapso.info"
REPO_SOURCE="$(cd "$(dirname "$0")/.." && pwd)"
BACKUP_DIR="/var/www/html/sites/sunbabe/backups/${DOMAIN}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Màu sắc
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }

# Kiểm tra quyền root
if [ "$EUID" -ne 0 ]; then
    echo "Vui lòng chạy với quyền root (sudo)"
    exit 1
fi

echo "=== CẬP NHẬT ÂM LỊCH PWA ==="
echo ""

# Backup phiên bản cũ
log_info "Backup phiên bản hiện tại..."
mkdir -p "${BACKUP_DIR}"
if [ -d "${WEB_ROOT}" ]; then
    tar -czf "${BACKUP_DIR}/backup_${TIMESTAMP}.tar.gz" -C "${WEB_ROOT}" .
    log_info "Backup lưu tại: ${BACKUP_DIR}/backup_${TIMESTAMP}.tar.gz"
fi

# Xóa backup cũ hơn 7 ngày
find "${BACKUP_DIR}" -name "backup_*.tar.gz" -mtime +7 -delete 2>/dev/null || true

# Sync code mới
log_info "Đồng bộ code mới..."
rsync -av --delete \
    --exclude='deploy' \
    --exclude='node_modules' \
    --exclude='.git' \
    --exclude='.htaccess' \
    "${REPO_SOURCE}/" "${WEB_ROOT}/"

# Phân quyền
chown -R www-data:www-data "${WEB_ROOT}"
chmod -R 755 "${WEB_ROOT}"

# Reload Apache (nếu cần)
systemctl reload apache2

log_info "Cập nhật hoàn tất!"
log_info "Phiên bản mới đã được deploy lên https://${DOMAIN}"
echo ""
log_warn "Lưu ý: Người dùng có thể cần xóa cache trình duyệt"
log_warn "hoặc chờ Service Worker tự cập nhật."
echo ""
