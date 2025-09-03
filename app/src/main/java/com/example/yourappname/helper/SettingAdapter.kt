package com.example.yourappname.helper

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.example.medtech.helper.ThemeManager
import com.example.yourappname.LoginActivity
import com.example.yourappname.R
import com.example.yourappname.data.SettingOption
import com.example.yourappname.ChangePasswordActivity

class SettingAdapter(private val context: Context, private val settings: List<SettingOption>) : BaseAdapter() {

    interface LogoutClickListener {
        fun onLogoutClick()
    }

    interface ChangePasswordClickListener {
        fun onChangePasswordClick()
    }

    private var logoutClickListener: LogoutClickListener? = null
    private var changePasswordClickListener: ChangePasswordClickListener? = null

    fun setLogoutClickListener(listener: LogoutClickListener) {
        this.logoutClickListener = listener
    }

    fun setChangePasswordClickListener(listener: ChangePasswordClickListener) {
        this.changePasswordClickListener = listener
    }

    override fun getCount(): Int = settings.size
    override fun getItem(position: Int): Any = settings[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_setting, parent, false)

        val icon = view.findViewById<ImageView>(R.id.icon)
        val title = view.findViewById<TextView>(R.id.title)
        val toggle = view.findViewById<Switch>(R.id.switch_toggle)

        val setting = settings[position]
        icon.setImageResource(setting.iconResId)
        title.text = setting.title

        if (setting.isSwitch) {
            toggle.visibility = View.VISIBLE
            toggle.isChecked = setting.isEnabled

            toggle.setOnCheckedChangeListener { _, isChecked ->
                setting.isEnabled = isChecked

                when (setting.title) {
                    "Dark Mode" -> {
                        ThemeManager.setDarkMode(isChecked, context)
                    }
                    "Notifications" -> {
                        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
                        Toast.makeText(context, "Notifications ${if (isChecked) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            toggle.visibility = View.GONE
            view.setOnClickListener {
                when (setting.title) {
                    "Log Out" -> {
                        logoutClickListener?.onLogoutClick() ?: performLogout()
                    }
                    "Change Password" -> {
                        changePasswordClickListener?.onChangePasswordClick() ?: openChangePassword()
                    }
                }
            }
        }

        return view
    }

    private fun performLogout() {
        val sharedPrefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    private fun openChangePassword() {
        val intent = Intent(context, ChangePasswordActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}
