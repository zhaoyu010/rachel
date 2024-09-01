package com.yinlin.rachel.dialog

import androidx.recyclerview.widget.LinearLayoutManager
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelFont
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.databinding.DialogLyricsEngineBinding
import com.yinlin.rachel.databinding.ItemLyricsEngineBinding
import com.yinlin.rachel.fragment.FragmentMusic
import com.yinlin.rachel.interceptScroll
import com.yinlin.rachel.model.RachelAdapter
import com.yinlin.rachel.model.RachelDialog
import com.yinlin.rachel.model.RachelPages


class DialogLyricsEngine(fragment: FragmentMusic, private val engineNames: MutableList<String>)
    : RachelDialog<DialogLyricsEngineBinding, FragmentMusic>(fragment, 0.5f) {
    class Adapter(private val dialog: DialogLyricsEngine, engineNames: MutableList<String>)
        : RachelAdapter<ItemLyricsEngineBinding, String>(engineNames) {
        override fun bindingClass() = ItemLyricsEngineBinding::class.java

        override fun update(v: ItemLyricsEngineBinding, item: String, position: Int) {
            v.name.text = item
            if (dialog.root.v.lyrics.hasEngine(item)) {
                v.locked.text = dialog.root.pages.getResString(R.string.unlocked)
                v.locked.setTextColor(dialog.root.pages.getResColor(R.color.sea_green))
            } else {
                v.locked.text = dialog.root.pages.getResString(R.string.locked)
                v.locked.setTextColor(dialog.root.pages.getResColor(R.color.red))
            }
        }

        override fun onItemClicked(v: ItemLyricsEngineBinding, item: String, position: Int) {
            dialog.dismiss()
            if (dialog.root.v.lyrics.hasEngine(item))
                dialog.root.pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_USE_LYRICS_ENGINE, item)
            else XToastUtils.warning("未解锁该歌词引擎")
        }
    }

    private val adapter = Adapter(this, engineNames)

    override fun bindingClass() = DialogLyricsEngineBinding::class.java

    override fun init() {
        v.title.typeface = RachelFont.bold

        v.list.layoutManager = LinearLayoutManager(root.pages.context)
        v.list.adapter = adapter

        v.list.interceptScroll()
    }
}