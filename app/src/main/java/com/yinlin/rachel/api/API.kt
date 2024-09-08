package com.yinlin.rachel.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.yinlin.rachel.Net
import com.yinlin.rachel.data.ResFile
import com.yinlin.rachel.data.ResFolder
import com.yinlin.rachel.data.User


object API {
    const val BASEURL: String = "http://49.235.151.78:1211"

    data class Result<T>(val ok: Boolean, val value: T)
    data class Result2<T, U>(val ok: Boolean, val value1: T, val value2: U)

    object ResAPI {
        private const val BASEURL: String = "${API.BASEURL}/res"

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
            }
            catch (ignored: Exception) { }
            return null
        }

        // 取美图信息
        fun getInfo(): ResFolder {
            try {
                val json = Net.get("$BASEURL/getInfo", null)
                return parseResFolder(null, "", json!!) ?: ResFolder.emptyRes
            }
            catch (ignored: Exception) { }
            return ResFolder.emptyRes
        }
    }

    object UserAPI {
        private const val BASEURL: String = "${API.BASEURL}/user"

        // 登录
        fun login(arg: Arg.Login): Result<String> {
            try {
                val json = Net.post("$BASEURL/login", Gson().toJson(arg), null)
                return Result(json!!["ok"].asBoolean, json["msg"].asString)
            }
            catch (ignored: Exception) { }
            return Result(false, "网络异常")
        }

        // 注册
        fun register(arg: Arg.Register): Result<String> {
            try {
                val json = Net.post("$BASEURL/register", Gson().toJson(arg), null)
                return Result(json!!["ok"].asBoolean, json["msg"].asString)
            }
            catch (ignored: Exception) { }
            return Result(false, "网络异常")
        }

        // 歌单云备份
        fun uploadPlaylist(arg: Arg.Playlist): Result<String> {
            try {
                val json = Net.post("$BASEURL/uploadPlaylist", Gson().toJson(arg), null)
                return Result(json!!["ok"].asBoolean, json["msg"].asString)
            }
            catch (ignored: Exception) { }
            return Result(false, "网络异常")
        }

        // 歌单云还原
        fun downloadPlaylist(arg: Arg.Login): Result2<String, String> {
            try {
                val json = Net.post("$BASEURL/downloadPlaylist", Gson().toJson(arg), null)
                return Result2(json!!["ok"].asBoolean, json["msg"].asString, json["playlist"].asString)
            }
            catch (ignored: Exception) {
                return Result2(false, "网络异常", "")
            }
        }

        // 取用户信息
        fun getInfo(arg: Arg.Login): User {
            try {
                val json = Net.post("$BASEURL/getInfo", Gson().toJson(arg), null)
                return Gson().fromJson(json, object : TypeToken<User>(){}.type) as User
            }
            catch (ignored: Exception) { }
            return User()
        }

        // 更新头像
        fun updateAvatar(id: String, pwd: String, filename: String): Result<String> {
            try {
                val json = Net.postForm("$BASEURL/updateAvatar",
                    mapOf("avatar" to filename), mapOf("id" to id, "pwd" to pwd), null)
                return Result(json!!["ok"].asBoolean, json["msg"].asString)
            }
            catch (ignored: Exception) { }
            return Result(false, "网络异常")
        }

        // 更新个性签名
        fun updateSignature(arg: Arg.Signature): Result<String> {
            try {
                val json = Net.post("$BASEURL/updateSignature", Gson().toJson(arg), null)
                return Result(json!!["ok"].asBoolean, json["msg"].asString)
            }
            catch (ignored: Exception) { }
            return Result(false, "网络异常")
        }

        // 更新背景墙
        fun updateWall(id: String, pwd: String, filename: String): Result<String> {
            try {
                val json = Net.postForm("$BASEURL/updateWall",
                    mapOf("wall" to filename), mapOf("id" to id, "pwd" to pwd), null)
                return Result(json!!["ok"].asBoolean, json["msg"].asString)
            }
            catch (ignored: Exception) { }
            return Result(false, "网络异常")
        }

        // 签到
        fun signIn(arg: Arg.Login): Result<String> {
            try {
                val json = Net.post("$BASEURL/signIn", Gson().toJson(arg), null)
                return Result(json!!["ok"].asBoolean, json["msg"].asString)
            }
            catch (ignored: Exception) { }
            return Result(false, "网络异常")
        }
    }

    object SysAPI {
        private const val BASEURL: String = "${API.BASEURL}/sys"

        // 取服务器版本
        fun getServerVersionCode(): Result2<Long, Long> {
            try {
                val json = Net.get("$BASEURL/getServerVersionCode", null)
                return Result2(true, json!!["targetVersion"].asLong, json["minVersion"].asLong)
            }
            catch (ignored: Exception) { }
            return Result2(false, 0L, 0L)
        }
    }
}