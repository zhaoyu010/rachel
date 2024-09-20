package com.yinlin.rachel.fragment

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.widget.imageview.nine.ItemImageClickListener
import com.xuexiang.xui.widget.imageview.nine.NineGridImageViewAdapter
import com.xuexiang.xui.widget.imageview.preview.PreviewBuilder
import com.yinlin.rachel.Config
import com.yinlin.rachel.Dialog
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelMessage.ME_REQUEST_USER_INFO
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API
import com.yinlin.rachel.bold
import com.yinlin.rachel.clear
import com.yinlin.rachel.content
import com.yinlin.rachel.data.Comment
import com.yinlin.rachel.data.Topic
import com.yinlin.rachel.databinding.FragmentTopicBinding
import com.yinlin.rachel.databinding.HeaderTopicBinding
import com.yinlin.rachel.databinding.ItemCommentBinding
import com.yinlin.rachel.interceptScroll
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelHeaderAdapter
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.model.RachelNineGridPicture
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.rachelClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FragmentTopic(pages: RachelPages, private val topicId: Int) : RachelFragment<FragmentTopicBinding>(pages) {
    class ImageAdapter(private val rilNet: RachelImageLoader)
        : NineGridImageViewAdapter<RachelNineGridPicture>(),
        ItemImageClickListener<RachelNineGridPicture> {

        override fun onDisplayImage(context: Context, view: ImageView, picture: RachelNineGridPicture) = view.load(rilNet, picture.url)

        override fun onItemImageClick(imageView: ImageView, index: Int, list: MutableList<RachelNineGridPicture>) {
            val view = imageView.parent as ViewGroup
            for (i in 0 until view.childCount) {
                val itemView = view.getChildAt(i)
                val bounds = Rect()
                itemView?.getGlobalVisibleRect(bounds)
                list[i].showBounds = bounds
            }
            PreviewBuilder.from(imageView.context as Activity)
                .setImgs(list).setCurrentIndex(index)
                .setType(PreviewBuilder.IndicatorType.Dot).start()
        }
    }

    class Adapter(private val fragment: FragmentTopic) : RachelHeaderAdapter<HeaderTopicBinding, ItemCommentBinding, Comment>() {
        private val pages = fragment.pages

        override fun bindingHeaderClass() = HeaderTopicBinding::class.java
        override fun bindingItemClass() = ItemCommentBinding::class.java

        override fun initHeader(v: HeaderTopicBinding) {
            v.id.bold = true
            v.title.bold = true
            v.avatar.rachelClick {
                if (!fragment.topic.isBroken) {
                    pages.navigate(FragmentProfile(pages, fragment.topic.id))
                }
            }
            v.pics.setAdapter(ImageAdapter(fragment.rilNet))
            // 评论
            v.inputComment.interceptScroll()
            v.buttonSend.rachelClick {
                val content = v.inputComment.content
                if (content.isNotEmpty()) fragment.sendComment(content)
            }
            // 投币
            v.buttonCoin.rachelClick {
                if (fragment.topic.id == Config.user_id) XToastUtils.warning("不能给自己投币哦")
                else Dialog.inputNumber(pages.context, "请输入投币数量(1-3)") { _, text ->
                    val value = text.toString().toIntOrNull() ?: 0
                    if (Config.user.coin < value) XToastUtils.warning("你的银币不够哦")
                    else if (value in 1..3) fragment.sendCoin(value)
                    else XToastUtils.warning("不能投${value}个币哦")
                }
            }
        }

        override fun init(holder: RachelItemViewHolder<ItemCommentBinding>, v: ItemCommentBinding) {
            v.id.bold = true
            v.avatar.rachelClick {
                val position = getHolderPosition(holder)
                pages.navigate(FragmentProfile(pages, items[position].id))
            }
        }

        override fun update(v: ItemCommentBinding, item: Comment, position: Int) {
            v.id.text = item.id
            v.time.text = item.date
            v.avatar.load(pages.ril, item.avatarPath)
            v.content.text = item.content
            v.userTitle.setTitle(item.userTitleGroup, item.userTitle)
            v.top.visibility = if (item.isTopTopic) View.VISIBLE else View.GONE
        }
    }

    private val rilNet = RachelImageLoader(pages.context, R.drawable.placeholder_pic, DiskCacheStrategy.ALL)

    private var topic = Topic(true)
    private val adapter = Adapter(this)

    override fun bindingClass() = FragmentTopicBinding::class.java

    override fun init() {
        v.list.layoutManager = LinearLayoutManager(pages.context)
        v.list.setHasFixedSize(true)
        v.list.recycledViewPool.setMaxRecycledViews(0, 10)
        v.list.setItemViewCacheSize(4)
        v.list.adapter = adapter

        requestTopic(topicId)
    }

    override fun back() = true

    @NewThread
    fun requestTopic(topicId: Int) {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            withContext(Dispatchers.IO) {
                topic = API.UserAPI.getTopic(topicId)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
            }
            val header = adapter.header
            if (topic.isBroken) {
                header.id.text = ""
                header.time.text = ""
                header.avatar.clear(rilNet)
                header.title.text = topic.title
                header.content.text = topic.content
                header.userTitle.setDefaultTitle()
                header.commentContainer.visibility = View.GONE
            }
            else {
                header.id.text = topic.id
                header.time.text = topic.date
                header.avatar.load(pages.ril, topic.avatarPath)
                header.title.text = topic.title
                header.content.text = topic.content
                header.userTitle.setTitle(topic.userTitleGroup, topic.userTitle)
                header.commentContainer.visibility = View.VISIBLE
            }
            val pics = ArrayList<RachelNineGridPicture>(topic.pics.size)
            for (pic in topic.pics) pics += RachelNineGridPicture(topic.picPath(pic))
            header.pics.setImagesData(pics)
            adapter.items = topic.comments
            adapter.notifyChangedEx()
        }
    }

    @NewThread
    private fun sendComment(content: String) {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) {
                val result = API.UserAPI.sendComment(Config.user_id, Config.user_pwd, topicId, content)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                result
            }
            if (result.ok) {
                adapter.header.inputComment.content = ""
                adapter.items += result.value2!!
                adapter.notifyInsertChangedEx(adapter.items.size)
                XToastUtils.success(result.value1)
            }
            else XToastUtils.error(result.value1)
        }
    }

    @NewThread
    private fun sendCoin(value: Int) {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) {
                val result = API.UserAPI.sendCoin(Config.user_id, Config.user_pwd, topic.id, topicId, value)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                result
            }
            if (result.ok) {
                pages.sendMessage(RachelPages.me, ME_REQUEST_USER_INFO)
                XToastUtils.success(result.value)
            }
            else XToastUtils.error(result.value)
        }
    }
}