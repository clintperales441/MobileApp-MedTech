package com.example.yourappname.data



data class SettingOption(
    val iconResId: Int,
    val title: String,
    val isSwitch: Boolean,
    var isEnabled: Boolean = false
)