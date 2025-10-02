package com.example.meditrack

import AddReminderDialogFragment
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

class ReminderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        //Tab Menu functionality
        val tabMenu = findViewById<TabLayout>(R.id.TabMenu)
        tabMenu.getTabAt(3)?.select()
        tabMenu.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {val intent = Intent(this@ReminderActivity, DashboardActivity::class.java)
                        startActivity(intent)}
                    1 -> { val intent = Intent(this@ReminderActivity, SearchActivity::class.java)
                        startActivity(intent)}
                    2 -> {
                        val intent = Intent(this@ReminderActivity, ProfileActivity::class.java)
                        startActivity(intent)
                    }
                    3 -> {}
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        fab.setOnClickListener {
            AddReminderDialogFragment().show(supportFragmentManager, "add_reminder")
        }

    }
}