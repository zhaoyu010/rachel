package com.yinlin.rachel.model.engine

import android.content.Context


object LyricsEngineFactory {
    private val engineMap: Map<String, Class<out LyricsEngine>> = mapOf(
        LineLyricsEngine.NAME to LineLyricsEngine::class.java,
        PAGLyricsEngine.NAME to PAGLyricsEngine::class.java,
    )

    fun newEngine(context: Context, name: String): LyricsEngine? = try {
        engineMap[name]?.getDeclaredConstructor(Context::class.java)?.newInstance(context)
    }
    catch (ignored: Exception) { null }

    fun hasEngine(name: String) = engineMap.containsKey(name)

    val engineInfos = listOf(
        LyricsEngineInfo(LineLyricsEngine.NAME, LineLyricsEngine.DESCRIPTION, LineLyricsEngine.ICON),
        LyricsEngineInfo(PAGLyricsEngine.NAME, PAGLyricsEngine.DESCRIPTION, PAGLyricsEngine.ICON)
    )
}