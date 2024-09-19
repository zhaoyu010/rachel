package com.yinlin.rachel.fragment

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.Dialog
import com.yinlin.rachel.IMusicInfoMap
import com.yinlin.rachel.IPlaylistMap
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.data.MusicInfo
import com.yinlin.rachel.data.Playlist
import com.yinlin.rachel.databinding.FragmentLibraryBinding
import com.yinlin.rachel.databinding.ItemMusicBinding
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelAdapter
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.rachelClick
import java.util.Arrays


class FragmentLibrary(pages: RachelPages,
                      private val musicInfos: IMusicInfoMap,
                      private val playlists: IPlaylistMap)
    : RachelFragment<FragmentLibraryBinding>(pages) {
    class Adapter(private val pages: RachelPages, private val fragment: FragmentLibrary)
        : RachelAdapter<ItemMusicBinding, MusicInfo>(fragment.musicInfos.values.toMutableList()) {
        var isManageMode = false
        val checkStatus = BooleanArray(items.size)

        override fun bindingClass() = ItemMusicBinding::class.java

        override fun update(v: ItemMusicBinding, item: MusicInfo, position: Int) {
            v.name.text = item.name // 歌名
            v.singer.text = item.singer
            v.version.labelText = item.version // 版本
            v.pic.load(pages.ril, item.recordPath) // 封面
            // 选择器
            v.select.visibility = if (isManageMode) View.VISIBLE else View.GONE
            v.select.setCheckedSilent(checkStatus[position])
        }

        override fun onItemClicked(v: ItemMusicBinding, item: MusicInfo, position: Int) {
            if (isManageMode) {
                checkStatus[position] = !checkStatus[position]
                if (checkStatus.all { !it }) {
                    isManageMode = false
                    fragment.setManageMode(View.GONE)
                }
                else notifyItemChanged(position)
            } else {
                pages.pop()
                pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_START_PLAYER, Playlist(pages.getResString(R.string.default_playlist_name), item.id))
            }
        }

        override fun onItemLongClicked(v: ItemMusicBinding, item: MusicInfo, position: Int) {
            if (!isManageMode) {
                checkStatus[position] = true
                fragment.setManageMode(View.VISIBLE)
            }
        }

        // 获取所有选中歌曲的编号
        val checkIds: List<String> get() {
            val arr = ArrayList<String>()
            for ((index, item) in items.withIndex()) {
                if (checkStatus[index]) arr += item.id
            }
            return arr
        }
    }

    private var adapter = Adapter(pages, this)

    override fun bindingClass() = FragmentLibraryBinding::class.java

    override fun init() {
        v.buttonPlaylist.rachelClick { addMusicIntoPlaylist() }
        v.buttonDelete.rachelClick { deleteMusic() }
        v.buttonUnselect.rachelClick { exitManageMode() }

        v.list.layoutManager = GridLayoutManager(context, 3)
        v.list.setHasFixedSize(true)
        v.list.recycledViewPool.setMaxRecycledViews(0, 14)
        v.list.setItemViewCacheSize(4)
        v.list.adapter = adapter
    }

    override fun back(): Boolean {
        if (adapter.isManageMode) {
            exitManageMode()
            return false
        }
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setManageMode(visibility: Int) {
        adapter.isManageMode = visibility == View.VISIBLE
        v.buttonPlaylist.visibility = visibility
        v.buttonDelete.visibility = visibility
        v.buttonUnselect.visibility = visibility
        adapter.notifyDataSetChanged()
    }

    private fun exitManageMode() {
        Arrays.fill(adapter.checkStatus, false)
        setManageMode(View.GONE)
    }

    // 添加到歌单
    private fun addMusicIntoPlaylist() {
        if (playlists.isNotEmpty()) {
            val selectIds = adapter.checkIds // 获得所有歌单名供选择
            if (selectIds.isNotEmpty()) {
                Dialog.choice(pages.context, "添加到歌单", playlists.keys) { _, _, _, text ->
                    pages.pop()
                    val playlist = playlists[text.toString()]
                    val num: Int = pages.sendMessageForResult(RachelPages.music, RachelMessage.MUSIC_ADD_MUSIC_INTO_PLAYLIST, playlist, selectIds)!!
                    XToastUtils.success("已添加${num}首歌曲")
                    true
                }
            }
            else XToastUtils.warning("请至少选择一首歌曲")
        }
        else XToastUtils.warning("没有创建任何歌单")
    }

    // 删除歌曲
    private fun deleteMusic() {
        val selectIds = adapter.checkIds
        if (selectIds.isNotEmpty()) {
            Dialog.confirm(pages.context, "是否从曲库中卸载指定歌曲?") { _, _ ->
                pages.pop()
                pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_DELETE_MUSIC, selectIds)
                XToastUtils.success("已卸载${selectIds.size}首歌曲")
            }
        }
        else XToastUtils.warning("请至少选择一首歌曲")
    }
}