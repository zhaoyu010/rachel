package com.yinlin.rachel.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.yinlin.rachel.R
import com.yinlin.rachel.textColor

class ActiveTextView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : AppCompatTextView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context): this(context, null)

    private var bottomY: Float = 0f
    private var normalColor: Int = context.getColor(R.color.black)
    private var activeColor: Int = context.getColor(R.color.steel_blue)

    init {
        bottomY = (paint.fontMetrics.descent - paint.fontMetrics.ascent) * 0.3f
        setPadding(paddingLeft, paddingTop, paddingRight, (paddingBottom + bottomY).toInt())
        normalColor = textColor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val padX = width * 0.2f
        val padY = bottomY * 0.3f
        canvas.drawRoundRect(paddingStart + padX, height - paddingBottom + padY, width - paddingEnd - padX, height - paddingBottom + bottomY - padY, 5f, 5f, paint)
    }

    var active: Boolean
        get() = textColor == activeColor
        set(value) {
            textColor = if (value) activeColor else normalColor
        }
}