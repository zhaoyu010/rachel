package com.yinlin.rachel.data

import com.yinlin.rachel.api.API

@JvmRecord
data class User(
    val isBroken: Boolean = false,
    // ID
    val id: String = "",
    // 邀请人
    val inviter: String? = null,
    // 权限
    // 1 备份
    // 2 下载美图
    // 4 主题发表与评论
    // 8 未定
    // 16 账号管理
    // 32 主题管理
    // 64 日历管理
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
) {
    val avatarPath: String get() = "${API.BASEURL}/user/${id}/avatar.webp"
    val wallPath: String get() = "${API.BASEURL}/user/${id}/wall.webp"
}