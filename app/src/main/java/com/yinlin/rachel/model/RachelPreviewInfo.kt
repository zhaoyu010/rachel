package com.yinlin.rachel.model

import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import com.xuexiang.xui.widget.imageview.preview.enitity.IPreviewInfo

class RachelPreviewInfo(private val url: String) : IPreviewInfo {
    companion object CREATOR : Parcelable.Creator<RachelPreviewInfo> {
        override fun createFromParcel(parcel: Parcel) = RachelPreviewInfo(parcel)
        override fun newArray(size: Int) = arrayOfNulls<RachelPreviewInfo>(size)
    }

    constructor(parcel: Parcel) : this(parcel.readString()!!)
    override fun getUrl(): String = url
    override fun getBounds(): Rect = Rect()
    override fun getVideoUrl(): String? = null
    override fun describeContents(): Int = 0
    override fun writeToParcel(dest: Parcel, flags: Int) = dest.writeString(url)
}