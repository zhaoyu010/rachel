package com.yinlin.rachel.dialog

import android.annotation.SuppressLint
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.bold
import com.yinlin.rachel.databinding.DialogLyricsEngineBinding
import com.yinlin.rachel.databinding.ItemLyricsEngineBinding
import com.yinlin.rachel.fragment.FragmentMusic
import com.yinlin.rachel.interceptScroll
import com.yinlin.rachel.model.RachelAdapter
import com.yinlin.rachel.model.RachelBottomDialog
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.textColor


class DialogLyricsEngine(fragment: FragmentMusic) : RachelBottomDialog<DialogLyricsEngineBinding, FragmentMusic>(fragment, 0.5f) {
    class Adapter(private val dialog: DialogLyricsEngine) : RachelAdapter<ItemLyricsEngineBinding, String>() {
        private val pages = dialog.root.pages

        override fun bindingClass() = ItemLyricsEngineBinding::class.java

        override fun update(v: ItemLyricsEngineBinding, item: String, position: Int) {
            v.name.text = item
            if (dialog.root.v.lyrics.hasEngine(item)) {
                v.locked.text = pages.getResString(R.string.unlocked)
                v.locked.textColor = pages.getResColor(R.color.sea_green)
            } else {
                v.locked.text = pages.getResString(R.string.locked)
                v.locked.textColor = pages.getResColor(R.color.red)
            }
        }

        override fun onItemClicked(v: ItemLyricsEngineBinding, item: String, position: Int) {
            dialog.hide()
            if (dialog.root.v.lyrics.hasEngine(item)) {
                pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_USE_LYRICS_ENGINE, item)
            }
            else XToastUtils.warning("未解锁该歌词引擎")
        }
    }

    private val adapter = Adapter(this)

    override fun bindingClass() = DialogLyricsEngineBinding::class.java

    override fun init() {
        super.init()

        v.title.bold = true

        v.list.layoutManager = LinearLayoutManager(root.pages.context)
        v.list.adapter = adapter

        v.list.interceptScroll()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(engineNames: MutableList<String>): DialogLyricsEngine {
        adapter.items = engineNames
        adapter.notifyDataSetChanged()
        return this
    }
}