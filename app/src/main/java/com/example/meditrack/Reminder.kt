package com.example.meditrack

// Data class for reminder
data class Reminder(
    val medicine: String = "",
    val dosage: String = "",
    val times: List<String> = emptyList(),
    val frequency: String = "",
    val startDate: String = "",
    val endDate: String = ""
)
