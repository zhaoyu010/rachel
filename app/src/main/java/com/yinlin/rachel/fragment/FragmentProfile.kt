package com.yinlin.rachel.fragment

import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.yinlin.rachel.Config
import com.yinlin.rachel.R
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API
import com.yinlin.rachel.bold
import com.yinlin.rachel.data.TopicPreview
import com.yinlin.rachel.data.UserProfile
import com.yinlin.rachel.databinding.FragmentProfileBinding
import com.yinlin.rachel.databinding.HeaderProfileBinding
import com.yinlin.rachel.databinding.ItemTopicBinding
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelHeaderAdapter
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.model.RachelPages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentProfile(pages: RachelPages, private val profileId: String) : RachelFragment<FragmentProfileBinding>(pages) {
    class Adapter(private val fragment: FragmentProfile) : RachelHeaderAdapter<HeaderProfileBinding, ItemTopicBinding, TopicPreview>() {
        private val rilNet = RachelImageLoader(fragment.pages.context, R.drawable.placeholder_loading, DiskCacheStrategy.ALL)

        override fun bindingHeaderClass() = HeaderProfileBinding::class.java
        override fun bindingItemClass() = ItemTopicBinding::class.java

        override fun initHeader(v: HeaderProfileBinding) {
            v.id.bold = true
            v.tvLevel.bold = true
            v.tvCoin.bold = true
        }

        override fun init(holder: RachelItemViewHolder<ItemTopicBinding>, v: ItemTopicBinding) {
            v.title.bold = true
        }

        override fun update(v: ItemTopicBinding, item: TopicPreview, position: Int) {
            if (item.pic == null) v.pic.setImageDrawable(null)
            else v.pic.load(rilNet, item.picPath)
            v.top.visibility = if (item.isTopTopic) View.VISIBLE else View.GONE
            v.title.text = item.title
            v.comment.text = item.commentNum.toString()
            v.coin.text = item.coinNum.toString()
        }

        override fun onItemClicked(v: ItemTopicBinding, item: TopicPreview, position: Int) {
            fragment.pages.navigate(FragmentTopic(fragment.pages, item.tid))
        }

        override fun onItemLongClicked(v: ItemTopicBinding, item: TopicPreview, position: Int) {

        }
    }

    private var profile = UserProfile(true)
    private val adapter = Adapter(this)

    override fun bindingClass() = FragmentProfileBinding::class.java

    override fun init() {
        v.list.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        v.list.setHasFixedSize(true)
        v.list.recycledViewPool.setMaxRecycledViews(0, 20)
        v.list.setItemViewCacheSize(4)
        v.list.adapter = adapter

        requestUserProfile()
    }

    override fun back() = true

    // 请求用户资料卡
    @NewThread
    private fun requestUserProfile() {
       lifecycleScope.launch {
           pages.loadingDialog.show()
           withContext(Dispatchers.IO) {
               profile = API.UserAPI.getProfile(profileId)
               withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
           }
           val header = adapter.header
           if (profile.isBroken) {
               header.id.text = pages.getResString(R.string.default_id)
               header.title.setTitle(1, pages.getResString(R.string.default_title))
               header.signature.text = pages.getResString(R.string.default_signature)
               header.level.text = "1"
               header.coin.text = "0"
               header.avatar.setImageDrawable(ColorDrawable(pages.getResColor(R.color.white)))
               header.wall.setImageDrawable(ColorDrawable(pages.getResColor(R.color.dark)))
           }
           else {
               header.id.text = profileId
               header.title.setTitle(profile.titleGroup, profile.title)
               header.signature.text = profile.signature
               header.level.text = profile.level.toString()
               header.coin.text = profile.coin.toString()
               header.avatar.load(pages.ril, profile.avatarPath)
               header.wall.load(pages.ril, profile.wallPath)
           }
           adapter.items = profile.topics
           adapter.notifyChangedEx()
       }
    }
}