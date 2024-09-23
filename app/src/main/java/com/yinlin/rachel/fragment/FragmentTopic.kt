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
import com.xuexiang.xui.widget.popupwindow.popup.XUIListPopup
import com.xuexiang.xui.widget.popupwindow.popup.XUISimplePopup
import com.yinlin.rachel.Config
import com.yinlin.rachel.model.RachelDialog
import com.yinlin.rachel.R
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API
import com.yinlin.rachel.bold
import com.yinlin.rachel.data.Comment
import com.yinlin.rachel.data.Topic
import com.yinlin.rachel.databinding.FragmentTopicBinding
import com.yinlin.rachel.databinding.HeaderTopicBinding
import com.yinlin.rachel.databinding.ItemCommentBinding
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelHeaderAdapter
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.model.RachelNineGridPicture
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.model.RachelPopMenu
import com.yinlin.rachel.rachelClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections


class FragmentTopic(pages: RachelPages, private val tid: Int) : RachelFragment<FragmentTopicBinding>(pages) {
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
                if (fragment.topic.ok) {
                    pages.navigate(FragmentProfile(pages, fragment.topic.id))
                }
            }
            v.pics.setAdapter(ImageAdapter(fragment.rilNet))

            // 菜单
            v.more.rachelClick(300) {
                val menuList = ArrayList<RachelPopMenu.Item>()
                val user = Config.user
                val topicId = fragment.topic.id
                if (Config.isLoginAndUpdate) {
                    val topText = if (fragment.topic.isTopTopic) "取消置顶" else "置顶"
                    if (user.canUpdateTopicTop(topicId)) menuList += RachelPopMenu.Item(topText) {
                        RachelDialog.confirm(pages.context, "${topText}此主题?") { _, _ ->
                            fragment.updateTopicTop(if (fragment.topic.isTopTopic) 0 else 1)
                        }
                    }
                    if (user.canDeleteTopic(topicId)) menuList += RachelPopMenu.Item("删除") {
                        RachelDialog.confirm(pages.context, "删除此主题?") { _, _ ->
                            fragment.deleteTopic()
                        }
                    }
                    if (menuList.isNotEmpty()) RachelPopMenu.showDown(it, menuList)
                }
            }

            // 评论
            v.buttonSend.rachelClick {
                if (Config.isLoginAndUpdate) {
                    RachelDialog.inputMultiLines(pages.context, "快来留下你的足迹~", 256, 5) { _, input ->
                        fragment.sendComment(input.toString())
                    }
                }
                else XToastUtils.warning("请先登录")
            }

            // 投币
            v.buttonCoin.rachelClick {
                if (Config.isLoginAndUpdate) {
                    if (fragment.topic.id == Config.user_id) XToastUtils.warning("不能给自己投币哦")
                    else RachelDialog.inputNumber(pages.context, "请输入投币数量(1-3)") { _, text ->
                        val value = text.toString().toIntOrNull() ?: 0
                        if (Config.user.coin < value) XToastUtils.warning("你的银币不够哦")
                        else if (value in 1..3) fragment.sendCoin(value)
                        else XToastUtils.warning("不能投${value}个币哦")
                    }
                }
                else XToastUtils.warning("请先登录")
            }
        }

        override fun init(holder: RachelItemViewHolder<ItemCommentBinding>, v: ItemCommentBinding) {
            v.id.bold = true
            v.avatar.rachelClick {
                val position = getHolderPosition(holder)
                pages.navigate(FragmentProfile(pages, items[position].id))
            }
            v.more.rachelClick(300) {
                val menuList = ArrayList<RachelPopMenu.Item>()
                val user = Config.user
                val topicId = fragment.topic.id
                if (Config.isLoginAndUpdate) {
                    val position = getHolderPosition(holder)
                    val comment = items[position]
                    val topText = if (comment.isTopComment) "取消置顶" else "置顶"
                    if (user.canUpdateCommentTop(topicId)) menuList += RachelPopMenu.Item(topText) {
                        RachelDialog.confirm(pages.context, "${topText}此评论?") { _, _ ->
                            fragment.updateCommentTop(comment.cid, position, if (comment.isTopComment) 0 else 1)
                        }
                    }
                    if (user.canDeleteComment(topicId, comment.id)) menuList += RachelPopMenu.Item("删除") {
                        RachelDialog.confirm(pages.context, "删除此评论?") { _, _ ->
                            fragment.deleteComment(comment.cid, position)
                        }
                    }
                    if (menuList.isNotEmpty()) RachelPopMenu.showDown(it, menuList)
                }
            }
        }

        override fun update(v: ItemCommentBinding, item: Comment, position: Int) {
            v.id.text = item.id
            v.time.text = item.date
            v.avatar.load(pages.ril, item.avatarPath)
            v.content.text = item.content
            v.userTitle.setTitle(item.userTitleGroup, item.userTitle)
            v.top.visibility = if (item.isTopComment) View.VISIBLE else View.GONE
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

        requestTopic(tid)
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
            if (topic.ok) {
                val header = adapter.header
                header.id.text = topic.id
                header.time.text = topic.date
                header.avatar.load(pages.ril, topic.avatarPath)
                header.title.text = topic.title
                header.content.text = topic.content
                header.userTitle.setTitle(topic.userTitleGroup, topic.userTitle)
                val pics = ArrayList<RachelNineGridPicture>(topic.pics.size)
                for (pic in topic.pics) pics += RachelNineGridPicture(topic.picPath(pic))
                header.pics.setImagesData(pics)
                adapter.items = topic.comments
                adapter.notifyChangedEx()
            }
            else {
                pages.pop()
                XToastUtils.error("主题不存在")
            }
        }
    }

    @NewThread
    private fun sendComment(content: String) {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) {
                val result = API.UserAPI.sendComment(Config.user_id, Config.user_pwd, tid, content)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                result
            }
            if (result.ok) {
                adapter.items += result.value2!!
                adapter.notifyInsertEx(adapter.items.size)
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
                val result = API.UserAPI.sendCoin(Config.user_id, Config.user_pwd, topic.id, tid, value)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                result
            }
            if (result.ok) {
                pages.requestUserInfo()
                XToastUtils.success(result.value)
            }
            else XToastUtils.error(result.value)
        }
    }

    @NewThread
    private fun deleteComment(cid: Long, position: Int) {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) {
                val result = API.UserAPI.deleteComment(Config.user_id, Config.user_pwd, cid, tid)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                result
            }
            if (result.ok) {
                adapter.items.removeAt(position)
                adapter.notifyRemovedEx(position)
                XToastUtils.success(result.value)
            }
            else XToastUtils.error(result.value)
        }
    }

    @NewThread
    private fun updateCommentTop(cid: Long, position: Int, isTop: Int) {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) {
                val result = API.UserAPI.updateCommentTop(Config.user_id, Config.user_pwd, cid, tid, isTop)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                result
            }
            if (result.ok) {
                val comment = adapter.items[position]
                if (comment.isTop != isTop) {
                    comment.isTop = isTop
                    adapter.notifyChangedEx(position)
                    if (position != 0) {
                        Collections.swap(adapter.items, position, 0)
                        adapter.notifyMovedEx(position, 0)
                    }
                }
                XToastUtils.success(result.value)
            }
            else XToastUtils.error(result.value)
        }
    }

    @NewThread
    private fun deleteTopic() {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) {
                val result = API.UserAPI.deleteTopic(Config.user_id, Config.user_pwd, tid)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                result
            }
            if (result.ok) {
                pages.pop()
                XToastUtils.success(result.value)
            }
            else XToastUtils.error(result.value)
        }
    }

    @NewThread
    private fun updateTopicTop(isTop: Int) {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) {
                val result = API.UserAPI.updateTopicTop(Config.user_id, Config.user_pwd, tid, isTop)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                result
            }
            if (result.ok) XToastUtils.success(result.value)
            else XToastUtils.error(result.value)
        }
    }
}