package com.example.yourappname

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.yourappname.data.SettingOption
import com.example.yourappname.helper.SettingAdapter

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDarkModeOn = prefs.getBoolean("dark_mode_enabled", false)
        val mode = if (isDarkModeOn) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)

        setContentView(R.layout.activity_setting)

        val settingsList = findViewById<ListView>(R.id.settings_list)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", false)

        val settings = listOf(
            SettingOption(R.drawable.ic_dark_mode, "Dark Mode", true, isDarkModeOn),
            SettingOption(R.drawable.ic_notifications, "Notifications", true, notificationsEnabled),
            SettingOption(R.drawable.padlock, "Change Password", false),
            SettingOption(R.drawable.ic_logout, "Log Out", false)
        )

        val adapter = SettingAdapter(this, settings)
        settingsList.adapter = adapter

        adapter.setLogoutClickListener(object : SettingAdapter.LogoutClickListener {
            override fun onLogoutClick() {
                logoutUser()
            }
        })

        adapter.setChangePasswordClickListener(object : SettingAdapter.ChangePasswordClickListener {
            override fun onChangePasswordClick() {
                val intent = Intent(this@SettingActivity, ChangePasswordActivity::class.java)
                startActivity(intent)
            }
        })


        val btnBack: ImageView = findViewById(R.id.button_back)
        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun logoutUser() {
        val sharedPrefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
