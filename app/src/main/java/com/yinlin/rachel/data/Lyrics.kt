package com.yinlin.rachel.data

import com.yinlin.rachel.model.engine.LineLyricsEngine


class Lyrics(source: String) {
    lateinit var plaintext: String
    val items = LinkedHashMap<String, List<String>>()

    init {
        try {
            // 解析歌词文件
            val lines = source.split("\\r?\\n".toRegex())
            var currentItem: MutableList<String>? = null
            for (item in lines) {
                val line = item.trim()
                if (line.isEmpty()) continue
                if (line.startsWith("$")) {
                    val engineName = line.substring(1)
                    if (engineName.isEmpty() || items.containsKey(engineName)) throw Exception()
                    else {
                        currentItem = ArrayList()
                        items[engineName] = currentItem
                    }
                }
                else currentItem!! += line
            }
            // 解析普通歌词文本
            val lineItem = items[LineLyricsEngine.NAME]
            val sb = StringBuilder()
            if (lineItem != null) {
                for (line in lineItem) {
                    var index = line.indexOf(']')
                    if (index == -1) throw Exception()
                    if (index + 1 != line.length) {
                        if (line[index + 1] == '+') ++index
                    }
                    sb.append(line.substring(index + 1)).append(System.lineSeparator())
                }
                plaintext = sb.toString()
            }
        } catch (e: Exception) {
            items.clear()
            plaintext = ""
        }
    }
}