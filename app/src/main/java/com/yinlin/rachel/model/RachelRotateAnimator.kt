package com.yinlin.rachel.model

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.LinearInterpolator


class RachelRotateAnimator(view: View, duration: Long) {
    private val animator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f)
    private var angle: Float = 0f
    init {
        animator.interpolator = LinearInterpolator()
        animator.duration = duration
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.repeatMode = ObjectAnimator.RESTART
        animator.addUpdateListener { angle = animator.animatedValue as Float }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) = animator.setFloatValues(angle, angle + 360f)
        })
    }

    fun start() = if (animator.isPaused) animator.resume() else animator.start()
    fun pause() = animator.pause()
}