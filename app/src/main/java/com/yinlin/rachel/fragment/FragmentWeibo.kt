package com.yinlin.rachel.fragment

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.Net
import com.yinlin.rachel.R
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.WeiboAPI
import com.yinlin.rachel.bold
import com.yinlin.rachel.data.Weibo
import com.yinlin.rachel.data.WeiboComment
import com.yinlin.rachel.databinding.FragmentWeiboBinding
import com.yinlin.rachel.databinding.ItemWeiboCommentBinding
import com.yinlin.rachel.fragment.FragmentMsg.ImageAdapter
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelAdapter
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.model.RachelOnClickListener
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.pureColor
import com.yinlin.rachel.rachelClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter

class FragmentWeibo(pages: RachelPages, private val weibo: Weibo) : RachelFragment<FragmentWeiboBinding>(pages) {
    class Adapter(fragment: FragmentWeibo, private val rilNet: RachelImageLoader) : RachelAdapter<ItemWeiboCommentBinding, WeiboComment>() {
        private val pages = fragment.pages

        override fun bindingClass() = ItemWeiboCommentBinding::class.java

        override fun init(holder: RachelViewHolder<ItemWeiboCommentBinding>, v: ItemWeiboCommentBinding) {
            v.name.bold = true
            v.text.setOnClickATagListener { _, _, _ -> true }
            v.pic.setOnLongClickListener {
                val position = holder.bindingAdapterPosition
                Net.downloadPicture(pages.context, items[position].pic)
                true
            }
        }

        override fun update(v: ItemWeiboCommentBinding, item: WeiboComment, position: Int) {
            v.placeholder.visibility = if (item.type == WeiboComment.Type.Comment) View.GONE else View.VISIBLE
            v.name.text = item.name
            v.avatar.load(rilNet, item.avatar)
            v.time.text = item.time
            v.location.text = item.location
            v.text.setHtml(item.text, HtmlHttpImageGetter(v.text))
            if (item.pic.isNotEmpty()) v.pic.load(rilNet, item.pic)
            else v.pic.pureColor = 0
        }
    }

    private val rilNet = RachelImageLoader(pages.context, R.drawable.placeholder_pic, DiskCacheStrategy.ALL)
    private val adapter = Adapter(this, rilNet)

    override fun bindingClass() = FragmentWeiboBinding::class.java

    override fun init() {
        // 微博
        v.name.bold = true
        v.pics.setAdapter(ImageAdapter(rilNet))
        v.text.setOnClickATagListener { _, _, _ -> true }
        val expandListener = RachelOnClickListener {
            if (v.expander.isExpanded) v.expander.collapse(true)
            else v.expander.expand(true)
        }
        v.weiboCard.rachelClick(expandListener)
        v.name.rachelClick(expandListener)
        v.time.rachelClick(expandListener)
        v.location.rachelClick(expandListener)
        v.text.rachelClick(expandListener)

        v.name.text = weibo.name
        v.avatar.load(rilNet, weibo.avatar)
        v.time.text = weibo.time
        v.location.text = weibo.location
        v.text.setHtml(weibo.text, HtmlHttpImageGetter(v.text))
        v.pics.setImagesData(weibo.pictures)
        v.pics.setItemImageLongClickListener { _, index, items ->
            val item = items[index] as Weibo.Picture
            if (item.type == Weibo.MsgType.VIDEO) Net.downloadVideo(pages.context, item.source)
            else Net.downloadPicture(pages.context, item.source)
            true
        }

        v.list.layoutManager = LinearLayoutManager(pages.context)
        v.list.recycledViewPool.setMaxRecycledViews(0, 10)
        v.list.adapter = adapter

        requestComment()
    }

    override fun back() = true

    // 刷新评论
    @NewThread @SuppressLint("NotifyDataSetChanged")
    fun requestComment() {
        lifecycleScope.launch {
            val comments = ArrayList<WeiboComment>()
            withContext(Dispatchers.IO) { WeiboAPI.details(weibo.id, comments) }
            if (comments.isNotEmpty()) {
                adapter.items = comments
                adapter.notifyDataSetChanged()
            }
        }
    }
}