package com.yinlin.rachel.fragment

import android.annotation.SuppressLint
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.Config
import com.yinlin.rachel.model.RachelDialog
import com.yinlin.rachel.R
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API
import com.yinlin.rachel.bold
import com.yinlin.rachel.clearAddAll
import com.yinlin.rachel.data.Mail
import com.yinlin.rachel.databinding.FragmentMailBinding
import com.yinlin.rachel.databinding.ItemMailBinding
import com.yinlin.rachel.model.RachelAdapter
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.rachelClick
import com.yinlin.rachel.textColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentMail(pages: RachelPages) : RachelFragment<FragmentMailBinding>(pages) {
    class Adapter(private val pages: RachelPages, private val fragment: FragmentMail) : RachelAdapter<ItemMailBinding, Mail>() {
        override fun bindingClass() = ItemMailBinding::class.java

        override fun init(holder: RachelViewHolder<ItemMailBinding>, v: ItemMailBinding) {
            v.title.bold = true
            v.buttonYes.rachelClick {
                val position = holder.bindingAdapterPosition
                val mail = items[position]
                if (mail.isProcessed) XToastUtils.error("此邮件已处理")
                else {
                    RachelDialog.confirm(pages.context, "处理此邮件?") { _, _ ->
                        processMail(position, mail, true)
                    }
                }
            }
            v.buttonNo.rachelClick {
                val position = holder.bindingAdapterPosition
                val mail = items[position]
                if (mail.isProcessed) XToastUtils.error("此邮件已处理")
                else {
                    RachelDialog.confirm(pages.context, "拒绝此邮件?") { _, _ ->
                        processMail(position, mail, false)
                    }
                }
            }
            v.buttonDelete.rachelClick {
                val position = holder.bindingAdapterPosition
                val mail = items[position]
                if (mail.isProcessed) {
                    RachelDialog.confirm(pages.context, "删除此邮件?") { _, _ ->
                        deleteMail(position, mail)
                    }
                }
                else XToastUtils.warning("你还没有处理这封邮件!")
            }
        }

        override fun update(v: ItemMailBinding, item: Mail, position: Int) {
            when (item.type) {
                Mail.TYPE_INFO -> {
                    v.label.labelText = "通知"
                    v.label.labelBackgroundColor = pages.getResColor(R.color.steel_blue)
                    v.buttonYes.visibility = View.GONE
                    v.buttonNo.visibility = View.GONE
                }
                Mail.TYPE_CONFIRM -> {
                    v.label.labelText = "验证"
                    v.label.labelBackgroundColor = pages.getResColor(R.color.green)
                    v.buttonYes.visibility = View.VISIBLE
                    v.buttonNo.visibility = View.GONE
                }
                Mail.TYPE_DECISION -> {
                    v.label.labelText = "决定"
                    v.label.labelBackgroundColor = pages.getResColor(R.color.purple)
                    v.buttonYes.visibility = View.VISIBLE
                    v.buttonNo.visibility = View.VISIBLE
                }
                Mail.TYPE_INPUT -> {
                    v.label.labelText = "输入"
                    v.label.labelBackgroundColor = pages.getResColor(R.color.orange)
                    v.buttonYes.visibility = View.GONE
                    v.buttonNo.visibility = View.GONE
                }
                else -> {
                    v.label.labelText = "未知"
                    v.label.labelBackgroundColor = pages.getResColor(R.color.red)
                    v.buttonYes.visibility = View.GONE
                    v.buttonNo.visibility = View.GONE
                }
            }
            if (item.isProcessed) {
                v.buttonYes.visibility = View.GONE
                v.buttonNo.visibility = View.GONE
            }
            v.title.text = item.title
            v.title.textColor = pages.getResColor(if (item.isProcessed) R.color.black else R.color.steel_blue)
            v.content.text = item.content
            v.date.text = item.date
            v.contentDetails.text = item.content
            v.buttonDelete.visibility = if (item.isProcessed) View.VISIBLE else View.GONE
        }

        override fun onItemClicked(v: ItemMailBinding, item: Mail, position: Int) {
            if (v.expander.isExpanded) {
                val drawable = ContextCompat.getDrawable(pages.context, R.drawable.svg_expand_gray)
                v.content.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null)
                v.expander.collapse(true)
            }
            else {
                val drawable = ContextCompat.getDrawable(pages.context, R.drawable.svg_expand_gray_down)
                v.content.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null)
                v.expander.expand(true)
            }
        }

        @NewThread
        private fun processMail(position: Int, mail: Mail, confirm: Boolean) {
            fragment.lifecycleScope.launch {
                pages.loadingDialog.show()
                val result = withContext(Dispatchers.IO) {
                    val result = API.UserAPI.processMail(Config.user_id, Config.user_pwd, mail.mid, confirm, mail.info)
                    withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                    result
                }
                if (result.ok) {
                    mail.isProcessed = true
                    notifyItemChanged(position)
                    XToastUtils.success(result.value)
                }
                else XToastUtils.error(result.value)
            }
        }

        @NewThread
        private fun deleteMail(position: Int, mail: Mail) {
            fragment.lifecycleScope.launch {
                pages.loadingDialog.show()
                val result = withContext(Dispatchers.IO) {
                    val result = API.UserAPI.deleteMail(Config.user_id, Config.user_pwd, mail.mid)
                    withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                    result
                }
                if (result.ok) {
                    items.removeAt(position)
                    notifyItemRemoved(position)
                    XToastUtils.success(result.value)
                }
                else XToastUtils.error(result.value)
            }
        }
    }

    private val adapter = Adapter(pages, this)

    override fun bindingClass() = FragmentMailBinding::class.java

    override fun init() {
        // 列表
        v.list.layoutManager = LinearLayoutManager(pages.context)
        v.list.recycledViewPool.setMaxRecycledViews(0, 10)
        v.list.setHasFixedSize(true)
        v.list.adapter = adapter

        // 下拉刷新
        v.container.setOnRefreshListener { loadMail() }

        // 首次刷新
        loadMail()
    }

    override fun back() = true

    @NewThread @SuppressLint("NotifyDataSetChanged")
    private fun loadMail() {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val mails = withContext(Dispatchers.IO) {
                val mails = API.UserAPI.getMail(Config.user_id, Config.user_pwd)
                withContext(Dispatchers.Main) {
                    if (v.container.isRefreshing) v.container.finishRefresh()
                    pages.loadingDialog.dismiss()
                }
                mails
            }
            if (mails.isEmpty()) v.state.showEmpty()
            else v.state.showContent()
            adapter.items.clearAddAll(mails)
            adapter.notifyDataSetChanged()
        }
    }
}