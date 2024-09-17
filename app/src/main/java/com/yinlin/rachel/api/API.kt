package com.yinlin.rachel.api

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.yinlin.rachel.IPlaylistMap
import com.yinlin.rachel.Net
import com.yinlin.rachel.data.Mail
import com.yinlin.rachel.data.ResFile
import com.yinlin.rachel.data.ResFolder
import com.yinlin.rachel.data.Topic
import com.yinlin.rachel.data.User
import com.yinlin.rachel.data.UserProfile
import com.yinlin.rachel.json
import com.yinlin.rachel.jsonMap
import com.yinlin.rachel.to


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
                val json = Net.get("$BASEURL/getInfo")
                return parseResFolder(null, "", json!!) ?: ResFolder.emptyRes
            }
            catch (ignored: Exception) { }
            return ResFolder.emptyRes
        }
    }

    object SysAPI {
        private const val BASEURL: String = "${API.BASEURL}/sys"

        // 取服务器版本
        fun getServerVersionCode(): Result2<Long, Long> {
            try {
                val json = Net.get("$BASEURL/getServerVersionCode")
                return Result2(true, json!!["targetVersion"].asLong, json["minVersion"].asLong)
            }
            catch (ignored: Exception) { }
            return Result2(false, 0L, 0L)
        }
    }

    object UserAPI {
        private const val BASEURL: String = "${API.BASEURL}/user"

        // 登录
        fun login(id: String, pwd: String): Result<String> {
            try {
                val json = Net.post("$BASEURL/login", jsonMap("id" to id, "pwd" to pwd))
                return Result(json!!["ok"].asBoolean, json["msg"].asString)
            }
            catch (ignored: Exception) { }
            return Result(false, "网络异常")
        }

        // 注册
        fun register(id: String, pwd: String, inviter: String): Result<String> {
            try {
                val json = Net.post("$BASEURL/register", jsonMap("id" to id, "pwd" to pwd, "inviter" to inviter))
                return Result(json!!["ok"].asBoolean, json["msg"].asString)
            }
            catch (ignored: Exception) { }
            return Result(false, "网络异常")
        }

        // 歌单云备份
        fun uploadPlaylist(id: String, pwd: String, playlist: IPlaylistMap): Result<String> {
            try {
                val json = Net.post("$BASEURL/uploadPlaylist", object { val id = id; val pwd = pwd; val playlist = playlist }.json())
                return Result(json!!["ok"].asBoolean, json["msg"].asString)
            }
            catch (ignored: Exception) { }
            return Result(false, "网络异常")
        }

        // 歌单云还原
        fun downloadPlaylist(id: String, pwd: String): Result2<String, IPlaylistMap> {
            try {
                val json = Net.post("$BASEURL/downloadPlaylist", jsonMap("id" to id, "pwd" to pwd))
                return Result2(json!!["ok"].asBoolean, json["msg"].asString, json["playlist"].to())
            }
            catch (ignored: Exception) {
                return Result2(false, "网络异常", hashMapOf())
            }
        }

        // 取用户信息
        fun getInfo(id: String, pwd: String): User {
            try {
                return Net.post("$BASEURL/getInfo", jsonMap("id" to id, "pwd" to pwd)).to()
            }
            catch (ignored: Exception) { }
            return User(true)
        }

        // 更新头像
        fun updateAvatar(id: String, pwd: String, filename: String): Result<String> {
            try {
                val json = Net.postForm("$BASEURL/updateAvatar", mapOf("avatar" to filename), mapOf("id" to id, "pwd" to pwd))
                return Result(json!!["ok"].asBoolean, json["msg"].asString)
            }
            catch (ignored: Exception) { }
            return Result(false, "网络异常")
        }

        // 更新个性签名
        fun updateSignature(id: String, pwd: String, signature: String): Result<String> {
            try {
                val json = Net.post("$BASEURL/updateSignature", jsonMap("id" to id, "pwd" to pwd, "signature" to signature))
                return Result(json!!["ok"].asBoolean, json["msg"].asString)
            }
            catch (ignored: Exception) { }
            return Result(false, "网络异常")
        }

        // 更新背景墙
        fun updateWall(id: String, pwd: String, filename: String): Result<String> {
            try {
                val json = Net.postForm("$BASEURL/updateWall", mapOf("wall" to filename), mapOf("id" to id, "pwd" to pwd))
                return Result(json!!["ok"].asBoolean, json["msg"].asString)
            }
            catch (ignored: Exception) { }
            return Result(false, "网络异常")
        }

        // 签到
        fun signIn(id: String, pwd: String): Result<String> {
            try {
                val json = Net.post("$BASEURL/signIn", jsonMap("id" to id, "pwd" to pwd))
                return Result(json!!["ok"].asBoolean, json["msg"].asString)
            }
            catch (ignored: Exception) { }
            return Result(false, "网络异常")
        }

        // 查询邮件
        fun getMail(id: String, pwd: String): List<Mail> {
            try {
                val json = Net.post("$BASEURL/getMail", jsonMap("id" to id, "pwd" to pwd))
                if (json!!["ok"].asBoolean) return json["mails"].to()
            }
            catch (ignored: Exception) { }
            return emptyList()
        }

        // 处理邮件
        fun processMail(id: String, pwd: String, mid: Long, confirm: Boolean, info: JsonElement): Result<String> {
            try {
                val json = Net.post("$BASEURL/processMail", object { val id = id; val pwd = pwd; val mid = mid; val confirm = confirm; val info = info; }.json())
                return Result(json!!["ok"].asBoolean, json["msg"].asString)
            }
            catch (ignored: Exception) { }
            return Result(false, "网络异常")
        }

        // 删除邮件
        fun deleteMail(id: String, pwd: String, mid: Long): Result<String> {
            try {
                val json = Net.post("$BASEURL/deleteMail", object { val id = id; val pwd = pwd; val mid = mid; }.json())
                return Result(json!!["ok"].asBoolean, json["msg"].asString)
            }
            catch (ignored: Exception) { }
            return Result(false, "网络异常")
        }

        // 取用户资料卡
        fun getProfile(id: String): UserProfile {
            try {
                return Net.post("$BASEURL/getProfile", mapOf("id" to id).json()).to()
            }
            catch (ignored: Exception) { }
            return UserProfile(true)
        }

        // 取主题
        fun getTopic(tid: Int) : Topic {
            try {
                return Net.post("$BASEURL/getTopic", mapOf("tid" to tid).json()).to()
            }
            catch (ignored: Exception) { }
            return Topic(true)
        }

        // 评论
        fun sendComment(id: String, pwd: String, tid: Int, content: String): Result<String> {
            try {
                val json = Net.post("$BASEURL/sendComment", object { val id = id; val pwd = pwd; val tid = tid; val content = content; }.json())
            }
            catch (ignored: Exception) { }
            return Result(false, "")
        }
    }
}