package com.yinlin.rachel

import android.app.Application
import android.graphics.Typeface
import android.view.Gravity
import com.tencent.mmkv.MMKV
import com.xuexiang.xui.XUI
import com.xuexiang.xui.widget.toast.XToast

class RachelApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 初始化XUI
        XUI.init(this)
        XUI.initFontStyle("fonts/hyyx.ttf")
        RachelFont.normal = XUI.getDefaultTypeface()!!
        RachelFont.bold = Typeface.create(RachelFont.normal, Typeface.BOLD)
        RachelFont.italic = Typeface.create(RachelFont.normal, Typeface.ITALIC)
        RachelFont.bold_italic = Typeface.create(RachelFont.normal, Typeface.BOLD_ITALIC)
        XToast.Config.get().setTextSize(14).setGravity(Gravity.CENTER)

        // 初始化目录
        basePath = filesDir.path
        pathAPP.createAll()
        pathMusic.createAll()

        // 初始化MMKV
        MMKV.initialize(this)
        Config.kv = MMKV.defaultMMKV()
    }
}