package com.yinlin.rachelmodmaker

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

var basePath = File("music")
var modPath = File("music.rachel")

typealias ChorusList = List<Long>
typealias LyricsFileMap = Map<String, List<String>>

data class MusicInfo (
    val version: String = "", // 版本号
    val author: String = "", // 作者
    val id: String = "", // 编号
    val name: String = "", // 歌名
    val singer: String = "", // 歌手
    val lyricist: String = "", // 词作
    val composer: String = "", // 曲作
    val album: String = "", // 专辑
    val bgd: Boolean = false, // 是否有动态背景
    val video: Boolean = false, // 是否有MV
    val chorus: ChorusList = emptyList(), // 副歌点
    var lyrics: LyricsFileMap = emptyMap(), // 歌词引擎
    // 拓展属性
    var lyricsText: String = "", // 歌词文本
) {
    val isCorrect: Boolean get() {
        if (id.isEmpty()) return false
        if (!audioPath.exists()) return false
        if (!recordPath.exists()) return false
        if (!bgsPath.exists()) return false
        if (bgd && !bgdPath.exists()) return false
        if (video && !videoPath.exists()) return false
        if (lyrics.isEmpty()) return false
        val lineEngine = lyrics["line"]
        if (lineEngine.isNullOrEmpty() || !lineEngine.contains("")) return false
        return true
    }

    val infoPath get() = basePath / (id + RachelMod.RES_INFO)
    val audioPath get() = basePath / (id + RachelMod.RES_AUDIO)
    val recordPath get() = basePath / (id + RachelMod.RES_RECORD)
    val defaultLrcPath get() = basePath / (id + RachelMod.RES_DEFAULT_LRC)
    val videoPath get() = basePath / (id + RachelMod.RES_VIDEO)
    val bgsPath get() = basePath / (id + RachelMod.RES_BGS)
    val bgdPath get() = basePath / (id + RachelMod.RES_BGD)
}

operator fun File.div(child: String) = File(this, child)

fun File.read() : ByteArray {
    var data = byteArrayOf()
    try {
        FileInputStream(this).use { stream ->
            val size = stream.available()
            if (size > 0) {
                data = ByteArray(size)
                stream.read(data)
            }
        }
    } catch (ignored: Exception) { }
    return data
}

fun File.readText() : String = read().toString(StandardCharsets.UTF_8)

inline fun <reified T> File.readJson(): T = readText().to()

val gson = Gson()
inline fun <reified T> String.to(): T = gson.fromJson(this, object : TypeToken<T>(){}.type)
inline fun <reified T> T.json(): String = gson.toJson(this, object : TypeToken<T>(){}.type)

object RachelMod {
    const val MOD_VERSION = 2
    val MOD_MAGIC = "RACHEL".toByteArray()
    const val MOD_BUFFER_SIZE = 1024 * 64

    const val RES_INFO = ".json"
    const val RES_AUDIO = ".flac"
    const val RES_RECORD = "_record.webp"
    const val RES_VIDEO = ".mp4"
    const val RES_BGS = "_bgs.webp"
    const val RES_BGD = "_bgd.webp"
    const val RES_DEFAULT_LRC = ".lrc"

    @JvmRecord
    data class MusicItem(val name: String = "", val version: String = "", val resList: MutableList<String> = ArrayList())

    class Metadata {
        var version: Int = MOD_VERSION // MOD版本
        var config: String = "" // MOD配置
        val items = HashMap<String, MusicItem>() // 音乐集

        val empty: Boolean get() = items.isEmpty()
        val totalCount: Int get() {
            var sum = 0
            for ((_, item) in items) sum += item.resList.size
            return sum
        }
    }

    fun interface MetaListener {
        fun onError(id: String, res: String)
    }

    fun interface Listener {
        fun onProcess(id: String, res: String, index: Int)
    }

    class Releaser(rawStream: InputStream) {
        private val stream = DataInputStream(rawStream)

        fun getMetadata(): Metadata {
            var metadata = Metadata()
            try {
                val magic = ByteArray(MOD_MAGIC.size)
                stream.read(magic)
                if (!magic.contentEquals(MOD_MAGIC)) return metadata
                metadata = stream.readUTF().to()
            }
            catch (ignored: Exception) {
                ignored.printStackTrace()
            }
            return metadata
        }

        fun run(folder: File, listener: Listener?): Boolean {
            try {
                val count = stream.readInt()
                val buffer = ByteArray(MOD_BUFFER_SIZE)
                var index = 0
                var countRead: Int
                for (i in 0 until count) {
                    val id = stream.readUTF()
                    val resCount = stream.readInt()
                    for (j in 0 until resCount) {
                        val resName = stream.readUTF()
                        var resLength = stream.readInt()
                        if (resLength <= 0) return false
                        FileOutputStream(folder / (id + resName)).use { writeStream ->
                            while (resLength > MOD_BUFFER_SIZE) {
                                countRead = stream.read(buffer)
                                writeStream.write(buffer, 0, countRead)
                                resLength -= countRead
                            }
                            countRead = stream.read(buffer, 0, resLength)
                            writeStream.write(buffer, 0, countRead)
                            listener?.onProcess(id, resName, index)
                        }
                        ++index
                    }
                }
            }
            catch (ignored: Exception) { return false }
            return true
        }

        fun close() {
            try { stream.close() }
            catch (ignored: Exception) { }
        }
    }

