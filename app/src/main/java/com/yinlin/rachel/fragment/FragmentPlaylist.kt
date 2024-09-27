package com.yinlin.rachel.fragment

import android.annotation.SuppressLint
import android.graphics.Paint
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import com.xuexiang.xui.widget.dialog.materialdialog.simplelist.MaterialSimpleListAdapter
import com.xuexiang.xui.widget.dialog.materialdialog.simplelist.MaterialSimpleListItem
import com.xuexiang.xui.widget.tabbar.TabSegment
import com.yinlin.rachel.IMusicInfoMap
import com.yinlin.rachel.IPlaylistMap
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.data.Playlist
import com.yinlin.rachel.databinding.FragmentPlaylistBinding
import com.yinlin.rachel.databinding.ItemPlaylistBinding
import com.yinlin.rachel.model.RachelAdapter
import com.yinlin.rachel.model.RachelDialog
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.rachelClick
import com.yinlin.rachel.textColor


class FragmentPlaylist(pages: RachelPages, musicInfos: IMusicInfoMap,
    private val playlists: IPlaylistMap, private val currentPlaylist: Playlist?)
    : RachelFragment<FragmentPlaylistBinding>(pages),
    TabSegment.OnTabSelectedListener, MaterialSimpleListAdapter.OnItemClickListener {
    class Adapter(private val pages: RachelPages, private val musicInfos: IMusicInfoMap)
        : RachelAdapter<ItemPlaylistBinding, String>() {
        internal var selectedPlaylist: Playlist? = null

        override fun bindingClass() = ItemPlaylistBinding::class.java

        override fun update(v: ItemPlaylistBinding, item: String, position: Int) {
            val musicInfo = musicInfos[item]
            if (musicInfo == null) {
                v.name.text = item
                v.name.textColor = pages.getResColor(R.color.red)
                v.name.paintFlags = v.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                v.singer.text = ""
            } else {
                v.name.text = musicInfo.name
                v.name.textColor = pages.getResColor(R.color.black)
                v.name.paintFlags = v.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                v.singer.text = musicInfo.singer
            }
        }

        override fun onItemLongClicked(v: ItemPlaylistBinding, item: String, position: Int) {
            // 删除歌单中的歌曲
            val musicInfo = musicInfos[item]
            // 若无音源显示未知, 歌曲被删除后仍然可以显示在歌单中
            val name = musicInfo?.name ?: item
            selectedPlaylist?.apply {
                RachelDialog.confirm(pages.context, "是否从歌单\"${this.name}\"中删除\"$name\"?") {
                    pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_DELETE_MUSIC_FROM_PLAYLIST, this, position)
                    notifyItemRemoved(position)
                }
            }
        }
    }

    private var adapter = Adapter(pages, musicInfos)
    private val editDialog: MaterialDialog

    init {
        // 歌单操作对话框
        val editAdapter = MaterialSimpleListAdapter(arrayListOf(
            MaterialSimpleListItem.Builder(pages.context).content("播放").icon(R.drawable.svg_play_blue).build(),
            MaterialSimpleListItem.Builder(pages.context).content("重命名").icon(R.drawable.svg_edit_blue).build(),
            MaterialSimpleListItem.Builder(pages.context).content("删除").icon(R.drawable.svg_delete_blue).build(),
            MaterialSimpleListItem.Builder(pages.context).content("分享").icon(R.drawable.svg_share_blue).build()
        )).setOnItemClickListener(this)
        editDialog = MaterialDialog.Builder(pages.context).adapter(editAdapter, null).build()
    }

    override fun bindingClass() = FragmentPlaylistBinding::class.java

    override fun init() {
        // 创建歌单
        v.buttonAdd.rachelClick {
            RachelDialog.input(pages.context, "请输入新歌单名称", 10) { createPlaylist(it) }
        }

        // 列表
        v.list.layoutManager = LinearLayoutManager(pages.context)
        v.list.setHasFixedSize(true)
        v.list.recycledViewPool.setMaxRecycledViews(0, 20)
        v.list.adapter = adapter

        // TAB
        v.tab.addOnTabSelectedListener(this)
        updatePlaylist()
    }

    override fun quit() {
        v.tab.removeOnTabSelectedListener(this)
    }

    override fun back() = true

    @SuppressLint("NotifyDataSetChanged")
    override fun onTabSelected(index: Int) {
        val name = v.tab.getTab(index).text.toString()
        playlists[name]?.apply {
            adapter.selectedPlaylist = this
            adapter.items = this.items
            adapter.notifyDataSetChanged()
        }
    }

    override fun onTabReselected(index: Int) {
        if (!editDialog.isShowing) editDialog.show()
    }

    override fun onTabUnselected(index: Int) { }
    override fun onDoubleTap(index: Int) { }

    override fun onMaterialListItemSelected(editDialog: MaterialDialog, index: Int, item: MaterialSimpleListItem) {
        editDialog.dismiss()
        when (item.content.toString()) {
            "播放" -> {
                pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_START_PLAYER, adapter.selectedPlaylist)
                pages.pop()
            }
            "重命名" -> RachelDialog.input(pages.context, "请输入歌单名称", 10) {
                if (pages.sendMessageForResult<Boolean>(RachelPages.music, RachelMessage.MUSIC_RENAME_PLAYLIST, adapter.selectedPlaylist, it)!!) {
                    XToastUtils.success("修改成功")
                    v.tab.getTab(v.tab.selectedIndex).text = it
                    v.tab.notifyDataChanged()
                }
                else XToastUtils.warning("歌单已存在或名称不合法")
            }
            "删除" -> RachelDialog.confirm(pages.context, "是否删除歌单\"${adapter.selectedPlaylist!!.name}\"") {
                pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_DELETE_PLAYLIST, adapter.selectedPlaylist)
                updatePlaylist()
            }
            "分享" -> {}
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updatePlaylist() {
        v.tab.reset()
        var currentTabIndex = 0
        for ((index, name) in playlists.keys.withIndex()) {
            v.tab.addTab(TabSegment.Tab(name))
            currentPlaylist?.apply { if (name == this.name) currentTabIndex = index }
        }
        if (playlists.isNotEmpty()) v.tab.selectTab(currentTabIndex)
        else {
            adapter.items = ArrayList()
            adapter.selectedPlaylist = null
            adapter.notifyDataSetChanged()
            v.tab.notifyDataChanged()
        }
    }

    private fun createPlaylist(newName: String) {
        if (pages.sendMessageForResult<Boolean>(RachelPages.music, RachelMessage.MUSIC_CREATE_PLAYLIST, newName)!!) {
            XToastUtils.success("创建成功")
            v.tab.addTab(TabSegment.Tab(newName))
            v.tab.selectTab(playlists.size - 1)
        }
        else XToastUtils.warning("歌单已存在或名称不合法")
    }
}