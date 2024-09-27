package com.yinlin.rachel.data

import com.yinlin.rachel.api.API

class ShowActivity(
    val ok: Boolean = false,
    ts: String = "",
    title: String = "",
    val content: String = "",
    val pics: List<String> = emptyList(),
    val showstart: String? = null,
    val damai: String? = null,
    val maoyan: String? = null
) : ShowActivityPreview(ts, title) {
    fun picPath(pic: String) = "${API.BASEURL}/activity/${pic}.webp"
}