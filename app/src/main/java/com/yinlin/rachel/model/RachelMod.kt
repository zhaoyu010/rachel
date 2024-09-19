package com.yinlin.rachel.model

import com.yinlin.rachel.data.MusicInfo
import com.yinlin.rachel.div
import com.yinlin.rachel.json
import com.yinlin.rachel.readJson
import com.yinlin.rachel.to
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


// | R | A | C | H | E | L |    ------ 6字节 Rachel标识
// | <META> |                   ------ ?字节 JSON元数据
// | <Items> |                  ------ ?字节 资源

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

    data class MusicItem(val name: String, val version: String, val resList: MutableList<String> = ArrayList())

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
            catch (ignored: Exception) { }
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
        fun getMetadata(ids: List<String>, filter: List<String>): Metadata {
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
            if (metadata.items.any { (_, value) ->
                    val resList: List<String> = value.resList
                    !resList.contains(RES_INFO) ||
                    !resList.contains(RES_AUDIO) ||
                    !resList.contains(RES_RECORD) ||
                    !resList.contains(RES_BGS) ||
                    !resList.contains(RES_DEFAULT_LRC)
                }) metadata.items.clear()
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