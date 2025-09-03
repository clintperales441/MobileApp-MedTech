package com.example.yourappname

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageButton
import android.widget.TextView

object AboutUsToggleHelper {
    fun toggleSection(textDescription: TextView, toggleButton: ImageButton) {
        if (textDescription.visibility == View.GONE) {
            textDescription.visibility = View.VISIBLE
            toggleButton.setImageResource(R.drawable.ic_expand_less)

            val fadeIn = AlphaAnimation(0.0f, 1.0f)
            fadeIn.duration = 300
            textDescription.startAnimation(fadeIn)
        } else {
            val fadeOut = AlphaAnimation(1.0f, 0.0f)
            fadeOut.duration = 300
            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    textDescription.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            textDescription.startAnimation(fadeOut)
            toggleButton.setImageResource(R.drawable.ic_expand_more)
        }
    }
}