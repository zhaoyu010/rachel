package com.yinlin.rachel.dialog

import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xuexiang.xqrcode.XQRCode
import com.yinlin.rachel.Config
import com.yinlin.rachel.R
import com.yinlin.rachel.bold
import com.yinlin.rachel.databinding.BottomDialogUserCardBinding
import com.yinlin.rachel.fragment.FragmentMe
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelBottomDialog
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.toDP

class BottomDialogUserCard(fragment: FragmentMe) : RachelBottomDialog<BottomDialogUserCardBinding, FragmentMe>(fragment, 0.9f) {
    private val pages = fragment.pages

    private val rilNet = RachelImageLoader(pages.context, R.drawable.placeholder_pic, DiskCacheStrategy.ALL)

    override fun bindingClass() = BottomDialogUserCardBinding::class.java

    override fun init() {
        super.init()
        v.id.bold = true
    }

    fun update(): BottomDialogUserCard {
        val id = Config.user_id
        v.id.text = id
        v.avatar.load(rilNet, Config.user.avatarPath, Config.cache_key_avatar.get())
        v.qrcode.setImageBitmap(XQRCode.createQRCodeWithLogo("rachel://yinlin/openProfile?id=${id}",
            200.toDP(pages.context), 200.toDP(pages.context),
            ResourcesCompat.getDrawable(pages.context.resources, R.mipmap.icon, pages.context.theme)!!.toBitmap()))
        return this
    }
}