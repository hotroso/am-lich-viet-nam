package io.github.hotroso.vietnameselunarcalendar.lunar

/**
 * Utility for Vietnamese Can Chi (Heavenly Stems & Earthly Branches) calculations.
 */
object CanChi {

    val THIEN_CAN = arrayOf(
        "Giáp", "Ất", "Bính", "Đinh", "Mậu",
        "Kỷ", "Canh", "Tân", "Nhâm", "Quý"
    )

    val DIA_CHI = arrayOf(
        "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tị",
        "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"
    )

    val THANG_AM = arrayOf(
        "Giêng", "Hai", "Ba", "Tư", "Năm", "Sáu",
        "Bảy", "Tám", "Chín", "Mười", "M.Một", "Chạp"
    )

    val TIET_KHI = arrayOf(
        "Xuân phân", "Thanh minh", "Cố vũ", "Lập hạ",
        "Tiểu mãn", "Mang chủng", "Hạ chí", "Tiểu thử",
        "Đại thử", "Lập thu", "Xử thử", "Bạch lộ",
        "Thu phân", "Hàn lộ", "Sương giáng", "Lập đông",
        "Tiểu tuyết", "Đại tuyết", "Đông chí", "Tiểu hàn",
        "Đại hàn", "Lập xuân", "Vũ thủy", "Kinh trập"
    )

    // Giờ hoàng đạo patterns (6 patterns, mỗi pattern 12 ký tự 0/1)
    val GIO_HOANG_DAO = arrayOf(
        "110100101100", "001101001011", "110011010010",
        "101100110100", "001011001101", "010010110011"
    )

    val TRUC = arrayOf(
        "Kiến", "Trừ", "Mãn", "Bình", "Định", "Chấp",
        "Phá", "Nguy", "Thành", "Thu", "Khai", "Bế"
    )

    val TRUC_DETAIL = arrayOf(
        "Kiến: Tốt cho khởi công, xây dựng, khai trương",
        "Trừ: Tốt cho dọn dẹp, phá bỏ cái cũ, chữa bệnh",
        "Mãn: Tốt cho cầu tài, cưới hỏi, khai trương",
        "Bình: Tốt cho mọi việc thường nhật, giao dịch",
        "Định: Tốt cho cưới hỏi, hợp tác, giao kết",
        "Chấp: Tốt cho xây dựng, sửa chữa, trồng cây",
        "Phá: Kiêng mọi việc, chỉ nên phá dỡ",
        "Nguy: Kiêng khởi sự, leo trèo, mạo hiểm",
        "Thành: Tốt cho mọi việc lớn, khai trương, cưới hỏi",
        "Thu: Tốt cho thu nợ, gặt hái, kết thúc",
        "Khai: Tốt cho khai trương, nhập học, khởi sự",
        "Bế: Kiêng mọi việc lớn, chỉ nên nghỉ ngơi"
    )

    // Ngũ hành nạp âm (60 giáp tử)
    val NGU_HANH_NAP_AM = arrayOf(
        "Hải Trung Kim", "Hải Trung Kim", "Lư Trung Hỏa", "Lư Trung Hỏa",
        "Đại Lâm Mộc", "Đại Lâm Mộc", "Lộ Bàng Thổ", "Lộ Bàng Thổ",
        "Kiếm Phong Kim", "Kiếm Phong Kim", "Sơn Đầu Hỏa", "Sơn Đầu Hỏa",
        "Giản Hạ Thủy", "Giản Hạ Thủy", "Thành Đầu Thổ", "Thành Đầu Thổ",
        "Bạch Lạp Kim", "Bạch Lạp Kim", "Dương Liễu Mộc", "Dương Liễu Mộc",
        "Tuyền Trung Thủy", "Tuyền Trung Thủy", "Ốc Thượng Thổ", "Ốc Thượng Thổ",
        "Tích Lịch Hỏa", "Tích Lịch Hỏa", "Tùng Bách Mộc", "Tùng Bách Mộc",
        "Trường Lưu Thủy", "Trường Lưu Thủy", "Sa Trung Kim", "Sa Trung Kim",
        "Sơn Hạ Hỏa", "Sơn Hạ Hỏa", "Bình Địa Mộc", "Bình Địa Mộc",
        "Bích Thượng Thổ", "Bích Thượng Thổ", "Kim Bạch Kim", "Kim Bạch Kim",
        "Phú Đăng Hỏa", "Phú Đăng Hỏa", "Thiên Hà Thủy", "Thiên Hà Thủy",
        "Đại Trạch Thổ", "Đại Trạch Thổ", "Thoa Xuyến Kim", "Thoa Xuyến Kim",
        "Tang Đố Mộc", "Tang Đố Mộc", "Đại Khê Thủy", "Đại Khê Thủy",
        "Sa Trung Thổ", "Sa Trung Thổ", "Thiên Thượng Hỏa", "Thiên Thượng Hỏa",
        "Thạch Lựu Mộc", "Thạch Lựu Mộc", "Đại Hải Thủy", "Đại Hải Thủy"
    )

