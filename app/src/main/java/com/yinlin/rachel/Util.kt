package com.yinlin.rachel

import android.content.Context
import android.util.TypedValue
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

val gson = Gson()
inline fun <reified T> JsonElement?.to(): T = gson.fromJson(this, object : TypeToken<T>(){}.type)
inline fun <reified T> String.to(): T = gson.fromJson(this, object : TypeToken<T>(){}.type)
inline fun <reified T> T.json(): String = gson.toJson(this, object : TypeToken<T>(){}.type)
inline fun <reified K, reified V> jsonMap(vararg pairs: Pair<K, V>): String = mapOf(*pairs).json()

fun Long.toStringTime(): String {
    val second = (this / 1000).toInt()
    return String.format(Locale.ENGLISH,"%02d:%02d", second / 60, second % 60)
}

fun String.toMD5(): String = try {
    val md = MessageDigest.getInstance("MD5")
    md.update(this.toByteArray(StandardCharsets.UTF_8))
    val messageDigest = md.digest()
    val hexString = StringBuilder()
    for (b in messageDigest) {
        val hex = Integer.toHexString(0xff and b.toInt())
        if (hex.length == 1) hexString.append('0')
        hexString.append(hex)
    }
    hexString.toString()
}
catch (ignored: Exception) { "" }

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