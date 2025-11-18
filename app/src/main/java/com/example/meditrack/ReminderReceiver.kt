package com.example.meditrack

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * ReminderReceiver.kt
 *
 * Receiver to wait for reminder alarm triggers from AlarmManager.
 * Triggers reminder notification.
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */
class ReminderReceiver : BroadcastReceiver() {

    // Fetches reminder details
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                rescheduleAllReminders(context)
            }
            else -> {
                val medicine = intent.getStringExtra("medicine") ?: return
                val dosage = intent.getStringExtra("dosage") ?: ""
                val reminderId = intent.getStringExtra("reminder_id") ?: return

                showNotification(context, medicine, dosage, reminderId.hashCode())
            }
        }
    }

    /**
     * Method to display reminder notfication
     */
    private fun showNotification(context: Context, medicine: String, dosage: String, notificationId: Int) {

        // Fetches the notification service
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Creates required notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pill Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for pill reminders"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open up app when notification is clicked
        val notificationIntent = Intent(context, ReminderActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Creates notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time to take $medicine")
            .setContentText("Dosage: $dosage")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Display notication
        notificationManager.notify(notificationId, notification)
        Log.d("ReminderReceiver", "Notification displayed for $medicine")
    }

    /**
     * Method to reschedule all remidners if the device reboots
     */
    private fun rescheduleAllReminders(context: Context) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(user.uid)
            .collection("reminders")
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    val reminder = Reminder(
                        medicine = doc.getString("medicine") ?: "",
                        dosage = doc.getString("dosage") ?: "",
                        startDate = doc.getString("startDate") ?: "",
                        endDate = doc.getString("endDate") ?: "",
                        times = doc.get("times") as? List<String> ?: emptyList(),
                        frequency = doc.getString("frequency") ?: ""
                    )
                    ReminderScheduler.scheduleReminder(context, reminder, doc.id)
                }
                Log.d("ReminderReceiver", "Rescheduled ${snapshot.size()} reminders after boot")
            }
    }

    companion object {
        private const val CHANNEL_ID = "medication_reminder_channel"
    }
}