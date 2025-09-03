package com.example.yourappname

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReminderActionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_action)


        val medicineName = intent.getStringExtra("medicineName")
        val medicineDescription = intent.getStringExtra("medicineDescription")
        val medicineDosage = intent.getStringExtra("medicineDosage")


        val medicineText = findViewById<TextView>(R.id.medicineText)
        val btnTake = findViewById<Button>(R.id.btnTake)
        val btnSkip = findViewById<Button>(R.id.btnSkip)

        medicineText.text = "It's time to take: $medicineName\nDosage: $medicineDosage"

        btnTake.setOnClickListener {
            medicineName?.let {
                ReminderUtils.updateReminderStatus(this, it, System.currentTimeMillis(), "Taken")
            }
            finish()
        }

        btnSkip.setOnClickListener {
            medicineName?.let {
                ReminderUtils.updateReminderStatus(this, it, System.currentTimeMillis(), "Skipped")
            }
            finish()
        }
    }
}