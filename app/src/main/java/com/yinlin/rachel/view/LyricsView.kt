package com.yinlin.rachel.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.TextureView
import android.widget.FrameLayout
import com.yinlin.rachel.data.MusicInfo
import com.yinlin.rachel.div
import com.yinlin.rachel.model.engine.LyricsEngine
import com.yinlin.rachel.model.engine.LyricsEngineFactory
import com.yinlin.rachel.pathMusic

class LyricsView(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
    : FrameLayout(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener {
    private var lyricsEngine: LyricsEngine? = null
    private var lyricsFileName: String = ""
    private var isEngineLoad: Boolean = false

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context): this(context, null)

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        println("onSurfaceTextureAvailable")
        lyricsEngine?.let {
            if (childCount != 0) {
                if (it.load(getChildAt(0) as TextureView, surface, width, height, lyricsFileName)) isEngineLoad = true
                else releaseEngine()
            }
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) { }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) { }

    private fun addTexture() {
        if (childCount != 0) removeAllViews()
        val textureView = TextureView(context)
        textureView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        textureView.isOpaque = false
        textureView.surfaceTextureListener = this
        addView(textureView)
    }

    // 加载歌词引擎
    fun loadEngine(musicInfo: MusicInfo): Boolean {
        val lyrics = musicInfo.lyrics
        // 先检查当前歌词引擎是否合适
        if (lyricsEngine != null) {
            // 检查歌曲是否含有当前歌词引擎
            if (lyrics.containsKey(lyricsEngine?.name)) lyricsEngine?.clear()
            else releaseEngine()
        }
        if (lyricsEngine == null) {
            // 寻找可以使用的歌词引擎
            for ((engineName, _) in lyrics) {
                if (LyricsEngineFactory.hasEngine(engineName)) {
                    lyricsEngine = LyricsEngineFactory.newEngine(engineName)
                    break
                }
            }
        }
        if (lyricsEngine == null) return false
        else {
            val firstLyrics = lyrics[lyricsEngine?.name]?.first() ?: ""
            lyricsFileName = (pathMusic / "${musicInfo.id}${if(firstLyrics.isEmpty()) "" else "_"}${firstLyrics}${lyricsEngine?.ext}").absolutePath
            addTexture()
            return true
        }
    }

    // 切换歌词引擎
    fun switchEngine(musicInfo: MusicInfo, engineName: String, name: String): Boolean {
        // 先检查当前歌词引擎是否合适
        if (lyricsEngine != null) {
            // 检查是否需要更换引擎
            if (lyricsEngine?.name != engineName) releaseEngine()
            else lyricsEngine?.clear()
        }
        if (lyricsEngine == null) lyricsEngine = LyricsEngineFactory.newEngine(engineName)
        if (lyricsEngine == null) return false
        else {
            lyricsFileName = (pathMusic / "${musicInfo.id}${if(name.isEmpty()) "" else "_"}${name}${lyricsEngine?.ext}").absolutePath
            addTexture()
            return true
        }
    }

    fun releaseEngine() {
        if (lyricsEngine != null) {
            lyricsEngine?.release()
            lyricsEngine = null
            isEngineLoad = false
            lyricsFileName = ""
            removeAllViews()
        }
    }

    fun update(position: Long) {
        if (isEngineLoad && lyricsEngine?.needUpdate(position) == true) lyricsEngine?.update(position)
    }
}