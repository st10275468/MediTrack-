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
    fun scheduleReminder(context: Context, reminder: Reminder, reminderId: String) {

        // AlarmManager service
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check permission to set alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("ReminderScheduler", "Cannot schedule exact alarms - permission not granted")
                return
            }
        }

        // Formatting date and time inputs.
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val startDate = try {
            dateFormat.parse(reminder.startDate) ?: return
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Invalid start date: ${reminder.startDate}", e)
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
                val time = timeFormat.parse(timeString) ?: return@forEachIndexed

                // Create a calendar instance for each scheduled alarm
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = startDate.time
                    val timeCalendar = Calendar.getInstance().apply {
                        timeInMillis = time.time
                    }

                    // Set time and date of alarm
                    set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // If time has passed, schedules for next day
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    when (reminder.frequency) {
                        "Daily" -> calendar.add(Calendar.DAY_OF_MONTH, 1)
                        "Weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                        "Once Off" -> {
                            Log.w("ReminderScheduler", "Once-off reminder time has passed, not scheduling")
                            return@forEachIndexed
                        }
                    }
                }

                if (endDate != null && calendar.timeInMillis > endDate.time) {
                    Log.w("ReminderScheduler", "Reminder is past end date, not scheduling")
                    return@forEachIndexed
                }

                scheduleNextAlarm(context, reminder, reminderId, index, calendar, endDate)

            } catch (e: Exception) {
                Log.e("ReminderScheduler", "Error scheduling time $timeString", e)
            }
        }
    }

    /**
     * Function to schedule the next alarm
     */
    private fun scheduleNextAlarm(
        context: Context,
        reminder: Reminder,
        reminderId: String,
        timeIndex: Int,
        calendar: Calendar,
        endDate: Date?
    ) {
        val requestCode = generateRequestCode(reminderId, timeIndex)


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

        // Fetch alarm manager adn schedule the next alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        Log.d("ReminderScheduler",
            "Scheduled exact alarm for ${reminder.medicine} at ${calendar.time} (${reminder.frequency})")

        // if recuurring, schedule the rescheduler
        if (reminder.frequency != "Once Off") {
            scheduleRescheduler(context, reminder, reminderId, timeIndex, calendar, endDate)
        }
    }

    /**
     * Function to setup the rescheduler to schedule next notification for reacurring reminders
     */
    private fun scheduleRescheduler(
        context: Context,
        reminder: Reminder,
        reminderId: String,
        timeIndex: Int,
        currentAlarm: Calendar,
        endDate: Date?
    ) {
        // Calculate when next alarm is
        val nextAlarm = currentAlarm.clone() as Calendar
        when (reminder.frequency) {
            "Daily" -> nextAlarm.add(Calendar.DAY_OF_MONTH, 1)
            "Weekly" -> nextAlarm.add(Calendar.WEEK_OF_YEAR, 1)
        }

        if (endDate != null && nextAlarm.timeInMillis > endDate.time) {
            Log.d("ReminderScheduler", "Next alarm exceeds end date, not rescheduling")
            return
        }

        // Will trigger 1 minute after the alarm
        val rescheduleTime = currentAlarm.timeInMillis + 60000

        val rescheduleIntent = Intent(context, ReminderRescheduler::class.java).apply {
            putExtra("medicine", reminder.medicine)
            putExtra("dosage", reminder.dosage)
            putExtra("startDate", reminder.startDate)
            putExtra("endDate", reminder.endDate)
            putExtra("times", ArrayList(reminder.times))
            putExtra("frequency", reminder.frequency)
            putExtra("reminder_id", reminderId)
            putExtra("time_index", timeIndex)
        }

        val reschedulePending = PendingIntent.getBroadcast(
            context,
            generateRequestCode(reminderId, timeIndex) + 10000, // Offset to avoid conflicts
            rescheduleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                rescheduleTime,
                reschedulePending
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                rescheduleTime,
                reschedulePending
            )
        }
    }

    /**
     * Function to cancel scheduled reminder notifications
     */
    fun cancelReminder(context: Context, reminderId: String, timesCount: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (index in 0 until timesCount) {
            val requestCode = generateRequestCode(reminderId, index)

            // Cancel main alarm
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()

            // Cancel rescheduler
            val rescheduleIntent = Intent(context, ReminderRescheduler::class.java)
            val reschedulePending = PendingIntent.getBroadcast(
                context,
                requestCode + 10000,
                rescheduleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(reschedulePending)
            reschedulePending.cancel()
        }

        Log.d("ReminderScheduler", "Cancelled all alarms for reminder: $reminderId")
    }

    private fun generateRequestCode(reminderId: String, timeIndex: Int): Int {
        return (reminderId.hashCode() + timeIndex).and(0x7FFFFFFF)
    }
}