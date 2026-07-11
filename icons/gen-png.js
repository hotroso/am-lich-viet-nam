// Generate PNG icons using Node.js built-in capabilities
// Creates simple colored circle PNG icons without external dependencies
const fs = require('fs');
const path = require('path');

function createPNG(width, height, r, g, b) {
    // Create a minimal PNG with solid circle
    // Using raw PNG format
    const { createCanvas } = (() => {
        try { return require('canvas'); } catch(e) { return null; }
    })() || {};
    
    if (createCanvas) {
        return createCanvasPNG(width, height);
    }
    
    // Fallback: create a simple solid color PNG
    // PNG signature
    const signature = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);
    
    // IHDR chunk
    const ihdr = createIHDR(width, height);
    
    // IDAT chunk (raw pixel data with zlib)
    const idat = createIDAT(width, height, r, g, b);
    
    // IEND chunk  
    const iend = createIEND();
    
    return Buffer.concat([signature, ihdr, idat, iend]);
}

function createIHDR(width, height) {
    const data = Buffer.alloc(13);
    data.writeUInt32BE(width, 0);
    data.writeUInt32BE(height, 4);
    data[8] = 8; // bit depth
    data[9] = 6; // color type (RGBA)
    data[10] = 0; // compression
    data[11] = 0; // filter
    data[12] = 0; // interlace
    return createChunk('IHDR', data);
}

function createIDAT(width, height, r, g, b) {
    const zlib = require('zlib');
    
    // Create raw image data (filter byte + RGBA for each pixel)
    const rowSize = 1 + width * 4; // filter byte + pixels (RGBA)
    const raw = Buffer.alloc(rowSize * height);
    
    const cx = width / 2;
    const cy = height / 2;
    const radius = width / 2;
    
    for (let y = 0; y < height; y++) {
        const offset = y * rowSize;
        raw[offset] = 0; // No filter
        for (let x = 0; x < width; x++) {
            const px = offset + 1 + x * 4;
            // Check if pixel is inside circle (with anti-aliasing)
            const dx = x - cx + 0.5;
            const dy = y - cy + 0.5;
            const dist = Math.sqrt(dx * dx + dy * dy);
            
            if (dist <= radius - 1) {
                // Fully inside
                raw[px] = r;
                raw[px + 1] = g;
                raw[px + 2] = b;
                raw[px + 3] = 255;
            } else if (dist <= radius) {
                // Edge (anti-alias)
                const alpha = Math.round((radius - dist) * 255);
                raw[px] = r;
                raw[px + 1] = g;
                raw[px + 2] = b;
                raw[px + 3] = alpha;
            } else {
                // Outside - transparent
                raw[px] = 0;
                raw[px + 1] = 0;
                raw[px + 2] = 0;
                raw[px + 3] = 0;
            }
        }
    }
    
    const compressed = zlib.deflateSync(raw, { level: 9 });
    return createChunk('IDAT', compressed);
}

function createIEND() {
    return createChunk('IEND', Buffer.alloc(0));
}

function createChunk(type, data) {
    const length = Buffer.alloc(4);
    length.writeUInt32BE(data.length);
    
    const typeBuffer = Buffer.from(type, 'ascii');
    const crcData = Buffer.concat([typeBuffer, data]);
    const crc = crc32(crcData);
    const crcBuffer = Buffer.alloc(4);
    crcBuffer.writeUInt32BE(crc >>> 0);
    
    return Buffer.concat([length, typeBuffer, data, crcBuffer]);
}

function crc32(buf) {
    let crc = 0xFFFFFFFF;
    for (let i = 0; i < buf.length; i++) {
        crc ^= buf[i];
        for (let j = 0; j < 8; j++) {
            if (crc & 1) {
                crc = (crc >>> 1) ^ 0xEDB88320;
            } else {
                crc = crc >>> 1;
            }
        }
    }
    return (crc ^ 0xFFFFFFFF) >>> 0;
}

// Generate icons with primary color #b71c1c (183, 28, 28)
const sizes = { 'apple-touch-icon-180.png': 180, 'icon-192.png': 192, 'icon-512.png': 512 };

Object.entries(sizes).forEach(([filename, size]) => {
    const png = createPNG(size, size, 183, 28, 28);
    fs.writeFileSync(path.join(__dirname, filename), png);
    console.log(`Generated: ${filename} (${size}x${size})`);
});

console.log('');
console.log('Note: These are placeholder circle icons.');
console.log('For proper icons with text, open icons/generate-png-icons.html in a browser');
console.log('and download the generated PNG files.');
