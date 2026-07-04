package io.github.hotroso.vietnameselunarcalendar.ui

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.hotroso.vietnameselunarcalendar.lunar.SolarDate
import io.github.hotroso.vietnameselunarcalendar.lunar.VietCalendar
import io.github.hotroso.vietnameselunarcalendar.reminder.AppDatabase
import io.github.hotroso.vietnameselunarcalendar.reminder.LunarEvent
import io.github.hotroso.vietnameselunarcalendar.reminder.ReminderRepeat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

/**
 * Custom RecyclerView-based calendar grid showing a month with lunar dates.
 */
class CalendarView2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    var year: Int = 0
        private set
    var month: Int = 0  // 0-based (Java Calendar convention)
        private set
    var day: Int = 0
        private set

    private var adapter2: CalendarAdapter? = null
    var onDateSelected: ((Int, Int, Int) -> Unit)? = null  // year, month(1-based), day

    // Cache user events for marking
    private var userEvents: List<LunarEvent> = emptyList()

    init {
        layoutManager = object : GridLayoutManager(context, 7) {
            override fun canScrollVertically(): Boolean = false
        }
        setHasFixedSize(true)
    }

    /** Reload user events from database (call on background thread or use cached) */
    fun loadUserEvents() {
        try {
            val db = AppDatabase.getInstance(context)
            userEvents = db.lunarEventDao().getAllEnabledEventsSync()
        } catch (e: Exception) {
            // DB not ready yet
        }
    }

    /** Set user events directly (from main thread with pre-loaded data) */
    fun setUserEvents(events: List<LunarEvent>) {
        userEvents = events
    }

    /**
     * Set displayed month and rebuild grid.
     * @param year Year
     * @param month Month (0-based, 0=January)
     * @param selectedDay Selected day of month
     */
    fun setMonth(year: Int, month: Int, selectedDay: Int) {
        this.year = year
        this.month = month
        this.day = selectedDay

        val items = buildDateItems()
        if (adapter2 == null) {
            adapter2 = CalendarAdapter(context, items) { item ->
                onItemClicked(item)
            }
            adapter = adapter2
        } else {
            adapter2!!.updateItems(items)
        }
    }

    private fun onItemClicked(item: DateItem) {
        val cal = item.calendar
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val d = cal.get(Calendar.DAY_OF_MONTH)

        if (y == year && m == month) {
            // Same month, just update selection
            this.day = d
            onDateSelected?.invoke(y, m + 1, d)
        } else {
            // Different month, reload
            setMonth(y, m, d)
            onDateSelected?.invoke(y, m + 1, d)
        }
    }

    private fun buildDateItems(): List<DateItem> {
        val tz = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        val items = mutableListOf<DateItem>()
        val cal = GregorianCalendar(tz).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        // Find start: Monday before or on the 1st
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val offset = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_MONTH, -offset)

        val today = GregorianCalendar(tz)
        val todayYear = today.get(Calendar.YEAR)
        val todayMonth = today.get(Calendar.MONTH)
        val todayDay = today.get(Calendar.DAY_OF_MONTH)

        // Build 42 cells (6 weeks)
        repeat(42) {
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val d = cal.get(Calendar.DAY_OF_MONTH)

            val lunar = VietCalendar.solarToLunar(d, m + 1, y)
            val isToday = (y == todayYear && m == todayMonth && d == todayDay)
            val isSelected = (y == year && m == month && d == day)
            val isOutside = (m != month)

            // Check if any user event matches this lunar date
            val hasUserEvent = userEvents.any { event ->
                event.lunarDay == lunar.day && event.lunarMonth == lunar.month &&
                        (event.repeat == ReminderRepeat.YEARLY ||
                         event.repeat == ReminderRepeat.MONTHLY ||
                         (event.repeat == ReminderRepeat.ONCE && (event.lunarYear == 0 || event.lunarYear == lunar.year)))
            }

            items.add(
                DateItem(
                    calendar = cal.clone() as GregorianCalendar,
                    lunarDate = lunar,
                    isToday = isToday,
                    isSelected = isSelected,
                    isOutsideMonth = isOutside,
                    hasUserEvent = hasUserEvent
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return items
    }
}
