package io.github.hotroso.vietnameselunarcalendar.reminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import io.github.hotroso.vietnameselunarcalendar.R
import io.github.hotroso.vietnameselunarcalendar.lunar.CanChi
import io.github.hotroso.vietnameselunarcalendar.lunar.VietCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EventListAdapter(
    private val onToggle: (LunarEvent, Boolean) -> Unit,
    private val onClick: (LunarEvent) -> Unit,
    private val onLongClick: (LunarEvent) -> Unit
) : ListAdapter<LunarEvent, EventListAdapter.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<LunarEvent>() {
        override fun areItemsTheSame(old: LunarEvent, new: LunarEvent) = old.id == new.id
        override fun areContentsTheSame(old: LunarEvent, new: LunarEvent) = old == new
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvEventTitle)
        val tvDate: TextView = view.findViewById(R.id.tvEventDate)
        val tvRepeat: TextView = view.findViewById(R.id.tvEventRepeat)
        val tvNext: TextView = view.findViewById(R.id.tvNextReminder)
        val switchEnabled: SwitchMaterial = view.findViewById(R.id.switchEnabled)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lunar_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = getItem(position)
        holder.tvTitle.text = event.title

        // Hiển thị ngày âm lịch
        val monthName = CanChi.tenThangAm(event.lunarMonth)
        holder.tvDate.text = "Ngày ${event.lunarDay} tháng $monthName" +
                if (event.isLeapMonth) " (nhuận)" else ""

        // Hiển thị tần suất
        holder.tvRepeat.text = when (event.repeat) {
            ReminderRepeat.YEARLY -> "🔁 Hàng năm · Nhắc trước ${event.remindDaysBefore} ngày · ${formatTime(event)}"
            ReminderRepeat.MONTHLY -> "🔁 Hàng tháng · ${formatTime(event)}"
            ReminderRepeat.WEEKLY -> "🔁 Hàng tuần · ${formatTime(event)}"
            ReminderRepeat.DAILY -> "🔁 Hàng ngày · ${formatTime(event)}"
            ReminderRepeat.ONCE -> "Một lần · ${formatTime(event)}"
        }

        // Tính và hiển thị ngày dương tương ứng (năm nay)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val solar = try {
            VietCalendar.lunarToSolar(
                event.lunarDay, event.lunarMonth, currentYear,
                if (event.isLeapMonth) 1 else 0
            )
        } catch (e: Exception) { null }

        if (solar != null) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            holder.tvNext.text = "📌 Năm nay: ${solar.day}/${solar.month}/${solar.year} dương lịch"
            holder.tvNext.visibility = View.VISIBLE
        } else {
            holder.tvNext.visibility = View.GONE
        }

        // Switch
        holder.switchEnabled.isChecked = event.isEnabled
        holder.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
            onToggle(event, isChecked)
        }

        // Click
        holder.itemView.setOnClickListener { onClick(event) }
        holder.itemView.setOnLongClickListener {
            onLongClick(event)
            true
        }

        // Delete button
        holder.btnDelete.setOnClickListener { onLongClick(event) }
    }

    private fun formatTime(event: LunarEvent): String {
        return String.format("%02d:%02d", event.remindHour, event.remindMinute)
    }
}
