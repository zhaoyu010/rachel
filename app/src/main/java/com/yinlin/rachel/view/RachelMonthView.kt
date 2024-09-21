package com.yinlin.rachel.view

import android.R.attr
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.MonthView
import com.yinlin.rachel.R
import com.yinlin.rachel.toDP


class RachelMonthView(context: Context) : MonthView(context) {
    private val weekendColor = context.getColor(R.color.sky_blue)
    private val schemeColor = context.getColor(R.color.steel_blue)
    private val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = context.resources.getDimension(R.dimen.sm)
        textAlign = Paint.Align.CENTER
        color = mCurMonthTextPaint.color
    }
    private val lunarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = context.resources.getDimension(R.dimen.xs)
        textAlign = Paint.Align.CENTER
        color = mCurMonthLunarTextPaint.color
    }
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
        color = schemeColor
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f.toDP(context)
        color = context.getColor(R.color.purple)
    }

    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean) = true

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
        val pad = 1f.toDP(context)
        canvas.drawCircle(x + mItemWidth / 2f, y + mItemHeight - 9 * pad, 2 * pad, pointPaint)
        canvas.drawRoundRect(x + pad, y + pad, x + mItemWidth - pad, y + mItemHeight - pad, 3 * pad, 3 * pad, borderPaint)
    }

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean) {
        val cx = x + mItemWidth / 2
        val top = y - mItemHeight / 6
        dayPaint.color = if (calendar.hasScheme()) schemeColor
            else if (calendar.isCurrentDay) mCurDayTextPaint.color
            else if (!calendar.isCurrentMonth) mOtherMonthTextPaint.color
            else if (calendar.isWeekend) weekendColor
            else mCurMonthTextPaint.color
        lunarPaint.color = if (calendar.hasScheme()) schemeColor
            else if (calendar.isCurrentDay) mCurDayLunarTextPaint.color
            else if (!calendar.isCurrentMonth) mOtherMonthLunarTextPaint.color
            else if (calendar.isWeekend) weekendColor
            else mCurMonthLunarTextPaint.color
        val dayText = if (calendar.hasScheme()) calendar.scheme else calendar.lunar
        lunarPaint.textSize = context.resources.getDimension(if (dayText.length > 3) R.dimen.xxs else R.dimen.xs)
        canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top, dayPaint)
        canvas.drawText(dayText, cx.toFloat(), mTextBaseLine + y + mItemHeight / 10f, lunarPaint)
    }
}