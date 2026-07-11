package io.github.hotroso.vietnameselunarcalendar.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.hotroso.vietnameselunarcalendar.MainActivity
import io.github.hotroso.vietnameselunarcalendar.R
import io.github.hotroso.vietnameselunarcalendar.lunar.CanChi
import io.github.hotroso.vietnameselunarcalendar.lunar.SolarDate
import io.github.hotroso.vietnameselunarcalendar.lunar.VietCalendar
import java.util.Calendar
import java.util.TimeZone

/**
 * Nhận alarm và hiển thị notification nhắc nhở ngày giỗ / sự kiện âm lịch.
 * Hỗ trợ multi-reminder: hiển thị note riêng của từng mốc nhắc.
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "lunar_event_reminder"
        const val EXTRA_EVENT_ID = "event_id"
        const val EXTRA_EVENT_TITLE = "event_title"
        const val EXTRA_EVENT_NOTE = "event_note"
        const val EXTRA_LUNAR_DAY = "lunar_day"
        const val EXTRA_LUNAR_MONTH = "lunar_month"
        const val EXTRA_DAYS_BEFORE = "days_before"
        const val EXTRA_REMINDER_NOTE = "reminder_note"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                ReminderScheduler.rescheduleAll(context)
            }
            return
        }

        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1)
        val title = intent.getStringExtra(EXTRA_EVENT_TITLE) ?: "Nhắc nhở"
        val eventNote = intent.getStringExtra(EXTRA_EVENT_NOTE) ?: ""
        val lunarDay = intent.getIntExtra(EXTRA_LUNAR_DAY, 0)
        val lunarMonth = intent.getIntExtra(EXTRA_LUNAR_MONTH, 0)
        val daysBefore = intent.getIntExtra(EXTRA_DAYS_BEFORE, 0)
        val reminderNote = intent.getStringExtra(EXTRA_REMINDER_NOTE) ?: ""

        createNotificationChannel(context)
        showNotification(context, eventId, title, eventNote, lunarDay, lunarMonth, daysBefore, reminderNote)

        // Lên lịch lần tiếp theo (cho event lặp lại)
        if (eventId > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(context)
                val event = db.lunarEventDao().getEventById(eventId)
                if (event != null && event.repeat != ReminderRepeat.ONCE) {
                    ReminderScheduler.scheduleEvent(context, event)
                }
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nhắc nhở sự kiện âm lịch",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo nhắc nhở ngày giỗ, sự kiện theo lịch âm"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(
        context: Context,
        eventId: Long,
        title: String,
        eventNote: String,
        lunarDay: Int,
        lunarMonth: Int,
        daysBefore: Int,
        reminderNote: String
    ) {
        // Build title with countdown prefix
        val displayTitle = if (daysBefore > 0) {
            "⏰ Còn $daysBefore ngày: $title"
        } else {
            "📅 $title"
        }

        val contentText = buildNotificationContent(
            lunarDay, lunarMonth, daysBefore, reminderNote, eventNote
        )

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, eventId.toInt(), openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Unique notification ID per event + daysBefore
        val notificationId = (eventId * 100 + daysBefore).toInt()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(displayTitle)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /**
     * Build nội dung notification theo mốc nhắc.
     */
    private fun buildNotificationContent(
        lunarDay: Int,
        lunarMonth: Int,
        daysBefore: Int,
        reminderNote: String,
        eventNote: String
    ): String {
        return buildString {
            // Ghi chú riêng của mốc nhắc (ưu tiên hiển thị đầu tiên)
            if (reminderNote.isNotEmpty()) {
                append("📝 $reminderNote\n")
            }

            val monthName = CanChi.tenThangAm(lunarMonth)
            if (daysBefore > 0) {
                append("Sự kiện diễn ra ngày $lunarDay tháng $monthName âm lịch")
            } else {
                append("🌙 Ngày $lunarDay tháng $monthName âm lịch")

                // Chỉ hiển thị giờ hoàng đạo khi đúng ngày
                val today = SolarDate.today()
                val lunar = VietCalendar.solarToLunar(today.day, today.month, today.year)
                val jdn = lunar.julianDay
                val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
                val currentHour = now.get(Calendar.HOUR_OF_DAY)
                val nextGoodHour = getNextGoodHour(jdn, currentHour)
                if (nextGoodHour != null) {
                    append("\n⏰ Giờ tốt: $nextGoodHour")
                }
            }

            // Ghi chú chung của event (nếu không có note riêng)
            if (reminderNote.isEmpty() && eventNote.isNotEmpty()) {
                append("\n📝 $eventNote")
            }
        }
    }

    private fun getNextGoodHour(jdn: Int, currentHour: Int): String? {
        val pattern = CanChi.GIO_HOANG_DAO[((jdn + 1) % 12) % 6]
        for (i in 0 until 12) {
            if (pattern[i] == '1') {
                val startHour = (i * 2 + 23) % 24
                val endHour = (startHour + 2) % 24
                if (startHour > currentHour || (startHour == 23 && currentHour < 1)) {
                    return "${CanChi.DIA_CHI[i]} (${startHour}h-${endHour}h)"
                }
            }
        }
        return null
    }
}
