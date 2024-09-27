package com.yinlin.rachel.dialog

import androidx.recyclerview.widget.LinearLayoutManager
import com.yinlin.rachel.bold
import com.yinlin.rachel.clearAddAll
import com.yinlin.rachel.databinding.BottomDialogLyricsEngineBinding
import com.yinlin.rachel.databinding.ItemLyricsEngineBinding
import com.yinlin.rachel.fragment.FragmentMusic
import com.yinlin.rachel.interceptScroll
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelAdapter
import com.yinlin.rachel.model.RachelBottomDialog
import com.yinlin.rachel.model.engine.LyricsEngineFactory
import com.yinlin.rachel.model.engine.LyricsEngineInfo

class BottomDialogLyricsEngine(fragment: FragmentMusic) : RachelBottomDialog<BottomDialogLyricsEngineBinding, FragmentMusic>(fragment, 0.8f) {
    class Adapter(private val dialog: BottomDialogLyricsEngine) : RachelAdapter<ItemLyricsEngineBinding, LyricsEngineInfo>() {
        override fun bindingClass() = ItemLyricsEngineBinding::class.java
        init { items.clearAddAll(LyricsEngineFactory.engineInfos) }

        override fun init(holder: RachelViewHolder<ItemLyricsEngineBinding>, v: ItemLyricsEngineBinding) {
            v.name.bold = true
        }

        override fun update(v: ItemLyricsEngineBinding, item: LyricsEngineInfo, position: Int) {
            v.name.text = item.name
            v.description.text = item.description
            v.icon.load(dialog.root.pages.ril, item.icon)
        }
    }

    private val adapter = Adapter(this)

    override fun bindingClass() = BottomDialogLyricsEngineBinding::class.java

    override fun init() {
        super.init()

        v.title.bold = true

        v.list.layoutManager = LinearLayoutManager(root.pages.context)
        v.list.adapter = adapter

        v.list.interceptScroll()
    }

    fun update(): BottomDialogLyricsEngine {
        v.list.scrollToPosition(0)
        return this
    }
}