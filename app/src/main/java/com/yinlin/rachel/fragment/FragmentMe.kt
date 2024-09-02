package com.yinlin.rachel.fragment

import android.graphics.drawable.ColorDrawable
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.yinlin.rachel.Config
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelFont
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.RachelMessage.*
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API
import com.yinlin.rachel.api.Arg
import com.yinlin.rachel.databinding.FragmentMeBinding
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.model.RachelOnClickListener
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.rachelClick


class FragmentMe(pages: RachelPages) : RachelFragment<FragmentMeBinding>(pages) {
    private val rilNet = RachelImageLoader(pages.context, R.drawable.placeholder_pic, DiskCacheStrategy.ALL)

    override fun bindingClass() = FragmentMeBinding::class.java

    override fun init() {
        v.id.typeface = RachelFont.bold
        v.tvLevel.typeface = RachelFont.bold
        v.tvCoin.typeface = RachelFont.bold


        // 设置
        v.buttonSettings.rachelClick { pages.gotoSecond(FragmentSettings(pages)) }

        // 下拉刷新
        v.container.setOnRefreshListener { _ ->
            if (isLogin) requestUserInfo()
            else pages.gotoSecond(FragmentLogin(pages))
        }

        // 首次刷新
        if (isLogin) requestUserInfo()
    }

    override fun message(msg: RachelMessage, arg: Any?) {
        when (msg) {
            ME_REQUEST_USER_INFO -> requestUserInfo()
            ME_UPDATE_USER_INFO -> updateUserInfo()
            else -> { }
        }
    }

    private fun updateUserInfo() {
        val user = Config.user
        if (user.isActive) {  // 更新 UI
            v.id.text = Config.user_id
            v.title.text = user.title
            v.signature.text = user.signature
            v.level.text = user.level.toString()
            v.coin.text = user.coin.toString()
            v.avatar.load(rilNet, user.avatarPath)
            v.wall.load(rilNet, user.wallPath)
        }
        else {
            v.id.text = pages.getResString(R.string.default_id)
            v.title.text = ""
            v.signature.text = pages.getResString(R.string.default_signature)
            v.level.text = "1"
            v.coin.text = "0"
            v.avatar.load(pages.ril, R.drawable.placeholder_pic)
            v.wall.setImageDrawable(ColorDrawable(pages.getResColor(R.color.dark)))
        }
    }

    @NewThread
    private fun requestUserInfo() {
        if (isLogin) {
            Thread {
                Config.user = API.getUserInfo(Arg.Login(Config.user_id, Config.user_pwd))
                post {
                    updateUserInfo()
                    if (v.container.isRefreshing) v.container.finishRefresh()
                }
            }.start()
        }
    }

    private val isLogin: Boolean get() = !Config.user_id_meta.isDefault() && !Config.user_pwd_meta.isDefault()
}