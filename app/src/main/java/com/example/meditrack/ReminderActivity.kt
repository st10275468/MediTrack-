package com.example.meditrack

import AddReminderDialogFragment
import ReminderAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * ReminderActivity.kt
 *
 * This activity displays the user with a list of current medication reminders and allows them to delete and add new reminders.
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */
class ReminderActivity : AppCompatActivity() {

    // Adapter to bind to recycler view
    private lateinit var adapter: ReminderAdapter

    // List of reminders
    private val reminders = mutableListOf<Reminder>()

    override fun attachBaseContext(newBase: Context){
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        // Setup recycler view
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ReminderAdapter(reminders) { reminder ->
            deleteReminder(reminder)
        }

        recyclerView.adapter = adapter

        // Fetch reminders from firestore
        fetchReminders()

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

        // Button for new reminder
        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        fab.setOnClickListener {
            AddReminderDialogFragment().show(supportFragmentManager, "add_reminder")
        }

    }

    /**
     * Method to fetch reminders for current user
     */
    fun fetchReminders() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(user.uid).collection("reminders")
            .get()
            .addOnSuccessListener { result ->
                reminders.clear()
                for (doc in result) {
                    reminders.add(doc.toObject(Reminder::class.java))
                }
                adapter.notifyDataSetChanged()
            }
    }

    /**
     * Method to delete reminder from firestore
     */
    private fun deleteReminder(reminder: Reminder) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete ${reminder.medicine}?")
            .setPositiveButton("Delete") { dialog, _ ->
                val user = FirebaseAuth.getInstance().currentUser ?: return@setPositiveButton
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(user.uid).collection("reminders")
                    .whereEqualTo("medicine", reminder.medicine)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        for (doc in snapshot) {
                            doc.reference.delete()
                        }
                        fetchReminders()
                    }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}