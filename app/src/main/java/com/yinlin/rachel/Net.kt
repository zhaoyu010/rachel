package com.yinlin.rachel

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import com.yinlin.rachel.annotation.NewThread
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


object Net {
    private const val DOWNLOAD_BUFFER_SIZE = 1024 * 64

    private val client: OkHttpClient = OkHttpClient()

    fun get(url: String, headers: Map<String, String>?): JsonObject? {
        try {
            val builder = Request.Builder().url(url)
            headers?.apply { builder.headers(this.toHeaders()) }
            val request = builder.build()
            client.newCall(request).execute().use { response ->
                return Gson().fromJson(response.body?.string(), JsonObject::class.java)
            }
        }
        catch (ignored: Exception) { }
        return null
    }

    fun post(url: String, data: String, headers: Map<String, String>?): JsonObject? {
        try {
            val builder = Request.Builder().url(url)
            headers?.apply { builder.headers(this.toHeaders()) }
            builder.post(data.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
            val request = builder.build()
            client.newCall(request).execute().use { response ->
                return Gson().fromJson(response.body?.string(), JsonObject::class.java)
            }
        }
        catch (ignored: Exception) { }
        return null
    }

    fun postForm(url: String, files: Map<String, String>, content: Map<String, String>?, headers: Map<String, String>?): JsonObject? {
        try {
            val builder = Request.Builder().url(url)
            headers?.apply { builder.headers(this.toHeaders()) }
            val bodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            content?.apply {
                for ((key, value) in content) bodyBuilder.addFormDataPart(key, value)
            }
            for ((key, value) in files) {
                val file = File(value)
                if (file.exists()) bodyBuilder.addFormDataPart(key, file.name, file.asRequestBody("file/raw".toMediaTypeOrNull()))
            }
            builder.post(bodyBuilder.build())
            val request = builder.build()
            client.newCall(request).execute().use { response ->
                return Gson().fromJson(response.body?.string(), JsonObject::class.java)
            }
        }
        catch (ignored: Exception) { }
        return null
    }

    @NewThread
    fun downloadPicture(context: Context, url: String, callback: ((Boolean) -> Unit)?) {
        val dialog = MaterialDialog.Builder(context).iconRes(R.mipmap.icon)
            .title("下载中...").negativeText(R.string.cancel)
            .progress(false, 0, true).build()
        val handler = Handler(context.mainLooper)
        val thread = Thread {
            val filename = url.substringAfterLast('/')
            var status = false
            try {
                client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                    response.body?.apply {
                        val inputStream = this.byteStream()
                        val totalSize = this.contentLength()
                        if (totalSize <= 0) return@apply
                        handler.post { dialog.maxProgress = (totalSize / 1024).toInt() }
                        val values = ContentValues()
                        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/webp")
                        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.apply {
                            context.contentResolver.openOutputStream(this).use { outputStream ->
                                outputStream?.apply {
                                    val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
                                    var bytesRead: Int
                                    var bytesReadTotal = 0
                                    while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                                        outputStream.write(buffer, 0, bytesRead)
                                        bytesReadTotal += bytesRead
                                        handler.post { dialog.setProgress( bytesReadTotal / 1024) }
                                        if (Thread.interrupted()) break
                                    }
                                    outputStream.flush()
                                    status = true
                                }
                            }
                        }
                    }
                }
            } catch (ignored: Exception) { }
            dialog.dismiss()
            callback?.invoke(status)
        }
        dialog.setOnCancelListener { thread.interrupt() }
        dialog.show()
        thread.start()
    }
}