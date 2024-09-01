package com.yinlin.rachel.fragment

import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.Config
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API.login
import com.yinlin.rachel.api.API.register
import com.yinlin.rachel.api.Arg
import com.yinlin.rachel.databinding.FragmentLoginBinding
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelOnClickListener
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.toMD5


class FragmentLogin(pages: RachelPages) : RachelFragment<FragmentLoginBinding>(pages) {
    override fun bindingClass() = FragmentLoginBinding::class.java

    override fun init() {
        // 没有账号
        v.labelRegister.setOnClickListener(RachelOnClickListener {
            v.loginContainer.collapse(false)
            v.registerId.setText("")
            v.registerPwd.setText("")
            v.registerConfirmPwd.setText("")
            v.registerInviter.setText("")
            v.registerContainer.expand(true)
        })
        // 忘记密码
        v.labelNopwd.setOnClickListener(RachelOnClickListener { })
        // 登录
        v.buttonLogin.setOnClickListener(RachelOnClickListener { login() })
        // 返回登录
        v.labelBackLogin.setOnClickListener(RachelOnClickListener {
            v.registerContainer.collapse(false)
            v.loginId.setText("")
            v.loginPwd.setText("")
            v.loginContainer.expand(true)
        })
        // 注册
        v.buttonRegister.setOnClickListener(RachelOnClickListener { register() })
    }

    override fun back(): Boolean = true

    @NewThread
    fun login() {
        val id: String? = v.loginId.text?.toString()
        val pwd: String? = v.loginPwd.text?.toString()
        if (id.isNullOrEmpty() || pwd.isNullOrEmpty()) {
            XToastUtils.error("ID或密码不能为空")
            return
        }
        val pwdMd5 = pwd.toMD5()
        Thread {
            val result = login(Arg.Login(id, pwdMd5))
            post {
                if (result.ok) {
                    Config.user_id.set(id)
                    Config.user_pwd.set(pwdMd5)
                    pages.popSecond()
                    pages.sendMessage(RachelPages.me, RachelMessage.ME_UPDATE_USER_INFO)
                }
                else XToastUtils.error(result.value)
            }
        }.start()
    }

    @NewThread
    fun register() {
        val id = v.registerId.text?.toString()
        val pwd = v.registerPwd.text?.toString()
        val confirmPwd = v.registerConfirmPwd.text?.toString()
        val inviter = v.registerInviter.text?.toString() ?: ""
        if (id.isNullOrEmpty() || pwd.isNullOrEmpty() || confirmPwd.isNullOrEmpty()) {
            XToastUtils.error("ID或密码不能为空")
            return
        }
        if (pwd != confirmPwd) {
            XToastUtils.error("两次输入的密码不相同")
            return
        }
        Thread {
            val result = register(Arg.Register(id, pwd.toMD5(), inviter))
            post {
                if (result.ok) {
                    XToastUtils.success(result.value)
                    v.registerContainer.collapse(false)
                    v.loginId.setText(id)
                    v.loginPwd.setText("")
                    v.loginContainer.expand(true)
                }
                else XToastUtils.error(result.value)
            }
        }.start()
    }
}