    // Hướng xuất hành - Tài thần (theo can ngày)
    val TAI_THAN = arrayOf(
        "Đông Nam", "Đông", "Bắc", "Bắc", "Đông Bắc",
        "Tây", "Tây Nam", "Tây Nam", "Nam", "Đông Nam"
    )

    // Hướng xuất hành - Hỷ thần (theo can ngày)
    val HY_THAN = arrayOf(
        "Đông Bắc", "Tây Bắc", "Tây Nam", "Nam", "Đông Nam",
        "Đông Bắc", "Tây Bắc", "Tây Nam", "Nam", "Đông Nam"
    )

    // Hướng xuất hành - Hạc thần (theo can ngày)
    val HAC_THAN = arrayOf(
        "Tây Nam", "Tây Bắc", "Đông Nam", "Đông Bắc", "Nam",
        "Tây Nam", "Tây Bắc", "Đông Nam", "Đông Bắc", "Nam"
    )

    // 12 ngày Hoàng Đạo / Hắc Đạo
    val HOANG_DAO_HAC_DAO = arrayOf(
        "Hoàng Đạo - Thanh Long",   // 0
        "Hắc Đạo - Minh Đường",     // 1
        "Hắc Đạo - Thiên Hình",     // 2
        "Hoàng Đạo - Chu Tước",     // 3
        "Hoàng Đạo - Kim Quỹ",      // 4
        "Hắc Đạo - Thiên Đức",      // 5
        "Hoàng Đạo - Bạch Hổ",      // 6
        "Hoàng Đạo - Ngọc Đường",   // 7
        "Hắc Đạo - Thiên Lao",      // 8
        "Hắc Đạo - Huyền Vũ",       // 9
        "Hoàng Đạo - Tư Mệnh",      // 10
        "Hắc Đạo - Câu Trần"        // 11
    )

    /**
     * Tính can chi cho ngày (dựa trên Julian Day Number).
     */
    fun canChiNgay(jdn: Int): String {
        val can = THIEN_CAN[(jdn + 9) % 10]
        val chi = DIA_CHI[(jdn + 1) % 12]
        return "$can $chi"
    }

    /**
     * Tính can chi cho tháng âm lịch.
     */
    fun canChiThang(lunarMonth: Int, lunarYear: Int): String {
        val can = THIEN_CAN[(lunarYear * 12 + lunarMonth + 3) % 10]
        val chi = DIA_CHI[(lunarMonth + 1) % 12]
        return "$can $chi"
    }

    /**
     * Tính can chi cho năm âm lịch.
     */
    fun canChiNam(lunarYear: Int): String {
        val can = THIEN_CAN[(lunarYear + 6) % 10]
        val chi = DIA_CHI[(lunarYear + 8) % 12]
        return "$can $chi"
    }

    /**
     * Tính can chi cho giờ (giờ đầu tiên = Tý).
     */
    fun canChiGio(jdn: Int): String {
        val can = THIEN_CAN[((jdn - 1) * 2) % 10]
        val chi = DIA_CHI[0]
        return "$can $chi"
    }

    /**
     * Lấy tiết khí theo Julian Day Number.
     */
    fun getTietKhi(jdn: Int): String {
        val sunLongIndex = VietCalendar.getSunLongitudeIndex(jdn)
        return TIET_KHI[sunLongIndex]
    }

    /**
     * Lấy trực theo Julian Day Number.
     */
    fun getTruc(jdn: Int): String {
        return TRUC[(jdn + 1) % 12]
    }

    /**
     * Lấy chi tiết trực (ý nghĩa).
     */
    fun getTrucDetail(jdn: Int): String {
        return TRUC_DETAIL[(jdn + 1) % 12]
    }

    /**
     * Lấy ngũ hành nạp âm của ngày (theo can chi ngày).
     */
    fun getNguHanhNapAm(jdn: Int): String {
        return NGU_HANH_NAP_AM[((jdn % 60) + 60) % 60]
    }

    /**
     * Lấy hướng Tài thần theo can của ngày.
     */
    fun getTaiThan(jdn: Int): String {
        val canIndex = (jdn + 9) % 10
        return TAI_THAN[canIndex]
    }

    /**
     * Lấy hướng Hỷ thần theo can của ngày.
     */
    fun getHyThan(jdn: Int): String {
        val canIndex = (jdn + 9) % 10
        return HY_THAN[canIndex]
    }

    /**
     * Lấy hướng Hạc thần theo can của ngày.
     */
    fun getHacThan(jdn: Int): String {
        val canIndex = (jdn + 9) % 10
        return HAC_THAN[canIndex]
    }

