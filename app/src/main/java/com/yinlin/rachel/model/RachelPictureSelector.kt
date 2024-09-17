package com.yinlin.rachel.model

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.utils.ActivityCompatHelper
import com.yalantis.ucrop.UCrop
import com.yinlin.rachel.R

object RachelPictureSelector {
    class RachelImageEngine : ImageEngine {
        companion object {
            val instance = RachelImageEngine()
        }

        override fun loadImage(context: Context, url: String, imageView: ImageView) {
            if (ActivityCompatHelper.assertValidRequest(context))
                Glide.with(context).load(url).into(imageView)
        }

        override fun loadImage(context: Context, imageView: ImageView, url: String, maxWidth: Int, maxHeight: Int) {
            if (ActivityCompatHelper.assertValidRequest(context))
                Glide.with(context).load(url).override(maxWidth, maxHeight).into(imageView)
        }

        override fun loadAlbumCover(context: Context, url: String, imageView: ImageView) {
            if (ActivityCompatHelper.assertValidRequest(context))
                Glide.with(context).asBitmap().load(url)
                    .override(180, 180).sizeMultiplier(0.5f)
                    .transform(CenterCrop(), RoundedCorners(8))
                    .placeholder(R.drawable.placeholder_pic).into(imageView)
        }

        override fun loadGridImage(context: Context, url: String, imageView: ImageView) {
            if (ActivityCompatHelper.assertValidRequest(context))
                Glide.with(context).load(url).override(200, 200)
                    .centerCrop().placeholder(R.drawable.placeholder_pic).into(imageView)
        }

        override fun pauseRequests(context: Context) {
            if (ActivityCompatHelper.assertValidRequest(context)) Glide.with(context).pauseRequests()
        }

        override fun resumeRequests(context: Context) {
            if (ActivityCompatHelper.assertValidRequest(context)) Glide.with(context).resumeRequests()
        }
    }

    fun interface SingleSelectListener {
        fun onSelected(filename: String)
    }

    fun single(context: Context, width: Int, height: Int, isCircle: Boolean, listener: SingleSelectListener) {
        PictureSelector.create(context).openGallery(SelectMimeType.ofImage())
            .setImageEngine(RachelImageEngine.instance)
            .setCropEngine { fragment, srcUri, destinationUri, dataSource, requestCode ->
                val options = UCrop.Options()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    options.setCompressionFormat(Bitmap.CompressFormat.WEBP_LOSSY)
                }
                else {
                    @Suppress("DEPRECATION")
                    options.setCompressionFormat(Bitmap.CompressFormat.WEBP)
                }
                options.setCompressionQuality(100)
                options.withAspectRatio(width.toFloat(), height.toFloat())
                options.withMaxResultSize(width, height)
                options.setCircleDimmedLayer(isCircle)
                UCrop.of(srcUri, destinationUri, dataSource).withOptions(options).start(fragment.requireActivity(), fragment, requestCode)
            }
            .setSelectionMode(SelectModeConfig.SINGLE)
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    if (result.size == 1) listener.onSelected(result[0].cutPath)
                }
                override fun onCancel() {}
            })
    }
}