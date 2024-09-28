package com.yinlin.rachel.fragment

import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.Config
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API
import com.yinlin.rachel.api.WeiboAPI
import com.yinlin.rachel.bold
import com.yinlin.rachel.clear
import com.yinlin.rachel.data.WeiboUser
import com.yinlin.rachel.databinding.FragmentSettingsBinding
import com.yinlin.rachel.dialog.BottomDialogAbout
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelDialog
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.model.RachelPictureSelector
import com.yinlin.rachel.rachelClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FragmentSettings(pages: RachelPages) : RachelFragment<FragmentSettingsBinding>(pages) {
    private val rilNet = RachelImageLoader(pages.context, R.drawable.placeholder_pic, DiskCacheStrategy.ALL)

    private val bottomDialogAbout = BottomDialogAbout(this)

    override fun bindingClass() = FragmentSettingsBinding::class.java

    override fun init() {
        bottomDialogAbout.init()

        /*    ----    账号设置    ----    */
        v.tvAccount.bold = true

        // 更换头像
        v.avatar.rachelClick {
            if (Config.isLoginAndUpdate) {
                RachelPictureSelector.single(pages.context, 256, 256, true) {
                    filename -> updateAvatar(filename)
                }
            }
            else XToastUtils.warning("请先登录")
        }

        // 更新个性签名
        v.signature.rachelClick {
            if (Config.isLoginAndUpdate) {
                RachelDialog.input(pages.context, "请输入个性签名", 64) { updateSignature(it) }
            }
            else XToastUtils.warning("请先登录")
        }

        // 更新背景墙
        v.wall.rachelClick {
            if (Config.isLoginAndUpdate) {
                RachelPictureSelector.single(pages.context, 910, 512, false) {
                    filename -> updateWall(filename)
                }
            }
            else XToastUtils.warning("请先登录")
        }
        // 退出登录
        v.logoff.setOnSuperTextViewClickListener {
            if (Config.isLoginAndUpdate) {
                RachelDialog.confirm(pages.context, "是否退出登录") { logoff() }
            }
            else XToastUtils.warning("请先登录")
        }

        /*    ----    资讯设置    ----    */
        v.tvMsg.bold = true
        // 添加微博用户
        v.weibo.setOnSuperTextViewClickListener {
            RachelDialog.input(pages.context, "请输入微博用户的uid(非昵称)", 20) {
                val weiboUser: WeiboUser? = Config.weibo_users[it]
                if (weiboUser != null) XToastUtils.warning(weiboUser.name + " 已存在")
                else addWeiboUser(it)
            }
        }
        // 删除微博用户
        v.weiboList.setOnTagClickListener { text ->
            RachelDialog.confirm(pages.context, "是否删除此微博用户") {
                val weiboUsers = Config.weibo_users
                weiboUsers.entries.removeIf { entry -> entry.value.name == text }
                Config.weibo_users = weiboUsers
                v.weiboList.clearAndAddTags(WeiboUser.getNames(weiboUsers))
            }
        }

        /*    ----    美图设置    ----    */
        v.tvRes.bold = true

        /*    ----    听歌设置    ----    */
        v.tvMusic.bold = true

        // 歌单云备份
        v.buttonUploadPlaylist.rachelClick {
            if (Config.isLogin) {
                RachelDialog.confirm(pages.context, "是否将本地所有歌单覆盖云端") { uploadPlaylist() }
            }
            else XToastUtils.warning("请先登录")
        }

        // 歌单云还原
        v.buttonDownloadPlaylist.rachelClick {
            if (Config.isLogin) {
                RachelDialog.confirm(pages.context, "是否从云端覆盖所有本地歌单") { downloadPlaylist() }
            }
            else XToastUtils.warning("请先登录")
        }

        /*    ----    其他    ----    */
        v.tvOther.bold = true

        v.checkUpdate.rachelClick { checkUpdate() }

        v.about.rachelClick { bottomDialogAbout.update().show() }

        v.feedback.rachelClick {
            if (Config.isLogin) {
                RachelDialog.inputMultiLines(pages.context, "请给出您宝贵的建议! 被采纳后将赠送银币!", 256, 10) {
                    sendFeedback(it)
                }
            }
            else XToastUtils.warning("请先登录")
        }

        updateInfo()
    }

    override fun quit() {
        bottomDialogAbout.release()
    }

    override fun back(): Boolean = true

    private fun updateInfo() {
        v.id.setRightString(Config.user_id)
        val user = Config.user
        if (user.ok) {
            v.avatar.load(rilNet, user.avatarPath, Config.cache_key_avatar.get())
            v.signature.text = user.signature
            v.inviter.setRightString(user.inviter ?: "")
            v.wall.load(rilNet, user.wallPath, Config.cache_key_wall.get())
        }
        else {
            v.avatar.load(pages.ril, R.drawable.placeholder_pic)
            v.signature.text = pages.getResString(R.string.default_signature)
            v.inviter.setRightString("")
            v.wall.clear(rilNet)
        }
        v.weiboList.clearAndAddTags(WeiboUser.getNames(Config.weibo_users))
    }

    private fun logoff() {
        Config.user_id_meta.setDefault()
        Config.user_pwd_meta.setDefault()
        Config.user_meta.setDefault()
        pages.pop()
        pages.sendMessage(RachelPages.me, RachelMessage.ME_UPDATE_USER_INFO)
    }

    // 添加微博用户
    @NewThread
    private fun addWeiboUser(uid: String) {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) {
                val result = WeiboAPI.extractContainerId(uid)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                result
            }
            if (result != null) {
                val weiboUsers = Config.weibo_users
                weiboUsers[result[0]] = WeiboUser(result[1], result[2])
                Config.weibo_users = weiboUsers
                v.weiboList.clearAndAddTags(WeiboUser.getNames(weiboUsers))
            }
            else XToastUtils.error("解析微博用户失败")
        }
    }

    // 更新头像
    @NewThread
    private fun updateAvatar(filename: String) {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) {
                val result = API.UserAPI.updateAvatar(Config.user_id, Config.user_pwd, filename)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                result
            }
            if (result.ok) {
                XToastUtils.success(result.value)
                Config.cache_key_avatar.update()
                v.avatar.load(rilNet, Config.user.avatarPath, Config.cache_key_avatar.get())
                pages.requestUserInfo()
            }
            else XToastUtils.error(result.value)
        }
    }

    // 更新个性签名
    @NewThread
    private fun updateSignature(signature: String) {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) {
                val result = API.UserAPI.updateSignature(Config.user_id, Config.user_pwd, signature)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                result
            }
            if (result.ok) {
                XToastUtils.success(result.value)
                v.signature.text = signature
                pages.requestUserInfo()
            }
            else XToastUtils.error(result.value)
        }
    }

    // 更新背景墙
    @NewThread
    private fun updateWall(wall: String) {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) {
                val result = API.UserAPI.updateWall(Config.user_id, Config.user_pwd, wall)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                result
            }
            if (result.ok) {
                XToastUtils.success(result.value)
                Config.cache_key_wall.update()
                v.wall.load(rilNet, Config.user.wallPath, Config.cache_key_wall.get())
                pages.requestUserInfo()
            }
            else XToastUtils.error(result.value)
        }
    }

    @NewThread
    private fun uploadPlaylist() {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                API.UserAPI.uploadPlaylist(Config.user_id, Config.user_pwd, Config.playlist)
            }
            if (result.ok) XToastUtils.success(result.value)
            else XToastUtils.error(result.value)
        }
    }

    @NewThread
    private fun downloadPlaylist() {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                API.UserAPI.downloadPlaylist(Config.user_id, Config.user_pwd)
            }
            if (result.ok) {
                pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_STOP_PLAYER)
                Config.playlist = result.value2
                pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_UPDATE_PLAYLIST)
                XToastUtils.success(result.value1)
            }
            else XToastUtils.error(result.value1)
        }
    }

    // 检查更新
    @NewThread
    private fun checkUpdate() {
        val srcVersion = pages.context.packageManager.getPackageInfo(pages.context.packageName, 0).longVersionCode
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) {
                val result = API.SysAPI.getServerVersionCode()
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                result
            }
            val content = "APP版本:${srcVersion}\n服务器版本:${result.value1}\n最低兼容版本:${result.value2}"
            if (result.ok) {
                if (srcVersion == result.value1) XToastUtils.success(content)
                else XToastUtils.warning(content)
            }
            else XToastUtils.error(content)
        }
    }

    // 提交反馈
    @NewThread
    private fun sendFeedback(content: String) {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) {
                val result = API.UserAPI.sendFeedback(Config.user_id, Config.user_pwd, content)
                withContext(Dispatchers.Main) { pages.loadingDialog.dismiss() }
                result
            }
            if (result.ok) XToastUtils.success(result.value)
            else XToastUtils.error(result.value)
        }
    }
}