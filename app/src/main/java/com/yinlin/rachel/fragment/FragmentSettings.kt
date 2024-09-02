package com.yinlin.rachel.fragment

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.text.InputType
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropImageEngine
import com.yalantis.ucrop.model.AspectRatio
import com.yinlin.rachel.Config
import com.yinlin.rachel.Dialog
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelFont
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.WeiboAPI
import com.yinlin.rachel.data.WeiboUser
import com.yinlin.rachel.databinding.FragmentSettingsBinding
import com.yinlin.rachel.err
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelImageEngine
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.rachelClick
import com.yinlin.rachel.warning


class FragmentSettings(pages: RachelPages) : RachelFragment<FragmentSettingsBinding>(pages) {
    private val rilNet = RachelImageLoader(pages.context, R.drawable.placeholder_pic, DiskCacheStrategy.ALL)

    override fun bindingClass() = FragmentSettingsBinding::class.java

    override fun init() {
        /*    ----    账号设置    ----    */
        v.tvAccount.typeface = RachelFont.bold
        // 更换头像
        v.avatar.rachelClick {
            PictureSelector.create(pages.context).openGallery(SelectMimeType.ofImage())
                .setImageEngine(RachelImageEngine.instance)
                .setCropEngine { fragment: Fragment, srcUri: Uri?, destinationUri: Uri?, dataSource: ArrayList<String?>?, requestCode: Int ->
                    val uCrop = UCrop.of(
                        srcUri!!, destinationUri!!, dataSource
                    )
                    uCrop.setImageEngine(object : UCropImageEngine {
                        override fun loadImage(
                            context: Context,
                            url: String,
                            imageView: ImageView
                        ) {
                            Glide.with(context).load(url).into(imageView)
                        }

                        override fun loadImage(
                            context: Context,
                            url: Uri,
                            maxWidth: Int,
                            maxHeight: Int,
                            call: UCropImageEngine.OnCallbackListener<Bitmap>
                        ) {
                        }
                    })
                    val options = UCrop.Options()
                    @Suppress("DEPRECATION")
                    options.setCompressionFormat(Bitmap.CompressFormat.WEBP)
                    options.setAspectRatioOptions(0, AspectRatio("方形", 1f, 1f))
                    uCrop.withOptions(options)
                    uCrop.start(fragment.requireActivity(), fragment, requestCode)
                }
                .setSelectionMode(SelectModeConfig.SINGLE)
                .forResult(object : OnResultCallbackListener<LocalMedia> {
                    override fun onResult(result: ArrayList<LocalMedia>) {
                        if (result.size == 1) {
                            val media = result[0]
                            println("Path: " + media.path)
                            println("RealPath: " + media.realPath)
                            println("OriginalPath: " + media.originalPath)
                            println("CutPath: " + media.cutPath)
                        }
                    }
                    override fun onCancel() {}
                })
        }
        // 退出登录
        v.logoff.setOnSuperTextViewClickListener {
            if (isLogin) Dialog.confirm(pages.context, "是否退出登录") { _, _ -> logoff() }
        }

        /*    ----    资讯设置    ----    */
        v.tvMsg.setTypeface(RachelFont.bold)
        // 添加微博用户
        v.weibo.setOnSuperTextViewClickListener {
            Dialog.input(pages.context, "请输入微博用户的uid(非昵称)", 20) { _, input ->
                val uid = input.toString()
                val weiboUser: WeiboUser? = Config.weibo_users[uid]
                (weiboUser == null).warning(weiboUser!!.name + " 已存在") { addWeiboUser(uid) }
            }
        }
        // 删除微博用户
        v.weiboList.setOnTagClickListener { text ->
            Dialog.confirm(pages.context, "是否删除此微博用户") { _, _ ->
                val weiboUsers = Config.weibo_users
                weiboUsers.entries.removeIf { entry -> entry.value.name == text }
                Config.weibo_users = weiboUsers
                v.weiboList.clearAndAddTags(WeiboUser.getNames(weiboUsers))
            }
        }

        /*    ----    美图设置    ----    */
        v.tvRes.setTypeface(RachelFont.bold)

        /*    ----    听歌设置    ----    */
        v.tvMusic.setTypeface(RachelFont.bold)

        updateInfo()
    }

    override fun back(): Boolean = true

    private fun updateInfo() {
        v.id.setRightString(Config.user_id)
        val user = Config.user
        if (user.isActive) {
            v.avatar.load(rilNet, user.avatarPath)
            v.signature.text = user.signature
            v.inviter.setRightString(user.inviter)
            v.wall.load(rilNet, user.wallPath)
        }
        else {
            v.avatar.load(pages.ril, R.drawable.placeholder_pic)
            v.signature.text = pages.getResString(R.string.default_signature)
        }
        v.weiboList.clearAndAddTags(WeiboUser.getNames(Config.weibo_users))
    }

    private fun logoff() {
        Config.user_id_meta.setDefault()
        Config.user_pwd_meta.setDefault()
        Config.user_meta.setDefault()
        pages.popSecond()
        pages.sendMessage(RachelPages.me, RachelMessage.ME_UPDATE_USER_INFO)
    }

    @NewThread
    private fun addWeiboUser(uid: String) {
        Thread {
            val result: Array<String>? = WeiboAPI.extractContainerId(uid)
            post {
                result.err("解析微博用户失败") {
                    val weiboUsers = Config.weibo_users
                    weiboUsers[it[0]] = WeiboUser(it[1], it[2])
                    Config.weibo_users = weiboUsers
                    v.weiboList.clearAndAddTags(WeiboUser.getNames(weiboUsers))
                }
            }
        }.start()
    }

    private val isLogin: Boolean get() = !Config.user_id_meta.isDefault() && !Config.user_pwd_meta.isDefault()
}