package io.github.hotroso.vietnameselunarcalendar.calendar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.hotroso.vietnameselunarcalendar.R
import io.github.hotroso.vietnameselunarcalendar.reminder.AppDatabase

/**
 * Activity cho phép user chọn calendar account và đồng bộ sự kiện âm lịch.
 */
class CalendarSyncActivity : AppCompatActivity() {

    private val calendarPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            loadCalendarsAndSync()
        } else {
            Toast.makeText(
                this,
                "Cần cấp quyền Calendar để đồng bộ",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_sync)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val btnSync = findViewById<MaterialButton>(R.id.btnSyncAll)
        btnSync.setOnClickListener {
            checkPermissionsAndSync()
        }
    }

    private fun checkPermissionsAndSync() {
        if (CalendarSyncHelper.hasCalendarPermission(this)) {
            loadCalendarsAndSync()
        } else {
            calendarPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
                )
            )
        }
    }

    private fun loadCalendarsAndSync() {
        val calendars = CalendarSyncHelper.getAvailableCalendars(this)

        if (calendars.isEmpty()) {
            Toast.makeText(
                this,
                "Không tìm thấy tài khoản Calendar nào trên thiết bị",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Show dialog chọn calendar
        val names = calendars.map { it.second }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle("Chọn lịch để đồng bộ")
            .setItems(names) { _, which ->
                val selectedCalendarId = calendars[which].first
                performSync(selectedCalendarId)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun performSync(calendarId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@CalendarSyncActivity)
            val events = db.lunarEventDao().getEnabledEvents()

            val count = CalendarSyncHelper.exportAllEvents(
                this@CalendarSyncActivity,
                events,
                calendarId
            )

            withContext(Dispatchers.Main) {
                if (count > 0) {
                    Toast.makeText(
                        this@CalendarSyncActivity,
                        "Đã đồng bộ $count sự kiện sang Calendar! 🎉",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@CalendarSyncActivity,
                        "Không có sự kiện nào để đồng bộ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
