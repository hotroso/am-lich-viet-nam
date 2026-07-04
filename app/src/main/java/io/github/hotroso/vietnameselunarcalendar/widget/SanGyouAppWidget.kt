package io.github.hotroso.vietnameselunarcalendar.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import io.github.hotroso.vietnameselunarcalendar.MainActivity
import io.github.hotroso.vietnameselunarcalendar.R

/**
 * 3-line widget: line 1 = date, line 2 = can chi, line 3 = tiết khí.
 */
class SanGyouAppWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, SanGyouAppWidget::class.java)
            )
            onUpdate(context, appWidgetManager, ids)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val data = WidgetHelper.computeToday()
            val views = RemoteViews(context.packageName, R.layout.san_gyou_app_widget)

            val leapStr = if (data.isLeapMonth) " nhuận" else ""
            views.setTextViewText(
                R.id.tv_widget_line1,
                "Ngày ${data.lunarDay} tháng ${data.tenThangAm}$leapStr năm ${data.canChiNam}"
            )
            views.setTextViewText(R.id.tv_widget_line2, "Ngày ${data.canChiNgay}, Tháng ${data.canChiThang}")
            views.setTextViewText(R.id.tv_widget_line3, "Tiết: ${data.tietKhi}")

            // Open MainActivity on tap
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 2, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
