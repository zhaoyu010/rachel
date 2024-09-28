package com.yinlin.rachel.dialog

import android.annotation.SuppressLint
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.LyricsInfoList
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.bold
import com.yinlin.rachel.clearAddAll
import com.yinlin.rachel.data.LyricsInfo
import com.yinlin.rachel.databinding.BottomDialogLyricsInfoBinding
import com.yinlin.rachel.databinding.ItemLyricsInfoBinding
import com.yinlin.rachel.fragment.FragmentMusic
import com.yinlin.rachel.interceptScroll
import com.yinlin.rachel.model.RachelAdapter
import com.yinlin.rachel.model.RachelBottomDialog
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.textColor


class BottomDialogLyricsInfo(fragment: FragmentMusic) : RachelBottomDialog<BottomDialogLyricsInfoBinding, FragmentMusic>(fragment, 0.5f) {
    class Adapter(private val dialog: BottomDialogLyricsInfo) : RachelAdapter<ItemLyricsInfoBinding, LyricsInfo>() {
        private val pages = dialog.root.pages

        override fun bindingClass() = ItemLyricsInfoBinding::class.java

        override fun update(v: ItemLyricsInfoBinding, item: LyricsInfo, position: Int) {
            v.engineName.text = item.engineName
            v.name.text = item.name
            if (item.available) {
                v.available.text = pages.getResString(R.string.unlocked)
                v.available.textColor = pages.getResColor(R.color.sea_green)
            } else {
                v.available.text = pages.getResString(R.string.locked)
                v.available.textColor = pages.getResColor(R.color.red)
            }
        }

        override fun onItemClicked(v: ItemLyricsInfoBinding, item: LyricsInfo, position: Int) {
            dialog.hide()
            if (item.available) {
                pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_USE_LYRICS_ENGINE, item.engineName, item.name)
            }
            else XToastUtils.warning("未解锁该歌词引擎")
        }
    }

    private val adapter = Adapter(this)

    override fun bindingClass() = BottomDialogLyricsInfoBinding::class.java

    override fun init() {
        super.init()

        v.title.bold = true

        v.list.layoutManager = LinearLayoutManager(root.pages.context)
        v.list.adapter = adapter

        v.list.interceptScroll()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(engineNames: LyricsInfoList): BottomDialogLyricsInfo {
        v.list.scrollToPosition(0)
        adapter.items.clearAddAll(engineNames)
        adapter.notifyDataSetChanged()
        return this
    }
}