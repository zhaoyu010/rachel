package com.yinlin.rachel.fragment

import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.Config
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelFont
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.RachelMessage.ME_UPDATE_USER_INFO
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API
import com.yinlin.rachel.api.Arg
import com.yinlin.rachel.databinding.FragmentMeBinding
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.model.RachelPages


class FragmentMe(pages: RachelPages) : RachelFragment<FragmentMeBinding>(pages) {
    private val rilNet = RachelImageLoader(pages.context, R.drawable.placeholder_pic, DiskCacheStrategy.ALL)

    override fun bindingClass() = FragmentMeBinding::class.java

    override fun init() {
        v.id.typeface = RachelFont.bold
        v.tvLevel.typeface = RachelFont.bold
        v.tvCoin.typeface = RachelFont.bold

        v.container.setOnRefreshListener { _ ->
            if (isLogin) updateUserInfo()
            else pages.gotoSecond(FragmentLogin(pages))
        }

        // 首次刷新
        if (isLogin) updateUserInfo()
    }

    override fun message(msg: RachelMessage, arg: Any?) {
        when (msg) {
            ME_UPDATE_USER_INFO -> updateUserInfo()
            else -> { }
        }
    }

    @NewThread
    private fun updateUserInfo() {
        Thread {
            Config.user.set(API.getUserInfo(Arg.Login(Config.user_id.get(), Config.user_pwd.get())))
            post {
                val user = Config.user.get()
                if (user.isActive) {  // 更新 UI
                    v.id.text = Config.user_id.get()
                    v.title.text = user.title
                    v.signature.text = user.signature
                    v.level.text = user.level.toString()
                    v.coin.text = user.coin.toString()
                    v.avatar.load(rilNet, user.avatarPath)
                    v.wall.load(rilNet, user.wallPath)
                }
                else XToastUtils.error("网络异常, 请稍后再试")
                if (v.container.isRefreshing) v.container.finishRefresh()
            }
        }.start()
    }

    private val isLogin: Boolean get() = !Config.user_id.isDefault() && !Config.user_pwd.isDefault()
}