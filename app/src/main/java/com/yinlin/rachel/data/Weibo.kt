package com.yinlin.rachel.data

import com.yinlin.rachel.model.RachelPreview

data class Weibo(
    var name: String, // 昵称
    var avatar: String, // 头像
    var text: String, // 内容
    var time: String, // 时间
    var location: String, // 定位
    var id: String, // 编号
    val pictures: MutableList<RachelPreview> = ArrayList(), // 图片集
)