    /**
     * Ngày Hoàng Đạo hay Hắc Đạo.
     * @return Pair(isHoangDao, tenNgay)
     */
    fun getHoangDaoHacDao(jdn: Int): Pair<Boolean, String> {
        val chiIndex = (jdn + 1) % 12
        val info = HOANG_DAO_HAC_DAO[chiIndex]
        val isHoangDao = info.startsWith("Hoàng Đạo")
        return Pair(isHoangDao, info)
    }

    /**
     * Tính nhị thập bát tú (28 ngôi sao) cho ngày.
     * Index 1-28
     */
    fun getNhiThapBatTu(jdn: Int): Int {
        return ((jdn + 12) % 28) + 1
    }

    /**
     * Tính tuổi xung khắc theo ngày.
     */
    fun getTuoiXungKhac(jdn: Int): String {
        val chiIndex = (jdn + 1) % 12
        // Tuổi xung: đối xung (cách 6 cung) và các tuổi tương khắc
        val xungIndex = (chiIndex + 6) % 12
        val phaIndex1 = (chiIndex + 3) % 12
        val phaIndex2 = (chiIndex + 9) % 12
        return "${DIA_CHI[xungIndex]}, ${DIA_CHI[phaIndex1]}, ${DIA_CHI[phaIndex2]}"
    }

    /**
     * Lấy giờ hoàng đạo theo Julian Day.
     * Returns string mô tả các giờ tốt.
     */
    fun getGioHoangDao(jdn: Int): String {
        val pattern = GIO_HOANG_DAO[((jdn + 1) % 12) % 6]
        val sb = StringBuilder()
        var count = 0
        for (i in 0 until 12) {
            if (pattern[i] == '1') {
                if (count > 0) sb.append(", ")
                val startHour = (i * 2 + 23) % 24
                val endHour = (i * 2 + 1) % 24
                sb.append("${DIA_CHI[i]} ($startHour-$endHour)")
                count++
            }
        }
        return sb.toString()
    }

    /**
     * Lấy tên tháng âm lịch.
     */
    fun tenThangAm(month: Int): String {
        return if (month in 1..12) THANG_AM[month - 1] else ""
    }

    // ============================================================
    // NGÀY TỐT / XẤU CHO CÔNG VIỆC (dựa trên Trực + Hoàng Đạo)
    // ============================================================

    /**
     * Mapping từ 12 Trực → danh sách "Nên làm" và "Không nên làm"
     * Dựa trên Bách Gia Lịch và Ngọc Hạp thông thư
     */
    val TRUC_NEN_LAM = arrayOf(
        // 0 - Kiến
        arrayOf("Xuất hành", "Khởi công", "Khai trương", "Động thổ", "Nhậm chức", "Giao dịch"),
        // 1 - Trừ
        arrayOf("Chữa bệnh", "Dọn dẹp", "Tẩy trần", "Cúng tế", "Giải trừ xui xẻo", "Phá dỡ"),
        // 2 - Mãn
        arrayOf("Cầu tài", "Cưới hỏi", "Khai trương", "Nhập trạch", "Thu tiền", "Ký hợp đồng"),
        // 3 - Bình
        arrayOf("Giao dịch", "Đi xa", "Ký kết", "Mở cửa hàng", "Mua bán", "Sửa chữa nhỏ"),
        // 4 - Định
        arrayOf("Cưới hỏi", "Ăn hỏi", "Hợp tác", "Giao kết", "Nhập học", "Cầu phúc"),
        // 5 - Chấp
        arrayOf("Xây dựng", "Sửa chữa", "Trồng cây", "Chăn nuôi", "Thu hoạch", "Mua tài sản"),
        // 6 - Phá
        arrayOf("Phá dỡ", "Tháo dỡ", "Điều trị bệnh"),
        // 7 - Nguy
        arrayOf("Cúng bái", "Cầu an", "Bốc thuốc"),
        // 8 - Thành
        arrayOf("Khai trương", "Cưới hỏi", "Nhập trạch", "Xuất hành", "Khởi công", "Ký hợp đồng", "Giao dịch lớn"),
        // 9 - Thu
        arrayOf("Thu nợ", "Gặt hái", "Kết thúc việc", "Thu tiền", "Cất tiền"),
        // 10 - Khai
        arrayOf("Khai trương", "Nhập học", "Khởi sự", "Khai bút", "Xuất hành", "Cưới hỏi", "Dọn nhà"),
        // 11 - Bế
        arrayOf("Nghỉ ngơi", "Cất của", "Trị bệnh mãn tính")
    )

