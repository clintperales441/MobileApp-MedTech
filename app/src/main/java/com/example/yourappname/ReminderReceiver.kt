package com.example.yourappname

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.yourappname.data.Medicine

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("medicineName") ?: "Your medicine"

        val actionIntent = Intent(context, ReminderActionActivity::class.java)
        actionIntent.putExtra("medicineName", medicineName)
        val sharedPrefs = context.getSharedPreferences("med_prefs", Context.MODE_PRIVATE)
        val gson = com.google.gson.Gson()
        val json = sharedPrefs.getString("medicine_list", null)
        if (json != null) {
            val type = object : com.google.gson.reflect.TypeToken<List<Medicine>>() {}.type
            val medicineList: List<Medicine> = gson.fromJson(json, type)

            var position = -1
            var description = ""
            var dosage = ""

            for (i in medicineList.indices) {
                if (medicineList[i].name == medicineName) {
                    position = i
                    description = medicineList[i].description
                    dosage = medicineList[i].dosage
                    break
                }
            }

            actionIntent.putExtra("medicinePosition", position)
            actionIntent.putExtra("medicineDescription", description)
            actionIntent.putExtra("medicineDosage", dosage)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            medicineName.hashCode(),
            actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "medicine_reminder_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = Uri.parse("android.resource://${context.packageName}/raw/ding")
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for MedTech medicine reminder alerts"
                setSound(soundUri, audioAttributes)
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }


        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Medicine Reminder")
            .setContentText("Time to take: $medicineName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(medicineName.hashCode(), notificationBuilder.build())
    }
}