    class Merger(private val folder: File) {
        fun getMetadata(ids: List<String>, filter: List<String>, listener: MetaListener?): Metadata {
            val metadata = Metadata()
            if (!folder.isDirectory) return metadata
            // 统计
            for (id in ids) {
                val infoFile = folder / (id + RES_INFO)
                if (!infoFile.exists()) continue
                val musicInfo: MusicInfo = infoFile.readJson()
                if (!musicInfo.isCorrect || musicInfo.id != id) continue
                metadata.items[id] = MusicItem(musicInfo.name, musicInfo.version)
            }
            // 遍历
            val files = folder.listFiles { obj: File -> obj.isFile }
            if (files == null) {
                metadata.items.clear()
                return metadata
            }
            for (file in files) {
                val filename = file.name
                var pos = filename.lastIndexOf('.')
                var name = if (pos == -1) filename else filename.substring(0, pos)
                var ext = if (pos == -1) "" else filename.substring(pos)
                pos = name.lastIndexOf('_')
                if (pos != -1) {
                    ext = name.substring(pos) + ext
                    name = name.substring(0, pos)
                }
                val item = metadata.items[name]
                if (item == null || ext.isEmpty() || filter.contains(ext)) continue
                item.resList.add(ext)
            }
            // 至少存在一个音乐基础信息不足
            for (meta in metadata.items) {
                val resList: List<String> = meta.value.resList
                if (!resList.contains(RES_INFO)) {
                    listener?.onError(meta.key, RES_INFO)
                    metadata.items.clear()
                    break
                }
                if (!resList.contains(RES_AUDIO)) {
                    listener?.onError(meta.key, RES_AUDIO)
                    metadata.items.clear()
                    break
                }
                if (!resList.contains(RES_RECORD)) {
                    listener?.onError(meta.key, RES_RECORD)
                    metadata.items.clear()
                    break
                }
                if (!resList.contains(RES_BGS)) {
                    listener?.onError(meta.key, RES_BGS)
                    metadata.items.clear()
                    break
                }
                if (!resList.contains(RES_DEFAULT_LRC)) {
                    listener?.onError(meta.key, RES_DEFAULT_LRC)
                    metadata.items.clear()
                    break
                }
            }
            return metadata
        }

        fun run(rawStream: OutputStream, metadata: Metadata, listener: Listener?): Boolean {
            if (!folder.isDirectory) return false
            try {
                DataOutputStream(rawStream).use { stream ->
                    stream.write(MOD_MAGIC)
                    stream.writeUTF(metadata.json())
                    stream.writeInt(metadata.items.size)
                    val buffer = ByteArray(MOD_BUFFER_SIZE)
                    var index = 0
                    var countRead: Int
                    for ((id, item) in metadata.items) {
                        stream.writeUTF(id)
                        stream.writeInt(item.resList.size)
                        for (resName in item.resList) {
                            stream.writeUTF(resName)
                            FileInputStream(folder / (id + resName)).use { readStream ->
                                val resSize = readStream.available()
                                if (resSize <= 0) return false
                                stream.writeInt(resSize)
                                while ((readStream.read(buffer).also { countRead = it }) != -1) {
                                    stream.write(buffer, 0, countRead)
                                }
                                listener?.onProcess(id, resName, index)
                            }
                            ++index
                        }
                    }
                }
            }
            catch (e: Exception) { return false }
            return true
        }
    }
}

fun main() {
    println("RachelModMaker v2.0")
    println("当前路径 ${File("").absolutePath}")
    println("[ 1 - 打包Mod (在music目录下放置所有配置)   ]")
    println("[ 2 - 解析Mod (在当前目录放置music.rachel) ]")
    println("[ 3 - 退出                               ]")
    println("请输入操作选项:")
    val input = readlnOrNull()?.toIntOrNull() ?: 0
    when (input) {
        1 -> {
            val files = basePath.listFiles { obj -> obj.extension == "json" }
            if (files == null || files.isEmpty()) println("${basePath.absolutePath}目录为空或不存在文件")
            else {
                val ids = arrayListOf<String>()
                for (file in files) ids += file.nameWithoutExtension
                println("已查找到${ids.size}首歌曲")
                val merger = RachelMod.Merger(basePath)
                val metadata = merger.getMetadata(ids, emptyList()) { id, res -> println("歌曲[${id}]缺失资源[${res}]") }
                if (!metadata.empty) {
                    try {
                        val fos = FileOutputStream(modPath)
                        merger.run(fos, metadata) { id, resName, index -> println("[${id} - ${index}] 打包 $resName") }
                        println("打包完成[music.rachel]")
                    }
                    catch (err: Exception) { err.printStackTrace() }
                }
                else println("打包失败")
            }
            println("请按任意键结束")
            readln()
        }
        2 -> {
            if (!modPath.exists()) println("当前目录下不存在${modPath}")
            else {
                try {
                    val fis = FileInputStream(modPath)
                    val releaser = RachelMod.Releaser(fis)
                    val metadata = releaser.getMetadata()
                    if (!metadata.empty) {
                        println("解析中...")
                        println("MOD版本: ${metadata.version}, 歌曲数: ${metadata.items.size}, 资源数: ${metadata.totalCount}")
                        var cnt = 0
                        for (item in metadata.items) {
                            val value = item.value
                            println("[${++cnt} ${item.key}] 歌名: ${value.name} 版本: ${value.version}")
                            println("\t资源表: ${value.resList}")
                        }
                        println("解析完成")
                    }
                    else println("解析失败")
                    releaser.close()
                }
                catch (err: Exception) { err.printStackTrace() }
            }
            println("请按任意键结束")
            readln()
        }
        else -> { }
    }
}