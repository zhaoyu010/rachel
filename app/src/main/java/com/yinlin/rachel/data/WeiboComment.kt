package com.yinlin.rachel.data

data class WeiboComment (
    var type: Type, // 评论类型
    var name: String, // 昵称
    var avatar: String, // 头像
    var text: String, // 内容
    var time: String, // 时间
    var location: String, // 定位
    var pic: String = "", // 图片
) {
    enum class Type { Comment, SubComment }
}