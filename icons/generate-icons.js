/**
 * Script tạo PNG icons từ SVG cho PWA
 * Chạy: node icons/generate-icons.js
 * Yêu cầu: npm install canvas
 */

const { createCanvas } = require('canvas');
const fs = require('fs');
const path = require('path');

const sizes = [180, 192, 512];

function drawIcon(size) {
    const canvas = createCanvas(size, size);
    const ctx = canvas.getContext('2d');
    const scale = size / 192;

    // Background circle
    ctx.beginPath();
    ctx.arc(size / 2, size / 2, size / 2, 0, Math.PI * 2);
    ctx.fillStyle = '#b71c1c';
    ctx.fill();

    // Chinese character 陰
    ctx.fillStyle = 'white';
    ctx.font = `bold ${Math.round(60 * scale)}px serif`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('陰', size / 2, size * 0.42);

    // Text "Âm Lịch VN"
    ctx.font = `${Math.round(18 * scale)}px sans-serif`;
    ctx.fillText('Âm Lịch VN', size / 2, size * 0.67);

    return canvas;
}

const iconsDir = path.join(__dirname);

sizes.forEach(size => {
    const canvas = drawIcon(size);
    const buffer = canvas.toBuffer('image/png');
    const filename = size === 180 ? 'apple-touch-icon-180.png' : `icon-${size}.png`;
    fs.writeFileSync(path.join(iconsDir, filename), buffer);
    console.log(`Generated: ${filename} (${size}x${size})`);
});

console.log('Done! All icons generated.');
