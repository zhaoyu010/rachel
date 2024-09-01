package com.yinlin.rachel.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.yinlin.rachel.data.Lyrics
import com.yinlin.rachel.model.engine.LineLyricsEngine
import com.yinlin.rachel.model.engine.LyricsEngine


class LyricsView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : View(context, attrs, defStyleAttr) {
    private val engineMap = if (!isInEditMode) linkedMapOf(LineLyricsEngine.NAME to LineLyricsEngine(context)) else LinkedHashMap() // 引擎集
    private var updatePosition: Long = -1L // 更新位置
    private var engine: LyricsEngine? = null // 当前引擎

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (updatePosition >= 0) engine?.update(canvas, updatePosition)
    }

    fun hasEngine(name: String) = engineMap.containsKey(name)

    fun switchEngine(lyrics: Lyrics, name: String): Boolean {
        stopLyrics()
        val findEngine: LyricsEngine? = engineMap[name]
        val lyricsSource: List<String>? = lyrics.items[name]
        if (findEngine != null && lyricsSource != null &&
            findEngine.load(width, height, lyricsSource)) {
            engine = findEngine
            return true
        }
        return false
    }

    val hotpots: List<Long> get () = engine?.getHotpots() ?: ArrayList()

    fun loadLyrics(lyrics: Lyrics): Boolean {
        stopLyrics()
        for ((key, value) in lyrics.items) {
            val findEngine: LyricsEngine? = engineMap[key]
            if (findEngine != null && findEngine.load(width, height, value)) {
                engine = findEngine
                return true
            }
        }
        return false
    }

    fun stopLyrics() {
        engine?.clear()
        engine = null
        updatePosition = -1
        invalidate()
    }

    fun updateLyrics(position: Long) {
        if (engine?.needUpdate(position) == true) {
            updatePosition = position
            invalidate()
        }
    }
}