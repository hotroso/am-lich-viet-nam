package io.github.hotroso.vietnameselunarcalendar.reminder

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.hotroso.vietnameselunarcalendar.R
import io.github.hotroso.vietnameselunarcalendar.lunar.VietCalendar
import java.util.Calendar

class AddEventActivity : AppCompatActivity() {

    private lateinit var etTitle: TextInputEditText
    private lateinit var etNote: TextInputEditText
    private lateinit var etLunarDay: TextInputEditText
    private lateinit var etLunarMonth: TextInputEditText
    private lateinit var etLunarYear: TextInputEditText
    private lateinit var cbLeapMonth: MaterialCheckBox
    private lateinit var tvLeapInfo: TextView
    private lateinit var chipGroupEventType: ChipGroup
    private lateinit var chipGroupRepeat: ChipGroup
    private lateinit var btnPickTime: MaterialButton
    private lateinit var btnSave: MaterialButton

    // Multi-reminder UI
    private lateinit var reminderListContainer: LinearLayout
    private lateinit var etReminderDays: EditText
    private lateinit var etReminderNote: EditText
    private lateinit var btnAddReminder: MaterialButton

    private var remindHour = 8
    private var remindMinute = 0
    private var editEventId: Long = 0

    // Danh sách mốc nhắc hiện tại
    private val currentReminders = mutableListOf<ReminderItemData>()

    /** Data class tạm cho UI (chưa có id/eventId) */
    private data class ReminderItemData(val daysBefore: Int, val note: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        title = "Thêm sự kiện âm lịch"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bindViews()
        setupLeapMonthAutoDetect()
        setupTimePicker()
        setupReminders()
        setupSaveButton()

        // Default: 1 mốc nhắc (đúng ngày)
        currentReminders.add(ReminderItemData(0, ""))
        renderReminderList()

        editEventId = intent.getLongExtra("event_id", 0)
        if (editEventId > 0) {
            loadExistingEvent(editEventId)
        }
    }

    private fun bindViews() {
        etTitle = findViewById(R.id.etTitle)
        etNote = findViewById(R.id.etNote)
        etLunarDay = findViewById(R.id.etLunarDay)
        etLunarMonth = findViewById(R.id.etLunarMonth)
        etLunarYear = findViewById(R.id.etLunarYear)
        cbLeapMonth = findViewById(R.id.cbLeapMonth)
        tvLeapInfo = findViewById(R.id.tvLeapInfo)
        chipGroupEventType = findViewById(R.id.chipGroupEventType)
        chipGroupRepeat = findViewById(R.id.chipGroupRepeat)
        btnPickTime = findViewById(R.id.btnPickTime)
        btnSave = findViewById(R.id.btnSave)

        // Multi-reminder views
        reminderListContainer = findViewById(R.id.reminderListContainer)
        etReminderDays = findViewById(R.id.etReminderDays)
        etReminderNote = findViewById(R.id.etReminderNote)
        btnAddReminder = findViewById(R.id.btnAddReminder)
    }

    private fun setupReminders() {
        btnAddReminder.setOnClickListener {
            val daysStr = etReminderDays.text.toString().trim()
            val days = daysStr.toIntOrNull()
            if (days == null || days < 0 || days > 90) {
                etReminderDays.error = "0-90"
                return@setOnClickListener
            }
            val note = etReminderNote.text.toString().trim()

            // Kiểm tra trùng
            val exists = currentReminders.any { it.daysBefore == days && it.note == note }
            if (!exists) {
                currentReminders.add(ReminderItemData(days, note))
                renderReminderList()
            }
            etReminderDays.text?.clear()
            etReminderNote.text?.clear()
        }

        // Preset buttons
        findViewById<Chip>(R.id.chipPresetSameDay)?.setOnClickListener { addPreset(0) }
        findViewById<Chip>(R.id.chipPreset1Day)?.setOnClickListener { addPreset(1) }
        findViewById<Chip>(R.id.chipPreset7Days)?.setOnClickListener { addPreset(7) }
        findViewById<Chip>(R.id.chipPreset14Days)?.setOnClickListener { addPreset(14) }
        findViewById<Chip>(R.id.chipPreset30Days)?.setOnClickListener { addPreset(30) }
    }

