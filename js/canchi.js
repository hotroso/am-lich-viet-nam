/**
 * Can Chi (Heavenly Stems & Earthly Branches) calculations
 * Ported from CanChi.kt
 */

const CanChi = (() => {
    const THIEN_CAN = ['Giáp', 'Ất', 'Bính', 'Đinh', 'Mậu', 'Kỷ', 'Canh', 'Tân', 'Nhâm', 'Quý'];

    const DIA_CHI = ['Tý', 'Sửu', 'Dần', 'Mão', 'Thìn', 'Tị', 'Ngọ', 'Mùi', 'Thân', 'Dậu', 'Tuất', 'Hợi'];

    const THANG_AM = ['Giêng', 'Hai', 'Ba', 'Tư', 'Năm', 'Sáu', 'Bảy', 'Tám', 'Chín', 'Mười', 'M.Một', 'Chạp'];

    const TIET_KHI = [
        'Xuân phân', 'Thanh minh', 'Cố vũ', 'Lập hạ',
        'Tiểu mãn', 'Mang chủng', 'Hạ chí', 'Tiểu thử',
        'Đại thử', 'Lập thu', 'Xử thử', 'Bạch lộ',
        'Thu phân', 'Hàn lộ', 'Sương giáng', 'Lập đông',
        'Tiểu tuyết', 'Đại tuyết', 'Đông chí', 'Tiểu hàn',
        'Đại hàn', 'Lập xuân', 'Vũ thủy', 'Kinh trập'
    ];

    const GIO_HOANG_DAO = [
        '110100101100', '001101001011', '110011010010',
        '101100110100', '001011001101', '010010110011'
    ];

    const TRUC = ['Kiến', 'Trừ', 'Mãn', 'Bình', 'Định', 'Chấp', 'Phá', 'Nguy', 'Thành', 'Thu', 'Khai', 'Bế'];

    const TRUC_DETAIL = [
        'Kiến: Tốt cho khởi công, xây dựng, khai trương',
        'Trừ: Tốt cho dọn dẹp, phá bỏ cái cũ, chữa bệnh',
        'Mãn: Tốt cho cầu tài, cưới hỏi, khai trương',
        'Bình: Tốt cho mọi việc thường nhật, giao dịch',
        'Định: Tốt cho cưới hỏi, hợp tác, giao kết',
        'Chấp: Tốt cho xây dựng, sửa chữa, trồng cây',
        'Phá: Kiêng mọi việc, chỉ nên phá dỡ',
        'Nguy: Kiêng khởi sự, leo trèo, mạo hiểm',
        'Thành: Tốt cho mọi việc lớn, khai trương, cưới hỏi',
        'Thu: Tốt cho thu nợ, gặt hái, kết thúc',
        'Khai: Tốt cho khai trương, nhập học, khởi sự',
        'Bế: Kiêng mọi việc lớn, chỉ nên nghỉ ngơi'
    ];

    const NGU_HANH_NAP_AM = [
        'Hải Trung Kim', 'Hải Trung Kim', 'Lư Trung Hỏa', 'Lư Trung Hỏa',
        'Đại Lâm Mộc', 'Đại Lâm Mộc', 'Lộ Bàng Thổ', 'Lộ Bàng Thổ',
        'Kiếm Phong Kim', 'Kiếm Phong Kim', 'Sơn Đầu Hỏa', 'Sơn Đầu Hỏa',
        'Giản Hạ Thủy', 'Giản Hạ Thủy', 'Thành Đầu Thổ', 'Thành Đầu Thổ',
        'Bạch Lạp Kim', 'Bạch Lạp Kim', 'Dương Liễu Mộc', 'Dương Liễu Mộc',
        'Tuyền Trung Thủy', 'Tuyền Trung Thủy', 'Ốc Thượng Thổ', 'Ốc Thượng Thổ',
        'Tích Lịch Hỏa', 'Tích Lịch Hỏa', 'Tùng Bách Mộc', 'Tùng Bách Mộc',
        'Trường Lưu Thủy', 'Trường Lưu Thủy', 'Sa Trung Kim', 'Sa Trung Kim',
        'Sơn Hạ Hỏa', 'Sơn Hạ Hỏa', 'Bình Địa Mộc', 'Bình Địa Mộc',
        'Bích Thượng Thổ', 'Bích Thượng Thổ', 'Kim Bạch Kim', 'Kim Bạch Kim',
        'Phú Đăng Hỏa', 'Phú Đăng Hỏa', 'Thiên Hà Thủy', 'Thiên Hà Thủy',
        'Đại Trạch Thổ', 'Đại Trạch Thổ', 'Thoa Xuyến Kim', 'Thoa Xuyến Kim',
        'Tang Đố Mộc', 'Tang Đố Mộc', 'Đại Khê Thủy', 'Đại Khê Thủy',
        'Sa Trung Thổ', 'Sa Trung Thổ', 'Thiên Thượng Hỏa', 'Thiên Thượng Hỏa',
        'Thạch Lựu Mộc', 'Thạch Lựu Mộc', 'Đại Hải Thủy', 'Đại Hải Thủy'
    ];

    const TAI_THAN = ['Đông Nam', 'Đông', 'Bắc', 'Bắc', 'Đông Bắc', 'Tây', 'Tây Nam', 'Tây Nam', 'Nam', 'Đông Nam'];
    const HY_THAN = ['Đông Bắc', 'Tây Bắc', 'Tây Nam', 'Nam', 'Đông Nam', 'Đông Bắc', 'Tây Bắc', 'Tây Nam', 'Nam', 'Đông Nam'];
    const HAC_THAN = ['Tây Nam', 'Tây Bắc', 'Đông Nam', 'Đông Bắc', 'Nam', 'Tây Nam', 'Tây Bắc', 'Đông Nam', 'Đông Bắc', 'Nam'];

    const HOANG_DAO_HAC_DAO = [
        'Hoàng Đạo - Thanh Long', 'Hắc Đạo - Minh Đường', 'Hắc Đạo - Thiên Hình',
        'Hoàng Đạo - Chu Tước', 'Hoàng Đạo - Kim Quỹ', 'Hắc Đạo - Thiên Đức',
        'Hoàng Đạo - Bạch Hổ', 'Hoàng Đạo - Ngọc Đường', 'Hắc Đạo - Thiên Lao',
        'Hắc Đạo - Huyền Vũ', 'Hoàng Đạo - Tư Mệnh', 'Hắc Đạo - Câu Trần'
    ];

    const TRUC_NEN_LAM = [
        ['Xuất hành', 'Khởi công', 'Khai trương', 'Động thổ', 'Nhậm chức', 'Giao dịch'],
        ['Chữa bệnh', 'Dọn dẹp', 'Tẩy trần', 'Cúng tế', 'Giải trừ xui xẻo', 'Phá dỡ'],
        ['Cầu tài', 'Cưới hỏi', 'Khai trương', 'Nhập trạch', 'Thu tiền', 'Ký hợp đồng'],
        ['Giao dịch', 'Đi xa', 'Ký kết', 'Mở cửa hàng', 'Mua bán', 'Sửa chữa nhỏ'],
        ['Cưới hỏi', 'Ăn hỏi', 'Hợp tác', 'Giao kết', 'Nhập học', 'Cầu phúc'],
        ['Xây dựng', 'Sửa chữa', 'Trồng cây', 'Chăn nuôi', 'Thu hoạch', 'Mua tài sản'],
        ['Phá dỡ', 'Tháo dỡ', 'Điều trị bệnh'],
        ['Cúng bái', 'Cầu an', 'Bốc thuốc'],
        ['Khai trương', 'Cưới hỏi', 'Nhập trạch', 'Xuất hành', 'Khởi công', 'Ký hợp đồng', 'Giao dịch lớn'],
        ['Thu nợ', 'Gặt hái', 'Kết thúc việc', 'Thu tiền', 'Cất tiền'],
        ['Khai trương', 'Nhập học', 'Khởi sự', 'Khai bút', 'Xuất hành', 'Cưới hỏi', 'Dọn nhà'],
        ['Nghỉ ngơi', 'Cất của', 'Trị bệnh mãn tính']
    ];

    const TRUC_KHONG_NEN = [
        ['Kiện cáo', 'Mổ xẻ'],
        ['Cưới hỏi', 'Khai trương', 'Khởi công'],
        ['Kiện cáo', 'Động thổ', 'Gieo trồng'],
        ['Cầu phúc lớn', 'Kiện tụng'],
        ['Kiện cáo', 'Tranh chấp', 'Xuất hành xa'],
        ['Xuất hành xa', 'Dọn nhà', 'Mở cửa hàng'],
        ['Cưới hỏi', 'Khai trương', 'Ký hợp đồng', 'Khởi công', 'Nhập trạch', 'Xuất hành'],
        ['Khởi công', 'Leo trèo', 'Xuất hành xa', 'Giao dịch lớn', 'Nhập trạch', 'Mạo hiểm'],
        ['Kiện cáo', 'Phá dỡ'],
        ['Khởi công', 'Khai trương', 'Xuất hành'],
        ['Chôn cất', 'Phá dỡ'],
        ['Khai trương', 'Xuất hành', 'Cưới hỏi', 'Động thổ', 'Khởi công', 'Ký kết']
    ];

    function canChiNgay(jdn) {
        const can = THIEN_CAN[(jdn + 9) % 10];
        const chi = DIA_CHI[(jdn + 1) % 12];
        return `${can} ${chi}`;
    }

    function canChiThang(lunarMonth, lunarYear) {
        const can = THIEN_CAN[(lunarYear * 12 + lunarMonth + 3) % 10];
        const chi = DIA_CHI[(lunarMonth + 1) % 12];
        return `${can} ${chi}`;
    }

    function canChiNam(lunarYear) {
        const can = THIEN_CAN[(lunarYear + 6) % 10];
        const chi = DIA_CHI[(lunarYear + 8) % 12];
        return `${can} ${chi}`;
    }

    function canChiGio(jdn) {
        const can = THIEN_CAN[((jdn - 1) * 2) % 10];
        const chi = DIA_CHI[0];
        return `${can} ${chi}`;
    }

    function getTietKhi(jdn) {
        const idx = VietCalendar.getSunLongitudeIndex(jdn);
        return TIET_KHI[idx];
    }

    function getTruc(jdn) {
        return TRUC[(jdn + 1) % 12];
    }

    function getTrucDetail(jdn) {
        return TRUC_DETAIL[(jdn + 1) % 12];
    }

    function getNguHanhNapAm(jdn) {
        return NGU_HANH_NAP_AM[((jdn % 60) + 60) % 60];
    }

    function getHoangDaoHacDao(jdn) {
        const chiIndex = (jdn + 1) % 12;
        const info = HOANG_DAO_HAC_DAO[chiIndex];
        const isHoangDao = info.startsWith('Hoàng Đạo');
        return { isHoangDao, label: info };
    }

    function getGioHoangDao(jdn) {
        const pattern = GIO_HOANG_DAO[((jdn + 1) % 12) % 6];
        const parts = [];
        for (let i = 0; i < 12; i++) {
            if (pattern[i] === '1') {
                const startHour = (i * 2 + 23) % 24;
                const endHour = (i * 2 + 1) % 24;
                parts.push(`${DIA_CHI[i]} (${startHour}h-${endHour}h)`);
            }
        }
        return parts.join(', ');
    }

    function getNhiThapBatTu(jdn) {
        return ((jdn + 12) % 28) + 1;
    }

    function getDayAdvice(jdn) {
        const trucIndex = (jdn + 1) % 12;
        const truc = TRUC[trucIndex];
        const { isHoangDao, label: hoangDaoLabel } = getHoangDaoHacDao(jdn);

        const trucTot = [0, 2, 4, 8, 10].includes(trucIndex);
        const trucXau = [6, 7, 11].includes(trucIndex);

        let rating, ratingLabel;
        if (trucTot && isHoangDao) {
            rating = 'VERY_GOOD';
            ratingLabel = 'Đại cát';
        } else if (trucTot || isHoangDao) {
            rating = 'GOOD';
            ratingLabel = 'Tốt';
        } else if (trucXau && !isHoangDao) {
            rating = 'VERY_BAD';
            ratingLabel = 'Xấu';
        } else if (trucXau) {
            rating = 'BAD';
            ratingLabel = 'Không tốt';
        } else {
            rating = 'NORMAL';
            ratingLabel = 'Bình thường';
        }

        return {
            rating,
            ratingLabel,
            truc,
            trucIndex,
            trucDetail: TRUC_DETAIL[trucIndex],
            isHoangDao,
            hoangDaoLabel,
            nenLam: TRUC_NEN_LAM[trucIndex],
            khongNen: TRUC_KHONG_NEN[trucIndex]
        };
    }

    function getFullInfo(lunarDate) {
        const jdn = lunarDate.julianDay;
        return {
            ngay: canChiNgay(jdn),
            thang: canChiThang(lunarDate.month, lunarDate.year),
            nam: canChiNam(lunarDate.year),
            gio: canChiGio(jdn),
            tenThangAm: lunarDate.month >= 1 && lunarDate.month <= 12 ? THANG_AM[lunarDate.month - 1] : '',
            tietKhi: getTietKhi(jdn),
            gioHoangDao: getGioHoangDao(jdn),
            nguHanhNapAm: getNguHanhNapAm(jdn),
            hoangDao: getHoangDaoHacDao(jdn),
            truc: getTruc(jdn),
            trucDetail: getTrucDetail(jdn),
            dayAdvice: getDayAdvice(jdn)
        };
    }

    return {
        THIEN_CAN, DIA_CHI, THANG_AM, TIET_KHI, TRUC,
        canChiNgay, canChiThang, canChiNam, canChiGio,
        getTietKhi, getTruc, getTrucDetail, getNguHanhNapAm,
        getHoangDaoHacDao, getGioHoangDao, getNhiThapBatTu,
        getDayAdvice, getFullInfo
    };
})();
