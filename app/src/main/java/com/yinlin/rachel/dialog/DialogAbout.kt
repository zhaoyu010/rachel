package com.yinlin.rachel.dialog

import android.content.Intent
import android.net.Uri
import android.view.View
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.bold
import com.yinlin.rachel.databinding.DialogAboutBinding
import com.yinlin.rachel.fragment.FragmentMe
import com.yinlin.rachel.interceptScroll
import com.yinlin.rachel.model.RachelBottomDialog
import com.yinlin.rachel.rachelClick


class DialogAbout(fragment: FragmentMe) : RachelBottomDialog<DialogAboutBinding, FragmentMe>(fragment, 0.9f) {
    override fun bindingClass() = DialogAboutBinding::class.java

    override fun init() {
        super.init()
        v.pic.rachelClick {
            try {
                val url = "mqqapi://card/show_pslcard?src_type=internal&version=1&uin=828049503&card_type=group&source=qrcode"
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                XToastUtils.error("未安装QQ")
            }
        }
        v.title.bold = true
        val listener = View.OnClickListener { gotoQQ(it.tag as String) }
        v.text1.rachelClick(listener)
        v.text2.rachelClick(listener)
        v.text3.rachelClick(listener)
        v.text4.rachelClick(listener)
        v.text5.rachelClick(listener)
        v.text6.rachelClick(listener)
        v.text7.rachelClick(listener)
        v.text8.rachelClick(listener)
        v.text9.rachelClick(listener)
        v.text10.rachelClick(listener)
        v.text11.rachelClick(listener)
        v.text12.rachelClick(listener)
        v.text13.rachelClick(listener)
        v.text14.rachelClick(listener)
        v.text15.rachelClick(listener)
        v.text16.rachelClick(listener)
        v.text17.rachelClick(listener)
        v.contentContainer.interceptScroll()
    }

    private fun gotoQQ(qq: String) {
        try {
            val url = "mqqapi://card/show_pslcard?src_type=internal&version=1&uin=${qq}&card_type=person&source=qrcode"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            XToastUtils.error("未安装QQ")
        }
    }

    fun update(): DialogAbout {
        v.contentContainer.scrollTo(0, 0)
        return this
    }
}