    private fun addPreset(days: Int) {
        val note = etReminderNote.text.toString().trim()
        val exists = currentReminders.any { it.daysBefore == days && it.note == note }
        if (!exists) {
            currentReminders.add(ReminderItemData(days, note))
            renderReminderList()
        }
        etReminderNote.text?.clear()
    }

    private fun renderReminderList() {
        reminderListContainer.removeAllViews()
        val sorted = currentReminders.sortedByDescending { it.daysBefore }

        for ((index, item) in sorted.withIndex()) {
            val view = LayoutInflater.from(this)
                .inflate(R.layout.layout_reminder_item, reminderListContainer, false)

            val tvDays = view.findViewById<TextView>(R.id.tvDaysBefore)
            val tvNote = view.findViewById<TextView>(R.id.tvReminderNote)
            val btnRemove = view.findViewById<ImageButton>(R.id.btnRemoveReminder)

            tvDays.text = if (item.daysBefore == 0) "Đúng ngày" else "Trước ${item.daysBefore} ngày"
            tvNote.text = item.note.ifEmpty { "(không có ghi chú)" }
            tvNote.alpha = if (item.note.isEmpty()) 0.5f else 1f

            btnRemove.setOnClickListener {
                currentReminders.remove(item)
                renderReminderList()
            }

            reminderListContainer.addView(view)
        }
    }

