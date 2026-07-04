package io.github.hotroso.vietnameselunarcalendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.hotroso.vietnameselunarcalendar.lunar.SolarDate

/**
 * BottomSheet để chọn ngày dương lịch (day/month/year).
 */
class DatePickerBottomSheet : BottomSheetDialogFragment() {

    var onDateSelected: ((day: Int, month: Int, year: Int) -> Unit)? = null

    private lateinit var npDay: NumberPicker
    private lateinit var npMonth: NumberPicker
    private lateinit var npYear: NumberPicker

    companion object {
        private const val ARG_DAY = "day"
        private const val ARG_MONTH = "month"
        private const val ARG_YEAR = "year"

        fun newInstance(day: Int, month: Int, year: Int): DatePickerBottomSheet {
            return DatePickerBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt(ARG_DAY, day)
                    putInt(ARG_MONTH, month)
                    putInt(ARG_YEAR, year)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_date_picker_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        npDay = view.findViewById(R.id.npDay)
        npMonth = view.findViewById(R.id.npMonth)
        npYear = view.findViewById(R.id.npYear)

        val today = SolarDate.today()
        val initDay = arguments?.getInt(ARG_DAY) ?: today.day
        val initMonth = arguments?.getInt(ARG_MONTH) ?: today.month
        val initYear = arguments?.getInt(ARG_YEAR) ?: today.year

        npDay.minValue = 1
        npDay.maxValue = 31
        npDay.value = initDay

        npMonth.minValue = 1
        npMonth.maxValue = 12
        npMonth.value = initMonth

        npYear.minValue = 1900
        npYear.maxValue = 2100
        npYear.value = initYear

        val btnSelect = view.findViewById<Button>(R.id.btnSelect)
        val btnToday = view.findViewById<Button>(R.id.btnToday)
        val btnClose = view.findViewById<Button>(R.id.btnClose)

        btnSelect.setOnClickListener {
            onDateSelected?.invoke(npDay.value, npMonth.value, npYear.value)
            dismiss()
        }

        btnToday.setOnClickListener {
            npDay.value = today.day
            npMonth.value = today.month
            npYear.value = today.year
            onDateSelected?.invoke(today.day, today.month, today.year)
            dismiss()
        }

        btnClose.setOnClickListener {
            dismiss()
        }
    }
}
