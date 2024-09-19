package com.yinlin.rachel.model.engine

import android.content.Context
import android.graphics.BlendMode
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.view.TextureView
import androidx.annotation.ColorRes
import com.yinlin.rachel.R
import com.yinlin.rachel.bold
import com.yinlin.rachel.readText
import com.yinlin.rachel.toDP
import java.io.File
import java.util.regex.Pattern


// 多行文本歌词引擎
class LineLyricsEngine : LyricsEngine {
    @JvmRecord
    data class LineItem(val position: Long, val text: String)

    class LinePaint(private val maxTextHeight: Int = 100) : Paint(ANTI_ALIAS_FLAG) {
        var textHeight: Float

        constructor(context: Context, isBold: Boolean, @ColorRes textColor: Int) : this(36.toDP(context)) {
            bold(context, isBold)
            color = context.getColor(textColor)
            setShadowLayer(1f, 1f, 1f, context.getColor(R.color.black))
        }

        init {
            style = Style.FILL_AND_STROKE
            strokeWidth = 1f
            textSize = 18f
            textAlign = Align.CENTER
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

    companion object { const val NAME = "line" }
    override val name: String = NAME
    override val ext: String = ".lrc"

    private var paintMain = LinePaint()
    private var paintSecond = LinePaint()
    private var paintFade = LinePaint()
    private var currentIndex: Int = -1
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0
    private var texture: TextureView? = null
    private val items = ArrayList<LineItem>()

    override fun load(texture: TextureView, surface: SurfaceTexture, width: Int, height: Int, file: String): Boolean {
        try {
            items.clear()
            this.texture = texture
            canvasWidth = width
            canvasHeight = height
            currentIndex = -1
            parseLrcText(texture.context, File(file).readText())
            return items.isNotEmpty()
        }
        catch (ignored: Exception) {
            clear()
        }
        return false
    }

    override fun clear() {
        items.clear()
        texture = null
        canvasWidth = 0
        canvasHeight = 0
        currentIndex = -1
    }

    override fun release() = clear()

    override fun needUpdate(position: Long): Boolean {
        val index = findIndex(position)
        if (index != currentIndex) {
            currentIndex = index
            return true
        }
        return false
    }

    override fun update(position: Long) {
        if (currentIndex >= 2) {
            texture?.apply {
                val canvas = lockCanvas()
                if (canvas != null) {
                    val item1 = items[currentIndex - 2]
                    val item2 = items[currentIndex - 1]
                    val item3 = items[currentIndex]
                    val item4 = items[currentIndex + 1]
                    val item5 = items[currentIndex + 2]

                    val x = canvasWidth / 2f
                    val y = canvasHeight / 2f - paintMain.textHeight
                    val dy1 = paintMain.textHeight * 1.2f
                    val dy2 = dy1 + paintSecond.textHeight * 1.2f

                    canvas.drawColor(Color.TRANSPARENT, BlendMode.CLEAR)
                    canvas.drawText(item3.text, x, y, paintMain)
                    canvas.drawText(item2.text, x, y - dy1, paintSecond)
                    canvas.drawText(item4.text, x, y + dy1, paintSecond)
                    canvas.drawText(item1.text, x, y - dy2, paintFade)
                    canvas.drawText(item5.text, x, y + dy2, paintFade)
                    unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    private fun parseLrcText(context: Context, source: String) {
        try {
            var maxLengthText = ""
            // 解析歌词文件
            val lines = source.split("\\r?\\n".toRegex())
            val pattern: Pattern = Pattern.compile("\\[(\\d{2}):(\\d{2}).(\\d{2})](.*)")
            // 前三空行
            items += LineItem(-3, "")
            items += LineItem(-2, "")
            items += LineItem(-1, "")
            for (item in lines) {
                val line = item.trim()
                if (line.isEmpty()) continue
                val matcher = pattern.matcher(line)
                if (matcher.matches()) {
                    val minutes = matcher.group(1)!!.toLong()
                    val seconds = matcher.group(2)!!.toLong()
                    val milliseconds = matcher.group(3)!!.toLong() * 10
                    val position = (minutes * 60 + seconds) * 1000 + milliseconds
                    val text: String = matcher.group(4)!!.trim()
                    if (text.isNotEmpty()) {
                        if (text.length > maxLengthText.length) maxLengthText = text
                        items += LineItem(position, text)
                    }
                }
            }
            // 后二空行
            items += LineItem(Long.MAX_VALUE - 1, "")
            items += LineItem(Long.MAX_VALUE, "")
            // 排序歌词时间顺序
            if (items.size < 11) throw Exception()
            items.sortWith { o1, o2 -> o1.position.compareTo(o2.position) }
            // 根据歌词最大宽度来调整字体大小
            paintMain = LinePaint(context, true, R.color.music_lyrics_main)
            paintSecond = LinePaint(context, false, R.color.music_lyrics_second)
            paintFade = LinePaint(context, false, R.color.music_lyrics_fade)
            paintMain.adjustTextSizeAndTextHeight(canvasWidth * 0.85f, maxLengthText)
            paintSecond.setTextSizeAndTextHeight(paintMain.textSize * 0.8f)
            paintSecond.adjustShadow(paintMain) { value -> value * 0.75f }
            paintFade.setTextSizeAndTextHeight(paintMain.textSize * 0.7f)
            paintFade.adjustShadow(paintMain) { value -> value * 0.5f }
        } catch (e: Exception) {
            items.clear()
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