    private fun setupLeapMonthAutoDetect() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateLeapMonthState() }
        }
        etLunarYear.addTextChangedListener(watcher)
        etLunarMonth.addTextChangedListener(watcher)
    }

    private fun updateLeapMonthState() {
        val year = etLunarYear.text?.toString()?.trim()?.toIntOrNull()
        val month = etLunarMonth.text?.toString()?.trim()?.toIntOrNull()

        if (year != null && year > 1900 && month != null && month in 1..12) {
            val leapMonth = VietCalendar.getLeapMonthOfYear(year)
            if (leapMonth == month) {
                cbLeapMonth.isEnabled = true
                tvLeapInfo.text = "Năm $year có tháng $month nhuận"
                tvLeapInfo.visibility = View.VISIBLE
            } else {
                cbLeapMonth.isChecked = false
                cbLeapMonth.isEnabled = false
                tvLeapInfo.text = if (leapMonth > 0) "Năm $year nhuận tháng $leapMonth" else "Năm $year không có tháng nhuận"
                tvLeapInfo.visibility = View.VISIBLE
            }
        } else {
            cbLeapMonth.isEnabled = true
            tvLeapInfo.visibility = View.GONE
        }
    }

    private fun setupTimePicker() {
        btnPickTime.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                remindHour = hour
                remindMinute = minute
                btnPickTime.text = String.format("%02d:%02d", hour, minute)
            }, remindHour, remindMinute, true).show()
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener { saveEvent() }
        findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener { finish() }
    }

    private fun getSelectedEventType(): EventType {
        return when (chipGroupEventType.checkedChipId) {
            R.id.chipGio -> EventType.GIO
            R.id.chipSinhNhat -> EventType.SINH_NHAT
            R.id.chipLe -> EventType.LE
            else -> EventType.KHAC
        }
    }

    private fun getSelectedRepeat(): ReminderRepeat {
        return when (chipGroupRepeat.checkedChipId) {
            R.id.chipYearly -> ReminderRepeat.YEARLY
            R.id.chipMonthly -> ReminderRepeat.MONTHLY
            R.id.chipOnce -> ReminderRepeat.ONCE
            else -> ReminderRepeat.YEARLY
        }
    }

    private fun saveEvent() {
        val title = etTitle.text?.toString()?.trim() ?: ""
        val note = etNote.text?.toString()?.trim() ?: ""
        val day = etLunarDay.text?.toString()?.trim()?.toIntOrNull()
        val month = etLunarMonth.text?.toString()?.trim()?.toIntOrNull()
        val yearStr = etLunarYear.text?.toString()?.trim() ?: ""

        if (title.isEmpty()) { etTitle.error = "Vui lòng nhập tên sự kiện"; return }
        if (day == null || day < 1 || day > 30) { etLunarDay.error = "Ngày 1-30"; return }
        if (month == null || month < 1 || month > 12) { etLunarMonth.error = "Tháng 1-12"; return }

        val year = yearStr.toIntOrNull() ?: 0
        if (year != 0 && (year < 1900 || year > 2100)) { etLunarYear.error = "Năm 1900-2100"; return }

        if (currentReminders.isEmpty()) {
            currentReminders.add(ReminderItemData(0, ""))
        }

        val event = LunarEvent(
            id = editEventId,
            title = title,
            note = note,
            lunarDay = day,
            lunarMonth = month,
            lunarYear = year,
            isLeapMonth = cbLeapMonth.isChecked,
            eventType = getSelectedEventType(),
            repeat = getSelectedRepeat(),
            remindDaysBefore = currentReminders.minOf { it.daysBefore }, // backward compat
            remindHour = remindHour,
            remindMinute = remindMinute
        )

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(this@AddEventActivity)
            val id = db.lunarEventDao().insert(event)

            // Lưu danh sách mốc nhắc
            val reminderItems = currentReminders.map {
                ReminderItem(eventId = id, daysBefore = it.daysBefore, note = it.note)
            }
            db.reminderItemDao().replaceRemindersForEvent(id, reminderItems)

            // Lên lịch notification
            val savedEvent = db.lunarEventDao().getEventById(id) ?: event.copy(id = id)
            ReminderScheduler.scheduleEvent(this@AddEventActivity, savedEvent)

            withContext(Dispatchers.Main) {
                val targetYear = if (year > 0) year else Calendar.getInstance().get(Calendar.YEAR)
                val solar = try {
                    VietCalendar.lunarToSolar(day, month, targetYear, if (cbLeapMonth.isChecked) 1 else 0)
                } catch (e: Exception) { null }

                val msg = if (solar != null) {
                    "Đã lưu! (${currentReminders.size} mốc nhắc) — ${solar.day}/${solar.month}/${solar.year} DL"
                } else {
                    "Đã lưu! (${currentReminders.size} mốc nhắc)"
                }
                Toast.makeText(this@AddEventActivity, msg, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun loadExistingEvent(eventId: Long) {
        title = "Sửa sự kiện"
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(this@AddEventActivity)
            val event = db.lunarEventDao().getEventById(eventId) ?: return@launch
            val reminders = db.reminderItemDao().getRemindersForEvent(eventId)

            withContext(Dispatchers.Main) {
                etTitle.setText(event.title)
                etNote.setText(event.note)
                etLunarDay.setText(event.lunarDay.toString())
                etLunarMonth.setText(event.lunarMonth.toString())
                if (event.lunarYear > 0) etLunarYear.setText(event.lunarYear.toString())
                cbLeapMonth.isChecked = event.isLeapMonth
                remindHour = event.remindHour
                remindMinute = event.remindMinute
                btnPickTime.text = String.format("%02d:%02d", remindHour, remindMinute)

                when (event.eventType) {
                    EventType.GIO -> chipGroupEventType.check(R.id.chipGio)
                    EventType.SINH_NHAT -> chipGroupEventType.check(R.id.chipSinhNhat)
                    EventType.LE -> chipGroupEventType.check(R.id.chipLe)
                    EventType.KHAC -> chipGroupEventType.check(R.id.chipKhac)
                }
                when (event.repeat) {
                    ReminderRepeat.YEARLY -> chipGroupRepeat.check(R.id.chipYearly)
                    ReminderRepeat.MONTHLY -> chipGroupRepeat.check(R.id.chipMonthly)
                    ReminderRepeat.ONCE -> chipGroupRepeat.check(R.id.chipOnce)
                    else -> chipGroupRepeat.check(R.id.chipYearly)
                }

                // Load reminders
                currentReminders.clear()
                if (reminders.isNotEmpty()) {
                    currentReminders.addAll(reminders.map { ReminderItemData(it.daysBefore, it.note) })
                } else {
                    currentReminders.add(ReminderItemData(event.remindDaysBefore, ""))
                }
                renderReminderList()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
