package com.yinlin.rachel.data

class Playlist(var name: String, var items: MutableList<String>) {
    constructor(name: String, item: String): this(name, ArrayList<String>()) {
        items += item
    }
}