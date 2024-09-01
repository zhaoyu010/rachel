package com.yinlin.rachel.model

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class RachelImageLoader(context: Context, val options: RequestOptions) {
    val glide: RequestManager = Glide.with(context)

    constructor(context: Context): this(context, RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
    constructor(context: Context, @DrawableRes placeholder: Int, strategy: DiskCacheStrategy):
            this(context, RequestOptions().placeholder(placeholder).diskCacheStrategy(strategy))
    constructor(context: Context, @DrawableRes placeholder: Int, strategy: DiskCacheStrategy, transformation: Transformation<Bitmap>):
            this(context, RequestOptions().placeholder(placeholder).diskCacheStrategy(strategy).transform(transformation))
}