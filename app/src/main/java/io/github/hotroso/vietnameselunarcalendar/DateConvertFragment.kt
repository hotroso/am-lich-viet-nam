package io.github.hotroso.vietnameselunarcalendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.github.hotroso.vietnameselunarcalendar.lunar.CanChi
import io.github.hotroso.vietnameselunarcalendar.lunar.SolarDate
import io.github.hotroso.vietnameselunarcalendar.lunar.VietCalendar

/**
 * Fragment chuyển đổi ngày Dương lịch ⇄ Âm lịch.
 */
class DateConvertFragment : Fragment() {

    private lateinit var tvSource: TextView
    private lateinit var tvDesc: TextView
    private lateinit var tvResult: TextView
    private lateinit var llSolar: LinearLayout
    private lateinit var llLunar: LinearLayout
    private lateinit var btnSwitch: Button

    private lateinit var npSDay: NumberPicker
    private lateinit var npSMonth: NumberPicker
    private lateinit var npSYear: NumberPicker
    private lateinit var npLDay: NumberPicker
    private lateinit var npLMonth: NumberPicker
    private lateinit var npLYear: NumberPicker

    // true = Solar→Lunar, false = Lunar→Solar
    private var isSolarToLunar = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_date_convert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupPickers()
        setupSwitch()
        convert()
    }

    private fun bindViews(view: View) {
        tvSource = view.findViewById(R.id.tv_convert_source)
        tvDesc = view.findViewById(R.id.tv_convert_desc)
        tvResult = view.findViewById(R.id.tv_result)
        llSolar = view.findViewById(R.id.ll_solar)
        llLunar = view.findViewById(R.id.ll_lunar)
        btnSwitch = view.findViewById(R.id.btn_switch)
        npSDay = view.findViewById(R.id.np_s_day)
        npSMonth = view.findViewById(R.id.np_s_month)
        npSYear = view.findViewById(R.id.np_s_year)
        npLDay = view.findViewById(R.id.np_l_day)
        npLMonth = view.findViewById(R.id.np_l_month)
        npLYear = view.findViewById(R.id.np_l_year)
    }

    private fun setupPickers() {
        val today = SolarDate.today()

        // Solar pickers
        npSDay.minValue = 1
        npSDay.maxValue = 31
        npSDay.value = today.day

        npSMonth.minValue = 1
        npSMonth.maxValue = 12
        npSMonth.value = today.month

        npSYear.minValue = 1900
        npSYear.maxValue = 2100
        npSYear.value = today.year

        // Lunar pickers
        npLDay.minValue = 1
        npLDay.maxValue = 30
        npLDay.value = 1

        npLMonth.minValue = 1
        npLMonth.maxValue = 12
        npLMonth.value = 1

        npLYear.minValue = 1900
        npLYear.maxValue = 2100
        npLYear.value = today.year

        // Listeners for auto-convert
        val solarListener = NumberPicker.OnValueChangeListener { _, _, _ -> convert() }
        npSDay.setOnValueChangedListener(solarListener)
        npSMonth.setOnValueChangedListener(solarListener)
        npSYear.setOnValueChangedListener(solarListener)

        val lunarListener = NumberPicker.OnValueChangeListener { _, _, _ -> convert() }
        npLDay.setOnValueChangedListener(lunarListener)
        npLMonth.setOnValueChangedListener(lunarListener)
        npLYear.setOnValueChangedListener(lunarListener)
    }

    private fun setupSwitch() {
        btnSwitch.setOnClickListener {
            isSolarToLunar = !isSolarToLunar
            updateDirection()
            convert()
        }
        updateDirection()
    }

    private fun updateDirection() {
        if (isSolarToLunar) {
            tvSource.text = "Dương lịch"
            tvDesc.text = "Âm lịch"
            llSolar.visibility = View.VISIBLE
            llLunar.visibility = View.GONE
        } else {
            tvSource.text = "Âm lịch"
            tvDesc.text = "Dương lịch"
            llSolar.visibility = View.GONE
            llLunar.visibility = View.VISIBLE
        }
    }

    private fun convert() {
        try {
            if (isSolarToLunar) {
                val day = npSDay.value
                val month = npSMonth.value
                val year = npSYear.value
                val lunar = VietCalendar.solarToLunar(day, month, year)
                val canChi = CanChi.canChiNam(lunar.year)
                val leapStr = if (lunar.isLeapMonth == 1) " (nhuận)" else ""
                tvResult.text = "Ngày ${lunar.day} tháng ${CanChi.tenThangAm(lunar.month)}$leapStr\n" +
                        "Năm $canChi (${lunar.year})"
            } else {
                val day = npLDay.value
                val month = npLMonth.value
                val year = npLYear.value
                val solar = VietCalendar.lunarToSolar(day, month, year, 0)
                tvResult.text = "Ngày ${solar.day}/${solar.month}/${solar.year}"
            }
        } catch (e: Exception) {
            tvResult.text = "Không thể chuyển đổi"
        }
    }
}
