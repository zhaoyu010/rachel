package com.yinlin.rachel.model.engine

import android.graphics.Canvas


interface LyricsEngine {
    fun getName(): String // 引擎名称
    fun getHotpots(): List<Long> // 副歌集
    fun load(canvasWidth: Int, canvasHeight: Int, lines: List<String>): Boolean // 载入歌词
    fun clear() // 清除歌词
    fun needUpdate(position: Long): Boolean // 是否需要更新
    fun update(canvas: Canvas, position: Long) // 更新歌词
}