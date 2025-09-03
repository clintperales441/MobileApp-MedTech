package com.example.yourappname.data

data class Medicine(
    val name: String,
    val description: String,
    val dosage: String,
    var reminderTime: String? = null
)
