package com.yinlin.rachel.fragment

import android.annotation.SuppressLint
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xuexiang.xui.utils.XToastUtils
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
import com.yinlin.rachel.rachelClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentDiscovery(pages: RachelPages) : RachelFragment<FragmentDiscoveryBinding>(pages), OnQueryTextListener {
    class Adapter(private val fragment: FragmentDiscovery) : RachelAdapter<ItemTopicUserBinding, TopicPreview>() {
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
            if (item.pic == null) v.pic.setImageDrawable(null)
            else v.pic.load(rilNet, item.picPath)
            v.title.text = item.title
            v.id.text = item.id
            v.avatar.load(rilNet, item.avatarPath)
            v.comment.text = item.commentNum.toString()
            v.coin.text = item.coinNum.toString()
        }

        override fun onItemClicked(v: ItemTopicUserBinding, item: TopicPreview, position: Int) {
            pages.navigate(FragmentTopic(pages, item.tid))
        }
    }

    private var topicUpper: Int = 2147483647
    private var topicOffset: Int = 0
    private var isLoadingMore: Boolean = false

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
            requestLatestTopic()
        }
        v.tvHot.rachelClick {
            v.tvLatest.active = false
            v.tvHot.active = true
            requestHotTopic()
        }
        v.buttonSearch.rachelClick {
            if (v.search.isSearchOpen) v.search.closeSearch()
            else v.search.showSearch()
        }
        v.buttonAdd.rachelClick {
            pages.navigate(FragmentCreateTopic(pages))
        }

        v.list.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        v.list.setHasFixedSize(true)
        v.list.recycledViewPool.setMaxRecycledViews(0, 20)
        v.list.setItemViewCacheSize(4)
        v.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val manager = recyclerView.layoutManager as StaggeredGridLayoutManager
                if (manager.itemCount > 0) {
                    val arr = manager.findLastCompletelyVisibleItemPositions(null)
                    if (arr.contains(manager.itemCount - 1)) loadMore()
                }
            }
        })
        v.list.adapter = adapter

        requestLatestTopic()
    }

    override fun quit() {
        v.list.clearOnScrollListeners()
        if (v.search.isSearchOpen) v.search.closeSearch()
    }

    override fun hidden() {
        if (v.search.isSearchOpen) v.search.closeSearch()
    }

    override fun back(): Boolean {
        if (v.search.isSearchOpen) v.search.closeSearch()
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
            pages.loadingDialog.show()
            v.list.scrollToPosition(0)
            val topics = withContext(Dispatchers.IO) {
                val topics = API.UserAPI.getLatestTopic()
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                topics
            }
            topicUpper = if (topics.isEmpty()) 2147483647 else topics.last().tid
            adapter.items.clearAddAll(topics)
            adapter.notifyDataSetChanged()
        }
    }

    @NewThread @SuppressLint("NotifyDataSetChanged")
    private fun requestHotTopic() {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            v.list.scrollToPosition(0)
            val topics = withContext(Dispatchers.IO) {
                val topics = API.UserAPI.getHotTopic()
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                topics
            }
            topicOffset = if (topics.isEmpty()) 0 else topics.size
            adapter.items.clearAddAll(topics)
            adapter.notifyDataSetChanged()
        }
    }

    @NewThread @SuppressLint("NotifyDataSetChanged")
    private fun requestLatestTopicMore() {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val topics = withContext(Dispatchers.IO) {
                val topics = API.UserAPI.getLatestTopic(topicUpper)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                topics
            }
            if (topics.isNotEmpty()) {
                topicUpper = topics.last().tid
                adapter.items.addAll(topics)
                adapter.notifyDataSetChanged()
            }
            else XToastUtils.warning("没有更多主题啦")
            isLoadingMore = false
        }
    }

    @NewThread @SuppressLint("NotifyDataSetChanged")
    private fun requestHotTopicMore() {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val topics = withContext(Dispatchers.IO) {
                val topics = API.UserAPI.getHotTopic(topicOffset)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                topics
            }
            if (topics.isNotEmpty()) {
                topicOffset += topics.size
                adapter.items.addAll(topics)
                adapter.notifyDataSetChanged()
            }
            else XToastUtils.warning("没有更多主题啦")
            isLoadingMore = false
        }
    }

    private fun loadMore() {
        if (!isLoadingMore) {
            isLoadingMore = true
            if (v.tvLatest.active) requestLatestTopicMore()
            else requestHotTopicMore()
        }
    }
}