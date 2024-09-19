package com.yinlin.rachel.data

import com.yinlin.rachel.api.API

data class TopicPreview(
    val tid: Int,
    val id: String,
    val title: String,
    var pic: String?,
    val isTop: Int,
    val coinNum: Int,
    val commentNum: Int,
) {
    val isTopTopic: Boolean get() = isTop == 1
    val picPath: String get() = "${API.BASEURL}/user/${id}/pics/${pic}.webp"
    val avatarPath: String get() = "${API.BASEURL}/user/${id}/avatar.webp"
}