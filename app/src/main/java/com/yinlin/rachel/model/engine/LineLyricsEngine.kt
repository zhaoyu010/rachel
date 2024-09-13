package com.yinlin.rachel.model.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorRes
import com.yinlin.rachel.R
import com.yinlin.rachel.bold
import com.yinlin.rachel.toDP
import java.util.regex.Pattern


class LineLyricsEngine(context: Context) : LyricsEngine {
    data class LineItem(val position: Long, val text: String)

    class LinePaint(context: Context, isBold: Boolean, @ColorRes textColor: Int) : Paint(ANTI_ALIAS_FLAG) {
        var textHeight: Float
        private val maxTextHeight = 36.toDP(context)

        init {
            bold(context, isBold)
            style = Style.FILL_AND_STROKE
            strokeWidth = 1f
            color = context.getColor(textColor)
            textSize = 18f
            textAlign = Align.CENTER
            setShadowLayer(1f, 1f, 1f, context.getColor(R.color.black))
            textHeight = fontMetrics.descent - fontMetrics.ascent
        }

        fun setTextSizeAndTextHeight(size: Float) {
            textSize = size
            textHeight = fontMetrics.descent - fontMetrics.ascent
        }

        fun adjustTextSizeAndTextHeight(maxWidth: Float, maxLengthText: String) {
            setTextSizeAndTextHeight(18f)
            setShadowLayer(1f, 1f, 1f, shadowLayerColor)
            var detectWidth = measureText(maxLengthText)
            while (detectWidth < maxWidth && textHeight <= maxTextHeight) {
                textSize += 5f
                textHeight = fontMetrics.descent - fontMetrics.ascent
                adjustShadow(this) { value -> value + 0.3f }
                detectWidth = measureText(maxLengthText)
            }
        }

        fun adjustShadow(target: LinePaint, opt: (Float) -> Float) {
            setShadowLayer(opt(target.shadowLayerRadius), opt(target.shadowLayerDx), opt(target.shadowLayerDy), target.shadowLayerColor)
        }
    }

    private val items = ArrayList<LineItem>()
    private val hotpots = ArrayList<Long>()
    private val paintMain = LinePaint(context, true, R.color.music_lyrics_main)
    private val paintSecond = LinePaint(context, false, R.color.music_lyrics_second)
    private val paintFade = LinePaint(context, false, R.color.music_lyrics_fade)
    private var currentIndex: Int = -1
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0

    companion object { const val NAME = "Line" }

    override fun getName() = NAME
    override fun getHotpots() = hotpots

    override fun load(canvasWidth: Int, canvasHeight: Int, lines: List<String>): Boolean {
        try {
            clear()
            this.canvasWidth = canvasWidth
            this.canvasHeight = canvasHeight
            var maxLengthText = ""
            items += LineItem(-3, "")
            items += LineItem(-2, "")
            items += LineItem(-1, "")
            val pattern: Pattern = Pattern.compile("\\[(\\d{2}):(\\d{2}).(\\d{2})](.*)")
            for (item in lines) {
                val matcher = pattern.matcher(item.trim())
                if (matcher.matches()) {
                    val minutes = matcher.group(1)!!.toLong()
                    val seconds = matcher.group(2)!!.toLong()
                    val milliseconds = matcher.group(3)!!.toLong() * 10
                    val position = (minutes * 60 + seconds) * 1000 + milliseconds
                    var text: String = matcher.group(4)!!.trim()
                    val isHotpot = text.startsWith("+")
                    if (isHotpot) text = text.substring(1)
                    if (text.isNotEmpty()) {
                        if (text.length > maxLengthText.length) maxLengthText = text
                        items += LineItem(position, text)
                        if (isHotpot) hotpots += position
                    }
                }
            }
            items += LineItem(Long.MAX_VALUE - 1, "")
            items += LineItem(Long.MAX_VALUE, "")
            if (items.size < 11) throw Exception()
            items.sortWith { o1, o2 -> o1.position.compareTo(o2.position) }
            // 根据歌词最大宽度来调整字体大小
            paintMain.adjustTextSizeAndTextHeight(canvasWidth * 0.85f, maxLengthText)
            paintSecond.setTextSizeAndTextHeight(paintMain.textSize * 0.8f)
            paintSecond.adjustShadow(paintMain) { value -> value * 0.75f }
            paintFade.setTextSizeAndTextHeight(paintMain.textSize * 0.7f)
            paintFade.adjustShadow(paintMain) { value -> value * 0.5f }
            return true
        } catch (e: Exception) { clear() }
        return false
    }

    override fun clear() {
        items.clear()
        hotpots.clear()
        currentIndex = -1
        canvasWidth = 0
        canvasHeight = 0
    }

    override fun needUpdate(position: Long): Boolean {
        val index = findIndex(position)
        if (index != currentIndex) {
            currentIndex = index
            return true
        }
        return false
    }

    override fun update(canvas: Canvas, position: Long) {
        if (currentIndex >= 2) {
            val item1 = items[currentIndex - 2]
            val item2 = items[currentIndex - 1]
            val item3 = items[currentIndex]
            val item4 = items[currentIndex + 1]
            val item5 = items[currentIndex + 2]

            val x = canvasWidth / 2f
            val y = canvasHeight / 2f - paintMain.textHeight
            val dy1 = paintMain.textHeight * 1.2f
            val dy2 = dy1 + paintSecond.textHeight * 1.2f
            canvas.drawText(item3.text, x, y, paintMain)
            canvas.drawText(item2.text, x, y - dy1, paintSecond)
            canvas.drawText(item4.text, x, y + dy1, paintSecond)
            canvas.drawText(item1.text, x, y - dy2, paintFade)
            canvas.drawText(item5.text, x, y + dy2, paintFade)
        }
    }

    private fun searchIndex(position: Long): Int {
        var index = 0
        val len = items.size - 1
        while (index < len) {
            val currentItem = items[index]
            val nextItem = items[index + 1]
            if (currentItem.position <= position && nextItem.position > position) return index
            ++index
        }
        return 2
    }

    private fun findIndex(position: Long): Int {
        if (currentIndex == -1) return 2
        else {
            val currentItem = items[currentIndex]
            val nextItem = items[currentIndex + 1]
            return if (currentItem.position <= position && nextItem.position > position) currentIndex else searchIndex(position)
        }
    }
}