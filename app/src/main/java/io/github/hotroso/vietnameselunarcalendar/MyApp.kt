package io.github.hotroso.vietnameselunarcalendar

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.hotroso.vietnameselunarcalendar.reminder.ReminderScheduler
import io.github.hotroso.vietnameselunarcalendar.reminder.ReminderWorker

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Layer 1: Reschedule all AlarmManager alarms
        CoroutineScope(Dispatchers.IO).launch {
            ReminderScheduler.rescheduleAll(this@MyApp)
        }

        // Layer 2: WorkManager daily check (backup cho AlarmManager)
        // Chạy mỗi ngày lúc 6:00 sáng để đảm bảo alarm không bị miss
        ReminderWorker.scheduleDailyCheck(this)
    }
}
