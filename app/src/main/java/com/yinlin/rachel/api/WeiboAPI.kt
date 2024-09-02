package com.yinlin.rachel.api

import com.yinlin.rachel.Net
import com.yinlin.rachel.IMsgInfoList
import com.yinlin.rachel.IWeiboUserMap
import com.yinlin.rachel.MsgInfoList
import com.yinlin.rachel.data.MsgInfo
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


object WeiboAPI {
    fun extractContainerId(uid: String): Array<String>? {
        try {
            val url = "https://m.weibo.cn/api/container/getIndex?type=uid&value=$uid"
            val json = Net.get(url, null)
            val data = json!!.getAsJsonObject("data")
            val name = data.getAsJsonObject("userInfo")["screen_name"].asString
            val tabs = data.getAsJsonObject("tabsInfo").getAsJsonArray("tabs")
            for (item in tabs) {
                val tab = item.asJsonObject
                if (tab["title"].asString == "微博") {
                    return arrayOf(uid, name, tab["containerid"].asString)
                }
            }
        } catch (ignored: Exception) { }
        return null
    }

    private fun extractSingle(uid: String, containerId: String, array: IMsgInfoList) {
        try {
            val url = "https://m.weibo.cn/api/container/getIndex?type=uid&value=$uid&containerid=$containerId"
            val json = Net.get(url, null)
            val cards = json!!.getAsJsonObject("data").getAsJsonArray("cards")
            for (item in cards) {
                val card = item.asJsonObject
                val cardType = card["card_type"].asInt
                if (cardType != 9) continue  // 非微博类型

                val blogs = card.getAsJsonObject("mblog")
                // 提取名称和头像
                val user = blogs.getAsJsonObject("user")
                val userName = user["screen_name"].asString
                val avatar = user["avatar_hd"].asString
                // 提取时间
                val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH)
                val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                var formattedTime = "未知时间"
                try {
                    val date = inputFormat.parse(blogs["created_at"].asString)
                    formattedTime = outputFormat.format(date!!)
                } catch (ignored: Exception) { }
                // 提取IP
                var location = "IP未知"
                if (blogs.has("region_name")) {
                    location = blogs["region_name"].asString
                    val index = location.indexOf(' ')
                    if (index != -1) location = location.substring(index + 1)
                }
                // 提取内容
                val text = blogs["text"].asString
                val info = MsgInfo(userName, avatar, text, formattedTime, location)
                // 图片微博
                if (blogs.has("pics")) {
                    for (picItem in blogs.getAsJsonArray("pics")) {
                        val pic = picItem.asJsonObject
                        info.pictures.add(MsgInfo.Picture(
                            MsgInfo.MsgType.PICTURE,
                            pic["url"].asString,
                            pic.getAsJsonObject("large")["url"].asString
                        ))
                    }
                } else if (blogs.has("page_info")) {
                    val pageInfo = blogs.getAsJsonObject("page_info")
                    if (pageInfo["type"].asString == "video") {
                        val urls = pageInfo.getAsJsonObject("urls")
                        val videoUrl = if (urls.has("mp4_720p_mp4")) urls["mp4_720p_mp4"].asString
                        else if (urls.has("mp4_hd_mp4")) urls["mp4_hd_mp4"].asString
                        else urls["mp4_ld_mp4"].asString
                        info.pictures.add(MsgInfo.Picture(
                            MsgInfo.MsgType.VIDEO,
                            pageInfo.getAsJsonObject("page_pic")["url"].asString,
                            videoUrl
                        ))
                    }
                }
                array += info
            }
        }
        catch (ignored: Exception) { }
    }

    fun extract(weiboUsers: IWeiboUserMap): IMsgInfoList {
        val array = MsgInfoList()
        for ((key, value) in weiboUsers) extractSingle(key, value.containerId, array)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        array.sortWith { o1: MsgInfo, o2: MsgInfo ->
            val dateTime1 = LocalDateTime.parse(o1.time, formatter)
            val dateTime2 = LocalDateTime.parse(o2.time, formatter)
            dateTime1.compareTo(dateTime2) * -1
        }
        return array
    }
}