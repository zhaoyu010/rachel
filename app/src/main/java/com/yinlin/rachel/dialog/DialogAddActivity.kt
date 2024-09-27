package com.yinlin.rachel.dialog

import android.annotation.SuppressLint
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.haibin.calendarview.Calendar
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.content
import com.yinlin.rachel.data.ShowActivity
import com.yinlin.rachel.databinding.DialogAddActivityBinding
import com.yinlin.rachel.date
import com.yinlin.rachel.model.RachelDialog
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.rachelClick

class DialogAddActivity(private val pages: RachelPages, private val calendar: Calendar) : RachelDialog<DialogAddActivityBinding>(pages.context) {
    private val schemeAPPWebView = WebView(pages.context)

    override fun bindingClass() = DialogAddActivityBinding::class.java

    @SuppressLint("SetJavaScriptEnabled")
    override fun init() {
        v.pics.init(9, 4)

        v.fetchShowstart.rachelClick {
            val showstart = v.showstart.content
            if (showstart.isEmpty()) XToastUtils.warning("还没有输入秀动ID")
            else schemeAPPWebView.loadUrl("https://wap.showstart.com/pages/activity/detail/detail?activityId=${showstart}")
        }

        val settings = schemeAPPWebView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        schemeAPPWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (view.progress == 100) {
                    if (url.contains("showstart")) {
                        val script = "document.getElementById('openApp').click();"
                        view.evaluateJavascript(script, null)
                    }
                }
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                if (request.url.scheme == "mlink") {
                    v.showstart.content = request.url.toString()
                    XToastUtils.success("提取秀动链接成功")
                }
            }
        }
    }

    override fun ok() {
        schemeAPPWebView.destroy()
        val title = v.title.content
        val content = v.content.content
        if (title.isEmpty() || content.isEmpty()) XToastUtils.warning("活动名称或内容不能为空")
        else {
            pages.sendMessage(RachelPages.me, RachelMessage.ME_ADD_ACTIVITY, calendar,
                ShowActivity(true, calendar.date, title, content, v.pics.images,
                    v.showstart.content.ifEmpty { null }, v.damai.content.ifEmpty { null },
                    v.maoyan.content.ifEmpty { null }))
        }
    }

    override fun cancel() {
        schemeAPPWebView.destroy()
    }
}