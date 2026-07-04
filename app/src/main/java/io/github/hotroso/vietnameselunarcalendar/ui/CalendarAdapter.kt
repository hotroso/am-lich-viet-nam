package io.github.hotroso.vietnameselunarcalendar.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.github.hotroso.vietnameselunarcalendar.R
import io.github.hotroso.vietnameselunarcalendar.lunar.LunarDate
import io.github.hotroso.vietnameselunarcalendar.lunar.VietCalendar
import java.util.Calendar
import java.util.GregorianCalendar

data class DateItem(
    val calendar: GregorianCalendar,
    val lunarDate: LunarDate?,
    val isToday: Boolean,
    var isSelected: Boolean,
    val isOutsideMonth: Boolean,
    val hasUserEvent: Boolean = false
)

class CalendarAdapter(
    private val context: Context,
    private var items: List<DateItem>,
    private val onDateClick: (DateItem) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    private var selectedPosition = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSolarDay: TextView = view.findViewById(R.id.textview1)
        val tvLunarDay: TextView = view.findViewById(R.id.textview2)
        val cellView: CellView = view as CellView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.date_cell, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val solarDay = item.calendar.get(Calendar.DAY_OF_MONTH)
        holder.tvSolarDay.text = solarDay.toString()

        // Show lunar day
        val lunar = item.lunarDate
        if (lunar != null) {
            holder.tvLunarDay.text = if (lunar.day == 1) {
                "${lunar.day}/${lunar.month}"
            } else {
                lunar.day.toString()
            }
        }

        // Event marker: show dot indicator if user has scheduled event on this day
        if (item.hasUserEvent && !item.isOutsideMonth) {
            holder.tvSolarDay.text = "${solarDay} •"
        }

        // Set colors based on state
        val dayOfWeek = item.calendar.get(Calendar.DAY_OF_WEEK)
        when {
            item.isOutsideMonth -> {
                holder.tvSolarDay.setTextColor(
                    ContextCompat.getColor(context, R.color.cell_text_color_disabled)
                )
                holder.tvLunarDay.setTextColor(
                    ContextCompat.getColor(context, R.color.cell_text2_color_disabled)
                )
            }
            dayOfWeek == Calendar.SUNDAY -> {
                holder.tvSolarDay.setTextColor(
                    ContextCompat.getColor(context, R.color.vietcal_text_special_date_color)
                )
                holder.tvLunarDay.setTextColor(
                    ContextCompat.getColor(context, R.color.vietcal_text_special_date_color)
                )
            }
            dayOfWeek == Calendar.SATURDAY -> {
                holder.tvSolarDay.setTextColor(
                    ContextCompat.getColor(context, R.color.vietcal_text_t7_color)
                )
                holder.tvLunarDay.setTextColor(
                    ContextCompat.getColor(context, R.color.vietcal_text_t7_color)
                )
            }
            else -> {
                holder.tvSolarDay.setTextColor(
                    ContextCompat.getColor(context, R.color.cell_text_color)
                )
                holder.tvLunarDay.setTextColor(
                    ContextCompat.getColor(context, R.color.cell_text2_color)
                )
            }
        }

        // Background
        when {
            item.isSelected -> {
                holder.cellView.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.vietcal_date_selected_color)
                )
            }
            item.isToday -> {
                holder.cellView.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.vietcal_date_today_color)
                )
            }
            else -> {
                holder.cellView.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.calendarview2_cell_normal_date)
                )
            }
        }

        holder.itemView.setOnClickListener {
            val prev = selectedPosition
            selectedPosition = holder.adapterPosition
            if (prev >= 0 && prev < items.size) {
                items[prev].isSelected = false
                notifyItemChanged(prev)
            }
            item.isSelected = true
            notifyItemChanged(selectedPosition)
            onDateClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<DateItem>) {
        items = newItems
        selectedPosition = newItems.indexOfFirst { it.isSelected }
        notifyDataSetChanged()
    }
}
