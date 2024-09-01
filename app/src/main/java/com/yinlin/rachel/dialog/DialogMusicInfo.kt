package com.yinlin.rachel.dialog

import com.yinlin.rachel.RachelFont
import com.yinlin.rachel.data.MusicInfo
import com.yinlin.rachel.databinding.DialogMusicInfoBinding
import com.yinlin.rachel.fragment.FragmentMusic
import com.yinlin.rachel.interceptScroll
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelDialog

class DialogMusicInfo(fragment: FragmentMusic, private val musicInfo: MusicInfo)
    : RachelDialog<DialogMusicInfoBinding, FragmentMusic>(fragment, 0.7f) {

    override fun bindingClass() = DialogMusicInfoBinding::class.java

    override fun init() {
        v.name.text = musicInfo.name
        v.name.typeface = RachelFont.bold
        v.version.text = "v ${musicInfo.version}"
        v.id.text = "ID: ${musicInfo.id}"
        v.pic.load(root.pages.ril, musicInfo.recordPath)
        v.singer.text = "演唱: ${musicInfo.singer}"
        v.lyricist.text = "作词: ${musicInfo.lyricist}"
        v.composer.text = "作曲: ${musicInfo.composer}"
        v.album.text = "专辑分类: ${musicInfo.album}"
        v.author.text = "MOD作者: ${musicInfo.author}"
        v.an.isChecked = musicInfo.bgd!!
        v.an.isEnabled = false
        v.mv.isChecked = musicInfo.video!!
        v.mv.isEnabled = false
        musicInfo.lyrics?.apply { v.lyrics.text = this.plaintext }
        v.lyricsContainer.interceptScroll()
    }
}