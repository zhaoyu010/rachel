package com.yinlin.rachel.fragment

import android.annotation.SuppressLint
import android.webkit.WebViewClient
import com.yinlin.rachel.databinding.FragmentWebpageBinding
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelPages

class FragmentWebpage(pages: RachelPages, private val url: String) : RachelFragment<FragmentWebpageBinding>(pages) {
    override fun bindingClass() = FragmentWebpageBinding::class.java

    @SuppressLint("SetJavaScriptEnabled")
    override fun init() {
        val settings = v.web.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        v.web.webViewClient = WebViewClient()
        v.web.loadUrl(url)
    }

    override fun quit() {
        v.web.destroy()
    }

    override fun back(): Boolean {
        if (v.web.canGoBack()) {
            v.web.goBack()
            return false
        }
        return true
    }
}