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
 * Hỗ trợ multi-reminder: mỗi event có thể có nhiều mốc nhắc,
 * mỗi mốc tạo 1 alarm riêng.
 *
 * Request code format: BASE + eventId * 100 + reminderIndex
 * Cho phép tối đa 100 mốc nhắc / event (thực tế ~5-10).
 */
object ReminderScheduler {

    private const val REQUEST_CODE_BASE = 10000

    /**
     * Lên lịch alarm cho tất cả mốc nhắc của 1 event.
     */
    suspend fun scheduleEvent(context: Context, event: LunarEvent) {
        if (!event.isEnabled) return

        val db = AppDatabase.getInstance(context)
        val reminders = db.reminderItemDao().getRemindersForEvent(event.id)

        if (reminders.isEmpty()) {
            // Backward compat: dùng remindDaysBefore cũ
            scheduleOneAlarm(context, event, event.remindDaysBefore, "", 0)
        } else {
            reminders.forEachIndexed { index, reminder ->
                scheduleOneAlarm(context, event, reminder.daysBefore, reminder.note, index)
            }
        }
    }

    /**
     * Lên lịch 1 alarm cho 1 mốc nhắc cụ thể.
     */
    private fun scheduleOneAlarm(
        context: Context,
        event: LunarEvent,
        daysBefore: Int,
        reminderNote: String,
        reminderIndex: Int
    ) {
        val alarmTime = calculateNextAlarmTime(event, daysBefore) ?: return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_EVENT_ID, event.id)
            putExtra(ReminderReceiver.EXTRA_EVENT_TITLE, event.title)
            putExtra(ReminderReceiver.EXTRA_EVENT_NOTE, event.note)
            putExtra(ReminderReceiver.EXTRA_LUNAR_DAY, event.lunarDay)
            putExtra(ReminderReceiver.EXTRA_LUNAR_MONTH, event.lunarMonth)
            putExtra(ReminderReceiver.EXTRA_DAYS_BEFORE, daysBefore)
            putExtra(ReminderReceiver.EXTRA_REMINDER_NOTE, reminderNote)
        }

        val requestCode = REQUEST_CODE_BASE + (event.id * 100).toInt() + reminderIndex
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
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
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent
            )
        }
    }

    /**
     * Tính thời điểm alarm cho 1 mốc nhắc cụ thể.
     */
    private fun calculateNextAlarmTime(event: LunarEvent, daysBefore: Int): Long? {
        val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
        val currentYear = now.get(Calendar.YEAR)

        when (event.repeat) {
            ReminderRepeat.YEARLY, ReminderRepeat.ONCE -> {
                for (yearOffset in 0..1) {
                    val targetYear = currentYear + yearOffset
                    val solar = lunarToSolarSafe(
                        event.lunarDay, event.lunarMonth, targetYear,
                        if (event.isLeapMonth) 1 else 0
                    ) ?: continue

                    val cal = toCalendarWithDaysBefore(solar, event, daysBefore)
                    if (cal.after(now)) {
                        return cal.timeInMillis
                    }
                }
            }
            ReminderRepeat.MONTHLY -> {
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

                    val cal = toCalendarWithDaysBefore(solar, event, daysBefore)
                    if (cal.after(now)) {
                        return cal.timeInMillis
                    }
                }
            }
            ReminderRepeat.WEEKLY -> {
                val cal = (now.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, event.remindHour)
                    set(Calendar.MINUTE, event.remindMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (!cal.after(now)) cal.add(Calendar.WEEK_OF_YEAR, 1)
                return cal.timeInMillis
            }
            ReminderRepeat.DAILY -> {
                val cal = (now.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, event.remindHour)
                    set(Calendar.MINUTE, event.remindMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (!cal.after(now)) cal.add(Calendar.DAY_OF_MONTH, 1)
                return cal.timeInMillis
            }
        }
        return null
    }

    private fun lunarToSolarSafe(
        lunarDay: Int, lunarMonth: Int, lunarYear: Int, leapMonth: Int
    ): SolarDate? {
        return try {
            VietCalendar.lunarToSolar(lunarDay, lunarMonth, lunarYear, leapMonth)
        } catch (e: Exception) { null }
    }

    private fun toCalendarWithDaysBefore(solar: SolarDate, event: LunarEvent, daysBefore: Int): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")).apply {
            set(Calendar.YEAR, solar.year)
            set(Calendar.MONTH, solar.month - 1)
            set(Calendar.DAY_OF_MONTH, solar.day)
            set(Calendar.HOUR_OF_DAY, event.remindHour)
            set(Calendar.MINUTE, event.remindMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, -daysBefore)
        }
    }

    /**
     * Hủy tất cả alarm cho 1 event (lên đến 100 mốc).
     */
    fun cancelEvent(context: Context, eventId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0 until 100) {
            val requestCode = REQUEST_CODE_BASE + (eventId * 100).toInt() + i
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            } else {
                break // Không còn alarm nào
            }
        }
    }

    /**
     * Lên lịch lại tất cả event đang enabled.
     */
    suspend fun rescheduleAll(context: Context) {
        val db = AppDatabase.getInstance(context)
        val events = db.lunarEventDao().getEnabledEvents()
        for (event in events) {
            scheduleEvent(context, event)
        }
    }
}
