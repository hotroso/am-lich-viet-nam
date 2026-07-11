package io.github.hotroso.vietnameselunarcalendar.reminder

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Một mốc nhắc nhở cho sự kiện âm lịch.
 * Mỗi LunarEvent có thể có nhiều ReminderItem (nhắc nhiều mốc).
 *
 * Ví dụ: Giỗ ông X có 3 mốc nhắc:
 * - 30 ngày trước: "Đặt vé máy bay"
 * - 7 ngày trước: "Mua đồ cúng"
 * - 0 ngày (đúng ngày): ""
 */
@Entity(
    tableName = "reminder_items",
    foreignKeys = [
        ForeignKey(
            entity = LunarEvent::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("eventId")]
)
data class ReminderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** ID của sự kiện cha */
    val eventId: Long,

    /** Nhắc trước bao nhiêu ngày (0 = đúng ngày) */
    val daysBefore: Int,

    /** Ghi chú riêng cho mốc nhắc này (VD: "Mua vé máy bay") */
    val note: String = ""
)
