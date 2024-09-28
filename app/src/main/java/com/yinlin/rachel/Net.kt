package com.yinlin.rachel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonObject
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import com.yinlin.rachel.annotation.NewThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.Proxy
import java.util.concurrent.CancellationException
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

    interface DownLoadMediaListener {
        fun onCancel()
        fun onDownloadComplete(status: Boolean)
    }

    private val simpleDownLoadMediaListener = object : DownLoadMediaListener {
        override fun onCancel() { }
        override fun onDownloadComplete(status: Boolean) {
            if (status) XToastUtils.success("下载成功")
            else XToastUtils.error("下载失败")
        }
    }

    @NewThread
    fun downloadMedia(context: Context, url: String, mediaUri: Uri, values: ContentValues, callback: DownLoadMediaListener?) {
        var job: Job? = null
        job = (context as LifecycleOwner).lifecycleScope.launch {
            val dialog = MaterialDialog.Builder(context).iconRes(R.mipmap.icon)
                .title("下载中...").negativeText(R.string.cancel)
                .progress(false, 0, true)
                .cancelListener { job?.cancel() }
                .show()
            var cancel = false
            var status = false
            try {
                val uri = context.contentResolver.insert(mediaUri, values) ?: return@launch
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri).use { outputStream ->
                        if (outputStream == null) return@withContext
                        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                            val body = response.body ?: return@withContext
                            val inputStream = body.byteStream()
                            val totalSize = body.contentLength()
                            if (totalSize <= 0) return@withContext
                            withContext(Dispatchers.Main) { dialog.maxProgress = (totalSize / 1024).toInt() }
                            val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
                            var bytesRead: Int
                            var bytesReadTotal = 0
                            while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                                bytesReadTotal += bytesRead
                                withContext(Dispatchers.Main) { dialog.setProgress( bytesReadTotal / 1024) }
                            }
                            outputStream.flush()
                            status = true
                        }
                    }
                }
            }
            catch (ignored: CancellationException) { cancel = true }
            catch (ignored: Exception) { }
            dialog.dismiss()
            if (cancel) callback?.onCancel()
            else callback?.onDownloadComplete(status)
        }
    }

    @NewThread
    fun downloadPicture(context: Context, url: String) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, url.substringAfterLast('/'))
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/webp")
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        downloadMedia(context, url, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values, simpleDownLoadMediaListener)
    }

    @NewThread
    fun downloadVideo(context: Context, url: String) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, url.substringAfterLast('/'))
        values.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        downloadMedia(context, url, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values, simpleDownLoadMediaListener)
    }
}