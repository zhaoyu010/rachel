package com.yinlin.rachel.data

import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale


data class Mail(
    val mid: Long,
    val id: String,
    val ts: String,
    val type: Int,
    var processed: Int,
    val title: String,
    val content: String,
    val filter: String,
    val param1: String?,
    val param2: String?,
    val info: String?,
) {
    companion object {
        const val TYPE_INFO = 1
        const val TYPE_CONFIRM = 2
        const val TYPE_DECISION = 4
        const val TYPE_INPUT = 8
    }

    var isProcessed: Boolean
        get() = processed != 0
        set(value) { processed = if (value) 1 else 0 }
    val date: String get() {
        try {
            val d1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).parse(ts)
            val zonedDateTime = ZonedDateTime.ofInstant(d1!!.toInstant(), ZoneId.systemDefault())
            val d2 = Date.from(zonedDateTime.plusHours(8).toInstant())
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(d2)
        }
        catch (ignored: Exception) { return ts }
    }
}