package com.example.meditrack

data class Reminder(
    val medicine: String = "",
    val dosage: String = "",
    val times: List<String> = emptyList(),
    val frequency: String = "",
    val startDate: String = "",
    val endDate: String = ""
)
