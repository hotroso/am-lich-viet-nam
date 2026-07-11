package io.github.hotroso.vietnameselunarcalendar.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import io.github.hotroso.vietnameselunarcalendar.R
import java.util.Calendar

/**
 * Dialog chọn tháng dạng grid 3x4 (giống web version).
 */
class MonthPickerDialog(
    context: Context,
    private val currentMonth: Int, // 0-based (0=Jan)
    private val currentYear: Int,
    private val onMonthSelected: (month: Int) -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_month_picker)

        val grid = findViewById<GridLayout>(R.id.gridMonths)
        val todayMonth = Calendar.getInstance().get(Calendar.MONTH)
        val todayYear = Calendar.getInstance().get(Calendar.YEAR)

        for (m in 0 until 12) {
            val btn = createPickerButton("Tháng ${m + 1}")

            // Highlight current month
            if (m == currentMonth) {
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dpToPx(8f)
                    setColor(Color.parseColor("#b22b23"))
                }
                btn.background = bg
                btn.setTextColor(Color.WHITE)
                btn.typeface = Typeface.DEFAULT_BOLD
            }

            // Mark today's month
            if (m == todayMonth && currentYear == todayYear && m != currentMonth) {
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dpToPx(8f)
                    setStroke(dpToPx(2f).toInt(), Color.parseColor("#d4a56f"))
                }
                btn.background = bg
            }

            btn.setOnClickListener {
                onMonthSelected(m)
                dismiss()
            }

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(m % 3, 1f)
                rowSpec = GridLayout.spec(m / 3)
                setMargins(dpToPx(3f).toInt(), dpToPx(3f).toInt(), dpToPx(3f).toInt(), dpToPx(3f).toInt())
            }
            grid.addView(btn, params)
        }
    }

    private fun createPickerButton(text: String): TextView {
        return TextView(context).apply {
            this.text = text
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(dpToPx(8f).toInt(), dpToPx(12f).toInt(), dpToPx(8f).toInt(), dpToPx(12f).toInt())
            setTextColor(Color.parseColor("#212121"))

            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(8f)
                setColor(Color.TRANSPARENT)
            }
            background = bg

            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            foreground = context.getDrawable(outValue.resourceId)
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}

/**
 * Dialog chọn năm dạng grid 4x3 với navigation [<] range [>] (giống web version).
 */
class YearPickerDialog(
    context: Context,
    private val currentYear: Int,
    private val onYearSelected: (year: Int) -> Unit
) : Dialog(context) {

    private var baseYear: Int = 0
    private lateinit var grid: GridLayout
    private lateinit var tvRange: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_year_picker)

        grid = findViewById(R.id.gridYears)
        tvRange = findViewById(R.id.tvYearRange)

        baseYear = currentYear - (currentYear % 12)
        renderGrid()

        findViewById<ImageButton>(R.id.btnYearPrev).setOnClickListener {
            baseYear -= 12
            renderGrid()
        }
        findViewById<ImageButton>(R.id.btnYearNext).setOnClickListener {
            baseYear += 12
            renderGrid()
        }
    }

    private fun renderGrid() {
        grid.removeAllViews()
        val rangeEnd = baseYear + 11
        tvRange.text = "$baseYear - $rangeEnd"

        val todayYear = Calendar.getInstance().get(Calendar.YEAR)

        for (i in 0 until 12) {
            val year = baseYear + i
            val btn = createPickerButton(year.toString())

            if (year == currentYear) {
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dpToPx(8f)
                    setColor(Color.parseColor("#b22b23"))
                }
                btn.background = bg
                btn.setTextColor(Color.WHITE)
                btn.typeface = Typeface.DEFAULT_BOLD
            }

            if (year == todayYear && year != currentYear) {
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dpToPx(8f)
                    setStroke(dpToPx(2f).toInt(), Color.parseColor("#d4a56f"))
                }
                btn.background = bg
            }

            btn.setOnClickListener {
                onYearSelected(year)
                dismiss()
            }

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(i % 4, 1f)
                rowSpec = GridLayout.spec(i / 4)
                setMargins(dpToPx(3f).toInt(), dpToPx(3f).toInt(), dpToPx(3f).toInt(), dpToPx(3f).toInt())
            }
            grid.addView(btn, params)
        }
    }

    private fun createPickerButton(text: String): TextView {
        return TextView(context).apply {
            this.text = text
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(dpToPx(8f).toInt(), dpToPx(12f).toInt(), dpToPx(8f).toInt(), dpToPx(12f).toInt())
            setTextColor(Color.parseColor("#212121"))

            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(8f)
                setColor(Color.TRANSPARENT)
            }
            background = bg

            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            foreground = context.getDrawable(outValue.resourceId)
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}
