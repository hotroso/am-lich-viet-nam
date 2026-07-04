package io.github.hotroso.vietnameselunarcalendar

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import io.github.hotroso.vietnameselunarcalendar.calendar.CalendarSyncActivity
import io.github.hotroso.vietnameselunarcalendar.reminder.EventListActivity
import io.github.hotroso.vietnameselunarcalendar.upcoming.UpcomingEventsActivity

class MainActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawerLayout = findViewById(R.id.drawer_layout)

        // Setup drawer navigation
        val navView = findViewById<NavigationView>(R.id.navigation_view)
        val headerView = navView.getHeaderView(0)

        headerView.findViewById<TextView>(R.id.tvEvents)?.setOnClickListener {
            drawerLayout.close()
            startActivity(Intent(this, EventListActivity::class.java))
        }

        headerView.findViewById<TextView>(R.id.tvUpcoming)?.setOnClickListener {
            drawerLayout.close()
            startActivity(Intent(this, UpcomingEventsActivity::class.java))
        }

        headerView.findViewById<TextView>(R.id.tvCalendarSync)?.setOnClickListener {
            drawerLayout.close()
            startActivity(Intent(this, CalendarSyncActivity::class.java))
        }

        headerView.findViewById<TextView>(R.id.tvConvert)?.setOnClickListener {
            drawerLayout.close()
            startActivity(Intent(this, DateConvertActivity::class.java))
        }

        headerView.findViewById<TextView>(R.id.tvInfo)?.setOnClickListener {
            drawerLayout.close()
            showInfoDialog()
        }

        // Handle back button: close drawer first, then exit
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isOpen) {
                    drawerLayout.close()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun showInfoDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setMessage(getString(R.string.app_about))
            .setPositiveButton("OK", null)
            .show()
    }
}
