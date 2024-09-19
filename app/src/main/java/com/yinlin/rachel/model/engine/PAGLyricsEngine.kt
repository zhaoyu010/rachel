package com.yinlin.rachel.model.engine

import android.graphics.SurfaceTexture
import android.view.TextureView
import org.libpag.PAGFile
import org.libpag.PAGPlayer
import org.libpag.PAGScaleMode
import org.libpag.PAGSurface


// PAG动态歌词引擎
class PAGLyricsEngine : LyricsEngine {
    companion object { const val NAME = "pag" }
    override val name: String = NAME
    override val ext: String = ".pag"

    private val player = PAGPlayer()

    init {
        player.setCacheEnabled(true)
        player.setUseDiskCache(true)
        player.setScaleMode(PAGScaleMode.Zoom)
    }

    override fun load(texture: TextureView, surface: SurfaceTexture, width: Int, height: Int, file: String): Boolean {
        player.composition = PAGFile.Load(file)
        player.surface = PAGSurface.FromSurfaceTexture(surface)
        player.prepare()
        return player.composition != null && player.surface != null
    }

    override fun clear() {
        if (player.surface != null) player.surface.release()
        player.surface = null
        player.composition = null
    }

    override fun release() {
        clear()
        player.release()
    }

    override fun needUpdate(position: Long): Boolean = true

    override fun update(position: Long) {
        if (player.surface != null) {
            player.progress = position.toDouble() * 1000 / player.duration()
            player.flush()
        }
    }
}