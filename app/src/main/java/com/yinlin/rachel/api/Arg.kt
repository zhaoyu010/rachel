package com.yinlin.rachel.api

object Arg {
    @JvmRecord
    data class Login(val id: String, val pwd: String)
    @JvmRecord
    data class Register(val id: String, val pwd: String, val inviter: String)
    @JvmRecord
    data class UploadPlaylist(val id: String, val pwd: String, val playlist: String)
}