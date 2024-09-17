package com.yinlin.rachel.data

import com.yinlin.rachel.api.API
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale

@JvmRecord
data class Topic (
    val isBroken: Boolean = false,
    val tid: Int = 0,
    val id: String = "",
    val ts: String = "",
    val title: String = "",
    val content: String = "",
    val pics: MutableList<String> = arrayListOf(),
    val isTop: Int = 0,
    val coinNum: Int = 0,
    val commentNum: Int = 0,
    val userTitle: String = "",
    val userTitleGroup: Int = 0,
    val comments: MutableList<Comment> = arrayListOf(),
) {
    val isTopTopic: Boolean get() = isTop == 1
    val date: String get() {
        try {
            val d1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).parse(ts)
            val zonedDateTime = ZonedDateTime.ofInstant(d1!!.toInstant(), ZoneId.systemDefault())
            val d2 = Date.from(zonedDateTime.plusHours(8).toInstant())
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(d2)
        }
        catch (ignored: Exception) { return ts }
    }
    val avatarPath: String get() = "${API.BASEURL}/user/${id}/avatar.webp"
    fun picPath(pic: String) = "${API.BASEURL}/user/${id}/pics/${pic}.webp"
}