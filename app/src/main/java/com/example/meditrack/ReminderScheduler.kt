package com.example.meditrack

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*


/**
 * ReminderScheduler.kt
 *
 * Object used to schedule and cancel reminder notifications.
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */
object ReminderScheduler {

    /**
     * Function to create reminder based on selected times and frequency.
     */
    fun scheduleReminder(context: Context, reminder: Reminder, reminderId: String){

        // AlarmManager service
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check permission to set alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("ReminderScheduler", "Cannot schedule exact alarms")
                return
            }
        }

        // Formatting date and time inputs.
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val startDate = try {
            dateFormat.parse(reminder.startDate)
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Invalid start date: ${reminder.startDate}")
            return
        }
        val endDate = if (reminder.endDate.isNotEmpty()) {
            try {
                dateFormat.parse(reminder.endDate)
            } catch (e: Exception) {
                null
            }
        } else null

        // Loop through each time specified
        reminder.times.forEachIndexed { index, timeString ->
            try {
                val time = timeFormat.parse(timeString)

                // Create a calendar instance for each scheduled alarm
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = startDate!!.time
                    val timeCalendar = Calendar.getInstance().apply {
                        timeInMillis = time!!.time
                    }

                    // Set time and date of alarm
                    set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                }

                // If time has passed, schedules for next day
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }

                val requestCode = generateRequestCode(reminderId, index)

                // Intent to call reminder receiver when alarm goes off
                val intent = Intent(context, ReminderReceiver::class.java).apply {
                    putExtra("medicine", reminder.medicine)
                    putExtra("dosage", reminder.dosage)
                    putExtra("reminder_id", reminderId)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Schedules alarm based on reminder frequency
                when (reminder.frequency) {
                    "Daily" -> {
                        alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            AlarmManager.INTERVAL_DAY,
                            pendingIntent
                        )
                    }
                    "Weekly" -> {
                        alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            AlarmManager.INTERVAL_DAY * 7,
                            pendingIntent
                        )
                    }
                    "Once Off" -> {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                }

                Log.d("ReminderScheduler", "Scheduled alarm for ${reminder.medicine} at ${calendar.time}")

            } catch (e: Exception) {
                Log.e("ReminderScheduler", "Error scheduling time $timeString: ${e.message}")
            }
        }
    }

    /**
     * Function to cancel scheduled alarm when reminder is deleted by user
     */
    fun cancelReminder(context: Context, reminderId: String, timesCount: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (index in 0 until timesCount) {
            val requestCode = generateRequestCode(reminderId, index)
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }

        Log.d("ReminderScheduler", "Cancelled alarms for reminder: $reminderId")
    }

    private fun generateRequestCode(reminderId: String, timeIndex: Int): Int {
        return (reminderId.hashCode() + timeIndex).and(0x7FFFFFFF)
    }
}