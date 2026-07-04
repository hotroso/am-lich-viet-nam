package io.github.hotroso.vietnameselunarcalendar.reminder

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.hotroso.vietnameselunarcalendar.R

class EventListActivity : AppCompatActivity() {

    private lateinit var rvEvents: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: EventListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvEvents = findViewById(R.id.rvEvents)
        tvEmpty = findViewById(R.id.tvEmpty)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)

        adapter = EventListAdapter(
            onToggle = { event, enabled ->
                toggleEvent(event, enabled)
            },
            onClick = { event ->
                editEvent(event)
            },
            onLongClick = { event ->
                deleteEvent(event)
            }
        )

        rvEvents.layoutManager = LinearLayoutManager(this)
        rvEvents.adapter = adapter

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEventActivity::class.java))
        }

        observeEvents()
    }

    override fun onResume() {
        super.onResume()
        observeEvents()
    }

    private fun observeEvents() {
        val db = AppDatabase.getInstance(this)
        db.lunarEventDao().getAllEvents().observe(this) { events ->
            adapter.submitList(events)
            tvEmpty.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun toggleEvent(event: LunarEvent, enabled: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@EventListActivity)
            db.lunarEventDao().update(event.copy(isEnabled = enabled))
            if (enabled) {
                ReminderScheduler.scheduleEvent(this@EventListActivity, event.copy(isEnabled = true))
            } else {
                ReminderScheduler.cancelEvent(this@EventListActivity, event.id)
            }
        }
    }

    private fun editEvent(event: LunarEvent) {
        val intent = Intent(this, AddEventActivity::class.java)
        intent.putExtra("event_id", event.id)
        startActivity(intent)
    }

    private fun deleteEvent(event: LunarEvent) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Xóa sự kiện")
            .setMessage("Bạn có chắc muốn xóa \"${event.title}\"?")
            .setPositiveButton("Xóa") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(this@EventListActivity)
                    db.lunarEventDao().delete(event)
                    ReminderScheduler.cancelEvent(this@EventListActivity, event.id)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}
