package com.yinlin.rachel.model.engine

import android.graphics.SurfaceTexture
import android.view.TextureView


interface LyricsEngine {
    val name: String
    val ext: String
    fun load(texture: TextureView, surface: SurfaceTexture, width: Int, height: Int, file: String): Boolean // 载入歌词
    fun clear() // 清除歌词
    fun release() // 释放引擎
    fun needUpdate(position: Long): Boolean // 是否需要更新
    fun update(position: Long) // 更新歌词
}