package com.yinlin.rachel.data


@JvmRecord
data class Playlist(
    val name: String,
    val items: MutableList<String>
) {
    constructor(name: String, item: String): this(name, ArrayList<String>()) {
        items += item
    }
}