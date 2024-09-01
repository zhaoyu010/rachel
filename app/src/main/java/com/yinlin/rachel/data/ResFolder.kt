package com.yinlin.rachel.data


class ResFolder(parent: ResFolder?, name: String, author: String)
    : ResFile(parent, name, author, null) {
    val items = ArrayList<ResFile>()

    companion object {
        val emptyRes get() = ResFolder(null, "", "")
    }
}