package com.yinlin.rachel.model

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xuexiang.xui.widget.imageview.nine.ItemImageClickListener
import com.xuexiang.xui.widget.imageview.nine.NineGridImageViewAdapter
import com.xuexiang.xui.widget.imageview.preview.PreviewBuilder
import com.yinlin.rachel.R
import com.yinlin.rachel.load
import com.yinlin.rachel.updateNineGridBounds

class RachelNineGridAdapter(context: Context) : NineGridImageViewAdapter<RachelNineGridPicture>(), ItemImageClickListener<RachelNineGridPicture> {
    private val rilNet: RachelImageLoader = RachelImageLoader(context, R.drawable.placeholder_pic, DiskCacheStrategy.ALL)

    override fun onDisplayImage(context: Context, view: ImageView, picture: RachelNineGridPicture) = view.load(rilNet, picture.url)

    override fun onItemImageClick(imageView: ImageView, index: Int, list: MutableList<RachelNineGridPicture>) {
        imageView.updateNineGridBounds { i, bounds -> list[i].showBounds = bounds }
        PreviewBuilder.from(imageView.context as Activity)
            .setImgs(list).setCurrentIndex(index)
            .setType(PreviewBuilder.IndicatorType.Dot).start()
    }
}