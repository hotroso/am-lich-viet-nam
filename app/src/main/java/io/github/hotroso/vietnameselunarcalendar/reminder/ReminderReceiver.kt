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

/**
 * Nhận alarm và hiển thị notification nhắc nhở ngày giỗ / sự kiện âm lịch.
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "lunar_event_reminder"
        const val EXTRA_EVENT_ID = "event_id"
        const val EXTRA_EVENT_TITLE = "event_title"
        const val EXTRA_EVENT_NOTE = "event_note"
        const val EXTRA_LUNAR_DAY = "lunar_day"
        const val EXTRA_LUNAR_MONTH = "lunar_month"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Handle device reboot: reschedule all alarms
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
        val note = intent.getStringExtra(EXTRA_EVENT_NOTE) ?: ""
        val lunarDay = intent.getIntExtra(EXTRA_LUNAR_DAY, 0)
        val lunarMonth = intent.getIntExtra(EXTRA_LUNAR_MONTH, 0)

        createNotificationChannel(context)
        showNotification(context, eventId, title, note, lunarDay, lunarMonth)

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
        note: String,
        lunarDay: Int,
        lunarMonth: Int
    ) {
        val contentText = buildString {
            append("Ngày $lunarDay tháng $lunarMonth âm lịch")
            if (note.isNotEmpty()) {
                append("\n$note")
            }
        }

        // Mở app khi tap notification
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, eventId.toInt(), openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📅 $title")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Check notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        NotificationManagerCompat.from(context).notify(eventId.toInt(), notification)
    }
}
