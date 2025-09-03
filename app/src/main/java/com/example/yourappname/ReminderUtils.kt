package com.example.yourappname

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

object ReminderUtils {
    private const val PREFS_NAME = "ReminderPrefs"
    private const val HISTORY_PREFS = "ReminderHistory"

    fun saveReminderStatus(context: Context, medicineName: String, time: Long, status: String) {
        val prefs = getPrefs(context)
        val key = "${medicineName}_$time"
        prefs.edit().putString(key, status).apply()

        val historyPrefs = getHistoryPrefs(context)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateTime = dateFormat.format(Date(time))

        val historyKey = "${medicineName}_${System.currentTimeMillis()}"
        val historyValue = "$status at $dateTime"

        historyPrefs.edit().putString(historyKey, historyValue).apply()
    }

    fun getReminderStatus(context: Context, medicineName: String, time: Long): String {
        val prefs = getPrefs(context)
        val key = "${medicineName}_$time"
        return prefs.getString(key, "Pending") ?: "Pending"
    }

    fun updateReminderStatus(context: Context, medicineName: String, time: Long, newStatus: String) {
        saveReminderStatus(context, medicineName, time, newStatus)
    }

    fun getMedicineHistory(context: Context, medicineName: String): List<String> {
        val historyPrefs = getHistoryPrefs(context)
        val history = mutableListOf<String>()

        val allEntries = historyPrefs.all
        for ((key, value) in allEntries) {
            if (key.startsWith("${medicineName}_")) {
                history.add(value.toString())
            }
        }

        return history
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getHistoryPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(HISTORY_PREFS, Context.MODE_PRIVATE)
    }
}