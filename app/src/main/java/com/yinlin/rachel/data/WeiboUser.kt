package com.yinlin.rachel.data

import com.google.gson.Gson

@JvmRecord
data class WeiboUser(val name: String, val containerId: String) {
    companion object {
        fun getDefaultWeiboUsers(): String = Gson().toJson(linkedMapOf(
            "2266537042" to WeiboUser("银临Rachel", "1076032266537042"),
            "7802114712" to WeiboUser("银临-欢银光临", "1076037802114712"),
            "3965226022" to WeiboUser("银临的小银库", "1076033965226022")
        ))

        fun getNames(map: Map<String, WeiboUser>): List<String> {
            val names = ArrayList<String>()
            for (item in map.values) names.add(item.name)
            return names
        }
    }
}