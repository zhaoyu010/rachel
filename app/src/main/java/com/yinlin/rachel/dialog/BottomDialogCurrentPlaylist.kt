package com.yinlin.rachel.dialog

import android.annotation.SuppressLint
import android.graphics.Paint
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.data.MusicInfo
import com.yinlin.rachel.data.Playlist
import com.yinlin.rachel.databinding.BottomDialogCurrentPlaylistBinding
import com.yinlin.rachel.databinding.ItemPlaylistBinding
import com.yinlin.rachel.fragment.FragmentMusic
import com.yinlin.rachel.interceptScroll
import com.yinlin.rachel.model.RachelAdapter
import com.yinlin.rachel.model.RachelBottomDialog
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.rachelClick
import com.yinlin.rachel.textColor


class BottomDialogCurrentPlaylist(fragment: FragmentMusic) : RachelBottomDialog<BottomDialogCurrentPlaylistBinding, FragmentMusic>(fragment, 0.6f) {
    class Adapter(private val dialog: BottomDialogCurrentPlaylist) : RachelAdapter<ItemPlaylistBinding, String>() {
        private val pages = dialog.root.pages
        private val musicInfos = dialog.root.musicInfos
        internal var currentMusicInfo = MusicInfo()

        override fun bindingClass() = ItemPlaylistBinding::class.java

        override fun update(v: ItemPlaylistBinding, item: String, position: Int) {
            val musicInfo: MusicInfo? = musicInfos[item]
            if (musicInfo == null) {
                v.name.text = item
                v.name.textColor = pages.getResColor(R.color.red)
                v.name.paintFlags = v.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                v.singer.text = ""
                v.singer.textColor = pages.getResColor(R.color.gray)
            } else {
                v.name.text = musicInfo.name
                v.name.textColor = pages.getResColor(if (currentMusicInfo == musicInfo) R.color.steel_blue else R.color.black)
                v.name.paintFlags = v.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                v.singer.text = musicInfo.singer
                v.singer.textColor = pages.getResColor(if (currentMusicInfo == musicInfo) R.color.steel_blue else R.color.gray)
            }
        }

        override fun onItemClicked(v: ItemPlaylistBinding, item: String, position: Int) {
            if (musicInfos[item] != null) {
                dialog.hide()
                pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_GOTO_MUSIC, item)
            }
            else XToastUtils.warning(pages.getResString(R.string.no_audio_source))
        }
    }

    private var adapter = Adapter(this)

    override fun bindingClass() = BottomDialogCurrentPlaylistBinding::class.java

    override fun init() {
        super.init()

        v.buttonStop.rachelClick {
            hide()
            root.pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_STOP_PLAYER)
        }

        v.list.layoutManager = LinearLayoutManager(root.pages.context)
        v.list.recycledViewPool.setMaxRecycledViews(0, 15)
        v.list.adapter = adapter
        v.list.interceptScroll()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(currentPlaylist: Playlist, currentMusicInfo: MusicInfo): BottomDialogCurrentPlaylist {
        v.title.text = currentPlaylist.name
        adapter.currentMusicInfo = currentMusicInfo
        adapter.items = currentPlaylist.items
        adapter.notifyDataSetChanged()
        return this
    }
}