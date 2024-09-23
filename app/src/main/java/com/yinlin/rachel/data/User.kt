package com.yinlin.rachel.data

import com.yinlin.rachel.api.API

@JvmRecord
data class User(
    val ok: Boolean = false,
    // ID
    val id: String = "",
    // 邀请人
    val inviter: String? = null,
    // 权限
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
    companion object {
        // 64 日历管理
        const val PRIVILEGE_BACKUP = 1 // 备份
        const val PRIVILEGE_RES = 2 // 美图
        const val PRIVILEGE_TOPIC = 4 // 主题
        const val PRIVILEGE_UNUSED = 8 // 未定
        const val PRIVILEGE_VIP_ACCOUNT = 16 // 账号管理
        const val PRIVILEGE_VIP_TOPIC = 32 // 主题管理
        const val PRIVILEGE_VIP_CALENDAR = 64 // 日历管理
    }

    val avatarPath: String get() = "${API.BASEURL}/user/${id}/avatar.webp"
    val wallPath: String get() = "${API.BASEURL}/user/${id}/wall.webp"

    fun hasPrivilege(mask: Int) = (privilege and mask) != 0

    fun canUpdateTopicTop(topicId: String) = id == topicId
    fun canDeleteTopic(topicId: String) = id == topicId || hasPrivilege(PRIVILEGE_VIP_ACCOUNT)
    fun canUpdateCommentTop(topicId: String) = id == topicId || hasPrivilege(PRIVILEGE_VIP_ACCOUNT)
    fun canDeleteComment(topicId: String, commentId: String) = id == topicId || id == commentId || hasPrivilege(PRIVILEGE_VIP_ACCOUNT)
}