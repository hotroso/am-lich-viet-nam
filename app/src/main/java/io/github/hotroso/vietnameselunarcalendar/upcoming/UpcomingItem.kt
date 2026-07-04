package io.github.hotroso.vietnameselunarcalendar.upcoming

import io.github.hotroso.vietnameselunarcalendar.lunar.CanChi
import io.github.hotroso.vietnameselunarcalendar.reminder.LunarEvent

/**
 * Data class cho 1 item trong danh sách "Sắp tới".
 */
data class UpcomingItem(
    val event: LunarEvent,
    val solarDay: Int,
    val solarMonth: Int,
    val solarYear: Int,
    val daysUntil: Int,
    val displayTitle: String,
    val dayRating: String,
    val dayRatingLevel: CanChi.DayRating
)
