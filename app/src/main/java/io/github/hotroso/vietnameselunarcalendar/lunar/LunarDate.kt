package io.github.hotroso.vietnameselunarcalendar.lunar

/**
 * Represents a Vietnamese Lunar date.
 * @param day Ngày âm lịch
 * @param month Tháng âm lịch
 * @param year Năm âm lịch
 * @param isLeapMonth Có phải tháng nhuận không
 * @param julianDay Julian Day Number
 * @param isBigMonth Tháng đủ (30 ngày) hay tháng thiếu (29 ngày)
 */
data class LunarDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val isLeapMonth: Int = 0,  // 0 = không nhuận, 1 = nhuận
    val julianDay: Int = 0,
    val isBigMonth: Boolean = false
)
