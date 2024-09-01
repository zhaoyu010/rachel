package com.yinlin.rachel

import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets


lateinit var basePath: String
val pathAPP: File
    get() = File(basePath)
val pathMusic: File
    get() = File(basePath, "music")

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

fun File.write(data: ByteArray) {
    try {
        FileOutputStream(this).use { stream ->
            stream.write(data)
        }
    }
    catch (ignored: Exception) { }
}

fun File.writeText(data: String) = write(data.toByteArray(StandardCharsets.UTF_8))

fun <T> File.readJson(type: Type): T = Gson().fromJson(readText(), type)

fun File.writeJson(obj: Any) = writeText(Gson().toJson(obj))

fun File.create(data: ByteArray) {
    if (!exists()) write(data)
}

fun File.createAll() = mkdirs()

fun File.deleteFilter(delName: String) {
    listFiles { file ->
        val filename: String = file.getName()
        var pos = filename.lastIndexOf('.')
        var name = if (pos == -1) filename else filename.substring(0, pos)
        pos = name.lastIndexOf('_')
        if (pos != -1) name = name.substring(0, pos)
        file.isFile() && delName.equals(name, ignoreCase = true)
    }?.apply {
        for (file in this) file.delete()
    }
}