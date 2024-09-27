package com.yinlin.rachel.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.yinlin.rachel.R
import com.yinlin.rachel.bold
import com.yinlin.rachel.model.RachelFrequencyCounter
import com.yinlin.rachel.toDP
import com.yinlin.rachel.toSP
import com.yinlin.rachel.toStringTime
import kotlin.math.abs


class HotpotProgressView(context: Context, attrs: AttributeSet?, defStyleAttr: Int): View(context, attrs, defStyleAttr) {
    fun interface OnProgressChangedListener {
        fun onProgressChanged(percent: Float)
    }

    private var played: Long = 0L
    private var duration: Long = 0L
    private var items: List<Long> = ArrayList()
    private val paintTotal = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintPlayed = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintHotpot = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressHeight: Int
    private val gap: Int
    private val textHeight: Float
    private val updateCounter: RachelFrequencyCounter
    private var listener: OnProgressChangedListener? = null

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context): this(context, null)

    init {
        paintTotal.color = context.getColor(R.color.music_progress_total)
        paintPlayed.color = context.getColor(R.color.music_progress_played)
        paintHotpot.color = context.getColor(R.color.music_progress_hotpot)
        paintText.color = context.getColor(R.color.white)
        paintText.textSize = 12f.toSP(context)
        if (!isInEditMode) paintText.bold(context, false)
        progressHeight = 10.toDP(context)
        gap = 5.toDP(context)
        val fontMetrics: Paint.FontMetrics = paintText.getFontMetrics()
        textHeight = fontMetrics.descent - fontMetrics.ascent
        updateCounter = RachelFrequencyCounter(4)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) =
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (progressHeight + textHeight + gap).toInt())

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val canvasWidth = width.toFloat()
        val canvasHeight = height.toFloat()
        val playedWidth = if (duration == 0L) 0f else played * canvasWidth / duration
        val progressTop = textHeight + gap
        val playedText = played.toStringTime()
        val totalText = duration.toStringTime()
        canvas.drawText(playedText, 0f, textHeight, paintText)
        canvas.drawText(totalText, canvasWidth - paintText.measureText(totalText), textHeight, paintText)
        val centerProgressY = progressTop + (canvasHeight - progressTop) / 2
        val progressHeightHalf = (canvasHeight - progressTop) / 6
        val radius = progressHeightHalf * 2
        canvas.drawRoundRect(0f, centerProgressY + progressHeightHalf, canvasWidth, centerProgressY - progressHeightHalf, radius, radius, paintTotal)
        canvas.drawRoundRect(0f, centerProgressY + progressHeightHalf, playedWidth, centerProgressY - progressHeightHalf, radius, radius, paintPlayed)
        if (duration != 0L) {
            for (position in items) {
                canvas.drawCircle(position * canvasWidth / duration, centerProgressY, radius, paintHotpot)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && duration != 0L) {
            val canvasWidth = width
            val x = event.x
            if (event.y > textHeight + gap) {
                val radiusX = height / 2f
                var percent = x / canvasWidth
                if (items.isNotEmpty()) { // 快进副歌事件
                    for (position in items) { // 搜索副歌热点事件
                        val hotpotX = position.toFloat() * canvasWidth / duration
                        if (abs((x - hotpotX).toDouble()) <= radiusX) { // 如果误差不超过副歌条的阈值
                            percent = position.toFloat() / duration
                            break
                        }
                    }
                }
                listener?.onProgressChanged(percent) // 普通进度变更事件
            }
        }
        return true
    }

    fun setInfo(items: List<Long>, duration: Long) {
        this.items = items
        this.duration = duration
        this.played = 0L
        updateCounter.reset()
        invalidate()
    }

    fun updateProgress(position: Long, immediately: Boolean) {
        if (immediately) {
            this.played = position
            invalidate()
        } else if (updateCounter.ok()) {
            this.played = position
            invalidate()
        }
    }

    fun setOnProgressChangedListener(listener: OnProgressChangedListener) {
        this.listener = listener
    }
}