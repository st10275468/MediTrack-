package com.example.meditrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.tabs.TabLayout
import java.util.Locale

/**
 * DashboardActivity.kt
 *
 * This activity is the home dashboard once a user is logged in and displays links to all the features
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */
class DashboardActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context){
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Set feature buttons
        val cvSearch = findViewById<CardView>(R.id.cvSearch)
        val cvReminder = findViewById<CardView>(R.id.cvReminder)
        val cvProfile = findViewById<CardView>(R.id.cvProfile)
        val cvMap = findViewById<CardView>(R.id.cvMap)
        val cvScanner = findViewById<CardView>(R.id.cvScanner)
        val cvSettings = findViewById<CardView>(R.id.cvSettings)

        // Button listeners for features
        cvSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
        cvReminder.setOnClickListener {
            val intent = Intent(this, ReminderActivity::class.java)
            startActivity(intent)
        }
        cvProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        cvMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
        cvScanner.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            startActivity(intent)
        }

        cvSettings.setOnClickListener {
            Toast.makeText(this, "Feature coming in next update", Toast.LENGTH_SHORT).show()
        }


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


        //Settings Menu functionality
        val settingsIcon = findViewById<ImageView>(R.id.imageView4)
        settingsIcon.setOnClickListener {
            val popup = PopupMenu(this, settingsIcon)
            popup.menuInflater.inflate(R.menu.menu_settings, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.menu_language -> {
                        val languagePopup = PopupMenu(this, settingsIcon)
                        languagePopup.menu.add("English")
                        languagePopup.menu.add("Afrikaans")

                        languagePopup.setOnMenuItemClickListener { langItem ->
                            val code = if (langItem.title == "English") "en" else "af"
                            LocaleHelper.setLocale(this, code)
                            LocaleHelper.refreshActivity(this)
                            true
                        }
                        languagePopup.show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }


    }
}