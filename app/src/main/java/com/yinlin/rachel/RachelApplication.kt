package com.yinlin.rachel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import com.tencent.mmkv.MMKV
import com.xuexiang.xui.XUI
import com.xuexiang.xui.widget.toast.XToast
import org.libpag.PAGFont
import java.io.PrintWriter
import java.io.StringWriter


class RachelApplication : Application() {
    class CrashHandler(val context: Context) : Thread.UncaughtExceptionHandler {
        private val sw = StringWriter()
        private val pw = PrintWriter(sw)

        override fun uncaughtException(t: Thread, e: Throwable) {
            e.printStackTrace(pw)
            val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.setPrimaryClip(ClipData.newPlainText("label", sw.toString()))
        }

        fun println(obj: Any) = pw.println(obj)
    }

    val crashHandler = CrashHandler(this)

    lateinit var fontNormal: Typeface
    lateinit var fontBold: Typeface
    lateinit var fontItalic: Typeface
    lateinit var fontBoldItalic: Typeface

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(crashHandler)

        // 初始化XUI
        XUI.init(this)
        XUI.initFontStyle("fonts/hyyx.ttf")
        XToast.Config.get().setTextSize(14).setGravity(Gravity.CENTER)

        // 初始化字体
        fontNormal = XUI.getDefaultTypeface()!!
        fontBold = Typeface.create(fontNormal, Typeface.BOLD)
        fontItalic = Typeface.create(fontNormal, Typeface.ITALIC)
        fontBoldItalic = Typeface.create(fontNormal, Typeface.BOLD_ITALIC)

        PAGFont.RegisterFont(assets, "fonts/hyyx.ttf")

        // 初始化目录
        basePath = filesDir.path
        pathAPP.createAll()
        pathMusic.createAll()

        // 初始化MMKV
        MMKV.initialize(this)
        Config.kv = MMKV.defaultMMKV()
    }
}