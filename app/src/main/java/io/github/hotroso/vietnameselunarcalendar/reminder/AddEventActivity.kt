package io.github.hotroso.vietnameselunarcalendar.reminder

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
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
    private lateinit var chipGroupDaysBefore: ChipGroup
    private lateinit var btnPickTime: MaterialButton
    private lateinit var btnSave: MaterialButton

    private var remindHour = 8
    private var remindMinute = 0
    private var editEventId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        title = "Thêm sự kiện âm lịch"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bindViews()
        setupLeapMonthAutoDetect()
        setupTimePicker()
        setupSaveButton()

        // Check if editing existing event
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
        chipGroupDaysBefore = findViewById(R.id.chipGroupDaysBefore)
        btnPickTime = findViewById(R.id.btnPickTime)
        btnSave = findViewById(R.id.btnSave)
    }

    /**
     * Khi user nhập năm + tháng → tự detect xem tháng đó có nhuận không.
     * - Nếu có năm: checkbox bị disable, tự set checked/unchecked + hiện info text
     * - Nếu không có năm: checkbox enable, user tự chọn
     */
    private fun setupLeapMonthAutoDetect() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateLeapMonthState()
            }
        }
        etLunarYear.addTextChangedListener(watcher)
        etLunarMonth.addTextChangedListener(watcher)
    }

    private fun updateLeapMonthState() {
        val yearStr = etLunarYear.text?.toString()?.trim() ?: ""
        val monthStr = etLunarMonth.text?.toString()?.trim() ?: ""

        val year = yearStr.toIntOrNull()
        val month = monthStr.toIntOrNull()

        if (year != null && year > 1900 && month != null && month in 1..12) {
            // Có năm → auto detect
            val leapMonth = VietCalendar.getLeapMonthOfYear(year)
            if (leapMonth == month) {
                // Tháng này CÓ nhuận trong năm đó
                cbLeapMonth.isEnabled = true
                tvLeapInfo.text = "Năm $year có tháng $month nhuận"
                tvLeapInfo.visibility = View.VISIBLE
            } else {
                // Tháng này KHÔNG nhuận
                cbLeapMonth.isChecked = false
                cbLeapMonth.isEnabled = false
                if (leapMonth > 0) {
                    tvLeapInfo.text = "Năm $year nhuận tháng $leapMonth (không phải tháng $month)"
                } else {
                    tvLeapInfo.text = "Năm $year không có tháng nhuận"
                }
                tvLeapInfo.visibility = View.VISIBLE
            }
        } else {
            // Không có năm → user tự chọn
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
        btnSave.setOnClickListener {
            saveEvent()
        }
        findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            finish()
        }
    }

    private fun getSelectedEventType(): EventType {
        return when (chipGroupEventType.checkedChipId) {
            R.id.chipGio -> EventType.GIO
            R.id.chipSinhNhat -> EventType.SINH_NHAT
            R.id.chipLe -> EventType.LE
            R.id.chipKhac -> EventType.KHAC
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

    private fun getSelectedDaysBefore(): Int {
        return when (chipGroupDaysBefore.checkedChipId) {
            R.id.chipSameDay -> 0
            R.id.chip1Day -> 1
            R.id.chip2Days -> 2
            R.id.chip7Days -> 7
            else -> 1
        }
    }

    private fun saveEvent() {
        val title = etTitle.text?.toString()?.trim() ?: ""
        val note = etNote.text?.toString()?.trim() ?: ""
        val dayStr = etLunarDay.text?.toString()?.trim() ?: ""
        val monthStr = etLunarMonth.text?.toString()?.trim() ?: ""
        val yearStr = etLunarYear.text?.toString()?.trim() ?: ""

        // Validation
        if (title.isEmpty()) {
            etTitle.error = "Vui lòng nhập tên sự kiện"
            return
        }
        val day = dayStr.toIntOrNull()
        val month = monthStr.toIntOrNull()
        if (day == null || day < 1 || day > 30) {
            etLunarDay.error = "Ngày 1-30"
            return
        }
        if (month == null || month < 1 || month > 12) {
            etLunarMonth.error = "Tháng 1-12"
            return
        }

        // Năm: optional (0 = không set)
        val year = yearStr.toIntOrNull() ?: 0
        if (year != 0 && (year < 1900 || year > 2100)) {
            etLunarYear.error = "Năm 1900-2100"
            return
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
            remindDaysBefore = getSelectedDaysBefore(),
            remindHour = remindHour,
            remindMinute = remindMinute
        )

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(this@AddEventActivity)
            val id = db.lunarEventDao().insert(event)

            // Lên lịch notification
            val savedEvent = db.lunarEventDao().getEventById(id) ?: event.copy(id = id)
            ReminderScheduler.scheduleEvent(this@AddEventActivity, savedEvent)

            withContext(Dispatchers.Main) {
                // Hiển thị thông tin ngày dương tương ứng
                val targetYear = if (year > 0) year else Calendar.getInstance().get(Calendar.YEAR)
                val solar = try {
                    VietCalendar.lunarToSolar(day, month, targetYear, if (cbLeapMonth.isChecked) 1 else 0)
                } catch (e: Exception) { null }

                val msg = if (solar != null) {
                    "Đã lưu! Tương ứng ${solar.day}/${solar.month}/${solar.year} dương lịch"
                } else {
                    "Đã lưu sự kiện!"
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

            withContext(Dispatchers.Main) {
                etTitle.setText(event.title)
                etNote.setText(event.note)
                etLunarDay.setText(event.lunarDay.toString())
                etLunarMonth.setText(event.lunarMonth.toString())
                if (event.lunarYear > 0) {
                    etLunarYear.setText(event.lunarYear.toString())
                }
                cbLeapMonth.isChecked = event.isLeapMonth
                remindHour = event.remindHour
                remindMinute = event.remindMinute
                btnPickTime.text = String.format("%02d:%02d", remindHour, remindMinute)

                // Set event type chip
                when (event.eventType) {
                    EventType.GIO -> chipGroupEventType.check(R.id.chipGio)
                    EventType.SINH_NHAT -> chipGroupEventType.check(R.id.chipSinhNhat)
                    EventType.LE -> chipGroupEventType.check(R.id.chipLe)
                    EventType.KHAC -> chipGroupEventType.check(R.id.chipKhac)
                }

                // Set repeat chip
                when (event.repeat) {
                    ReminderRepeat.YEARLY -> chipGroupRepeat.check(R.id.chipYearly)
                    ReminderRepeat.MONTHLY -> chipGroupRepeat.check(R.id.chipMonthly)
                    ReminderRepeat.ONCE -> chipGroupRepeat.check(R.id.chipOnce)
                    else -> chipGroupRepeat.check(R.id.chipYearly)
                }

                // Set days before chip
                when (event.remindDaysBefore) {
                    0 -> chipGroupDaysBefore.check(R.id.chipSameDay)
                    1 -> chipGroupDaysBefore.check(R.id.chip1Day)
                    2 -> chipGroupDaysBefore.check(R.id.chip2Days)
                    7 -> chipGroupDaysBefore.check(R.id.chip7Days)
                    else -> chipGroupDaysBefore.check(R.id.chip1Day)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
