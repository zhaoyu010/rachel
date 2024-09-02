package com.yinlin.rachel.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.abdshammout.UBV.OnClickListenerBreadcrumbs
import com.abdshammout.UBV.model.PathItem
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.widget.imageview.preview.PreviewBuilder
import com.xuexiang.xui.widget.imageview.preview.enitity.IPreviewInfo
import com.yinlin.rachel.Net
import com.yinlin.rachel.R
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API
import com.yinlin.rachel.clearAddAll
import com.yinlin.rachel.data.ResFile
import com.yinlin.rachel.data.ResFolder
import com.yinlin.rachel.databinding.FragmentResBinding
import com.yinlin.rachel.databinding.ItemResBinding
import com.yinlin.rachel.err
import com.yinlin.rachel.model.RachelAdapter
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.model.RachelOnClickListener
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.model.RachelPreviewInfo
import com.yinlin.rachel.load
import com.yinlin.rachel.rachelClick


class FragmentRes(pages: RachelPages) : RachelFragment<FragmentResBinding>(pages) {
    class Adapter(private val fragment: FragmentRes, var currentRes: ResFolder) : RachelAdapter<ItemResBinding, ResFile>(currentRes.items) {
        var currentPos: Int = -1
        private val rilNet = RachelImageLoader(fragment.pages.context, R.drawable.placeholder_res, DiskCacheStrategy.ALL)

        override fun bindingClass() = ItemResBinding::class.java

        override fun init(holder: RachelViewHolder<ItemResBinding>) {
            val v = holder.v
            val context = fragment.pages.context
            v.downloadHd.rachelClick {
                items[holder.bindingAdapterPosition].thumbUrl?.apply { Net.downloadPicture(context, this, ::processDownloadResult) }
            }
            v.download4k.rachelClick {
                items[holder.bindingAdapterPosition].sourceUrl?.apply { Net.downloadPicture(context, this, ::processDownloadResult) }
            }
        }

        override fun update(v: ItemResBinding, item: ResFile, position: Int) {
            v.name.text = item.name
            if (item is ResFolder) {
                v.author.text = ""
                v.author.visibility = View.GONE
                v.downloadContainer.visibility = View.GONE
                v.pic.load(fragment.pages.ril, R.drawable.photo_album)
            } else {
                v.author.text = item.author
                v.author.visibility = View.VISIBLE
                v.downloadContainer.visibility = View.VISIBLE
                item.thumbUrl?.apply { v.pic.load(rilNet, this) }
            }
        }

        override fun onItemClicked(v: ItemResBinding, item: ResFile, position: Int) {
            if (item is ResFolder) {
                fragment.v.header.addToPath(PathItem(item.name))
                setRes(currentPos + 1, item)
            } else {
                item.thumbUrl?.apply {
                    PreviewBuilder.from((fragment.pages.context as Activity))
                        .setImg<IPreviewInfo>(RachelPreviewInfo(this))
                        .setType(PreviewBuilder.IndicatorType.Dot).start()
                }
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setRes(position: Int, res: ResFolder) {
            if (res != this.currentRes) {
                currentPos = position
                currentRes = res
                items.clearAddAll(currentRes.items)
                notifyDataSetChanged()
            }
        }

        private fun processDownloadResult(status: Boolean) {
            status.err("下载失败") { XToastUtils.success("下载成功") }
        }
    }

    private var rootRes = ResFolder.emptyRes
    private val adapter = Adapter(this, rootRes)

    override fun bindingClass() = FragmentResBinding::class.java

    override fun init() {
        // 面包屑
        v.header.setOnClickListenerBreadcrumbs(object : OnClickListenerBreadcrumbs {
            override fun onBackClick() {
                adapter.currentRes.parent?.apply { adapter.setRes(adapter.currentPos - 1, this) }
            }
            override fun onPathItemClick(index: Int, title: String, id: Int) {
                var root = adapter.currentRes
                var pos = adapter.currentPos
                while (index != pos) {
                    root = root.parent!!
                    --pos
                }
                adapter.setRes(pos, root)
            }
            override fun onPathItemLongClick(index: Int, title: String, id: Int) { }
        })
        v.header.initUltimateBreadcrumbsView()
        // 列表
        v.list.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        v.list.adapter = adapter
        // 刷新
        v.buttonUpdate.rachelClick { updateRes() }
        // 首次加载
        updateRes()
    }

    override fun back(): Boolean {
        adapter.currentRes.parent?.apply {
            v.header.back()
            adapter.setRes(adapter.currentPos - 1, this)
        }
        return false
    }

    @NewThread
    fun updateRes() {
        v.state.showLoading("正在更新美图数据源...")
        Thread {
            rootRes = API.resInfo()
            post {
                if (rootRes.items.isEmpty()) v.state.showOffline { updateRes() }
                else v.state.showContent()
                while (v.header.itemCount > 0) v.header.back()
                adapter.setRes(-1, rootRes)
            }
        }.start()
    }
}