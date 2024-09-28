package com.yinlin.rachel.model

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xuexiang.xui.widget.imageview.nine.ItemImageClickListener
import com.xuexiang.xui.widget.imageview.nine.NineGridImageViewAdapter
import com.yinlin.rachel.R
import com.yinlin.rachel.load

class RachelNineGridAdapter(private val context: Context) : NineGridImageViewAdapter<RachelPreview>(), ItemImageClickListener<RachelPreview> {
    private val rilNet: RachelImageLoader = RachelImageLoader(context, R.drawable.placeholder_pic, DiskCacheStrategy.ALL)

    override fun onDisplayImage(context: Context, view: ImageView, picture: RachelPreview) = view.load(rilNet, picture.mImageUrl)

    override fun onItemImageClick(view: ImageView, index: Int, items: List<RachelPreview>) {
        RachelPreview.updateRect(view.parent as ViewGroup, items)
        RachelPreview.show(context, items, index)
    }
}