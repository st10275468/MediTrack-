package com.example.meditrack

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabItem

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)




        //Tab menu functionality
        val tbProfile = findViewById<TabItem>(R.id.tbProfile)
        tbProfile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val tbSearch = findViewById<TabItem>(R.id.tbSearch)
        tbSearch.setOnClickListener{
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        val tbReminders = findViewById<TabItem>(R.id.tbReminder)
        tbReminders.setOnClickListener{
            val intent = Intent(this, ReminderActivity::class.java)
            startActivity(intent)
        }
    }
}