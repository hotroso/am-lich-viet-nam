package io.github.hotroso.vietnameselunarcalendar.lunar

import java.util.Calendar
import java.util.TimeZone

/**
 * Represents a Solar (Gregorian) date.
 */
data class SolarDate(
    val day: Int,
    val month: Int,  // 1-based (1 = January)
    val year: Int
) {
    companion object {
        fun today(): SolarDate {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
            return SolarDate(
                day = cal.get(Calendar.DAY_OF_MONTH),
                month = cal.get(Calendar.MONTH) + 1,
                year = cal.get(Calendar.YEAR)
            )
        }
    }
}
