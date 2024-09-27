package com.yinlin.rachel.data

import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import com.xuexiang.xui.widget.imageview.preview.enitity.IPreviewInfo

data class Weibo(
    var name: String, // 昵称
    var avatar: String, // 头像
    var text: String, // 内容
    var time: String, // 时间
    var location: String, // 定位
    var id: String, // 编号
    val pictures: MutableList<Picture> = ArrayList(), // 图片集
) {
    enum class MsgType { PICTURE, VIDEO }

    class Picture(var type: MsgType = MsgType.PICTURE,
                  private var thumb: String = "",
                  var source: String = "") : IPreviewInfo {
        var showBounds: Rect = Rect()

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

        fun getThumb() = thumb
    }
}