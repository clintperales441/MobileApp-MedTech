package com.example.yourappname.helper

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.example.medtech.helper.ThemeManager


class ThemeHelper(private val activity: AppCompatActivity) : LifecycleObserver {


    private val themeChangeListener: () -> Unit = {
        if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            activity.recreate()
        }
    }

    init {
        activity.lifecycle.addObserver(this)
        ThemeManager.addThemeChangeListener(themeChangeListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        ThemeManager.removeThemeChangeListener(themeChangeListener)
        activity.lifecycle.removeObserver(this)
    }

    companion object {
        fun attach(activity: AppCompatActivity): ThemeHelper {
            return ThemeHelper(activity)
        }


    }
}