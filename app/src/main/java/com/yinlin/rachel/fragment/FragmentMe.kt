package com.yinlin.rachel.fragment

import android.graphics.drawable.ColorDrawable
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.Config
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.RachelMessage.ME_REQUEST_USER_INFO
import com.yinlin.rachel.RachelMessage.ME_UPDATE_USER_INFO
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API
import com.yinlin.rachel.bold
import com.yinlin.rachel.databinding.FragmentMeBinding
import com.yinlin.rachel.dialog.DialogAbout
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

    private val dialogAbout = DialogAbout(this)

    override fun init() {
        dialogAbout.init()

        v.id.bold = true
        v.tvLevel.bold = true
        v.tvCoin.bold = true

        // 扫码
        v.buttonScan.rachelClick {

        }

        // 名片
        v.buttonProfile.rachelClick {

        }

        // 设置
        v.buttonSettings.rachelClick { pages.navigate(FragmentSettings(pages)) }

        // 更新
        v.buttonUpdate.rachelClick { checkUpdate() }

        // 关于
        v.buttonAbout.rachelClick { dialogAbout.update().show() }

        // 签到
        v.buttonSignIn.rachelClick {
            if (isLogin) signIn()
            else {
                XToastUtils.warning("请先登录")
                pages.navigate(FragmentLogin(pages))
            }
        }

        // 好友
        v.buttonFriend.rachelClick {

        }

        // 主题
        v.buttonTopic.rachelClick {
            if (isLogin) pages.navigate(FragmentProfile(pages, Config.user_id))
            else {
                XToastUtils.warning("请先登录")
                pages.navigate(FragmentLogin(pages))
            }
        }

        // 邮箱
        v.buttonMail.rachelClick {
            if (isLogin) pages.navigate(FragmentMail(pages))
            else {
                XToastUtils.warning("请先登录")
                pages.navigate(FragmentLogin(pages))
            }
        }

        // 下拉刷新
        v.container.setOnRefreshListener {
            if (isLogin) { requestUserInfo() }
            else {
                v.container.finishRefresh()
                pages.navigate(FragmentLogin(pages))
            }
        }

        // 首次刷新
        if (isLogin) requestUserInfo()
    }

    override fun quit() {
        dialogAbout.release()
    }

    override fun message(msg: RachelMessage, vararg args: Any?) {
        when (msg) {
            ME_REQUEST_USER_INFO -> requestUserInfo()
            ME_UPDATE_USER_INFO -> updateUserInfo()
            else -> { }
        }
    }

    private fun updateUserInfo() {
        val user = Config.user
        if (user.isBroken) {
            v.id.text = pages.getResString(R.string.default_id)
            v.title.setDefaultTitle()
            v.signature.text = pages.getResString(R.string.default_signature)
            v.level.text = "1"
            v.coin.text = "0"
            v.avatar.setImageDrawable(ColorDrawable(pages.getResColor(R.color.white)))
            v.wall.setImageDrawable(ColorDrawable(pages.getResColor(R.color.dark)))
        }
        else {
            v.id.text = Config.user_id
            v.title.setTitle(user.titleGroup, user.title)
            v.signature.text = user.signature
            v.level.text = user.level.toString()
            v.coin.text = user.coin.toString()
            v.avatar.load(rilNet, user.avatarPath, Config.cache_key_avatar.get())
            v.wall.load(rilNet, user.wallPath, Config.cache_key_wall.get())
        }
    }

    // 请求用户信息
    @NewThread
    private fun requestUserInfo() {
        if (isLogin) {
            lifecycleScope.launch {
                pages.loadingDialog.show()
                Config.user = withContext(Dispatchers.IO) { API.UserAPI.getInfo(Config.user_id, Config.user_pwd) }
                if (v.container.isRefreshing) v.container.finishRefresh()
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
            if (result.ok) {
                if (srcVersion == result.value1) XToastUtils.success(content)
                else XToastUtils.warning(content)
            }
            else XToastUtils.error(content)
        }
    }

    // 签到
    @NewThread
    private fun signIn() {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) { API.UserAPI.signIn(Config.user_id, Config.user_pwd) }
            pages.loadingDialog.dismiss()
            if (result.ok) {
                requestUserInfo()
                XToastUtils.success(result.value)
            }
            else XToastUtils.error(result.value)
        }
    }
}