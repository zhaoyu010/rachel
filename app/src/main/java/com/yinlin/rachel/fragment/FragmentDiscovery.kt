package com.yinlin.rachel.fragment

import android.annotation.SuppressLint
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xuexiang.xui.widget.searchview.MaterialSearchView.OnQueryTextListener
import com.yinlin.rachel.R
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API
import com.yinlin.rachel.bold
import com.yinlin.rachel.clearAddAll
import com.yinlin.rachel.data.TopicPreview
import com.yinlin.rachel.databinding.FragmentDiscoveryBinding
import com.yinlin.rachel.databinding.ItemTopicUserBinding
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelAdapter
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.pureColor
import com.yinlin.rachel.rachelClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentDiscovery(pages: RachelPages) : RachelFragment<FragmentDiscoveryBinding>(pages), OnQueryTextListener {
    class Adapter(fragment: FragmentDiscovery) : RachelAdapter<ItemTopicUserBinding, TopicPreview>() {
        private val pages = fragment.pages
        private val rilNet = RachelImageLoader(pages.context, R.drawable.placeholder_loading, DiskCacheStrategy.ALL)

        override fun bindingClass() = ItemTopicUserBinding::class.java

        override fun init(holder: RachelViewHolder<ItemTopicUserBinding>, v: ItemTopicUserBinding) {
            v.id.bold = true
            v.title.bold = true
            v.avatar.rachelClick {
                val position = holder.bindingAdapterPosition
                pages.navigate(FragmentProfile(pages, items[position].id))
            }
        }

        override fun update(v: ItemTopicUserBinding, item: TopicPreview, position: Int) {
            if (item.pic == null) v.pic.pureColor = 0
            else v.pic.load(rilNet, item.picPath)
            v.title.text = item.title
            v.id.text = item.id
            v.avatar.load(pages.ril, item.avatarPath)
            v.comment.text = item.commentNum.toString()
            v.coin.text = item.coinNum.toString()
        }

        override fun onItemClicked(v: ItemTopicUserBinding, item: TopicPreview, position: Int) {
            pages.navigate(FragmentTopic(pages, item.tid))
        }
    }

    private var topicUpper: Int = 2147483647
    private var topicOffset: Int = 0

    private val adapter = Adapter(this)

    override fun bindingClass() = FragmentDiscoveryBinding::class.java

    override fun init() {
        v.search.setVoiceSearch(false)
        v.search.setEllipsize(true)
        v.search.setOnQueryTextListener(this)

        v.tvLatest.active = true
        v.tvLatest.rachelClick {
            v.tvLatest.active = true
            v.tvHot.active = false
            v.container.autoRefresh()
        }
        v.tvHot.rachelClick {
            v.tvLatest.active = false
            v.tvHot.active = true
            v.container.autoRefresh()
        }
        v.buttonSearch.rachelClick {
            if (v.search.isSearchOpen) v.search.closeSearch()
            else v.search.showSearch()
        }
        v.buttonAdd.rachelClick {
            pages.navigate(FragmentCreateTopic(pages))
        }

        // 刷新与加载
        v.container.setEnableAutoLoadMore(true)
        v.container.setEnableOverScrollDrag(false)
        v.container.setEnableOverScrollBounce(false)
        v.container.setEnableLoadMore(true)
        v.container.setOnRefreshListener {
            v.container.setNoMoreData(false)
            if (v.tvLatest.active) requestLatestTopic()
            else requestHotTopic()
        }
        v.container.setOnLoadMoreListener {
            if (v.tvLatest.active) requestLatestTopicMore()
            else requestHotTopicMore()
        }

        v.list.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        v.list.setHasFixedSize(true)
        v.list.recycledViewPool.setMaxRecycledViews(0, 20)
        v.list.setItemViewCacheSize(4)
        v.list.adapter = adapter

        requestLatestTopic()
    }

    override fun quit() {
        if (v.search.isSearchOpen) v.search.closeSearch()
    }

    override fun hidden() {
        if (v.search.isSearchOpen) v.search.closeSearch()
    }

    override fun back(): Boolean {
        if (v.search.isSearchOpen) v.search.closeSearch()
        else {
            v.list.smoothScrollToPosition(0)
        }
        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String) = false

    // 最新主题
    @NewThread @SuppressLint("NotifyDataSetChanged")
    private fun requestLatestTopic() {
        lifecycleScope.launch {
            v.list.scrollToPosition(0)
            val topics = withContext(Dispatchers.IO) { API.UserAPI.getLatestTopic() }
            if (topics.isEmpty()) {
                topicUpper = 2147483647
                v.container.finishRefreshWithNoMoreData()
            }
            else {
                topicUpper = topics.last().tid
                v.container.finishRefresh()
            }
            adapter.items.clearAddAll(topics)
            adapter.notifyDataSetChanged()
        }
    }

    @NewThread @SuppressLint("NotifyDataSetChanged")
    private fun requestHotTopic() {
        lifecycleScope.launch {
            v.list.scrollToPosition(0)
            val topics = withContext(Dispatchers.IO) { API.UserAPI.getHotTopic() }
            topicOffset = topics.size
            if (topics.isEmpty()) v.container.finishRefreshWithNoMoreData()
            else v.container.finishRefresh()
            adapter.items.clearAddAll(topics)
            adapter.notifyDataSetChanged()
        }
    }

    @NewThread
    private fun requestLatestTopicMore() {
        lifecycleScope.launch {
            val topics = withContext(Dispatchers.IO) { API.UserAPI.getLatestTopic(topicUpper) }
            if (topics.isEmpty()) v.container.finishLoadMoreWithNoMoreData()
            else {
                val newCount = topics.size
                topicUpper = topics.last().tid
                adapter.items.addAll(topics)
                adapter.notifyItemRangeInserted(adapter.items.size - newCount, newCount)
                v.container.finishLoadMore()
            }
        }
    }

    @NewThread
    private fun requestHotTopicMore() {
        lifecycleScope.launch {
            val topics = withContext(Dispatchers.IO) { API.UserAPI.getHotTopic(topicOffset) }
            if (topics.isEmpty()) v.container.finishLoadMoreWithNoMoreData()
            else {
                val newCount = topics.size
                topicOffset += topics.size
                adapter.items.addAll(topics)
                adapter.notifyItemRangeInserted(adapter.items.size - newCount, newCount)
                v.container.finishLoadMore()
            }
        }
    }
}