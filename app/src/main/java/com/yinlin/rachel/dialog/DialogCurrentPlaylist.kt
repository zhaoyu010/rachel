package com.yinlin.rachel.dialog

import android.graphics.Paint
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.IMusicInfoMap
import com.yinlin.rachel.data.MusicInfo
import com.yinlin.rachel.data.Playlist
import com.yinlin.rachel.databinding.DialogCurrentPlaylistBinding
import com.yinlin.rachel.databinding.ItemPlaylistBinding
import com.yinlin.rachel.fragment.FragmentMusic
import com.yinlin.rachel.interceptScroll
import com.yinlin.rachel.model.RachelAdapter
import com.yinlin.rachel.model.RachelDialog
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.rachelClick
import com.yinlin.rachel.textColor


class DialogCurrentPlaylist(fragment: FragmentMusic,
                            private val currentPlaylist: Playlist,
                            private val musicInfos: IMusicInfoMap,
                            private val currentMusicInfo: MusicInfo)
    : RachelDialog<DialogCurrentPlaylistBinding, FragmentMusic>(fragment, 0.6f) {
    class Adapter(private val dialog: DialogCurrentPlaylist)
        : RachelAdapter<ItemPlaylistBinding, String>(dialog.currentPlaylist.items) {
        private val pages = dialog.root.pages

        override fun bindingClass() = ItemPlaylistBinding::class.java

        override fun update(v: ItemPlaylistBinding, item: String, position: Int) {
            val musicInfo: MusicInfo? = dialog.musicInfos[item]
            if (musicInfo == null) {
                v.name.text = item
                v.name.textColor = pages.getResColor(R.color.red)
                v.name.paintFlags = v.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                v.singer.text = ""
            } else {
                v.name.text = musicInfo.name
                v.name.textColor = pages.getResColor(if (dialog.currentMusicInfo === musicInfo) R.color.steel_blue else R.color.black)
                v.name.paintFlags = v.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                v.singer.text = musicInfo.singer
                v.singer.textColor = pages.getResColor(if (dialog.currentMusicInfo === musicInfo) R.color.steel_blue else R.color.black)
            }
        }

        override fun onItemClicked(v: ItemPlaylistBinding, item: String, position: Int) {
            if (dialog.musicInfos[item] != null) {
                dialog.dismiss()
                pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_GOTO_MUSIC, item)
            }
            else XToastUtils.warning(pages.getResString(R.string.no_audio_source))
        }
    }

    private var adapter = Adapter(this)

    override fun bindingClass() = DialogCurrentPlaylistBinding::class.java

    override fun init() {
        v.title.text = currentPlaylist.name

        v.buttonStop.rachelClick {
            dismiss()
            root.pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_STOP_PLAYER)
        }

        v.list.layoutManager = LinearLayoutManager(root.pages.context)
        v.list.adapter = adapter
        v.list.interceptScroll()
    }
}