package com.yinlin.rachel.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.yinlin.rachel.R
import com.yinlin.rachel.bold
import com.yinlin.rachel.toSP

class TitleView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : AppCompatImageView(context, attrs, defStyleAttr) {
    private val levelMap = arrayOf(
        R.drawable.title0, R.drawable.title1,
        R.drawable.title2, R.drawable.title3,
        R.drawable.title4
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var text: String = "小银子"

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context): this(context, null)

    init {
        paint.color = context.getColor(R.color.dark)
        if (!isInEditMode) paint.bold(context, true)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 10f.toSP(context)
        setImageResource(levelMap[1])
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawText(text, width / 2f, height * 0.77f, paint)
    }

    fun setTitle(titleGroup: Int, text: String) {
        this.text = text
        this.setImageDrawable(ContextCompat.getDrawable(context, levelMap[titleGroup]))
    }
}