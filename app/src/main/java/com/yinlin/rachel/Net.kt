package com.yinlin.rachel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import com.google.gson.JsonObject
import com.xuexiang.xui.utils.XToastUtils
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
import java.net.Proxy
import java.util.concurrent.TimeUnit


object Net {
    private const val DOWNLOAD_BUFFER_SIZE = 1024 * 64

    private val client: OkHttpClient = OkHttpClient.Builder()
        .proxy(Proxy.NO_PROXY)
        .connectTimeout(5, TimeUnit.SECONDS)
        .callTimeout(5, TimeUnit.SECONDS)
        .build()

    fun get(url: String, headers: Map<String, String>? = null): JsonObject? {
        try {
            val builder = Request.Builder().url(url)
            headers?.apply { builder.headers(this.toHeaders()) }
            val request = builder.build()
            client.newCall(request).execute().use { response ->
                return gson.fromJson(response.body?.string(), JsonObject::class.java)
            }
        }
        catch (ignored: Exception) { }
        return null
    }

    fun post(url: String, data: String, headers: Map<String, String>? = null): JsonObject? {
        try {
            val builder = Request.Builder().url(url)
            headers?.apply { builder.headers(this.toHeaders()) }
            builder.post(data.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
            val request = builder.build()
            client.newCall(request).execute().use { response ->
                return gson.fromJson(response.body?.string(), JsonObject::class.java)
            }
        }
        catch (ignored: Exception) { }
        return null
    }

    fun postForm(url: String, files: Map<String, String>, content: Map<String, String>?, headers: Map<String, String>? = null): JsonObject? {
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
                return gson.fromJson(response.body?.string(), JsonObject::class.java)
            }
        }
        catch (ignored: Exception) { }
        return null
    }

    @NewThread
    fun download(context: Context, url: String, mediaUri: Uri, values: ContentValues) {
        val dialog = MaterialDialog.Builder(context).iconRes(R.mipmap.icon)
            .title("下载中...").negativeText(R.string.cancel)
            .progress(false, 0, true).build()
        val handler = Handler(context.mainLooper)
        val thread = Thread {
            var status = false
            try {
                client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                    response.body?.apply {
                        val inputStream = this.byteStream()
                        val totalSize = this.contentLength()
                        if (totalSize <= 0) return@apply
                        handler.post { dialog.maxProgress = (totalSize / 1024).toInt() }
                        context.contentResolver.insert(mediaUri, values)?.apply {
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
            handler.post {
                if (status) XToastUtils.success("下载成功")
                else XToastUtils.error("下载失败")
            }
            dialog.dismiss()
        }
        dialog.setOnCancelListener { thread.interrupt() }
        dialog.show()
        thread.start()
    }

    @NewThread
    fun downloadPicture(context: Context, url: String) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, url.substringAfterLast('/'))
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/webp")
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        download(context, url, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    @NewThread
    fun downloadVideo(context: Context, url: String) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, url.substringAfterLast('/'))
        values.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        download(context, url, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
    }
}