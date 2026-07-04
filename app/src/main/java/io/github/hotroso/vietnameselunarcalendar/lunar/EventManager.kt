package io.github.hotroso.vietnameselunarcalendar.lunar

import android.content.Context
import org.json.JSONArray

data class CalendarEvent(
    val name: String,
    val lunarDate: IntArray,   // [day, month] - ngày âm
    val solarDate: IntArray    // [day, month] - ngày dương
)

/**
 * Quản lý sự kiện / ngày lễ Việt Nam.
 */
class EventManager(context: Context) {

    private val events: List<CalendarEvent> by lazy {
        loadEvents(context)
    }

    private fun loadEvents(context: Context): List<CalendarEvent> {
        val list = mutableListOf<CalendarEvent>()
        try {
            val json = context.assets.open("events.json")
                .bufferedReader().use { it.readText() }
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val name = obj.getString("name")
                val lunar = obj.getJSONArray("lunar_date")
                val solar = obj.getJSONArray("solar_date")
                list.add(
                    CalendarEvent(
                        name = name,
                        lunarDate = intArrayOf(lunar.getInt(0), lunar.getInt(1)),
                        solarDate = intArrayOf(solar.getInt(0), solar.getInt(1))
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    /**
     * Lấy danh sách sự kiện cho một ngày cụ thể.
     * @param solarMonth tháng dương (1-12)
     * @param solarDay ngày dương
     * @param lunarMonth tháng âm (1-12)
     * @param lunarDay ngày âm
     */
    fun getEventsForDate(
        solarDay: Int, solarMonth: Int,
        lunarDay: Int, lunarMonth: Int
    ): List<String> {
        val result = mutableListOf<String>()
        for (event in events) {
            // Check solar date match
            if (event.solarDate[0] > 0 &&
                event.solarDate[0] == solarDay &&
                event.solarDate[1] == solarMonth
            ) {
                result.add(event.name)
            }
            // Check lunar date match
            if (event.lunarDate[0] > 0 &&
                event.lunarDate[0] == lunarDay &&
                event.lunarDate[1] == lunarMonth
            ) {
                result.add(event.name)
            }
        }
        return result
    }
}
