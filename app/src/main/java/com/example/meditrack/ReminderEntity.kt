package com.example.meditrack

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Reminder entity for storage in roomDB
 */
@Entity(tableName = "reminders")
data class ReminderEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firebaseID: String? = null,
    val medicine: String,
    val dosage: String,
    val times: List<String>,
    val frequency: String,
    val startDate: String,
    val endDate: String,
    val createdAt: Long
)
