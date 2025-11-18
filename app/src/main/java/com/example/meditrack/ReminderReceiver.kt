package com.example.meditrack

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

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

    override fun onReceive(context: Context, intent: Intent) {

        // Fetches reminder details
        val medicine = intent.getStringExtra("medicine") ?: "Medicine"
        val dosage = intent.getStringExtra("dosage") ?: ""
        val reminderId = intent.getStringExtra("reminder_id")?.hashCode() ?: 0

        if (medicine.isNullOrEmpty() || reminderId == 0) {
            return
        }

        // Displays the notification
        showNotification(context, medicine, dosage, reminderId)
    }

    /**
    * Method to display reminder notfication
    */
    private fun showNotification(context: Context, medicine: String, dosage: String, reminderId: Int ){

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
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open up app when notification is clicked
        val notificationIntent = Intent(context, ReminderActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Creates notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time to take $medicine")
            .setContentText("Dosage: $dosage")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Display notication
        notificationManager.notify(reminderId, notification)
    }

    companion object {
        private const val CHANNEL_ID = "medication_reminder_channel"
    }
}