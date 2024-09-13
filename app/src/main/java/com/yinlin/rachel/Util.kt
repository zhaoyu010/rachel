package com.yinlin.rachel

import android.content.Context
import android.util.Base64
import android.util.TypedValue
import com.google.gson.Gson
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

fun Long.toStringTime(): String {
    val second = (this / 1000).toInt()
    return String.format(Locale.ENGLISH,"%02d:%02d", second / 60, second % 60)
}

fun String.toLongTime(): Long {
    var second: Long = 0
    try {
        val parts = this.split(":")
        second = parts[0].toLong() * 60 + parts[1].toLong()
    } catch (ignored: Exception) { }
    return second * 1000
}

fun String.toMD5(): String {
    try {
        val md = MessageDigest.getInstance("MD5")
        md.update(this.toByteArray(StandardCharsets.UTF_8))
        val messageDigest = md.digest()
        val hexString = StringBuilder()
        for (b in messageDigest) {
            val hex = Integer.toHexString(0xff and b.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }
    catch (ignored: Exception) { }
    return ""
}

fun Any.encodeBase64(): String {
    try {
        val src = Gson().toJson(this)
        return Base64.encodeToString(src.toByteArray(StandardCharsets.UTF_8), Base64.DEFAULT)
    } catch (ignored: Exception) { }
    return ""
}

fun <T> String.decodeBase64(type: Type): T? {
    try {
        val src = String(Base64.decode(this, Base64.DEFAULT), StandardCharsets.UTF_8)
        return Gson().fromJson(src, type)
    } catch (ignored: Exception) { }
    return null
}

fun Float.toDP(context: Context) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)
fun Float.toSP(context: Context) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, context.resources.displayMetrics)
fun Int.toDP(context: Context) = toFloat().toDP(context).toInt()
fun Int.toSP(context: Context) = toFloat().toSP(context).toInt()

fun <E> MutableCollection<E>.clearAddAll(element: Collection<E>) {
    this.clear()
    this.addAll(element)
}

fun <K,V> MutableMap<K, V>.clearAddAll(element: Map<K, V>) {
    this.clear()
    this.putAll(element)
}