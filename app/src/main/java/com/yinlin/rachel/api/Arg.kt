package com.yinlin.rachel.api

object Arg {
    @JvmRecord
    data class Login(val id: String, val pwd: String)
    @JvmRecord
    data class Register(val id: String, val pwd: String, val inviter: String)
    @JvmRecord
    data class Playlist(val id: String, val pwd: String, val playlist: String)
    @JvmRecord
    data class Signature(val id: String, val pwd: String, val signature: String)
    @JvmRecord
    data class ProcessMail(val id: String, val pwd: String, val mid: Long, val confirm: Boolean, val arg: String?)
    @JvmRecord
    data class DeleteMail(val id: String, val pwd: String, val mid: Long)
}