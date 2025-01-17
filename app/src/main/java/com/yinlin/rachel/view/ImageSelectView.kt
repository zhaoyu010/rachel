package com.yinlin.rachel.view

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.forjrking.lubankt.Luban
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.R
import com.yinlin.rachel.databinding.ItemImageSelectBinding
import com.yinlin.rachel.model.RachelPictureSelector.RachelImageEngine
import com.yinlin.rachel.model.RachelPreview
import com.yinlin.rachel.rachelClick


class ImageSelectView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : RecyclerView(context, attrs, defStyleAttr) {
    class ViewHolder(val v: ItemImageSelectBinding) : RecyclerView.ViewHolder(v.root)

    class Adapter(private val rv: ImageSelectView) : RecyclerView.Adapter<ViewHolder>() {
        internal val items: MutableList<String> = arrayListOf()

        private val compressEngine = CompressFileEngine { context, source, call ->
            val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY
            else @Suppress("DEPRECATION") Bitmap.CompressFormat.WEBP
            Luban.with(context as FragmentActivity).load(source)
                .concurrent(true).useDownSample(true)
                .format(format).ignoreBy(200).quality(90).compressObserver {
                    onSuccess = {
                        val count = it.size
                        for ((index, pic) in source.withIndex()) {
                            if (index < count) call.onCallback(pic.toString(), it[index].absolutePath)
                            else call.onCallback(pic.toString(), null)
                        }
                    }
                    onError = { _, pic ->
                        call.onCallback(pic.toString(), null)
                    }
                }.launch()
        }

        private val resultProcessor = object : OnResultCallbackListener<LocalMedia> {
            override fun onResult(result: ArrayList<LocalMedia>) {
                val currentSize = items.size
                val addSize = result.size
                for (pic in result) items += pic.compressPath
                notifyItemRangeChanged(currentSize, addSize + 1)
            }
            override fun onCancel() {}
        }

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
                            .setCompressEngine(compressEngine)
                            .forResult(resultProcessor)
                    }
                }
                else { // 是图片
                    val previews = RachelPreview.fromSingleUri(items)
                    RachelPreview.updateRect(parent, previews)
                    RachelPreview.show(context, previews, position)
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