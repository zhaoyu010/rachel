package com.yinlin.rachel.dialog

import com.yinlin.rachel.bold
import com.yinlin.rachel.data.MusicInfo
import com.yinlin.rachel.databinding.BottomDialogMusicInfoBinding
import com.yinlin.rachel.fragment.FragmentMusic
import com.yinlin.rachel.interceptScroll
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelBottomDialog

class BottomDialogMusicInfo(fragment: FragmentMusic) : RachelBottomDialog<BottomDialogMusicInfoBinding, FragmentMusic>(fragment, 0.7f) {
    private var currentMusicInfo = MusicInfo()

    override fun bindingClass() = BottomDialogMusicInfoBinding::class.java

    override fun init() {
        super.init()
        v.lyricsContainer.interceptScroll()
        v.name.bold = true
        v.an.isEnabled = false
        v.mv.isEnabled = false
    }

    fun update(musicInfo: MusicInfo): BottomDialogMusicInfo {
        if (currentMusicInfo != musicInfo) {
            currentMusicInfo = musicInfo
            v.name.text = musicInfo.name
            v.version.text = "v ${musicInfo.version}"
            v.id.text = "ID: ${musicInfo.id}"
            v.pic.load(root.pages.ril, musicInfo.recordPath)
            v.singer.text = "演唱: ${musicInfo.singer}"
            v.lyricist.text = "作词: ${musicInfo.lyricist}"
            v.composer.text = "作曲: ${musicInfo.composer}"
            v.album.text = "专辑分类: ${musicInfo.album}"
            v.author.text = "MOD作者: ${musicInfo.author}"
            v.an.isChecked = musicInfo.bgd
            v.mv.isChecked = musicInfo.video
            v.lyrics.text = musicInfo.lyricsText
            v.lyricsContainer.scrollTo(0, 0)
        }
        return this
    }
}