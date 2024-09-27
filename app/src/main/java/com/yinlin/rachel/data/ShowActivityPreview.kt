package com.yinlin.rachel.data

import com.haibin.calendarview.Calendar
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

open class ShowActivityPreview(
    val ts: String,
    val title: String,
) {
    val calendar: Calendar? get() = try {
        val d = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).parse(ts)
        val zonedDateTime = ZonedDateTime.ofInstant(d!!.toInstant(), ZoneId.systemDefault()).plusHours(8)
        Calendar().apply {
            year = zonedDateTime.year
            month = zonedDateTime.monthValue
            day = zonedDateTime.dayOfMonth
            scheme = title
        }
    }
    catch (ignored: Exception) { null }
}