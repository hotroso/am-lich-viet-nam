package io.github.hotroso.vietnameselunarcalendar.upcoming

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.hotroso.vietnameselunarcalendar.R
import io.github.hotroso.vietnameselunarcalendar.lunar.CanChi
import io.github.hotroso.vietnameselunarcalendar.lunar.VietCalendar
import io.github.hotroso.vietnameselunarcalendar.reminder.AppDatabase
import io.github.hotroso.vietnameselunarcalendar.reminder.LunarEvent
import io.github.hotroso.vietnameselunarcalendar.reminder.ReminderRepeat
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Màn hình "Sắp tới" - hiển thị danh sách sự kiện sắp đến kèm countdown.
 */
class UpcomingEventsActivity : AppCompatActivity() {

    private lateinit var rvUpcoming: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvSummary: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upcoming_events)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvUpcoming = findViewById(R.id.rvUpcoming)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvSummary = findViewById(R.id.tvSummary)

        rvUpcoming.layoutManager = LinearLayoutManager(this)

        loadUpcomingEvents()
    }

    override fun onResume() {
        super.onResume()
        loadUpcomingEvents()
    }

    private fun loadUpcomingEvents() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@UpcomingEventsActivity)
            val events = db.lunarEventDao().getEnabledEvents()
            val upcomingItems = computeUpcomingEvents(events)

            withContext(Dispatchers.Main) {
                if (upcomingItems.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rvUpcoming.visibility = View.GONE
                    tvSummary.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    rvUpcoming.visibility = View.VISIBLE
                    tvSummary.visibility = View.VISIBLE
                    tvSummary.text = "${upcomingItems.size} sự kiện trong 90 ngày tới"
                    rvUpcoming.adapter = UpcomingAdapter(upcomingItems)
                }
            }
        }
    }

    /**
     * Tính toán danh sách sự kiện sắp tới, sort theo ngày gần nhất.
     */
    private fun computeUpcomingEvents(events: List<LunarEvent>): List<UpcomingItem> {
        val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
        val currentYear = now.get(Calendar.YEAR)
        val items = mutableListOf<UpcomingItem>()

        for (event in events) {
            // Tính ngày dương cho năm nay và năm sau
            for (yearOffset in 0..1) {
                val targetYear = currentYear + yearOffset
                val solar = try {
                    VietCalendar.lunarToSolar(
                        event.lunarDay, event.lunarMonth, targetYear,
                        if (event.isLeapMonth) 1 else 0
                    )
                } catch (e: Exception) {
                    continue
                }

                val eventCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")).apply {
                    set(Calendar.YEAR, solar.year)
                    set(Calendar.MONTH, solar.month - 1)
                    set(Calendar.DAY_OF_MONTH, solar.day)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Chỉ lấy sự kiện trong 90 ngày tới
                val diffMillis = eventCal.timeInMillis - now.timeInMillis
                val daysUntil = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()

                if (daysUntil in 0..90) {
                    // Lấy thông tin Can Chi cho ngày đó
                    val lunar = VietCalendar.solarToLunar(solar.day, solar.month, solar.year)
                    val dayAdvice = CanChi.getDayAdvice(lunar.julianDay)

                    items.add(
                        UpcomingItem(
                            event = event,
                            solarDay = solar.day,
                            solarMonth = solar.month,
                            solarYear = solar.year,
                            daysUntil = daysUntil,
                            displayTitle = event.getDisplayTitle(lunar.year),
                            dayRating = dayAdvice.ratingLabel,
                            dayRatingLevel = dayAdvice.rating
                        )
                    )
                    break // Chỉ lấy lần xuất hiện gần nhất
                }
            }
        }

        return items.sortedBy { it.daysUntil }
    }
}
