package com.yinlin.rachel.model.engine

import android.content.Context
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.view.TextureView
import androidx.annotation.ColorRes
import com.yinlin.rachel.R
import com.yinlin.rachel.bold
import com.yinlin.rachel.readText
import com.yinlin.rachel.textHeight
import com.yinlin.rachel.toDP
import java.io.File
import java.util.regex.Pattern


// 多行文本歌词引擎
class LineLyricsEngine(context: Context) : LyricsEngine {
    @JvmRecord
    data class LineItem(val position: Long, val text: String)

    class TextPaints(context: Context) {
        private var canvasWidth: Float = 512f
        private var canvasHeight: Float = 512f
        private val maxTextHeight: Float = 30f.toDP(context)

        private val main = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = context.getColor(R.color.music_lyrics_main)
            textAlign = Paint.Align.CENTER
            setShadowLayer(2f, 2f, 2f, context.getColor(R.color.dark))
            bold(context, true)
        }

        private val second = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = context.getColor(R.color.music_lyrics_second)
            textAlign = Paint.Align.CENTER
            setShadowLayer(1f, 1f, 1f, context.getColor(R.color.dark))
            bold(context, false)
        }

        private val fade = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = context.getColor(R.color.music_lyrics_fade)
            textAlign = Paint.Align.CENTER
            bold(context, false)
        }

        fun adjustPaint(maxWidth: Float, maxHeight: Float, maxLengthText: String) {
            canvasWidth = maxWidth
            canvasHeight = maxHeight
            val availableWidth = maxWidth * MAX_WIDTH_RATIO
            main.textSize = 18f
            var detectWidth = main.measureText(maxLengthText)
            while (detectWidth < availableWidth) {
                main.textSize += 5f
                if (main.textHeight > maxTextHeight) break
                detectWidth = main.measureText(maxLengthText)
            }
            second.textSize = main.textSize * 0.85f
            fade.textSize = main.textSize * 0.8f
        }

        fun draw(canvas: Canvas, str1: String, str2: String, str3: String, str4: String, str5: String) {
            val x = canvasWidth / 2f
            val y = canvasHeight / 2f
            val gap = maxTextHeight * 1.2f

            canvas.drawColor(Color.TRANSPARENT, BlendMode.CLEAR)
            canvas.drawText(str3, x, y, main)
            canvas.drawText(str2, x, y - gap, second)
            canvas.drawText(str4, x, y + gap, second)
            canvas.drawText(str1, x, y - gap * 2, fade)
            canvas.drawText(str5, x, y + gap * 2, fade)
        }
    }

    companion object {
        const val MAX_WIDTH_RATIO = 0.8f
        const val NAME = "line"
        const val DESCRIPTION = "原生逐行歌词渲染引擎, 自适应字体大小, 自内向外渐隐"
        val ICON: Int = R.drawable.lyrics_engine_line
    }
    override val name: String = NAME
    override val ext: String = ".lrc"

    private val paints = TextPaints(context)
    private var currentIndex: Int = -1
    private var texture: TextureView? = null
    private val items = ArrayList<LineItem>()

    override fun load(texture: TextureView, surface: SurfaceTexture, width: Int, height: Int, file: String): Boolean {
        try {
            items.clear()
            this.texture = texture
            currentIndex = -1
            val maxLengthText = parseLrcText(File(file).readText())
            if (items.isNotEmpty()) {
                paints.adjustPaint(texture.measuredWidth.toFloat(), texture.measuredHeight.toFloat(), maxLengthText)
                return true
            }
        }
        catch (ignored: Exception) {
            clear()
        }
        return false
    }

    override fun clear() {
        items.clear()
        texture = null
        currentIndex = -1
    }

    override fun release() = clear()

    override fun needUpdate(position: Long): Boolean {
        if (items.isNotEmpty()) {
            val index = findIndex(position)
            if (index != currentIndex) {
                currentIndex = index
                return true
            }
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
                    paints.draw(canvas, item1.text, item2.text, item3.text, item4.text, item5.text)
                    unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    private fun parseLrcText(source: String): String {
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
            return maxLengthText
        } catch (e: Exception) {
            items.clear()
            return ""
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