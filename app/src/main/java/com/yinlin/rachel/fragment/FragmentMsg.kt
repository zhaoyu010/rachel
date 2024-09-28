package com.yinlin.rachel.fragment

import android.annotation.SuppressLint
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
import com.yinlin.rachel.model.RachelNineGridAdapter
import com.yinlin.rachel.model.RachelOnClickListener
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.model.RachelPreview
import com.yinlin.rachel.rachelClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter


class FragmentMsg(pages: RachelPages) : RachelFragment<FragmentMsgBinding>(pages) {
    class Adapter(private val pages: RachelPages) : RachelAdapter<ItemWeiboBinding, Weibo>() {
        private val rilNet = RachelImageLoader(pages.context, R.drawable.placeholder_pic, DiskCacheStrategy.ALL)

        override fun bindingClass() = ItemWeiboBinding::class.java

        override fun init(holder: RachelViewHolder<ItemWeiboBinding>, v: ItemWeiboBinding) {
            v.name.bold = true
            v.pics.setAdapter(RachelNineGridAdapter(pages.context))
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
                val item = items[index] as RachelPreview
                if (item.isVideo) Net.downloadVideo(pages.context, item.mVideoUrl)
                else Net.downloadPicture(pages.context, item.mSourceUrl)
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
    private var sinceId: Long = 0L

    override fun bindingClass() = FragmentMsgBinding::class.java

    override fun init() {
        v.tvWeibo.active = true
        v.tvWeibo.rachelClick {
            v.tvWeibo.active = true
            v.tvChaohua.active = false
            v.container.autoRefresh()
        }
        v.tvChaohua.rachelClick {
            v.tvWeibo.active = false
            v.tvChaohua.active = true
            v.container.autoRefresh()
        }

        // 刷新与加载
        v.container.setEnableAutoLoadMore(true)
        v.container.setEnableOverScrollDrag(false)
        v.container.setEnableOverScrollBounce(false)
        v.container.setOnRefreshListener {
            v.container.setNoMoreData(false)
            if (v.tvWeibo.active) requestWeibo()
            else requestChaohua()
        }
        v.container.setOnLoadMoreListener {
            if (v.tvChaohua.active) requestChaohuaMore()
        }

        // 列表
        v.list.layoutManager = LinearLayoutManager(pages.context)
        v.list.setHasFixedSize(true)
        v.list.recycledViewPool.setMaxRecycledViews(0, 16)
        v.list.setItemViewCacheSize(4)
        v.list.adapter = adapter

        // 首次刷新
        requestWeibo()
    }

    override fun back(): Boolean {
        v.list.smoothScrollToPosition(0)
        return false
    }

    @NewThread @SuppressLint("NotifyDataSetChanged")
    fun requestWeibo() {
        lifecycleScope.launch {
            v.list.scrollToPosition(0)
            v.state.showLoading("加载资讯中...")
            adapter.items.clear()
            withContext(Dispatchers.IO) { WeiboAPI.getAllWeibo(Config.weibo_users, adapter.items) }
            if (adapter.items.isEmpty()) v.state.showOffline { requestWeibo() }
            else v.state.showContent()
            adapter.notifyDataSetChanged()
            v.container.finishRefreshWithNoMoreData()
        }
    }

    @NewThread @SuppressLint("NotifyDataSetChanged")
    fun requestChaohua() {
        lifecycleScope.launch {
            v.list.scrollToPosition(0)
            v.state.showLoading("加载资讯中...")
            adapter.items.clear()
            sinceId = withContext(Dispatchers.IO) { WeiboAPI.getChaohua(0, adapter.items) }
            if (sinceId == 0L) {
                v.state.showOffline { requestChaohua() }
                v.container.finishRefreshWithNoMoreData()
            }
            else {
                v.state.showContent()
                v.container.finishRefresh()
            }
            adapter.notifyDataSetChanged()
        }
    }

    @NewThread
    fun requestChaohuaMore() {
        lifecycleScope.launch {
            val oldSize = adapter.items.size
            sinceId = withContext(Dispatchers.IO) { WeiboAPI.getChaohua(sinceId, adapter.items) }
            if (sinceId == 0L) v.container.finishLoadMoreWithNoMoreData()
            else {
                adapter.notifyItemRangeInserted(oldSize, adapter.items.size - oldSize)
                v.container.finishLoadMore()
            }
        }
    }
}