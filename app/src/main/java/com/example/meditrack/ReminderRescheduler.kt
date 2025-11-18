package com.example.meditrack

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * ReminderRescheduler.kt
 *
 * Receiver to handle the automatic rescheduling of reminders if set to reaccur.
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */
class ReminderRescheduler : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicine = intent.getStringExtra("medicine") ?: return
        val dosage = intent.getStringExtra("dosage") ?: return
        val startDate = intent.getStringExtra("startDate") ?: return
        val endDate = intent.getStringExtra("endDate") ?: ""
        val times = intent.getStringArrayListExtra("times") ?: return
        val frequency = intent.getStringExtra("frequency") ?: return
        val reminderId = intent.getStringExtra("reminder_id") ?: return

        val reminder = Reminder(
            medicine = medicine,
            dosage = dosage,
            startDate = startDate,
            endDate = endDate,
            times = times,
            frequency = frequency
        )

        // Reschedule the reminder for the time
        ReminderScheduler.scheduleReminder(context, reminder, reminderId)

        Log.d("ReminderRescheduler", "Rescheduled reminder for $medicine")
    }
}