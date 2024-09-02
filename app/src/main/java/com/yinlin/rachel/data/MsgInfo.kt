package com.yinlin.rachel.data

import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import com.xuexiang.xui.widget.imageview.preview.enitity.IPreviewInfo


class MsgInfo(val name: String, val avatar: String, val text: String, val time: String, val location: String) {
    val pictures = ArrayList<Picture>()

    enum class MsgType { PICTURE, VIDEO }

    class Picture(private var type: MsgType = MsgType.PICTURE,
                  private var thumb: String = "",
                  private var source: String = "") : IPreviewInfo {
        private var showBounds: Rect = Rect()

        companion object CREATOR : Parcelable.Creator<Picture> {
            override fun createFromParcel(parcel: Parcel) = Picture(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Picture>(size)
        }

        constructor(parcel: Parcel) : this() {
            type = if (parcel.readInt() == 0) MsgType.PICTURE else MsgType.VIDEO
            thumb = parcel.readString() ?: ""
            @Suppress("DEPRECATION")
            showBounds = parcel.readParcelable(Rect::class.java.classLoader) ?: Rect()
            source = parcel.readString() ?: ""
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(if (type == MsgType.PICTURE) 0 else 1)
            parcel.writeString(thumb)
            parcel.writeParcelable(showBounds, flags)
            parcel.writeString(source)
        }

        override fun getUrl() = if (type == MsgType.VIDEO) thumb else source
        override fun getBounds(): Rect = showBounds
        override fun getVideoUrl() = if (type == MsgType.VIDEO) source else null
        override fun describeContents() = 0

        fun setShowBounds(rect: Rect) { showBounds = rect }
        fun getThumb() = thumb
    }
}