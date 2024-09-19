package com.yinlin.rachel.view

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.widget.imageview.preview.PreviewBuilder
import com.yinlin.rachel.R
import com.yinlin.rachel.databinding.ItemImageSelectBinding
import com.yinlin.rachel.div
import com.yinlin.rachel.model.RachelPictureSelector.RachelImageEngine
import com.yinlin.rachel.model.RachelPreviewInfo
import com.yinlin.rachel.rachelClick
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ImageSelectView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : RecyclerView(context, attrs, defStyleAttr) {
    class ViewHolder(val v: ItemImageSelectBinding) : RecyclerView.ViewHolder(v.root)

    class Adapter(private val rv: ImageSelectView) : RecyclerView.Adapter<ViewHolder>() {
        internal val items: MutableList<String> = arrayListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val context = parent.context
            val v = ItemImageSelectBinding.inflate(LayoutInflater.from(context), parent, false)
            val holder = ViewHolder(v)
            v.delete.rachelClick {
                val position = holder.bindingAdapterPosition
                if (position != items.size) { // 是图片
                    items.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
            v.root.rachelClick {
                val position = holder.bindingAdapterPosition
                if (position == items.size) { // 是添加按钮
                    val addNum = maxOf(rv.maxPictureNum - items.size, 0)
                    if (addNum == 0) XToastUtils.warning("最多只能添加${rv.maxPictureNum}张图片")
                    else {
                        PictureSelector.create(context).openGallery(SelectMimeType.ofImage())
                            .setImageEngine(RachelImageEngine.instance)
                            .setSelectionMode(SelectModeConfig.MULTIPLE)
                            .setMaxSelectNum(addNum)
                            .setCompressEngine(CompressFileEngine { context, source, call ->
                                Thread {
                                    for ((index, pic) in source.withIndex()) {
                                        var bitmap: Bitmap? = null
                                        try {
                                            val fis = context.contentResolver.openInputStream(pic)
                                            bitmap = BitmapFactory.decodeStream(fis)
                                            if (fis != null && bitmap != null) {
                                                val width = bitmap.width
                                                val height = bitmap.height
                                                if (width * height > 3000000) {
                                                    bitmap = Bitmap.createScaledBitmap(bitmap, width / 2, height / 2, true)
                                                }
                                                else if (width * height > 1000000) {
                                                    bitmap = Bitmap.createScaledBitmap(bitmap, width * 3 / 4, height * 3 / 4, true)
                                                }
                                                val des = context.cacheDir / "${System.currentTimeMillis()}_${index}.webp"
                                                val fos = FileOutputStream(des)
                                                val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY
                                                else @Suppress("DEPRECATION") Bitmap.CompressFormat.WEBP
                                                val success = bitmap.compress(format, 90, fos)
                                                fis.close()
                                                fos.close()
                                                call.onCallback(pic.toString(), if (success) des.absolutePath else null)
                                            }
                                        }
                                        catch (ignored: Exception) {
                                            call.onCallback(pic.toString(), null)
                                        }
                                        finally {
                                            if (bitmap != null && !bitmap.isRecycled) bitmap.recycle()
                                        }
                                    }
                                }.start()
                            })
                            .forResult(object : OnResultCallbackListener<LocalMedia> {
                                override fun onResult(result: ArrayList<LocalMedia>) {
                                    val currentSize = items.size
                                    val addSize = result.size
                                    for (pic in result) items.add(pic.compressPath)
                                    notifyItemRangeChanged(currentSize, addSize + 1)
                                }
                                override fun onCancel() {}
                            })
                    }
                }
                else { // 是图片
                    val pics = ArrayList<RachelPreviewInfo>()
                    for (pic in items) pics += RachelPreviewInfo(pic)
                    PreviewBuilder.from(context as Activity)
                        .setImgs(pics).setCurrentIndex(position)
                        .setType(PreviewBuilder.IndicatorType.Dot).start()
                }
            }
            return holder
        }

        override fun getItemCount() = items.size + 1

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val v = holder.v
            if (position == items.size) { // 是添加按钮
                v.delete.visibility = View.GONE
                Glide.with(v.root.context).load(R.drawable.svg_add_image).into(v.pic)
            }
            else { // 是图片
                v.delete.visibility = View.VISIBLE
                Glide.with(v.root.context).load(items[position]).into(v.pic)
            }
        }
    }

    private var maxPictureNum: Int = 9

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context): this(context, null)

    init {
        setHasFixedSize(true)
    }

    fun init(maxNum: Int, column: Int) {
        maxPictureNum = if (maxNum <= 0) 9 else maxNum
        layoutManager = GridLayoutManager(context, if (column <= 0) 4 else column)
        recycledViewPool.setMaxRecycledViews(0, maxPictureNum)
        setItemViewCacheSize(maxPictureNum / 10)
        adapter = Adapter(this)
    }

    val images: List<String> get() = (adapter as Adapter).items
}