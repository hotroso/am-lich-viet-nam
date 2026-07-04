package io.github.hotroso.vietnameselunarcalendar.reminder

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker chạy mỗi ngày lúc sáng sớm để kiểm tra xem
 * hôm nay (hoặc N ngày tới) có event âm lịch nào cần nhắc không.
 *
 * Đây là lớp backup cho AlarmManager - đảm bảo notification không bị miss
 * trên các device có aggressive battery optimization (Xiaomi, Oppo, Vivo...).
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Reschedule tất cả alarm (phòng trường hợp bị kill)
        ReminderScheduler.rescheduleAll(applicationContext)

        // Kiểm tra xem hôm nay có event nào cần nhắc ngay không
        checkTodayEvents()

        return Result.success()
    }

    private suspend fun checkTodayEvents() {
        val db = AppDatabase.getInstance(applicationContext)
        val enabledEvents = db.lunarEventDao().getEnabledEvents()
        val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
        val today = now.get(Calendar.DAY_OF_MONTH)
        val thisMonth = now.get(Calendar.MONTH) + 1
        val thisYear = now.get(Calendar.YEAR)

        for (event in enabledEvents) {
            // Tính ngày dương của event năm nay
            val solar = try {
                io.github.hotroso.vietnameselunarcalendar.lunar.VietCalendar.lunarToSolar(
                    event.lunarDay, event.lunarMonth, thisYear,
                    if (event.isLeapMonth) 1 else 0
                )
            } catch (e: Exception) { continue }

            // Kiểm tra: ngày dương của event - remindDaysBefore == hôm nay?
            val eventCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, solar.year)
                set(Calendar.MONTH, solar.month - 1)
                set(Calendar.DAY_OF_MONTH, solar.day)
                add(Calendar.DAY_OF_MONTH, -event.remindDaysBefore)
            }

            val reminderDay = eventCal.get(Calendar.DAY_OF_MONTH)
            val reminderMonth = eventCal.get(Calendar.MONTH) + 1

            if (reminderDay == today && reminderMonth == thisMonth) {
                // Đặt lại alarm cho hôm nay nếu chưa có
                ReminderScheduler.scheduleEvent(applicationContext, event)
            }
        }
    }

    companion object {
        private const val WORK_NAME = "lunar_reminder_daily_check"

        /**
         * Lên lịch WorkManager chạy mỗi ngày.
         * Gọi 1 lần khi app khởi động.
         */
        fun scheduleDailyCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            // Tính delay đến 6:00 sáng mai
            val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
            val target = (now.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 6)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
            }
            val delayMillis = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<ReminderWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
