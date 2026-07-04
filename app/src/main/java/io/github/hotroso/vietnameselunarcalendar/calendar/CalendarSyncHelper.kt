package io.github.hotroso.vietnameselunarcalendar.calendar

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import io.github.hotroso.vietnameselunarcalendar.lunar.CanChi
import io.github.hotroso.vietnameselunarcalendar.lunar.VietCalendar
import io.github.hotroso.vietnameselunarcalendar.reminder.EventType
import io.github.hotroso.vietnameselunarcalendar.reminder.LunarEvent
import io.github.hotroso.vietnameselunarcalendar.reminder.ReminderRepeat
import java.util.Calendar
import java.util.TimeZone

/**
 * Helper class để đồng bộ sự kiện âm lịch sang Google Calendar (hoặc calendar app mặc định).
 * Sử dụng CalendarContract provider — không cần internet.
 */
object CalendarSyncHelper {

    private const val PREFS_NAME = "calendar_sync_prefs"
    private const val KEY_PREFIX = "synced_event_"

    /**
     * Kiểm tra xem có quyền đọc/ghi Calendar không.
     */
    fun hasCalendarPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.WRITE_CALENDAR
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Lấy danh sách calendar accounts trên device.
     * @return List of Pair(calendarId, displayName)
     */
    fun getAvailableCalendars(context: Context): List<Pair<Long, String>> {
        if (!hasCalendarPermission(context)) return emptyList()

        val calendars = mutableListOf<Pair<Long, String>>()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
        )

        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?",
            arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString()),
            null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID)
            val nameIndex = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val name = cursor.getString(nameIndex) ?: "Calendar"
                calendars.add(Pair(id, name))
            }
        }

        return calendars
    }

    /**
     * Export 1 sự kiện âm lịch sang Google Calendar.
     * Tự động chuyển ngày âm → dương, thêm mô tả chi tiết.
     *
     * @return URI id của event đã tạo trong calendar, hoặc -1 nếu thất bại.
     */
    fun exportEventToCalendar(
        context: Context,
        event: LunarEvent,
        calendarId: Long,
        yearsToSync: Int = 3
    ): List<Long> {
        if (!hasCalendarPermission(context)) return emptyList()

        val createdIds = mutableListOf<Long>()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // Tạo event cho N năm tới (vì lịch âm không thể dùng RRULE dương lịch)
        val yearsRange = when (event.repeat) {
            ReminderRepeat.YEARLY -> (currentYear..currentYear + yearsToSync)
            ReminderRepeat.ONCE -> (currentYear..currentYear)
            else -> (currentYear..currentYear)
        }

        for (year in yearsRange) {
            val solar = try {
                VietCalendar.lunarToSolar(
                    event.lunarDay, event.lunarMonth, year,
                    if (event.isLeapMonth) 1 else 0
                )
            } catch (e: Exception) {
                continue
            }

            // Tính Julian Day để lấy thông tin Can Chi
            val lunar = VietCalendar.solarToLunar(solar.day, solar.month, solar.year)
            val info = CanChi.getFullInfo(lunar)

            // Build description
            val description = buildEventDescription(event, lunar.year, info)

            // Build title
            val title = buildEventTitle(event, year)

            // Thời gian bắt đầu: all-day event
            val startCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")).apply {
                set(Calendar.YEAR, solar.year)
                set(Calendar.MONTH, solar.month - 1)
                set(Calendar.DAY_OF_MONTH, solar.day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val endCal = (startCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, 1)
            }

            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.DTSTART, startCal.timeInMillis)
                put(CalendarContract.Events.DTEND, endCal.timeInMillis)
                put(CalendarContract.Events.ALL_DAY, 1)
                put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Ho_Chi_Minh")
                put(
                    CalendarContract.Events.AVAILABILITY,
                    CalendarContract.Events.AVAILABILITY_FREE
                )
            }

            try {
                val uri = context.contentResolver.insert(
                    CalendarContract.Events.CONTENT_URI, values
                )
                if (uri != null) {
                    val eventId = ContentUris.parseId(uri)
                    createdIds.add(eventId)

                    // Thêm reminder trước event.remindDaysBefore ngày
                    if (event.remindDaysBefore > 0) {
                        addReminder(context, eventId, event.remindDaysBefore * 24 * 60)
                    }
                    // Nhắc đúng ngày lúc remindHour:remindMinute
                    addReminder(context, eventId, event.remindHour * 60 + event.remindMinute)
                }
            } catch (e: Exception) {
                // Ignore individual failures
            }
        }

        // Lưu mapping để có thể xóa sau
        if (createdIds.isNotEmpty()) {
            saveSyncedIds(context, event.id, createdIds)
        }

        return createdIds
    }

    /**
     * Xóa các event đã sync sang calendar (khi user xóa event trong app).
     */
    fun removeSyncedEvents(context: Context, lunarEventId: Long) {
        if (!hasCalendarPermission(context)) return

        val ids = getSyncedIds(context, lunarEventId)
        for (id in ids) {
            try {
                val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id)
                context.contentResolver.delete(uri, null, null)
            } catch (e: Exception) {
                // Event may have been deleted manually
            }
        }
        clearSyncedIds(context, lunarEventId)
    }

    /**
     * Export tất cả enabled events sang calendar.
     */
    fun exportAllEvents(
        context: Context,
        events: List<LunarEvent>,
        calendarId: Long
    ): Int {
        var count = 0
        for (event in events) {
            if (event.isEnabled) {
                val ids = exportEventToCalendar(context, event, calendarId)
                if (ids.isNotEmpty()) count++
            }
        }
        return count
    }

    private fun buildEventTitle(event: LunarEvent, currentYear: Int): String {
        val base = event.title
        if (event.lunarYear > 0 && event.lunarYear < currentYear) {
            val years = currentYear - event.lunarYear
            return when (event.eventType) {
                EventType.GIO -> "$base (kỷ niệm $years năm)"
                EventType.SINH_NHAT -> "$base (tròn $years tuổi)"
                else -> "$base ($years năm)"
            }
        }
        return base
    }

    private fun buildEventDescription(
        event: LunarEvent,
        @Suppress("UNUSED_PARAMETER") currentLunarYear: Int,
        info: CanChi.CanChiInfo
    ): String {
        return buildString {
            appendLine("🌙 Ngày ${event.lunarDay} tháng ${CanChi.tenThangAm(event.lunarMonth)} âm lịch")
            if (event.isLeapMonth) appendLine("(Tháng nhuận)")
            appendLine("📌 Can Chi ngày: ${info.ngay}")
            appendLine("🌿 Tiết khí: ${info.tietKhi}")
            if (event.note.isNotEmpty()) {
                appendLine()
                appendLine("📝 ${event.note}")
            }
            appendLine()
            appendLine("— Âm Lịch Việt Nam")
        }
    }

    private fun addReminder(context: Context, eventId: Long, minutesBefore: Int) {
        val values = ContentValues().apply {
            put(CalendarContract.Reminders.EVENT_ID, eventId)
            put(CalendarContract.Reminders.MINUTES, minutesBefore)
            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        }
        try {
            context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun saveSyncedIds(context: Context, lunarEventId: Long, calendarEventIds: List<Long>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("$KEY_PREFIX$lunarEventId", calendarEventIds.joinToString(","))
            .apply()
    }

    private fun getSyncedIds(context: Context, lunarEventId: Long): List<Long> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val str = prefs.getString("$KEY_PREFIX$lunarEventId", null) ?: return emptyList()
        return str.split(",").mapNotNull { it.toLongOrNull() }
    }

    private fun clearSyncedIds(context: Context, lunarEventId: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove("$KEY_PREFIX$lunarEventId").apply()
    }
}
