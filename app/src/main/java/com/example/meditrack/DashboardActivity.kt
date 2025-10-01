package com.example.meditrack

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        //Tab Menu functionality
        val tabMenu = findViewById<TabLayout>(R.id.TabMenu)
        tabMenu.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {}
                    1 -> {
                        val intent = Intent(this@DashboardActivity, SearchActivity::class.java)
                        startActivity(intent)
                    }
                    2 -> {
                        val intent = Intent(this@DashboardActivity, ProfileActivity::class.java)
                        startActivity(intent)
                    }
                    3 -> {
                        val intent = Intent(this@DashboardActivity, ReminderActivity::class.java)
                        startActivity(intent)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


    }
}