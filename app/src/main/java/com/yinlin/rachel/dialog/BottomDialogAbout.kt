package com.yinlin.rachel.dialog

import android.view.View
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.R
import com.yinlin.rachel.bold
import com.yinlin.rachel.databinding.BottomDialogAboutBinding
import com.yinlin.rachel.fragment.FragmentMe
import com.yinlin.rachel.gotoQQ
import com.yinlin.rachel.gotoQQGroup
import com.yinlin.rachel.interceptScroll
import com.yinlin.rachel.model.RachelBottomDialog
import com.yinlin.rachel.rachelClick


class BottomDialogAbout(fragment: FragmentMe) : RachelBottomDialog<BottomDialogAboutBinding, FragmentMe>(fragment, 0.9f) {
    private val pages = root.pages

    override fun bindingClass() = BottomDialogAboutBinding::class.java

    override fun init() {
        super.init()
        v.pic.rachelClick {
            if (!gotoQQGroup(pages.context, pages.getResString(R.string.qqgroup_main))) XToastUtils.error("未安装QQ")
        }

        v.title.bold = true

        v.contentContainer.interceptScroll()

        val listener = View.OnClickListener {
            if (!gotoQQ(pages.context, it.tag as String)) XToastUtils.error("未安装QQ")
        }

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
    }

    fun update(): BottomDialogAbout {
        v.contentContainer.scrollTo(0, 0)
        return this
    }
}