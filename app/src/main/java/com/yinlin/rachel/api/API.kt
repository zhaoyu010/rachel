package com.yinlin.rachel.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.yinlin.rachel.Config
import com.yinlin.rachel.IPlaylistMap
import com.yinlin.rachel.Net
import com.yinlin.rachel.data.Comment
import com.yinlin.rachel.data.Mail
import com.yinlin.rachel.data.ResFile
import com.yinlin.rachel.data.ResFolder
import com.yinlin.rachel.data.Topic
import com.yinlin.rachel.data.TopicPreview
import com.yinlin.rachel.data.User
import com.yinlin.rachel.data.UserProfile
import com.yinlin.rachel.json
import com.yinlin.rachel.jsonMap
import com.yinlin.rachel.to
import kotlin.random.Random


object API {
    private fun urlQ(v: Int): String {
        var hc = v + v.hashCode()
        val hr = Random.nextInt(100)
        hc += hr
        hc -= v.hashCode()
        return (hc - hr).toString()
    }

    val BASEURL: String = "http://${urlQ(49)}.${urlQ(235)}.${urlQ(151)}.${urlQ(78)}:${urlQ(1211)}"

    data class Result<T>(val ok: Boolean, val value: T)
    data class Result2<T, U>(val ok: Boolean, val value1: T, val value2: U)

    private fun JsonObject.fetchMsg() = Result(this["ok"].asBoolean, this["msg"].asString)
    private val errResult = Result(false, "网络异常")
    private fun <U> errResult2(u: U) = Result2(false, "网络异常", u)
    private fun <T, U> errResult2(t: T, u: U) = Result2(false, t, u)

    object ResAPI {
        private val BASEURL: String = "${API.BASEURL}/res"

        private fun parseResFolder(parent: ResFolder?, name: String, json: JsonObject): ResFolder? = try {
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
            root
        }
        catch (ignored: Exception) { null }

        // 取美图信息
        fun getInfo(): ResFolder = try {
            parseResFolder(null, "", Net.get("$BASEURL/getInfo")!!) ?: ResFolder.emptyRes
        } catch (ignored: Exception) {
            ResFolder.emptyRes
        }
    }

    object SysAPI {
        private val BASEURL: String = "${API.BASEURL}/sys"

        // 取服务器版本
        fun getServerVersionCode(): Result2<Long, Long> = try {
            val json = Net.get("$BASEURL/getServerVersionCode")
            Result2(true, json!!["targetVersion"].asLong, json["minVersion"].asLong)
        }
        catch (ignored: Exception) {
            errResult2(0L, 0L)
        }
    }

    object UserAPI {
        private val BASEURL: String = "${API.BASEURL}/user"

        // 登录
        fun login(id: String, pwd: String) = try {
            Net.post("$BASEURL/login", jsonMap("id" to id, "pwd" to pwd))!!.fetchMsg()
        }
        catch (ignored: Exception) {
            errResult
        }

        // 注册
        fun register(id: String, pwd: String, inviter: String) = try {
            Net.post("$BASEURL/register",
                jsonMap("id" to id, "pwd" to pwd, "inviter" to inviter)
            )!!.fetchMsg()
        }
        catch (ignored: Exception) {
            errResult
        }

        // 忘记密码
        fun forgotPassword(id: String, pwd: String) = try {
            Net.post("$BASEURL/forgotPassword", jsonMap("id" to id, "pwd" to pwd))!!.fetchMsg()
        }
        catch (ignored: Exception) {
            errResult
        }

        // 歌单云备份
        fun uploadPlaylist(id: String, pwd: String, playlist: IPlaylistMap) = try {
            Net.post("$BASEURL/uploadPlaylist",
                object { val id = id; val pwd = pwd; val playlist = playlist }.json()
            )!!.fetchMsg()
        }
        catch (ignored: Exception) {
            errResult
        }

        // 歌单云还原
        fun downloadPlaylist(id: String, pwd: String): Result2<String, IPlaylistMap> = try {
            val json = Net.post("$BASEURL/downloadPlaylist", jsonMap("id" to id, "pwd" to pwd))
            Result2(json!!["ok"].asBoolean, json["msg"].asString, json["playlist"].to())
        }
        catch (ignored: Exception) {
            errResult2(hashMapOf())
        }

        // 取用户信息
        fun getInfo(id: String, pwd: String): User = try {
            Net.post("$BASEURL/getInfo", jsonMap("id" to id, "pwd" to pwd)).to()
        } catch (ignored: Exception) {
            User(false)
        }

        // 更新头像
        fun updateAvatar(id: String, pwd: String, filename: String) = try {
            Net.postForm("$BASEURL/updateAvatar",
                mapOf("avatar" to filename), mapOf("id" to id, "pwd" to pwd)
            )!!.fetchMsg()
        }
        catch (ignored: Exception) {
            errResult
        }

        // 更新个性签名
        fun updateSignature(id: String, pwd: String, signature: String) = try {
            Net.post("$BASEURL/updateSignature",
                jsonMap("id" to id, "pwd" to pwd, "signature" to signature)
            )!!.fetchMsg()
        }
        catch (ignored: Exception) {
            errResult
        }

