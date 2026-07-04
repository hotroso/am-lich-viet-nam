package io.github.hotroso.vietnameselunarcalendar.reminder

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tần suất nhắc lịch.
 */
enum class ReminderRepeat {
    ONCE,       // Nhắc 1 lần (năm nay)
    YEARLY,     // Hàng năm (ngày giỗ, sinh nhật âm lịch)
    MONTHLY,    // Hàng tháng (mùng 1, rằm...)
    WEEKLY,     // Hàng tuần
    DAILY       // Hàng ngày
}

/**
 * Sự kiện âm lịch do người dùng tạo.
 *
 * Ví dụ: Ngày giỗ ông nội = ngày 15 tháng 3 âm lịch, nhắc hàng năm,
 * nhắc trước 1 ngày và trong ngày.
 */
@Entity(tableName = "lunar_events")
data class LunarEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Tên sự kiện, ví dụ: "Giỗ ông nội" */
    val title: String,

    /** Ghi chú thêm */
    val note: String = "",

    /** Ngày âm lịch (1-30) */
    val lunarDay: Int,

    /** Tháng âm lịch (1-12) */
    val lunarMonth: Int,

    /** Năm âm lịch cụ thể (0 = tất cả các năm, dùng cho sự kiện hàng năm) */
    val lunarYear: Int = 0,

    /** Có phải tháng nhuận không */
    val isLeapMonth: Boolean = false,

    /** Tần suất lặp lại */
    val repeat: ReminderRepeat = ReminderRepeat.YEARLY,

    /** Nhắc trước bao nhiêu ngày (0 = nhắc đúng ngày) */
    val remindDaysBefore: Int = 1,

    /** Giờ nhắc trong ngày (0-23) */
    val remindHour: Int = 8,

    /** Phút nhắc trong ngày (0-59) */
    val remindMinute: Int = 0,

    /** Đã bật nhắc nhở chưa */
    val isEnabled: Boolean = true,

    /** Thời điểm tạo */
    val createdAt: Long = System.currentTimeMillis()
)
