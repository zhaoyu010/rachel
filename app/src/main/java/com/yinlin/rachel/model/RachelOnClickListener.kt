package com.yinlin.rachel.model

import android.view.View


class RachelOnClickListener : View.OnClickListener {
    private var delay: Long = 500L
    private var lastTime: Long = 0L
    private var listener: View.OnClickListener

    constructor(listener: View.OnClickListener) {
        this.listener = listener
    }

    constructor(delay: Long, listener: View.OnClickListener) {
        this.listener = listener
        this.delay = delay
    }

    override fun onClick(view: View) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTime > delay) {
            lastTime = currentTime
            listener.onClick(view)
        }
    }
}