        // 更新背景墙
        fun updateWall(id: String, pwd: String, filename: String) = try {
            Net.postForm("$BASEURL/updateWall",
                mapOf("wall" to filename), mapOf("id" to id, "pwd" to pwd)
            )!!.fetchMsg()
        }
        catch (ignored: Exception) {
            errResult
        }

        // 签到
        fun signIn(id: String, pwd: String) = try {
            Net.post("$BASEURL/signIn", jsonMap("id" to id, "pwd" to pwd))!!.fetchMsg()
        } catch (ignored: Exception) {
            errResult
        }

        // 查询邮件
        fun getMail(id: String, pwd: String): List<Mail> = try {
            Net.post("$BASEURL/getMail", jsonMap("id" to id, "pwd" to pwd))!!["mails"].to()
        } catch (ignored: Exception) {
            emptyList()
        }

        // 处理邮件
        fun processMail(id: String, pwd: String, mid: Long, confirm: Boolean, info: JsonElement) = try {
            Net.post("$BASEURL/processMail",
                object { val id = id; val pwd = pwd; val mid = mid; val confirm = confirm; val info = info; }.json()
            )!!.fetchMsg()
        } catch (ignored: Exception) {
            errResult
        }

        // 删除邮件
        fun deleteMail(id: String, pwd: String, mid: Long) = try {
            Net.post("$BASEURL/deleteMail",
                object { val id = id; val pwd = pwd; val mid = mid; }.json()
            )!!.fetchMsg()
        } catch (ignored: Exception) {
            errResult
        }

        // 取资料卡
        fun getProfile(id: String): UserProfile = try {
            Net.post("$BASEURL/getProfile", jsonMap("id" to id)).to()
        } catch (ignored: Exception) {
            UserProfile(false)
        }

        // 取最新主题
        fun getLatestTopic(upper: Int = 2147483647): List<TopicPreview> = try {
            Net.post("$BASEURL/getLatestTopic", jsonMap("upper" to upper))!!["topics"].to()
        } catch (ignored: Exception) {
            emptyList()
        }

        // 取热门主题
        fun getHotTopic(offset: Int = 0): List<TopicPreview> = try {
            Net.post("$BASEURL/getHotTopic", jsonMap("offset" to offset))!!["topics"].to()
        } catch (ignored: Exception) {
            emptyList()
        }

        // 取主题
        fun getTopic(tid: Int) : Topic = try {
            Net.post("$BASEURL/getTopic", jsonMap("tid" to tid)).to()
        } catch (ignored: Exception) {
            Topic(false)
        }

        // 发主题
        fun sendTopic(id: String, pwd: String, title: String, content: String, files: List<String>) = try {
            val fileMap = LinkedHashMap<String, String>()
            for (index in 0 until minOf(files.size, 9)) fileMap["pic${index + 1}"] = files[index]
            Net.postForm("$BASEURL/sendTopic", fileMap,
                mapOf("id" to id, "pwd" to pwd, "title" to title, "content" to content)
            )!!.fetchMsg()
        }
        catch (ignored: Exception) {
            errResult
        }

        // 评论
        fun sendComment(id: String, pwd: String, tid: Int, content: String): Result2<String, Comment?> = try {
            val json = Net.post("$BASEURL/sendComment",
                object { val id = id; val pwd = pwd; val tid = tid; val content = content; }.json()
            )
            val user = Config.user
            Result2(json!!["ok"].asBoolean, json["msg"].asString, Comment(json["cid"].asLong, id, json["ts"].asString, content, 0, user.title, user.titleGroup))
        } catch (ignored: Exception) {
            errResult2(null)
        }

        // 投币
        fun sendCoin(srcId: String, pwd: String, desId: String, tid: Int, value: Int) = try {
            Net.post("$BASEURL/sendCoin",
                object { val srcId = srcId; val pwd = pwd; val desId = desId; val tid = tid; val value = value; }.json()
            )!!.fetchMsg()
        } catch (ignored: Exception) {
            errResult
        }

        // 删除评论
        fun deleteComment(id: String, pwd: String, cid: Long, tid: Int) = try {
            Net.post("$BASEURL/deleteComment",
                object { val id = id; val pwd = pwd; val cid = cid; val tid = tid; }.json()
            )!!.fetchMsg()
        } catch (ignored: Exception) {
            errResult
        }

        // 更新评论置顶
        fun updateCommentTop(id: String, pwd: String, cid: Long, tid: Int, type: Int) = try {
            Net.post("$BASEURL/updateCommentTop",
                object { val id = id; val pwd = pwd; val cid = cid; val tid = tid; val type = type }.json()
            )!!.fetchMsg()
        } catch (ignored: Exception) {
            errResult
        }

        // 删除主题
        fun deleteTopic(id: String, pwd: String, tid: Int) = try {
            Net.post("$BASEURL/deleteTopic",
                object { val id = id; val pwd = pwd; val tid = tid; }.json()
            )!!.fetchMsg()
        } catch (ignored: Exception) {
            errResult
        }

        // 更新主题置顶
        fun updateTopicTop(id: String, pwd: String, tid: Int, type: Int) = try {
            Net.post("$BASEURL/updateTopicTop",
                object { val id = id; val pwd = pwd; val tid = tid; val type = type }.json()
            )!!.fetchMsg()
        } catch (ignored: Exception) {
            errResult
        }
    }
}