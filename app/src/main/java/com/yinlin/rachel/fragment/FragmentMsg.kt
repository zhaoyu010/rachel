package com.yinlin.rachel.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xuexiang.xui.widget.imageview.nine.ItemImageClickListener
import com.xuexiang.xui.widget.imageview.nine.NineGridImageViewAdapter
import com.xuexiang.xui.widget.imageview.preview.PreviewBuilder
import com.yinlin.rachel.Config
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelFont
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.WeiboAPI
import com.yinlin.rachel.clearAddAll
import com.yinlin.rachel.data.MsgInfo
import com.yinlin.rachel.databinding.FragmentMsgBinding
import com.yinlin.rachel.databinding.ItemMsgBinding
import com.yinlin.rachel.model.RachelAdapter
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.load
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter


class FragmentMsg(pages: RachelPages) : RachelFragment<FragmentMsgBinding>(pages) {
    class ImageAdapter(private val rilNet: RachelImageLoader)
        : NineGridImageViewAdapter<MsgInfo.Picture>(), ItemImageClickListener<MsgInfo.Picture> {

        override fun onDisplayImage(context: Context, view: ImageView, picture: MsgInfo.Picture) = view.load(rilNet, picture.getThumb())

        override fun onItemImageClick(imageView: ImageView, index: Int, list: List<MsgInfo.Picture>) {
            val view = imageView.parent as ViewGroup
            for (i in 0 until view.childCount) {
                val itemView = view.getChildAt(i)
                val bounds = Rect()
                itemView?.getGlobalVisibleRect(bounds)
                list[i].setShowBounds(bounds)
            }
            PreviewBuilder.from(imageView.context as Activity)
                .setImgs(list).setCurrentIndex(index)
                .setType(PreviewBuilder.IndicatorType.Dot).start()
        }
    }

    class Adapter(context: Context) : RachelAdapter<ItemMsgBinding, MsgInfo>() {
        private val rilNet = RachelImageLoader(context, R.drawable.placeholder_pic, DiskCacheStrategy.ALL)

        override fun bindingClass() = ItemMsgBinding::class.java

        override fun init(holder: RachelViewHolder<ItemMsgBinding>) {
            val v = holder.v
            v.name.typeface = RachelFont.bold
            v.pics.setAdapter(ImageAdapter(rilNet))
        }

        override fun update(v: ItemMsgBinding, item: MsgInfo, position: Int) {
            v.name.text = item.name
            v.avatar.load(rilNet, item.avatar)
            v.time.text = item.time
            v.location.text = item.location
            v.text.setHtml(item.text, HtmlHttpImageGetter(v.text))
            v.pics.setImagesData(item.pictures)
        }
    }

    private val adapter = Adapter(pages.context)

    override fun bindingClass() = FragmentMsgBinding::class.java

    override fun init() {
        // 列表
        v.list.layoutManager = LinearLayoutManager(pages.context)
        v.list.adapter = adapter

        // 下拉刷新
        v.container.setOnRefreshListener { loadMsg() }

        // 首次刷新
        v.container.autoRefresh()
    }

    override fun back(): Boolean {
        v.list.smoothScrollToPosition(0)
        return false
    }

    @NewThread @SuppressLint("NotifyDataSetChanged")
    fun loadMsg() {
        v.state.showLoading("正在读取最新资讯...")
        Thread {
            val msgInfos = WeiboAPI.extract(Config.weibo_users.get())
            post {
                if (msgInfos.isEmpty()) v.state.showOffline { v.container.autoRefresh() }
                else v.state.showContent()
                adapter.items.clearAddAll(msgInfos)
                adapter.notifyDataSetChanged()
                if (v.container.isRefreshing) v.container.finishRefresh()
            }
        }.start()
    }
}