package io.github.hotroso.vietnameselunarcalendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.hotroso.vietnameselunarcalendar.lunar.CanChi
import io.github.hotroso.vietnameselunarcalendar.lunar.EventManager
import io.github.hotroso.vietnameselunarcalendar.lunar.SolarDate
import io.github.hotroso.vietnameselunarcalendar.reminder.AppDatabase
import io.github.hotroso.vietnameselunarcalendar.reminder.LunarEvent
import io.github.hotroso.vietnameselunarcalendar.ui.CalendarView2
import io.github.hotroso.vietnameselunarcalendar.ui.DigitalClock

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var eventManager: EventManager

    // Views
    private lateinit var tvBigDay: TextView
    private lateinit var tvDayOfWeek: TextView
    private lateinit var tvCalendarTitle: TextView
    private lateinit var tvAlMonth: TextView
    private lateinit var tvAlDay: TextView
    private lateinit var tvAlYear: TextView
    private lateinit var tvAlcMonth: TextView
    private lateinit var tvAlcDay: TextView
    private lateinit var tvCanChiGio: TextView
    private lateinit var tvTietKhi: TextView
    private lateinit var tvEvent: TextView
    private lateinit var tvGioHoangDao: TextView
    private lateinit var tvDayRating: TextView
    private lateinit var calendarView: CalendarView2
    private lateinit var imgTetBg: ImageView
    private lateinit var imgConGiap: ImageView
    private lateinit var btnSetting: ImageButton
    private lateinit var btnChooseDate: ImageButton
    private lateinit var btnDayInfo: ImageButton

    // Month navigation
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private lateinit var tvMonthLabel: TextView
    private lateinit var tvYearLabel: TextView

    // Zodiac drawables mapping: index → drawable resource
    private val zodiacDrawables = intArrayOf(
        R.drawable.ic_zodiac_rat_small,      // 0 = Tý (Rat)
        R.drawable.ic_zodiac_buffalo_small,   // 1 = Sửu (Buffalo)
        R.drawable.ic_zodiac_tiger_small,     // 2 = Dần (Tiger)
        R.drawable.ic_zodiac_cat_small,       // 3 = Mão (Cat)
        R.drawable.ic_zodiac_dragon_small,    // 4 = Thìn (Dragon)
        R.drawable.ic_zodiac_snake_small,     // 5 = Tị (Snake)
        R.drawable.ic_zodiac_horse_small,     // 6 = Ngọ (Horse)
        R.drawable.ic_zodiac_goat_small,      // 7 = Mùi (Goat)
        R.drawable.ic_zodiac_monkey_small,    // 8 = Thân (Monkey)
        R.drawable.ic_zodiac_rooster_small,   // 9 = Dậu (Rooster)
        R.drawable.ic_zodiac_dog_small,       // 10 = Tuất (Dog)
        R.drawable.ic_zodiac_pig_small        // 11 = Hợi (Pig)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventManager = EventManager(requireContext())
        bindViews(view)
        setupCalendar(view)
        setupButtons()
        setupMonthNavigation(view)
        setupBottomSheet(view)
        setupLongPress()
        observeViewModel()

        // Initialize with today
        viewModel.goToToday()
    }

    override fun onResume() {
        super.onResume()
        // Reload user events when returning (e.g. after adding/editing event)
        lifecycleScope.launch(Dispatchers.IO) {
            calendarView.loadUserEvents()
            withContext(Dispatchers.Main) {
                calendarView.setMonth(calendarView.year, calendarView.month, calendarView.day)
                // Re-trigger event display for current selection
                val solar = viewModel.selectedDate.value
                if (solar != null) {
                    viewModel.selectDate(solar.day, solar.month, solar.year)
                }
            }
        }
    }

    private fun bindViews(view: View) {
        tvBigDay = view.findViewById(R.id.main_big_day)
        tvDayOfWeek = view.findViewById(R.id.main_day_of_week)
        tvCalendarTitle = view.findViewById(R.id.calendarViewTitle)
        tvAlMonth = view.findViewById(R.id.main_al_month)
        tvAlDay = view.findViewById(R.id.main_al_day)
        tvAlYear = view.findViewById(R.id.main_al_year)
        tvAlcMonth = view.findViewById(R.id.main_alc_month)
        tvAlcDay = view.findViewById(R.id.main_alc_day)
        tvCanChiGio = view.findViewById(R.id.tvCanchigio)
        tvTietKhi = view.findViewById(R.id.tvTietkhi)
        tvEvent = view.findViewById(R.id.main_event)
        tvGioHoangDao = view.findViewById(R.id.tvGioHoangDao)
        tvDayRating = view.findViewById(R.id.tvDayRating)
        imgTetBg = view.findViewById(R.id.imageViewTetBackground)
        imgConGiap = view.findViewById(R.id.imgCongiap)
        btnSetting = view.findViewById(R.id.btnSetting)
        btnChooseDate = view.findViewById(R.id.btnChooseDate2)
        btnDayInfo = view.findViewById(R.id.btnDayInfo)
    }

    private fun setupCalendar(view: View) {
        calendarView = view.findViewById(R.id.recyclerView)
        val today = SolarDate.today()

        // Load user events on background then set calendar
        lifecycleScope.launch(Dispatchers.IO) {
            calendarView.loadUserEvents()
            withContext(Dispatchers.Main) {
                calendarView.setMonth(today.year, today.month - 1, today.day)
                calendarView.onDateSelected = { year, month, day ->
                    viewModel.selectDate(day, month, year)
                    updateMonthNavLabel(month - 1, year)
                }
            }
        }
    }

    private fun setupButtons() {
        btnSetting.setOnClickListener {
            (activity as? MainActivity)?.drawerLayout?.open()
        }
        btnChooseDate.setOnClickListener {
            showDatePickerBottomSheet()
        }
        btnDayInfo.setOnClickListener {
            showDateDetail()
        }
    }

    private fun showDatePickerBottomSheet() {
        val solar = viewModel.selectedDate.value ?: SolarDate.today()
        val picker = DatePickerBottomSheet.newInstance(solar.day, solar.month, solar.year)
        picker.onDateSelected = { day, month, year ->
            viewModel.selectDate(day, month, year)
            calendarView.setMonth(year, month - 1, day)
            updateMonthNavLabel(month - 1, year)
        }
        picker.show(childFragmentManager, "DatePicker")
    }

    private fun setupLongPress() {
        // Long press on big day or calendar title to show date detail
        tvBigDay.setOnLongClickListener {
            showDateDetail()
            true
        }
        tvCalendarTitle.setOnLongClickListener {
            showDateDetail()
            true
        }
    }

    private fun showDateDetail() {
        val solar = viewModel.selectedDate.value ?: return
        val detail = DateDetailFragment.newInstance(solar.day, solar.month, solar.year)
        detail.show(childFragmentManager, "DateDetail")
    }

    private fun setupMonthNavigation(view: View) {
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)
        tvMonthLabel = view.findViewById(R.id.tvMonthLabel)
        tvYearLabel = view.findViewById(R.id.tvYearLabel)

        val today = SolarDate.today()
        updateMonthNavLabel(today.month - 1, today.year)

        btnPrevMonth.setOnClickListener {
            navigateMonth(-1)
        }

        btnNextMonth.setOnClickListener {
            navigateMonth(1)
        }

        // Tap năm → hiện dialog chọn năm
        tvYearLabel.setOnClickListener {
            showYearPickerDialog()
        }
    }

    private fun navigateMonth(offset: Int) {
        var month = calendarView.month + offset
        var year = calendarView.year
        if (month < 0) {
            month = 11
            year--
        } else if (month > 11) {
            month = 0
            year++
        }
        calendarView.setMonth(year, month, 1)
        updateMonthNavLabel(month, year)
        // Select day 1 of new month
        viewModel.selectDate(1, month + 1, year)
    }

    private fun showYearPickerDialog() {
        val currentYear = calendarView.year
        val numberPicker = NumberPicker(requireContext()).apply {
            minValue = 1900
            maxValue = 2100
            value = currentYear
            wrapSelectorWheel = false
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Chọn năm")
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                val selectedYear = numberPicker.value
                val month = calendarView.month
                calendarView.setMonth(selectedYear, month, 1)
                updateMonthNavLabel(month, selectedYear)
                viewModel.selectDate(1, month + 1, selectedYear)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateMonthNavLabel(month: Int, year: Int) {
        tvMonthLabel.text = "Tháng ${month + 1}"
        tvYearLabel.text = year.toString()
    }

    /**
     * Setup bottom sheet: luôn mở rộng, không cho vuốt ẩn.
     */
    private fun setupBottomSheet(view: View) {
        val frameLayout = view.findViewById<View>(R.id.frameLayout)
        frameLayout.post {
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(frameLayout)
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }
    }

    private fun observeViewModel() {
        val daysOfWeek = arrayOf(
            "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm",
            "Thứ sáu", "Thứ bảy", "Chủ nhật"
        )

        viewModel.selectedDate.observe(viewLifecycleOwner) { solar ->
            tvBigDay.text = solar.day.toString()
            tvCalendarTitle.text = getString(R.string.day_title, solar.month, solar.year)
            val dowIndex = io.github.hotroso.vietnameselunarcalendar.lunar.VietCalendar
                .getDayOfWeek(solar.day, solar.month, solar.year)
            tvDayOfWeek.text = daysOfWeek[dowIndex]
        }

        viewModel.lunarDate.observe(viewLifecycleOwner) { lunar ->
            tvAlDay.text = lunar.day.toString()
            tvAlMonth.text = getString(
                R.string.thang_format,
                CanChi.tenThangAm(lunar.month)
            ) + if (lunar.isLeapMonth == 1) " nhuận" else ""
            tvAlYear.text = getString(R.string.nam_format, CanChi.canChiNam(lunar.year))

            // Tết background visibility
            imgTetBg.visibility = if (lunar.month <= 3 && lunar.day == 1) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // Update zodiac image based on lunar year
            updateZodiacImage(lunar.year)

            // Lunar day color (red for special days)
            val solar = viewModel.selectedDate.value ?: return@observe
            tvAlDay.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isSpecialDay(lunar.month, lunar.day))
                        android.R.color.holo_red_dark
                    else R.color.black
                )
            )

            // Events
            val events = eventManager.getEventsForDate(
                solar.day, solar.month, lunar.day, lunar.month
            )

            // Also load user-created lunar events
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getInstance(requireContext())
                val userEvents = db.lunarEventDao().getEventsByLunarDate(lunar.day, lunar.month)
                val currentLunarYear = lunar.year
                val userEventNames = userEvents
                    .filter { it.isEnabled }
                    .map { "📌 ${it.getDisplayTitle(currentLunarYear)}" }

                withContext(Dispatchers.Main) {
                    val allEvents = events + userEventNames
                    if (allEvents.isNotEmpty()) {
                        tvEvent.text = allEvents.joinToString("\n")
                        tvEvent.visibility = View.VISIBLE
                    } else {
                        tvEvent.visibility = View.GONE
                    }
                }
            }
        }

        viewModel.canChiInfo.observe(viewLifecycleOwner) { info ->
            tvAlcMonth.text = getString(R.string.thang) + " " + info.thang
            tvAlcDay.text = getString(R.string.ngay) + " " + info.ngay
            tvCanChiGio.text = getString(R.string.gio) + " " + info.gio
            tvTietKhi.text = getString(R.string.tiet) + " " + info.tietKhi
            tvGioHoangDao.text = "Giờ Hoàng Đạo: " + info.gioHoangDao
        }

        // Observe lunarDate để update "Ngày tốt/xấu" section
        viewModel.lunarDate.observe(viewLifecycleOwner) { lunar ->
            updateDayAdvice(lunar.julianDay)
        }
    }

    /**
     * Set zodiac image based on lunar year's earthly branch index.
     * Formula: (lunarYear + 8) % 12
     * 0=Rat, 1=Buffalo, 2=Tiger, 3=Cat, 4=Dragon, 5=Snake,
     * 6=Horse, 7=Goat, 8=Monkey, 9=Rooster, 10=Dog, 11=Pig
     */
    private fun updateZodiacImage(lunarYear: Int) {
        val index = (lunarYear + 8) % 12
        imgConGiap.setImageResource(zodiacDrawables[index])
        imgConGiap.alpha = 0.3f
    }

    private fun isSpecialDay(lunarMonth: Int, lunarDay: Int): Boolean {
        // Mùng 1, 15 hàng tháng; Tết; Giỗ tổ Hùng Vương...
        return lunarDay == 1 || lunarDay == 15
    }

    /**
     * Cập nhật section "Ngày tốt/xấu cho công việc" dựa trên Julian Day.
     * Hiển thị badge ngắn gọn, bấm vào mở chi tiết.
     */
    private fun updateDayAdvice(jdn: Int) {
        if (jdn == 0) return

        val advice = CanChi.getDayAdvice(jdn)

        // Rating label + color
        val ratingColor = when (advice.rating) {
            CanChi.DayRating.VERY_GOOD -> android.graphics.Color.parseColor("#1B5E20")
            CanChi.DayRating.GOOD -> android.graphics.Color.parseColor("#2E7D32")
            CanChi.DayRating.NORMAL -> android.graphics.Color.parseColor("#F57F17")
            CanChi.DayRating.BAD -> android.graphics.Color.parseColor("#E65100")
            CanChi.DayRating.VERY_BAD -> android.graphics.Color.parseColor("#B71C1C")
        }

        val ratingEmoji = when (advice.rating) {
            CanChi.DayRating.VERY_GOOD -> "🌟"
            CanChi.DayRating.GOOD -> "👍"
            CanChi.DayRating.NORMAL -> "➖"
            CanChi.DayRating.BAD -> "⚠️"
            CanChi.DayRating.VERY_BAD -> "❌"
        }

        tvDayRating.text = "$ratingEmoji ${advice.ratingLabel} — Trực ${advice.truc}  ▸ Xem chi tiết"
        tvDayRating.setTextColor(ratingColor)

        tvDayRating.setOnClickListener {
            showDateDetail()
        }
    }
}
