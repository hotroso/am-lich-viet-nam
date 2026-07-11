package io.github.hotroso.vietnameselunarcalendar.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import io.github.hotroso.vietnameselunarcalendar.R
import java.util.Calendar

/**
 * Dialog chọn tháng dạng grid 3 cột x 4 hàng.
 */
class MonthPickerDialog(
    context: Context,
    private val currentMonth: Int, // 0-based
    private val currentYear: Int,
    private val onMonthSelected: (month: Int) -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_month_picker)

        val container = findViewById<LinearLayout>(R.id.gridMonths)
        val todayMonth = Calendar.getInstance().get(Calendar.MONTH)
        val todayYear = Calendar.getInstance().get(Calendar.YEAR)

        // Build 4 rows x 3 columns
        for (row in 0 until 4) {
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            for (col in 0 until 3) {
                val m = row * 3 + col
                val btn = createCell("Tháng ${m + 1}")

                when {
                    m == currentMonth -> applySelectedStyle(btn)
                    m == todayMonth && currentYear == todayYear -> applyTodayStyle(btn)
                }

                btn.setOnClickListener {
                    onMonthSelected(m)
                    dismiss()
                }

                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(dp(4), dp(4), dp(4), dp(4))
                }
                rowLayout.addView(btn, params)
            }

            container.addView(rowLayout)
        }
    }

    private fun createCell(text: String): TextView {
        return TextView(context).apply {
            this.text = text
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(14), dp(4), dp(14))
            setTextColor(Color.parseColor("#212121"))
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(8).toFloat()
                setColor(Color.parseColor("#f5f5f5"))
            }
            background = bg
        }
    }

    private fun applySelectedStyle(tv: TextView) {
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(8).toFloat()
            setColor(Color.parseColor("#b22b23"))
        }
        tv.background = bg
        tv.setTextColor(Color.WHITE)
        tv.typeface = Typeface.DEFAULT_BOLD
    }

    private fun applyTodayStyle(tv: TextView) {
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(8).toFloat()
            setColor(Color.parseColor("#f5f5f5"))
            setStroke(dp(2), Color.parseColor("#d4a56f"))
        }
        tv.background = bg
    }

    private fun dp(value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }
}

/**
 * Dialog chọn năm dạng grid 4 cột x 3 hàng + navigation [<][>].
 */
class YearPickerDialog(
    context: Context,
    private val currentYear: Int,
    private val onYearSelected: (year: Int) -> Unit
) : Dialog(context) {

    private var baseYear: Int = 0
    private lateinit var container: LinearLayout
    private lateinit var tvRange: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_year_picker)

        container = findViewById(R.id.gridYears)
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
        container.removeAllViews()
        tvRange.text = "$baseYear - ${baseYear + 11}"

        val todayYear = Calendar.getInstance().get(Calendar.YEAR)

        // Build 3 rows x 4 columns
        for (row in 0 until 3) {
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            for (col in 0 until 4) {
                val index = row * 4 + col
                val year = baseYear + index
                val btn = createCell(year.toString())

                when {
                    year == currentYear -> applySelectedStyle(btn)
                    year == todayYear -> applyTodayStyle(btn)
                }

                btn.setOnClickListener {
                    onYearSelected(year)
                    dismiss()
                }

                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(dp(4), dp(4), dp(4), dp(4))
                }
                rowLayout.addView(btn, params)
            }

            container.addView(rowLayout)
        }
    }

    private fun createCell(text: String): TextView {
        return TextView(context).apply {
            this.text = text
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(14), dp(4), dp(14))
            setTextColor(Color.parseColor("#212121"))
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(8).toFloat()
                setColor(Color.parseColor("#f5f5f5"))
            }
            background = bg
        }
    }

    private fun applySelectedStyle(tv: TextView) {
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(8).toFloat()
            setColor(Color.parseColor("#b22b23"))
        }
        tv.background = bg
        tv.setTextColor(Color.WHITE)
        tv.typeface = Typeface.DEFAULT_BOLD
    }

    private fun applyTodayStyle(tv: TextView) {
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(8).toFloat()
            setColor(Color.parseColor("#f5f5f5"))
            setStroke(dp(2), Color.parseColor("#d4a56f"))
        }
        tv.background = bg
    }

    private fun dp(value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }
}
