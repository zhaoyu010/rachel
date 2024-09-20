package com.yinlin.rachel.fragment

import androidx.lifecycle.lifecycleScope
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.Config
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API
import com.yinlin.rachel.content
import com.yinlin.rachel.databinding.FragmentLoginBinding
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.rachelClick
import com.yinlin.rachel.toMD5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FragmentLogin(pages: RachelPages) : RachelFragment<FragmentLoginBinding>(pages) {
    override fun bindingClass() = FragmentLoginBinding::class.java

    override fun init() {
        // 没有账号
        v.labelRegister.rachelClick {
            v.loginContainer.collapse(false)
            v.registerId.content = ""
            v.registerPwd.content = ""
            v.registerConfirmPwd.content = ""
            v.registerInviter.content = ""
            v.registerContainer.expand(true)
        }
        // 忘记密码
        v.labelNopwd.rachelClick { forgotPassword() }
        // 登录
        v.buttonLogin.rachelClick { login() }
        // 返回登录
        v.labelBackLogin.rachelClick {
            v.registerContainer.collapse(false)
            v.loginId.content = ""
            v.loginId.content = ""
            v.loginPwd.content = ""
            v.loginContainer.expand(true)
        }
        // 注册
        v.buttonRegister.rachelClick { register() }
    }

    override fun back(): Boolean = true

    @NewThread
    fun login() {
        val id = v.loginId.content
        val pwd = v.loginPwd.content
        if (id.isNotEmpty() && pwd.isNotEmpty()) {
            val pwdMd5 = pwd.toMD5()
            lifecycleScope.launch {
                pages.loadingDialog.show()
                val result = withContext(Dispatchers.IO) {
                    val result = API.UserAPI.login(id, pwdMd5)
                    withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                    result
                }
                if (result.ok) {
                    Config.user_id = id
                    Config.user_pwd = pwdMd5
                    pages.pop()
                    pages.sendMessage(RachelPages.me, RachelMessage.ME_REQUEST_USER_INFO)
                }
                else XToastUtils.error(result.value)
            }
        }
        else XToastUtils.warning("ID和密码不能为空")
    }

    @NewThread
    fun register() {
        val id = v.registerId.content
        val pwd = v.registerPwd.content
        val confirmPwd = v.registerConfirmPwd.content
        val inviter = v.registerInviter.content
        if (id.isNotEmpty() && pwd.isNotEmpty() && confirmPwd.isNotEmpty() && inviter.isNotEmpty()) {
            if (pwd == confirmPwd) {
                lifecycleScope.launch {
                    pages.loadingDialog.show()
                    val result = withContext(Dispatchers.IO) {
                        val result = API.UserAPI.register(id, pwd.toMD5(), inviter)
                        withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                        result
                    }
                    if (result.ok) {
                        XToastUtils.success(result.value)
                        v.registerContainer.collapse(false)
                        v.loginId.content = id
                        v.loginPwd.content = ""
                        v.loginContainer.expand(true)
                    }
                    else XToastUtils.error(result.value)
                }
            }
            else XToastUtils.warning("两次输入的密码不相同")
        }
        else XToastUtils.warning("ID、密码、邀请人均不能为空")
    }

    @NewThread
    fun forgotPassword() {
        val id = v.loginId.content
        val pwd = v.loginPwd.content
        if (id.isNotEmpty() && pwd.isNotEmpty()) {
            val pwdMd5 = pwd.toMD5()
            lifecycleScope.launch {
                pages.loadingDialog.show()
                val result = withContext(Dispatchers.IO) {
                    val result = API.UserAPI.forgotPassword(id, pwdMd5)
                    withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                    result
                }
                if (result.ok) XToastUtils.success(result.value)
                else XToastUtils.error(result.value)
            }
        }
        else XToastUtils.warning("ID和新密码不能为空")
    }
}