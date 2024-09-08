package com.yinlin.rachel.data

import com.yinlin.rachel.api.API

class User {
    // 邀请人
    var inviter: String? = null
    // 权限
    // 1
    // 2
    // 4
    // 8
    // 16
    // 32
    // 64
    var privilege: Int? = null
    // 个性签名
    var signature: String? = null
    // 头衔
    var title: String? = null
    // 头衔所在组
    var titleGroup: Int? = null
    // 等级
    var level: Int? = null
    // 银币
    var coin: Int? = null
    // 头像
    var avatar: String? = null
    // 背景墙
    var wall: String? = null
    val avatarPath: String get() = API.BASEURL + avatar
    val wallPath: String get() = API.BASEURL + wall
    val isActive: Boolean get() = level != null && coin != null
}