package com.yinlin.rachel.fragment

import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.Config
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API.login
import com.yinlin.rachel.api.API.register
import com.yinlin.rachel.api.Arg
import com.yinlin.rachel.content
import com.yinlin.rachel.databinding.FragmentLoginBinding
import com.yinlin.rachel.err
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelOnClickListener
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.rachelClick
import com.yinlin.rachel.toMD5


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
        v.labelNopwd.rachelClick { }
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
        (id.isNotEmpty() && pwd.isNotEmpty()).err("ID或密码不能为空") {
            val pwdMd5 = pwd.toMD5()
            Thread {
                val result = login(Arg.Login(id, pwdMd5))
                post {
                    result.ok.err(result.value) {
                        Config.user_id = id
                        Config.user_pwd = pwdMd5
                        pages.popSecond()
                        pages.sendMessage(RachelPages.me, RachelMessage.ME_REQUEST_USER_INFO)
                    }
                }
            }.start()
        }
    }

    @NewThread
    fun register() {
        val id = v.registerId.content
        val pwd = v.registerPwd.content
        val confirmPwd = v.registerConfirmPwd.content
        val inviter = v.registerInviter.content
        (id.isNotEmpty() && pwd.isNotEmpty() && confirmPwd.isNotEmpty()).err("ID或密码不能为空") {
            (pwd == confirmPwd).err("两次输入的密码不相同") {
                Thread {
                    val result = register(Arg.Register(id, pwd.toMD5(), inviter))
                    post {
                        result.ok.err(result.value) {
                            XToastUtils.success(result.value)
                            v.registerContainer.collapse(false)
                            v.loginId.content = id
                            v.loginPwd.content = ""
                            v.loginContainer.expand(true)
                        }
                    }
                }.start()
            }
        }
    }
}