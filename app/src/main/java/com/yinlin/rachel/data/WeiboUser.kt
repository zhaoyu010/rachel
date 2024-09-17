package com.yinlin.rachel.data

import com.yinlin.rachel.IWeiboUserMap
import com.yinlin.rachel.json

@JvmRecord
data class WeiboUser(
    val name: String,
    val containerId: String
) {
    companion object {
        fun getDefaultWeiboUsers(): String = linkedMapOf(
            "2266537042" to WeiboUser("银临Rachel", "1076032266537042"),
            "7802114712" to WeiboUser("银临-欢银光临", "1076037802114712"),
            "3965226022" to WeiboUser("银临的小银库", "1076033965226022")
        ).json()

        fun getNames(map: IWeiboUserMap): List<String> {
            val names = ArrayList<String>()
            for (item in map.values) names.add(item.name)
            return names
        }
    }
}