package com.yinlin.rachel.data

import com.yinlin.rachel.div
import com.yinlin.rachel.model.RachelMod
import com.yinlin.rachel.pathMusic

class MusicInfo {
    val version: String? = null // 版本号
    val author: String? = null // 作者
    val id: String? = null // 编号
    val name: String? = null // 歌名
    val singer: String? = null // 歌手
    val lyricist: String? = null // 词作
    val composer: String? = null // 曲作
    val album: String? = null // 专辑
    val bgd: Boolean? = null // 是否有动态背景
    val video: Boolean? = null // 是否有MV
    // 拓展属性
    var lyrics: Lyrics? = null // 歌词

    val isCorrect: Boolean get() {
        if (id == null || bgd == null || video == null) return false
        if (!audioPath.exists()) return false
        if (!recordPath.exists()) return false
        if (!lyricsPath.exists()) return false
        if (!bgsPath.exists()) return false
        if (bgd && !bgdPath.exists()) return false
        if (video && !videoPath.exists()) return false
        return true
    }

    val infoPath get() = pathMusic / (id + RachelMod.RES_INFO)
    val audioPath get() = pathMusic / (id + RachelMod.RES_AUDIO)
    val recordPath get() = pathMusic / (id + RachelMod.RES_RECORD)
    val lyricsPath get() = pathMusic / (id + RachelMod.RES_LYRICS)
    val videoPath get() = pathMusic / (id + RachelMod.RES_VIDEO)
    val bgsPath get() = pathMusic / (id + RachelMod.RES_BGS)
    val bgdPath get() = pathMusic / (id + RachelMod.RES_BGD)
}