package com.example.meditrack

import AddReminderDialogFragment
import ReminderAdapter
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReminderActivity : AppCompatActivity() {

    private lateinit var adapter: ReminderAdapter
    private val reminders = mutableListOf<Reminder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ReminderAdapter(reminders) { reminder ->
            deleteReminder(reminder)
        }

        recyclerView.adapter = adapter
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

        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        fab.setOnClickListener {
            AddReminderDialogFragment().show(supportFragmentManager, "add_reminder")
        }

    }

    private fun fetchReminders() {
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