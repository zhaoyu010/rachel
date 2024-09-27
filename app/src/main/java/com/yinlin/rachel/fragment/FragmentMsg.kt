package com.yinlin.rachel.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xuexiang.xui.widget.imageview.nine.ItemImageClickListener
import com.xuexiang.xui.widget.imageview.nine.NineGridImageViewAdapter
import com.xuexiang.xui.widget.imageview.preview.PreviewBuilder
import com.yinlin.rachel.Config
import com.yinlin.rachel.Net
import com.yinlin.rachel.R
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.WeiboAPI
import com.yinlin.rachel.bold
import com.yinlin.rachel.data.Weibo
import com.yinlin.rachel.databinding.FragmentMsgBinding
import com.yinlin.rachel.databinding.ItemWeiboBinding
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelAdapter
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.model.RachelOnClickListener
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.rachelClick
import com.yinlin.rachel.updateNineGridBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter


class FragmentMsg(pages: RachelPages) : RachelFragment<FragmentMsgBinding>(pages) {
    class ImageAdapter(private val rilNet: RachelImageLoader)
        : NineGridImageViewAdapter<Weibo.Picture>(), ItemImageClickListener<Weibo.Picture> {

        override fun onDisplayImage(context: Context, view: ImageView, picture: Weibo.Picture) = view.load(rilNet, picture.getThumb())

        override fun onItemImageClick(imageView: ImageView, index: Int, list: List<Weibo.Picture>) {
            imageView.updateNineGridBounds { i, bounds -> list[i].showBounds = bounds }
            PreviewBuilder.from(imageView.context as Activity)
                .setImgs(list).setCurrentIndex(index)
                .setType(PreviewBuilder.IndicatorType.Dot).start()
        }
    }

    class Adapter(private val pages: RachelPages) : RachelAdapter<ItemWeiboBinding, Weibo>() {
        private val rilNet = RachelImageLoader(pages.context, R.drawable.placeholder_pic, DiskCacheStrategy.ALL)

        override fun bindingClass() = ItemWeiboBinding::class.java

        override fun init(holder: RachelViewHolder<ItemWeiboBinding>, v: ItemWeiboBinding) {
            v.name.bold = true
            v.pics.setAdapter(ImageAdapter(rilNet))
            v.text.setOnClickATagListener { _, _, href ->
                href?.apply {
                    val url = if (href.startsWith("http") || href.startsWith("www")) href
                    else if (href.startsWith("/status/")) "${WeiboAPI.BASEURL}${href}"
                    else ""
                    if (url != "") pages.navigate(FragmentWebpage(pages, url))
                }
                true
            }
            val detailsListener = RachelOnClickListener {
                pages.navigate(FragmentWeibo(pages, items[holder.bindingAdapterPosition]))
            }
            v.weiboCard.rachelClick(detailsListener)
            v.name.rachelClick(detailsListener)
            v.time.rachelClick(detailsListener)
            v.location.rachelClick(detailsListener)
            v.pics.setItemImageLongClickListener { _, index, items ->
                val item = items[index] as Weibo.Picture
                if (item.type == Weibo.MsgType.VIDEO) Net.downloadVideo(pages.context, item.source)
                else Net.downloadPicture(pages.context, item.source)
                true
            }
        }

        override fun update(v: ItemWeiboBinding, item: Weibo, position: Int) {
            v.name.text = item.name
            v.avatar.load(rilNet, item.avatar)
            v.time.text = item.time
            v.location.text = item.location
            v.text.setHtml(item.text, HtmlHttpImageGetter(v.text))
            v.pics.setImagesData(item.pictures)
        }
    }

    private val adapter = Adapter(pages)

    override fun bindingClass() = FragmentMsgBinding::class.java

    override fun init() {
        // 列表
        v.list.layoutManager = LinearLayoutManager(pages.context)
        v.list.setHasFixedSize(true)
        v.list.recycledViewPool.setMaxRecycledViews(0, 8)
        v.list.setItemViewCacheSize(4)
        v.list.adapter = adapter

        // 下拉刷新
        v.container.setOnRefreshListener { loadMsg() }

        // 首次刷新
        loadMsg()
    }

    override fun back(): Boolean {
        v.list.smoothScrollToPosition(0)
        return false
    }

    @NewThread @SuppressLint("NotifyDataSetChanged")
    fun loadMsg() {
        lifecycleScope.launch {
            v.state.showLoading("加载资讯中...")
            adapter.items.clear()
            withContext(Dispatchers.IO) { WeiboAPI.extract(Config.weibo_users, adapter.items) }
            if (v.container.isRefreshing) v.container.finishRefresh()
            if (adapter.items.isEmpty()) v.state.showOffline { loadMsg() }
            else v.state.showContent()
            adapter.notifyDataSetChanged()
        }
    }
}