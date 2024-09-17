package com.yinlin.rachel.model

import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import com.xuexiang.xui.widget.imageview.preview.enitity.IPreviewInfo

class RachelNineGridPicture(private var picUrl: String = "") : IPreviewInfo {
    var showBounds: Rect = Rect()

    companion object CREATOR : Parcelable.Creator<RachelNineGridPicture> {
        override fun createFromParcel(parcel: Parcel) = RachelNineGridPicture(parcel)
        override fun newArray(size: Int) = arrayOfNulls<RachelNineGridPicture>(size)
    }

    constructor(parcel: Parcel) : this() {
        picUrl = parcel.readString() ?: ""
        @Suppress("DEPRECATION")
        showBounds = parcel.readParcelable(Rect::class.java.classLoader) ?: Rect()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(picUrl)
        parcel.writeParcelable(showBounds, flags)
    }

    override fun getUrl(): String = picUrl
    override fun getBounds(): Rect = showBounds
    override fun getVideoUrl(): String? = null
    override fun describeContents(): Int = 0
}