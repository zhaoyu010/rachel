package com.yinlin.rachel.fragment

import android.annotation.SuppressLint
import android.graphics.Paint
import android.text.InputType
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.reflect.TypeToken
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog.SingleButtonCallback
import com.xuexiang.xui.widget.dialog.materialdialog.simplelist.MaterialSimpleListAdapter
import com.xuexiang.xui.widget.dialog.materialdialog.simplelist.MaterialSimpleListItem
import com.xuexiang.xui.widget.tabbar.TabSegment
import com.yinlin.rachel.Config
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API
import com.yinlin.rachel.api.Arg
import com.yinlin.rachel.clearAddAll
import com.yinlin.rachel.data.MusicInfo
import com.yinlin.rachel.data.Playlist
import com.yinlin.rachel.databinding.FragmentPlaylistBinding
import com.yinlin.rachel.databinding.ItemPlaylistBinding
import com.yinlin.rachel.decodeBase64
import com.yinlin.rachel.encodeBase64
import com.yinlin.rachel.model.RachelAdapter
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelOnClickListener
import com.yinlin.rachel.model.RachelPages


class FragmentPlaylist(pages: RachelPages, private val musicInfos: MutableMap<String, MusicInfo>,
    private val playlists: MutableMap<String, Playlist>, private val currentPlaylist: Playlist?)
    : RachelFragment<FragmentPlaylistBinding>(pages),
    TabSegment.OnTabSelectedListener, MaterialSimpleListAdapter.OnItemClickListener {
    class Adapter(private val pages: RachelPages, private val musicInfos: MutableMap<String, MusicInfo>)
        : RachelAdapter<ItemPlaylistBinding, String>() {
        internal var selectedPlaylist: Playlist? = null

        override fun bindingClass() = ItemPlaylistBinding::class.java

        override fun update(v: ItemPlaylistBinding, item: String, position: Int) {
            val musicInfo = musicInfos[item]
            if (musicInfo == null) {
                v.name.text = item
                v.name.setTextColor(pages.getResColor(R.color.red))
                v.name.paintFlags = v.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                v.singer.text = ""
            } else {
                v.name.text = musicInfo.name
                v.name.setTextColor(pages.getResColor(R.color.black))
                v.name.paintFlags = v.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                v.singer.text = musicInfo.singer
            }
        }

        override fun onItemLongClicked(v: ItemPlaylistBinding, item: String, position: Int) {
            // 删除歌单中的歌曲
            val musicInfo = musicInfos[item]
            // 若无音源显示未知, 歌曲被删除后仍然可以显示在歌单中
            val name = if (musicInfo == null) item else musicInfo.name
            selectedPlaylist?.apply {
                val content = "是否从歌单\"${this.name}\"中删除\"$name\"?"
                MaterialDialog.Builder(pages.context).content(content)
                    .positiveText(R.string.yes).negativeText(R.string.no)
                    .onPositive { _, _ ->
                        val args = arrayOf(this, position)
                        pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_DELETE_MUSIC_FROM_PLAYLIST, args)
                        notifyItemRemoved(position)
                    }.show()
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
        v.buttonAdd.setOnClickListener(RachelOnClickListener {
            MaterialDialog.Builder(pages.context).iconRes(R.mipmap.icon)
                .title("创建歌单").positiveText(R.string.ok).negativeText(R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT).inputRange(1, 10)
                .input("请输入新歌单名称", "", false) { _, input ->
                   createPlaylist(input.toString())
                }.cancelable(false).show()
        })

        // 云备份
        v.buttonUpload.setOnClickListener(RachelOnClickListener {
            MaterialDialog.Builder(pages.context).content("是否将本地所有歌单覆盖云端")
                .positiveText(R.string.ok).negativeText(R.string.cancel)
                .onPositive { _, _ -> uploadPlaylist() }.show()
        })

        // 云还原
        v.buttonDownload.setOnClickListener(RachelOnClickListener {
            MaterialDialog.Builder(pages.context).content("是否从云端覆盖所有本地歌单")
                .positiveText(R.string.ok).negativeText(R.string.cancel)
                .onPositive { _, _ -> downloadPlaylist() }.show()
        })

        // 列表
        v.list.layoutManager = LinearLayoutManager(pages.context)
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
            adapter.items.clearAddAll(this.items)
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
                pages.popSecond()
            }
            "重命名" -> MaterialDialog.Builder(pages.context).iconRes(R.mipmap.icon)
                .title("重命名歌单").positiveText(R.string.ok).negativeText(R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT).inputRange(1, 10)
                .input("请输入歌单名称", "", false) { _, input ->
                    val newName = input.toString()
                    val args = arrayOf(adapter.selectedPlaylist, newName)
                    val result = pages.sendMessageForResult(RachelPages.music, RachelMessage.MUSIC_RENAME_PLAYLIST, args) as Boolean
                    if (result) {
                        XToastUtils.success("修改成功")
                        v.tab.getTab(v.tab.selectedIndex).text = newName
                        v.tab.notifyDataChanged()
                    } else XToastUtils.warning("歌单已存在或名称不合法")
                }.cancelable(false).show()
            "删除" -> MaterialDialog.Builder(pages.context)
                .content("是否删除歌单\"${adapter.selectedPlaylist!!.name}\"")
                .positiveText(R.string.yes).negativeText(R.string.no)
                .onPositive { _, _ ->
                    pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_DELETE_PLAYLIST, adapter.selectedPlaylist)
                    updatePlaylist()
                }.show()
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
            adapter.items.clear()
            adapter.selectedPlaylist = null
            adapter.notifyDataSetChanged()
            v.tab.notifyDataChanged()
        }
    }

    private fun createPlaylist(newName: String) {
        if (pages.sendMessageForResult(RachelPages.music,
                RachelMessage.MUSIC_CREATE_PLAYLIST, newName) as Boolean) {
            XToastUtils.success("创建成功")
            v.tab.addTab(TabSegment.Tab(newName))
            v.tab.selectTab(playlists.size - 1)
        }
        else XToastUtils.warning("歌单已存在或名称不合法")
    }

    @NewThread
    private fun uploadPlaylist() {
        Thread {
            val result = API.uploadPlaylist(Arg.UploadPlaylist(Config.user_id.get(),
                Config.user_pwd.get(), playlists.encodeBase64()))
            post {
                if (result.ok) XToastUtils.success(result.value)
                else XToastUtils.error(result.value)
            }
        }.start()
    }

    @NewThread
    private fun downloadPlaylist() {
        Thread {
            val result = API.downloadPlaylist(Arg.Login(Config.user_id.get(), Config.user_pwd.get()))
            post {
                if (result.ok) {
                    (result.value2.decodeBase64(object : TypeToken<MutableMap<String, Playlist>>(){}.type)
                            as MutableMap<String, Playlist>?)?.apply {
                        pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_STOP_PLAYER)
                        Config.playlist.set(this)
                        playlists.clearAddAll(this)
                        updatePlaylist()
                        XToastUtils.success(result.value1)
                        return@post
                    }
                }
                XToastUtils.error(result.value1)
            }
        }.start()
    }
}