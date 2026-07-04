package io.github.hotroso.vietnameselunarcalendar.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import io.github.hotroso.vietnameselunarcalendar.lunar.SolarDate
import io.github.hotroso.vietnameselunarcalendar.lunar.VietCalendar
import java.util.Calendar
import java.util.TimeZone

/**
 * Lên lịch notification cho các sự kiện âm lịch.
 *
 * Logic:
 * 1. Tính ngày dương lịch tương ứng cho sự kiện âm lịch (năm hiện tại hoặc năm tới)
 * 2. Trừ đi số ngày nhắc trước (remindDaysBefore)
 * 3. Đặt alarm vào giờ đã chọn (remindHour:remindMinute)
 */
object ReminderScheduler {

    private const val REQUEST_CODE_BASE = 10000

    /**
     * Lên lịch alarm cho 1 event cụ thể.
     * Trả về thời điểm alarm (millis) hoặc -1 nếu không lên lịch được.
     */
    fun scheduleEvent(context: Context, event: LunarEvent): Long {
        if (!event.isEnabled) return -1

        val alarmTime = calculateNextAlarmTime(event) ?: return -1

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_EVENT_ID, event.id)
            putExtra(ReminderReceiver.EXTRA_EVENT_TITLE, event.title)
            putExtra(ReminderReceiver.EXTRA_EVENT_NOTE, event.note)
            putExtra(ReminderReceiver.EXTRA_LUNAR_DAY, event.lunarDay)
            putExtra(ReminderReceiver.EXTRA_LUNAR_MONTH, event.lunarMonth)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (REQUEST_CODE_BASE + event.id).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent
                )
            } else {
                // Fallback: inexact alarm
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent
            )
        }

        return alarmTime
    }

    /**
     * Tính thời điểm alarm tiếp theo cho event.
     * Chuyển ngày âm → dương, trừ đi ngày nhắc trước, set giờ.
     */
    private fun calculateNextAlarmTime(event: LunarEvent): Long? {
        val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
        val currentYear = now.get(Calendar.YEAR)

        // Tính ngày dương cho năm nay và năm sau
        val candidates = mutableListOf<Calendar>()

        when (event.repeat) {
            ReminderRepeat.YEARLY, ReminderRepeat.ONCE -> {
                // Thử năm nay trước
                for (yearOffset in 0..1) {
                    val targetYear = currentYear + yearOffset
                    val solar = lunarToSolarSafe(
                        event.lunarDay, event.lunarMonth, targetYear,
                        if (event.isLeapMonth) 1 else 0
                    ) ?: continue

                    val cal = toCalendarWithReminder(solar, event)
                    if (cal.after(now)) {
                        candidates.add(cal)
                        break
                    }
                }
            }
            ReminderRepeat.MONTHLY -> {
                // Tháng hiện tại và tháng sau (âm lịch)
                val todayLunar = VietCalendar.solarToLunar(
                    now.get(Calendar.DAY_OF_MONTH),
                    now.get(Calendar.MONTH) + 1,
                    currentYear
                )
                for (monthOffset in 0..2) {
                    var targetMonth = todayLunar.month + monthOffset
                    var targetYear = todayLunar.year
                    if (targetMonth > 12) {
                        targetMonth -= 12
                        targetYear++
                    }
                    val solar = lunarToSolarSafe(
                        event.lunarDay, targetMonth, targetYear, 0
                    ) ?: continue

                    val cal = toCalendarWithReminder(solar, event)
                    if (cal.after(now)) {
                        candidates.add(cal)
                        break
                    }
                }
            }
            ReminderRepeat.WEEKLY -> {
                // Nhắc hàng tuần: lấy ngày alarm tiếp theo dựa trên ngày hiện tại
                val cal = (now.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, event.remindHour)
                    set(Calendar.MINUTE, event.remindMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (!cal.after(now)) {
                    cal.add(Calendar.WEEK_OF_YEAR, 1)
                }
                candidates.add(cal)
            }
            ReminderRepeat.DAILY -> {
                val cal = (now.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, event.remindHour)
                    set(Calendar.MINUTE, event.remindMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (!cal.after(now)) {
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                }
                candidates.add(cal)
            }
        }

        return candidates.minByOrNull { it.timeInMillis }?.timeInMillis
    }

    /**
     * Chuyển đổi ngày âm → dương an toàn (trả null nếu không hợp lệ).
     */
    private fun lunarToSolarSafe(
        lunarDay: Int, lunarMonth: Int, lunarYear: Int, leapMonth: Int
    ): SolarDate? {
        return try {
            VietCalendar.lunarToSolar(lunarDay, lunarMonth, lunarYear, leapMonth)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Tạo Calendar từ ngày dương + thông tin nhắc trước.
     */
    private fun toCalendarWithReminder(solar: SolarDate, event: LunarEvent): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")).apply {
            set(Calendar.YEAR, solar.year)
            set(Calendar.MONTH, solar.month - 1)
            set(Calendar.DAY_OF_MONTH, solar.day)
            set(Calendar.HOUR_OF_DAY, event.remindHour)
            set(Calendar.MINUTE, event.remindMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Trừ đi số ngày nhắc trước
            add(Calendar.DAY_OF_MONTH, -event.remindDaysBefore)
        }
    }

    /**
     * Hủy alarm cho 1 event.
     */
    fun cancelEvent(context: Context, eventId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (REQUEST_CODE_BASE + eventId).toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Lên lịch lại tất cả event đang enabled.
     * Gọi khi app khởi động hoặc sau khi device restart.
     */
    suspend fun rescheduleAll(context: Context) {
        val db = AppDatabase.getInstance(context)
        val events = db.lunarEventDao().getEnabledEvents()
        for (event in events) {
            scheduleEvent(context, event)
        }
    }
}
