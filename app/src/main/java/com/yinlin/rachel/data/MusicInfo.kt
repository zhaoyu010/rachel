package com.yinlin.rachel.data

import com.yinlin.rachel.ChorusList
import com.yinlin.rachel.LyricsFileMap
import com.yinlin.rachel.div
import com.yinlin.rachel.model.RachelMod
import com.yinlin.rachel.model.engine.LineLyricsEngine
import com.yinlin.rachel.model.engine.LineLyricsEngine.LineItem
import com.yinlin.rachel.pathMusic
import com.yinlin.rachel.readText
import java.util.regex.Pattern

data class MusicInfo (
    val version: String = "", // 版本号
    val author: String = "", // 作者
    val id: String = "", // 编号
    val name: String = "", // 歌名
    val singer: String = "", // 歌手
    val lyricist: String = "", // 词作
    val composer: String = "", // 曲作
    val album: String = "", // 专辑
    val bgd: Boolean = false, // 是否有动态背景
    val video: Boolean = false, // 是否有MV
    val chorus: ChorusList = emptyList(), // 副歌点
    var lyrics: LyricsFileMap = emptyMap(), // 歌词引擎
    // 拓展属性
    var lyricsText: String = "", // 歌词文本
) {
    val isCorrect: Boolean get() {
        if (id.isEmpty()) return false
        if (!audioPath.exists()) return false
        if (!recordPath.exists()) return false
        if (!bgsPath.exists()) return false
        if (bgd && !bgdPath.exists()) return false
        if (video && !videoPath.exists()) return false
        if (lyrics.isEmpty()) return false
        val lineEngine = lyrics[LineLyricsEngine.NAME]
        if (lineEngine.isNullOrEmpty() || !lineEngine.contains("")) return false
        return true
    }

    val infoPath get() = pathMusic / (id + RachelMod.RES_INFO)
    val audioPath get() = pathMusic / (id + RachelMod.RES_AUDIO)
    val recordPath get() = pathMusic / (id + RachelMod.RES_RECORD)
    val defaultLrcPath get() = pathMusic / (id + RachelMod.RES_DEFAULT_LRC)
    val videoPath get() = pathMusic / (id + RachelMod.RES_VIDEO)
    val bgsPath get() = pathMusic / (id + RachelMod.RES_BGS)
    val bgdPath get() = pathMusic / (id + RachelMod.RES_BGD)

    fun parseLyricsText() {
        try {
            val sb = StringBuilder()
            val pattern: Pattern = Pattern.compile("\\[(\\d{2}):(\\d{2}).(\\d{2})](.*)")
            for (item in defaultLrcPath.readText().split("\\r?\\n".toRegex())) {
                val line = item.trim()
                if (line.isEmpty()) continue
                val matcher = pattern.matcher(line)
                if (matcher.matches()) sb.append(matcher.group(4)?.trim()).append('\n')
            }
            lyricsText = sb.toString()
        }
        catch (ignored: Exception) {
            lyricsText = ""
        }
    }
}