package com.yinlin.rachel.fragment

import androidx.lifecycle.lifecycleScope
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.Config
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API
import com.yinlin.rachel.bold
import com.yinlin.rachel.content
import com.yinlin.rachel.databinding.FragmentCreateTopicBinding
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.rachelClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentCreateTopic(pages: RachelPages) : RachelFragment<FragmentCreateTopicBinding>(pages) {
    override fun bindingClass() = FragmentCreateTopicBinding::class.java

    override fun init() {
        v.cancel.bold = true
        v.send.bold = true
        v.title.bold = true

        v.picList.init(9, 4)

        v.cancel.rachelClick { pages.pop() }

        v.send.rachelClick {
            val title = v.title.content
            val content = v.content.content
            if (title.isNotEmpty() && content.isNotEmpty()) sendTopic(title, content, v.picList.images)
            else XToastUtils.warning("你还未填写任何内容呢")
        }
    }

    override fun back() = true

    @NewThread
    private fun sendTopic(title: String, content: String, pics: List<String>) {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) {
                val result = API.UserAPI.sendTopic(Config.user_id, Config.user_pwd, title, content, pics)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                result
            }
            if (result.ok) {
                XToastUtils.success(result.value)
                pages.pop()
            }
            else XToastUtils.error(result.value)
        }
    }
}