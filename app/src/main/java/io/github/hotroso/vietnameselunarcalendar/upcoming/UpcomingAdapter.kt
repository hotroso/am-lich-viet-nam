package io.github.hotroso.vietnameselunarcalendar.upcoming

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.hotroso.vietnameselunarcalendar.R
import io.github.hotroso.vietnameselunarcalendar.lunar.CanChi
import io.github.hotroso.vietnameselunarcalendar.reminder.EventType

class UpcomingAdapter(
    private val items: List<UpcomingItem>
) : RecyclerView.Adapter<UpcomingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvUpcomingTitle)
        val tvDate: TextView = view.findViewById(R.id.tvUpcomingDate)
        val tvCountdown: TextView = view.findViewById(R.id.tvCountdown)
        val tvLunarDate: TextView = view.findViewById(R.id.tvUpcomingLunarDate)
        val tvRating: TextView = view.findViewById(R.id.tvUpcomingRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_upcoming_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Icon theo loại sự kiện
        val icon = when (item.event.eventType) {
            EventType.GIO -> "🕯️"
            EventType.SINH_NHAT -> "🎂"
            EventType.LE -> "🎉"
            EventType.KHAC -> "📌"
        }

        holder.tvTitle.text = "$icon ${item.displayTitle}"

        // Ngày dương lịch
        val daysOfWeek = arrayOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
        val dowIndex = io.github.hotroso.vietnameselunarcalendar.lunar.VietCalendar
            .getDayOfWeek(item.solarDay, item.solarMonth, item.solarYear)
        holder.tvDate.text = "${daysOfWeek[dowIndex]}, ${item.solarDay}/${item.solarMonth}/${item.solarYear}"

        // Ngày âm lịch
        val monthName = CanChi.tenThangAm(item.event.lunarMonth)
        holder.tvLunarDate.text = "Ngày ${item.event.lunarDay} tháng $monthName âm lịch"

        // Countdown
        val countdownText = when (item.daysUntil) {
            0 -> "Hôm nay! 🔔"
            1 -> "Ngày mai"
            else -> "Còn ${item.daysUntil} ngày"
        }
        holder.tvCountdown.text = countdownText

        // Color theo urgency
        val countdownColor = when {
            item.daysUntil == 0 -> Color.parseColor("#D32F2F")
            item.daysUntil <= 3 -> Color.parseColor("#E65100")
            item.daysUntil <= 7 -> Color.parseColor("#F57F17")
            else -> Color.parseColor("#2E7D32")
        }
        holder.tvCountdown.setTextColor(countdownColor)

        // Day rating badge
        val ratingColor = when (item.dayRatingLevel) {
            CanChi.DayRating.VERY_GOOD -> Color.parseColor("#1B5E20")
            CanChi.DayRating.GOOD -> Color.parseColor("#2E7D32")
            CanChi.DayRating.NORMAL -> Color.parseColor("#F57F17")
            CanChi.DayRating.BAD -> Color.parseColor("#E65100")
            CanChi.DayRating.VERY_BAD -> Color.parseColor("#B71C1C")
        }
        holder.tvRating.text = item.dayRating
        holder.tvRating.setTextColor(ratingColor)
    }

    override fun getItemCount(): Int = items.size
}
