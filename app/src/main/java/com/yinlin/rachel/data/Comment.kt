package com.yinlin.rachel.data

import com.yinlin.rachel.api.API
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale

data class Comment (
    val cid: Long,
    val id: String,
    val ts: String,
    val content: String,
    var isTop: Int,
    val userTitle: String,
    val userTitleGroup: Int,
) {
    val isTopComment: Boolean get() = isTop == 1
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
}