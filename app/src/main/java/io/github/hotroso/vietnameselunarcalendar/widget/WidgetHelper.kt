package io.github.hotroso.vietnameselunarcalendar.widget

import io.github.hotroso.vietnameselunarcalendar.lunar.CanChi
import io.github.hotroso.vietnameselunarcalendar.lunar.SolarDate
import io.github.hotroso.vietnameselunarcalendar.lunar.VietCalendar

/**
 * Shared helper for computing widget display text from current date.
 */
object WidgetHelper {

    data class WidgetData(
        val solarDay: Int,
        val solarMonth: Int,
        val solarYear: Int,
        val lunarDay: Int,
        val lunarMonth: Int,
        val lunarYear: Int,
        val isLeapMonth: Boolean,
        val canChiNgay: String,
        val canChiThang: String,
        val canChiNam: String,
        val tietKhi: String,
        val tenThangAm: String
    )

    fun computeToday(): WidgetData {
        val today = SolarDate.today()
        val lunar = VietCalendar.solarToLunar(today.day, today.month, today.year)
        val info = CanChi.getFullInfo(lunar)
        return WidgetData(
            solarDay = today.day,
            solarMonth = today.month,
            solarYear = today.year,
            lunarDay = lunar.day,
            lunarMonth = lunar.month,
            lunarYear = lunar.year,
            isLeapMonth = lunar.isLeapMonth == 1,
            canChiNgay = info.ngay,
            canChiThang = info.thang,
            canChiNam = info.nam,
            tietKhi = info.tietKhi,
            tenThangAm = info.tenThangAm
        )
    }

    fun singleLineText(data: WidgetData): String {
        val leapStr = if (data.isLeapMonth) " (nhuận)" else ""
        return "Ngày ${data.lunarDay} tháng ${data.tenThangAm}$leapStr - ${data.canChiNgay}"
    }

    fun lunarMonthYearText(data: WidgetData): String {
        val leapStr = if (data.isLeapMonth) " nhuận" else ""
        return "Tháng ${data.tenThangAm}$leapStr, ${data.canChiNam}"
    }

    fun solarDateText(data: WidgetData): String {
        return "${data.solarDay}/${data.solarMonth}/${data.solarYear}"
    }
}
