package io.github.hotroso.vietnameselunarcalendar.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.github.hotroso.vietnameselunarcalendar.R

/**
 * Configuration activity for all app widgets.
 * Allows user to set background color, text color, text size, and background height.
 * This activity is launched when a widget is placed or user taps "configure".
 */
class AppWidgetConfigActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private var backgroundColor = Color.parseColor("#80000000")
    private var textColor = Color.WHITE
    private var textSizeLevel = 2 // 0=rất nhỏ, 1=nhỏ, 2=mặc định, 3=lớn, 4=rất lớn
    private var backgroundHeight = 0

    private lateinit var viewBackgroundColor: View
    private lateinit var viewTextColor: View
    private lateinit var textViewTextSize: TextView
    private lateinit var previewText: TextView
    private lateinit var seekBarTextSize: SeekBar
    private lateinit var spinnerBackgroundSize: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED)

        setContentView(R.layout.activity_app_widget_config)

        // Get widget ID from intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Load saved config
        loadConfig()

        // Init views
        viewBackgroundColor = findViewById(R.id.viewBackgroundColor)
        viewTextColor = findViewById(R.id.viewTextColor)
        textViewTextSize = findViewById(R.id.textViewTextSize)
        previewText = findViewById(R.id.textView)
        seekBarTextSize = findViewById(R.id.seekbar_text_size)
        spinnerBackgroundSize = findViewById(R.id.spinnerBackgroundSize)

        // Apply initial values
        viewBackgroundColor.setBackgroundColor(backgroundColor)
        viewTextColor.setBackgroundColor(textColor)
        seekBarTextSize.progress = textSizeLevel
        spinnerBackgroundSize.setText(backgroundHeight.toString())
        updateTextSizeLabel()
        updatePreview()

        // Background color picker
        findViewById<View>(R.id.layoutBackgroundColor).setOnClickListener {
            showColorPickerDialog(backgroundColor) { color ->
                backgroundColor = color
                viewBackgroundColor.setBackgroundColor(color)
                updatePreview()
            }
        }

        // Text color picker
        findViewById<View>(R.id.layoutTextColor).setOnClickListener {
            showColorPickerDialog(textColor) { color ->
                textColor = color
                viewTextColor.setBackgroundColor(color)
                updatePreview()
            }
        }

        // Text size seekbar
        seekBarTextSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textSizeLevel = progress
                updateTextSizeLabel()
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Save button
        findViewById<Button>(R.id.button_save).setOnClickListener {
            backgroundHeight = try {
                spinnerBackgroundSize.text.toString().toInt()
            } catch (e: NumberFormatException) {
                0
            }
            saveConfig()
            updateAllWidgets()
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }

    private fun updateTextSizeLabel() {
        val sizeNames = arrayOf("Rất nhỏ", "Nhỏ", "Mặc định", "Lớn", "Rất lớn")
        textViewTextSize.text = "Cỡ chữ: ${sizeNames[textSizeLevel]}"
    }

    private fun updatePreview() {
        previewText.setTextColor(textColor)
        previewText.setBackgroundColor(backgroundColor)
        val sizes = floatArrayOf(10f, 12f, 14f, 16f, 18f)
        previewText.textSize = sizes[textSizeLevel]
    }

    private fun showColorPickerDialog(currentColor: Int, onColorSelected: (Int) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_color_picker, null)
        val preview = dialogView.findViewById<View>(R.id.colorPreview)
        val seekAlpha = dialogView.findViewById<SeekBar>(R.id.seekBarAlpha)
        val seekRed = dialogView.findViewById<SeekBar>(R.id.seekBarRed)
        val seekGreen = dialogView.findViewById<SeekBar>(R.id.seekBarGreen)
        val seekBlue = dialogView.findViewById<SeekBar>(R.id.seekBarBlue)
        val hexEdit = dialogView.findViewById<EditText>(R.id.colorHex)

        seekAlpha.progress = Color.alpha(currentColor)
        seekRed.progress = Color.red(currentColor)
        seekGreen.progress = Color.green(currentColor)
        seekBlue.progress = Color.blue(currentColor)
        preview.setBackgroundColor(currentColor)
        hexEdit.setText(String.format("#%08X", currentColor))

        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val color = Color.argb(
                    seekAlpha.progress, seekRed.progress,
                    seekGreen.progress, seekBlue.progress
                )
                preview.setBackgroundColor(color)
                hexEdit.setText(String.format("#%08X", color))
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
        seekAlpha.setOnSeekBarChangeListener(listener)
        seekRed.setOnSeekBarChangeListener(listener)
        seekGreen.setOnSeekBarChangeListener(listener)
        seekBlue.setOnSeekBarChangeListener(listener)

        AlertDialog.Builder(this)
            .setTitle("Chọn màu")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val color = Color.argb(
                    seekAlpha.progress, seekRed.progress,
                    seekGreen.progress, seekBlue.progress
                )
                onColorSelected(color)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun saveConfig() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt(keyBgColor(appWidgetId), backgroundColor)
            putInt(keyTextColor(appWidgetId), textColor)
            putInt(keyTextSize(appWidgetId), textSizeLevel)
            putInt(keyBgHeight(appWidgetId), backgroundHeight)
            apply()
        }
    }

    private fun loadConfig() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        backgroundColor = prefs.getInt(keyBgColor(appWidgetId), Color.parseColor("#80000000"))
        textColor = prefs.getInt(keyTextColor(appWidgetId), Color.WHITE)
        textSizeLevel = prefs.getInt(keyTextSize(appWidgetId), 2)
        backgroundHeight = prefs.getInt(keyBgHeight(appWidgetId), 0)
    }

    private fun updateAllWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        MainAppWidget.updateWidget(this, appWidgetManager, appWidgetId)
    }

    companion object {
        const val PREFS_NAME = "widget_config"

        fun keyBgColor(widgetId: Int) = "bg_color_$widgetId"
        fun keyTextColor(widgetId: Int) = "text_color_$widgetId"
        fun keyTextSize(widgetId: Int) = "text_size_$widgetId"
        fun keyBgHeight(widgetId: Int) = "bg_height_$widgetId"

        fun getConfig(context: Context, widgetId: Int): WidgetConfig {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return WidgetConfig(
                bgColor = prefs.getInt(keyBgColor(widgetId), Color.parseColor("#80000000")),
                textColor = prefs.getInt(keyTextColor(widgetId), Color.WHITE),
                textSizeLevel = prefs.getInt(keyTextSize(widgetId), 2),
                bgHeight = prefs.getInt(keyBgHeight(widgetId), 0)
            )
        }

        fun deleteConfig(context: Context, widgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                remove(keyBgColor(widgetId))
                remove(keyTextColor(widgetId))
                remove(keyTextSize(widgetId))
                remove(keyBgHeight(widgetId))
                apply()
            }
        }
    }

    data class WidgetConfig(
        val bgColor: Int,
        val textColor: Int,
        val textSizeLevel: Int,
        val bgHeight: Int
    ) {
        fun getTextSizeSp(): Float {
            return floatArrayOf(10f, 12f, 14f, 16f, 18f)[textSizeLevel.coerceIn(0, 4)]
        }
    }
}
