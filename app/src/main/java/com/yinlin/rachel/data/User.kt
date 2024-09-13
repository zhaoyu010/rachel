package com.yinlin.rachel.data

import com.yinlin.rachel.api.API

@JvmRecord
data class User(
    val isBroken: Boolean = false,
    // 邀请人
    val inviter: String? = null,
    // 权限
    // 1
    // 2
    // 4
    // 8
    // 16
    // 32
    // 64
    val privilege: Int = 0,
    // 个性签名
    val signature: String = "",
    // 头衔
    val title: String = "",
    // 头衔所在组
    val titleGroup: Int = 0,
    // 等级
    val level: Int = 0,
    // 银币
    val coin: Int = 0,
    // 头像
    val avatar: String = "",
    // 背景墙
    val wall: String = "",
) {
    val avatarPath: String get() = API.BASEURL + avatar
    val wallPath: String get() = API.BASEURL + wall
}