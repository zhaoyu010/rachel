package com.yinlin.rachel.dialog

import android.content.Intent
import android.net.Uri
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.bold
import com.yinlin.rachel.data.ShowActivity
import com.yinlin.rachel.databinding.BottomDialogActivityBinding
import com.yinlin.rachel.fragment.FragmentMe
import com.yinlin.rachel.fragment.FragmentWebpage
import com.yinlin.rachel.model.RachelBottomDialog
import com.yinlin.rachel.model.RachelNineGridAdapter
import com.yinlin.rachel.model.RachelNineGridPicture
import com.yinlin.rachel.rachelClick

class BottomDialogActivity(fragment: FragmentMe) : RachelBottomDialog<BottomDialogActivityBinding, FragmentMe>(fragment, 0.8f) {
    override fun bindingClass() = BottomDialogActivityBinding::class.java

    private var showstartUrl: String? = null
    private var damaiUrl: String? = null
    private var maoyanUrl: String? = null

    override fun init() {
        super.init()

        v.title.bold = true
        v.pics.setAdapter(RachelNineGridAdapter(root.pages.context))
        v.showstart.rachelClick {
            if (showstartUrl != null) {
                hide()
                try {
                    root.startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(showstartUrl)))
                }
                catch (ignored: Exception) {
                    XToastUtils.warning("未安装秀动APP")
                }
            }
            else XToastUtils.warning("此平台未售票，请稍后再试")
        }
        v.damai.rachelClick {
            if (damaiUrl != null) {
                hide()
                root.pages.navigate(FragmentWebpage(root.pages, "https://m.damai.cn/shows/item.html?itemId=${damaiUrl!!}"))
            }
            else XToastUtils.warning("此平台未售票，请稍后再试")
        }
        v.maoyan.rachelClick {
            if (maoyanUrl != null) {
                hide()
                root.pages.navigate(FragmentWebpage(root.pages, "https://show.maoyan.com/qqw#/detail/${maoyanUrl!!}"))
            }
            else XToastUtils.warning("此平台未售票，请稍后再试")
        }
    }

    fun update(activity: ShowActivity): BottomDialogActivity {
        showstartUrl = activity.showstart
        damaiUrl = activity.damai
        maoyanUrl = activity.maoyan
        v.title.text = activity.title
        v.content.text = activity.content
        v.pics.setImagesData(RachelNineGridPicture.make(activity.pics) { activity.picPath(it) })
        return this
    }
}