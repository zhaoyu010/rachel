package com.yinlin.rachel.fragment

import android.graphics.drawable.ColorDrawable
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.Config
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.RachelMessage.*
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API
import com.yinlin.rachel.api.Arg
import com.yinlin.rachel.bold
import com.yinlin.rachel.databinding.FragmentMeBinding
import com.yinlin.rachel.err
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.rachelClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FragmentMe(pages: RachelPages) : RachelFragment<FragmentMeBinding>(pages) {
    private val rilNet = RachelImageLoader(pages.context, R.drawable.placeholder_pic, DiskCacheStrategy.ALL)

    override fun bindingClass() = FragmentMeBinding::class.java

    private val isLogin: Boolean get() = !Config.user_id_meta.isDefault() && !Config.user_pwd_meta.isDefault()

    override fun init() {
        v.id.bold = true
        v.tvLevel.bold = true
        v.tvCoin.bold = true

        // 设置
        v.buttonSettings.rachelClick { pages.gotoSecond(FragmentSettings(pages)) }

        // 下拉刷新
        v.container.setOnRefreshListener {
            if (isLogin) {
                v.container.finishRefresh()
                requestUserInfo()
            }
            else pages.gotoSecond(FragmentLogin(pages))
        }

        // 检查更新
        v.buttonUpdate.rachelClick { checkUpdate() }

        // 签到
        v.buttonSignIn.rachelClick { signIn() }

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
            v.title.setTitle(user.titleGroup!!, user.title!!)
            v.signature.text = user.signature
            v.level.text = user.level.toString()
            v.coin.text = user.coin.toString()
            v.avatar.load(rilNet, user.avatarPath, Config.cache_key_avatar.get())
            v.wall.load(rilNet, user.wallPath, Config.cache_key_wall.get())
        }
        else {
            v.id.text = pages.getResString(R.string.default_id)
            v.title.setTitle(1, pages.getResString(R.string.default_title))
            v.signature.text = pages.getResString(R.string.default_signature)
            v.level.text = "1"
            v.coin.text = "0"
            v.avatar.load(pages.ril, R.drawable.placeholder_pic)
            v.wall.setImageDrawable(ColorDrawable(pages.getResColor(R.color.dark)))
        }
    }

    // 请求用户信息
    @NewThread
    private fun requestUserInfo() {
        if (isLogin) {
            lifecycleScope.launch {
                pages.loadingDialog.show()
                Config.user = withContext(Dispatchers.IO) { API.UserAPI.getInfo(Arg.Login(Config.user_id, Config.user_pwd)) }
                pages.loadingDialog.dismiss()
                updateUserInfo()
            }
        }
    }

    // 检查更新
    @NewThread
    private fun checkUpdate() {
        val srcVersion = pages.context.packageManager.getPackageInfo(pages.context.packageName, 0).longVersionCode
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) { API.SysAPI.getServerVersionCode() }
            pages.loadingDialog.dismiss()
            val content = "APP版本:${srcVersion}\n服务器版本:${result.value1}\n最低兼容版本:${result.value2}"
            result.ok.err(content) {
                if (srcVersion == result.value1) XToastUtils.success(content)
                else XToastUtils.warning(content)
            }
        }
    }

    // 签到
    @NewThread
    private fun signIn() {
        if (isLogin) {
            lifecycleScope.launch {
                pages.loadingDialog.show()
                val result = withContext(Dispatchers.IO) { API.UserAPI.signIn(Arg.Login(Config.user_id, Config.user_pwd)) }
                pages.loadingDialog.dismiss()
                result.ok.err(result.value) {
                    requestUserInfo()
                    XToastUtils.success(result.value)
                }
            }
        }
    }
}