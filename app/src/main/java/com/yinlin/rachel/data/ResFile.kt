package com.yinlin.rachel.data

open class ResFile(val parent: ResFolder?, val name: String, val author: String, url: String?) {
    var thumbUrl: String? = null
    var sourceUrl: String? = null
    init {
        url?.apply {
            val dotIndex = this.lastIndexOf('.')
            thumbUrl = if (dotIndex != -1) "${this.substring(0, dotIndex)}.md${this.substring(dotIndex)}" else "$this.md"
            sourceUrl = this
        }
    }
}