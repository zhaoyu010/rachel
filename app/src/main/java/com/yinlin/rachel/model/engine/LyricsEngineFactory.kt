package com.yinlin.rachel.model.engine


object LyricsEngineFactory {
    private val engineMap: Map<String, Class<out LyricsEngine>> = mapOf(
        LineLyricsEngine.NAME to LineLyricsEngine::class.java,
        PAGLyricsEngine.NAME to PAGLyricsEngine::class.java,
    )

    fun newEngine(name: String): LyricsEngine? = try {
        engineMap[name]?.getDeclaredConstructor()?.newInstance()
    }
    catch (ignored: Exception) { null }

    fun hasEngine(name: String) = engineMap.containsKey(name)
}