    val TRUC_KHONG_NEN = arrayOf(
        // 0 - Kiến
        arrayOf("Kiện cáo", "Mổ xẻ"),
        // 1 - Trừ
        arrayOf("Cưới hỏi", "Khai trương", "Khởi công"),
        // 2 - Mãn
        arrayOf("Kiện cáo", "Động thổ", "Gieo trồng"),
        // 3 - Bình
        arrayOf("Cầu phúc lớn", "Kiện tụng"),
        // 4 - Định
        arrayOf("Kiện cáo", "Tranh chấp", "Xuất hành xa"),
        // 5 - Chấp
        arrayOf("Xuất hành xa", "Dọn nhà", "Mở cửa hàng"),
        // 6 - Phá
        arrayOf("Cưới hỏi", "Khai trương", "Ký hợp đồng", "Khởi công", "Nhập trạch", "Xuất hành"),
        // 7 - Nguy
        arrayOf("Khởi công", "Leo trèo", "Xuất hành xa", "Giao dịch lớn", "Nhập trạch", "Mạo hiểm"),
        // 8 - Thành
        arrayOf("Kiện cáo", "Phá dỡ"),
        // 9 - Thu
        arrayOf("Khởi công", "Khai trương", "Xuất hành"),
        // 10 - Khai
        arrayOf("Chôn cất", "Phá dỡ"),
        // 11 - Bế
        arrayOf("Khai trương", "Xuất hành", "Cưới hỏi", "Động thổ", "Khởi công", "Ký kết")
    )

    /**
     * Đánh giá tổng quan ngày: Tốt / Bình thường / Xấu
     * Dựa trên: Trực + Hoàng Đạo/Hắc Đạo
     */
    enum class DayRating {
        VERY_GOOD,  // Hoàng đạo + Trực tốt
        GOOD,       // Hoàng đạo hoặc Trực tốt
        NORMAL,     // Bình thường
        BAD,        // Trực xấu + Hắc đạo
        VERY_BAD    // Phá/Bế + Hắc đạo
    }

    data class DayAdvice(
        val rating: DayRating,
        val ratingLabel: String,
        val truc: String,
        val trucIndex: Int,
        val isHoangDao: Boolean,
        val hoangDaoLabel: String,
        val nenLam: List<String>,
        val khongNen: List<String>
    )

    /**
     * Tính lời khuyên ngày tốt/xấu cho một ngày cụ thể.
     */
    fun getDayAdvice(jdn: Int): DayAdvice {
        val trucIndex = (jdn + 1) % 12
        val truc = TRUC[trucIndex]
        val (isHoangDao, hoangDaoLabel) = getHoangDaoHacDao(jdn)

        // Trực tốt: Kiến(0), Mãn(2), Định(4), Thành(8), Khai(10)
        // Trực xấu: Phá(6), Nguy(7), Bế(11)
        // Trực trung bình: Trừ(1), Bình(3), Chấp(5), Thu(9)
        val trucTot = trucIndex in intArrayOf(0, 2, 4, 8, 10)
        val trucXau = trucIndex in intArrayOf(6, 7, 11)

        val rating = when {
            trucTot && isHoangDao -> DayRating.VERY_GOOD
            trucTot || isHoangDao -> DayRating.GOOD
            trucXau && !isHoangDao -> DayRating.VERY_BAD
            trucXau -> DayRating.BAD
            else -> DayRating.NORMAL
        }

        val ratingLabel = when (rating) {
            DayRating.VERY_GOOD -> "Đại cát"
            DayRating.GOOD -> "Tốt"
            DayRating.NORMAL -> "Bình thường"
            DayRating.BAD -> "Không tốt"
            DayRating.VERY_BAD -> "Xấu"
        }

        return DayAdvice(
            rating = rating,
            ratingLabel = ratingLabel,
            truc = truc,
            trucIndex = trucIndex,
            isHoangDao = isHoangDao,
            hoangDaoLabel = hoangDaoLabel,
            nenLam = TRUC_NEN_LAM[trucIndex].toList(),
            khongNen = TRUC_KHONG_NEN[trucIndex].toList()
        )
    }

    /**
     * Chi tiết can chi đầy đủ cho một ngày.
     */
    data class CanChiInfo(
        val ngay: String,
        val thang: String,
        val nam: String,
        val gio: String,
        val tenThangAm: String,
        val tietKhi: String,
        val gioHoangDao: String
    )

    fun getFullInfo(lunarDate: LunarDate): CanChiInfo {
        val jdn = lunarDate.julianDay
        return CanChiInfo(
            ngay = canChiNgay(jdn),
            thang = canChiThang(lunarDate.month, lunarDate.year),
            nam = canChiNam(lunarDate.year),
            gio = canChiGio(jdn),
            tenThangAm = tenThangAm(lunarDate.month),
            tietKhi = getTietKhi(jdn),
            gioHoangDao = getGioHoangDao(jdn)
        )
    }
}
