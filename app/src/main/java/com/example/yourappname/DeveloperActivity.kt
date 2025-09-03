package com.example.yourappname

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.yourappname.helper.ThemeHelper

class DeveloperActivity : AppCompatActivity() {

    private lateinit var themeHelper: ThemeHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer)

        themeHelper = ThemeHelper.attach(this)

        val btnToggleMission = findViewById<ImageButton>(R.id.btnToggleMission)
        val btnToggleVision = findViewById<ImageButton>(R.id.btnToggleVision)
        val btnToggleDev1 = findViewById<ImageButton>(R.id.btnToggleDev1)
        val btnToggleDev2 = findViewById<ImageButton>(R.id.btnToggleDev2)

        val textMissionDescription = findViewById<TextView>(R.id.textMissionDescription)
        val textVisionDescription = findViewById<TextView>(R.id.textVisionDescription)
        val textDev1Bio = findViewById<TextView>(R.id.textDev1Bio)
        val textDev2Bio = findViewById<TextView>(R.id.textDev2Bio)

        val btnBack = findViewById<ImageButton>(R.id.button_back)
        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnToggleMission.setOnClickListener {
            AboutUsToggleHelper.toggleSection(textMissionDescription, btnToggleMission)
        }

        btnToggleVision.setOnClickListener {
            AboutUsToggleHelper.toggleSection(textVisionDescription, btnToggleVision)
        }

        btnToggleDev1.setOnClickListener {
            AboutUsToggleHelper.toggleSection(textDev1Bio, btnToggleDev1)
        }

        btnToggleDev2.setOnClickListener {
            AboutUsToggleHelper.toggleSection(textDev2Bio, btnToggleDev2)

        }
    }
}