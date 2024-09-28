package com.yinlin.rachel.model

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import com.xuexiang.xui.widget.imageview.preview.PreviewBuilder
import com.xuexiang.xui.widget.imageview.preview.enitity.IPreviewInfo

class RachelPreview(var mImageUrl: String) : IPreviewInfo {
    var mSourceUrl: String = mImageUrl
    var mVideoUrl: String = ""
    var mPosition = Rect()

    constructor(imgUrl: String, sourceUrl: String) : this(imgUrl) {
        mSourceUrl = sourceUrl
    }

    constructor(imgUrl: String, sourceUrl: String, videoUrl: String) : this(imgUrl, sourceUrl) {
        mVideoUrl = videoUrl
    }

    constructor(parcel: Parcel) : this(parcel.readString() ?: "") {
        mSourceUrl = parcel.readString() ?: ""
        mVideoUrl = parcel.readString() ?: ""
        @Suppress("DEPRECATION")
        mPosition = parcel.readParcelable(Rect::class.java.classLoader) ?: Rect()
    }

    companion object CREATOR : Parcelable.Creator<RachelPreview> {
        override fun createFromParcel(parcel: Parcel) = RachelPreview(parcel)
        override fun newArray(size: Int) = arrayOfNulls<RachelPreview>(size)

        fun <T> fromSingleUri(collection: Collection<T>, fetch: ((T) -> String)? = null): List<RachelPreview> {
            val pics = ArrayList<RachelPreview>(collection.size)
            if (fetch == null) {
                for (item in collection) {
                    if (item is String) pics += RachelPreview(item)
                }
            }
            else {
                for (item in collection) pics += RachelPreview(fetch(item))
            }
            return pics
        }

        fun updateRect(parent: ViewGroup, items: List<RachelPreview>) {
            for ((i, preview) in items.withIndex()) parent.getChildAt(i).getGlobalVisibleRect(preview.mPosition)
        }

        fun show(context: Context, items: List<RachelPreview>, position: Int) {
            PreviewBuilder.from(context as Activity)
                .setImgs(items).setCurrentIndex(position)
                .setSingleShowType(false).setSingleFling(true)
                .setType(PreviewBuilder.IndicatorType.Dot).start()
        }

        fun show(context: Context, item: RachelPreview) {
            show(context, listOf(item), 0)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mImageUrl)
        parcel.writeString(mSourceUrl)
        parcel.writeString(mVideoUrl)
        parcel.writeParcelable(mPosition, flags)
    }

    override fun getBounds(): Rect = mPosition
    override fun getUrl(): String = mImageUrl
    override fun getVideoUrl(): String? = mVideoUrl.ifEmpty { null }
    override fun describeContents() = 0

    val isImage: Boolean = mVideoUrl.isEmpty()
    val isVideo: Boolean = mVideoUrl.isNotEmpty()

    fun updateRect(view: View) { view.getGlobalVisibleRect(mPosition) }
}