package com.yinlin.rachel.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.yinlin.rachel.Net
import com.yinlin.rachel.api.Arg.Login
import com.yinlin.rachel.data.ResFile
import com.yinlin.rachel.data.ResFolder
import com.yinlin.rachel.data.User


object API {
    const val BASEURL: String = "http://49.235.151.78:1211"

    data class Result<T>(val ok: Boolean, val value: T)
    data class Result2<T, U>(val ok: Boolean, val value1: T, val value2: U)

    private fun parseResFolder(parent: ResFolder?, name: String, json: JsonObject): ResFolder? {
        try {
            val root = ResFolder(parent, name, if (json.has("author")) json["author"].asString else parent!!.author)
            if (json.has("folders")) {
                for ((folderName, value) in json.getAsJsonObject("folders").entrySet()) {
                    parseResFolder(root, folderName, value.asJsonObject)?.apply { root.items += this }
                }
            }
            if (json.has("files")) {
                var index = 0
                for (entry in json.getAsJsonArray("files")) {
                    if (entry.isJsonObject) {
                        val file = entry.asJsonObject
                        val fileName = if (file.has("name")) file["name"].asString else index++.toString()
                        val fileAuthor = if (file.has("author")) file["author"].asString else root.author
                        val fileUrl = file["url"].asString
                        root.items += ResFile(root, fileName, fileAuthor, fileUrl)
                    } else {
                        val fileName = index++.toString()
                        val fileUrl = entry.asString
                        root.items += ResFile(root, fileName, root.author, fileUrl)
                    }
                }
            }
            return root
        } catch (ignored: Exception) { }
        return null
    }

    // 取美图信息
    fun resInfo(): ResFolder {
        try {
            val json = Net.get("$BASEURL/getResInfo", null)
            return parseResFolder(null, "", json!!) ?: ResFolder.emptyRes
        } catch (ignored: Exception) { }
        return ResFolder.emptyRes
    }

    // 登录
    fun login(arg: Arg.Login): Result<String> {
        try {
            val json = Net.post("$BASEURL/login", Gson().toJson(arg), null)
            return Result(json!!["ok"].asBoolean, json["msg"].asString)
        } catch (ignored: Exception) {
            return Result(false, "网络异常")
        }
    }

    // 注册
    fun register(arg: Arg.Register): Result<String> {
        try {
            val json = Net.post("$BASEURL/register", Gson().toJson(arg), null)
            return Result(json!!["ok"].asBoolean, json["msg"].asString)
        } catch (ignored: Exception) {
            return Result(false, "网络异常")
        }
    }

    // 歌单云备份
    fun uploadPlaylist(arg: Arg.UploadPlaylist): Result<String> {
        try {
            val json = Net.post("$BASEURL/uploadPlaylist", Gson().toJson(arg), null)
            return Result(json!!["ok"].asBoolean, json["msg"].asString)
        } catch (ignored: Exception) {
            return Result(false, "网络异常")
        }
    }

    // 歌单云还原
    fun downloadPlaylist(arg: Arg.Login): Result2<String, String> {
        try {
            val json = Net.post("$BASEURL/downloadPlaylist", Gson().toJson(arg), null)
            return Result2(json!!["ok"].asBoolean, json["msg"].asString, json["playlist"].asString)
        } catch (ignored: Exception) {
            return Result2(false, "网络异常", "")
        }
    }

    // 取用户信息
    fun getUserInfo(arg: Login): User {
        try {
            val json = Net.post("$BASEURL/getUserInfo", Gson().toJson(arg), null)
            return Gson().fromJson(json, object : TypeToken<User>(){}.type) as User
        } catch (ignored: Exception) { }
        return User()
    }
}