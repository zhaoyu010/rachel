package com.yinlin.rachel.fragment

import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.haibin.calendarview.Calendar
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.Config
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.RachelMessage.ME_ADD_ACTIVITY
import com.yinlin.rachel.RachelMessage.ME_UPDATE_USER_INFO
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API
import com.yinlin.rachel.bold
import com.yinlin.rachel.data.ShowActivity
import com.yinlin.rachel.databinding.FragmentMeBinding
import com.yinlin.rachel.date
import com.yinlin.rachel.dialog.BottomDialogAbout
import com.yinlin.rachel.dialog.BottomDialogActivity
import com.yinlin.rachel.dialog.DialogAddActivity
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelDialog
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.pureColor
import com.yinlin.rachel.rachelClick
import com.yinlin.rachel.view.ActivityCalendarView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FragmentMe(pages: RachelPages) : RachelFragment<FragmentMeBinding>(pages) {
    private val rilNet = RachelImageLoader(pages.context, R.drawable.placeholder_pic, DiskCacheStrategy.ALL)

    override fun bindingClass() = FragmentMeBinding::class.java

    private val bottomDialogAbout = BottomDialogAbout(this)
    private val bottomDialogActivity = BottomDialogActivity(this)

    override fun init() {
        bottomDialogAbout.init()
        bottomDialogActivity.init()

        v.id.bold = true
        v.tvLevel.bold = true
        v.tvCoin.bold = true
        v.tvAccount.bold = true
        v.calendarMonth.bold = true

        // 扫码
        v.buttonScan.rachelClick {

        }

        // 名片
        v.buttonProfile.rachelClick {

        }

        // 设置
        v.buttonSettings.rachelClick { pages.navigate(FragmentSettings(pages)) }

        // 更新
        v.buttonUpdate.rachelClick { checkUpdate() }

        // 关于
        v.buttonAbout.rachelClick { bottomDialogAbout.update().show() }

        // 签到
        v.buttonSignIn.rachelClick {
            if (Config.isLoginAndUpdate) signIn()
            else {
                XToastUtils.warning("请先登录")
                pages.navigate(FragmentLogin(pages))
            }
        }

        // 好友
        v.buttonFriend.rachelClick {

        }

        // 主题
        v.buttonTopic.rachelClick {
            if (Config.isLoginAndUpdate) pages.navigate(FragmentProfile(pages, Config.user_id))
            else {
                XToastUtils.warning("请先登录")
                pages.navigate(FragmentLogin(pages))
            }
        }

        // 邮箱
        v.buttonMail.rachelClick {
            if (Config.isLoginAndUpdate) pages.navigate(FragmentMail(pages))
            else {
                XToastUtils.warning("请先登录")
                pages.navigate(FragmentLogin(pages))
            }
        }

        // 下拉刷新
        v.container.setOnRefreshListener {
            if (Config.isLogin) { pages.requestUserInfo() }
            else pages.navigate(FragmentLogin(pages))
            v.container.finishRefresh()
        }

        // 日历
        v.calendarMonth.text = "${v.calendar.curYear}年${v.calendar.curMonth}月"
        v.calendar.listener = object: ActivityCalendarView.Listener {
            override fun onClick(calendar: Calendar) {
                if (calendar.hasScheme()) getActivityInfo(calendar)
            }
            override fun onLongClick(calendar: Calendar) {
                if (Config.isLoginAndUpdate && Config.user.hasPrivilegeVIPCalendar()) {
                    if (calendar.hasScheme()) RachelDialog.confirm(pages.context, "确定要删除该活动吗?") {
                        deleteActivity(calendar)
                    }
                    else DialogAddActivity(pages, calendar).show()
                }
            }
            override fun onMonthChanged(year: Int, month: Int) {
                v.calendarMonth.text = "${year}年${month}月"
            }
        }
        v.buttonActivityRefresh.rachelClick { getActivities() }

        getActivities()
    }

    override fun update() {
        updateUserInfo()
    }

    override fun quit() {
        bottomDialogAbout.release()
        bottomDialogActivity.release()
    }

    override fun message(msg: RachelMessage, vararg args: Any?) {
        when (msg) {
            ME_UPDATE_USER_INFO -> updateUserInfo()
            ME_ADD_ACTIVITY -> addActivity(args[0] as Calendar, args[1] as ShowActivity)
            else -> { }
        }
    }

    private fun updateUserInfo() {
        val user = Config.user
        if (user.ok) {
            v.id.text = Config.user_id
            v.title.setTitle(user.titleGroup, user.title)
            v.signature.text = user.signature
            v.level.text = user.level.toString()
            v.coin.text = user.coin.toString()
            v.avatar.load(rilNet, user.avatarPath, Config.cache_key_avatar.get())
            v.wall.load(rilNet, user.wallPath, Config.cache_key_wall.get())
        }
        else {
            v.id.text = pages.getResString(R.string.default_id)
            v.title.setDefaultTitle()
            v.signature.text = pages.getResString(R.string.default_signature)
            v.level.text = "1"
            v.coin.text = "0"
            v.avatar.pureColor = pages.getResColor(R.color.white)
            v.wall.pureColor = pages.getResColor(R.color.dark)
        }
    }

    // 获取活动列表
    @NewThread
    private fun getActivities() {
        lifecycleScope.launch {
            v.calendarState.showLoading("刷新日历中...")
            val activities = withContext(Dispatchers.IO) { API.UserAPI.getActivities() }
            v.calendar.setActivities(activities)
            v.calendar.scrollToCurrent(true)
            v.calendarState.showContent()
        }
    }

    // 获取活动详情
    private fun getActivityInfo(calendar: Calendar) {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val activity = withContext(Dispatchers.IO) { API.UserAPI.getActivityInfo(calendar.date) }
            pages.loadingDialog.dismiss()
            if (activity.ok) bottomDialogActivity.update(activity).show()
            else XToastUtils.error("活动异常")
        }
    }

    // 添加活动
    @NewThread
    private fun addActivity(calendar: Calendar, show: ShowActivity) {
        lifecycleScope.launch {
            v.calendarState.showLoading("刷新日历中...")
            val result = withContext(Dispatchers.IO) { API.UserAPI.addActivity(Config.user_id, Config.user_pwd, show) }
            if (result.ok) {
                XToastUtils.success(result.value)
                v.calendar.addActivity(calendar, show.title)
            }
            else XToastUtils.error(result.value)
            v.calendarState.showContent()
        }
    }

    // 删除活动
    @NewThread
    private fun deleteActivity(calendar: Calendar) {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) { API.UserAPI.deleteActivity(Config.user_id, Config.user_pwd, calendar.date) }
            pages.loadingDialog.dismiss()
            if (result.ok) {
                v.calendar.removeSchemeDate(calendar)
                XToastUtils.success(result.value)
            }
            else XToastUtils.error(result.value)
        }
    }

    // 检查更新
    @NewThread
    private fun checkUpdate() {
        val srcVersion = pages.context.packageManager.getPackageInfo(pages.context.packageName, 0).longVersionCode
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) { API.SysAPI.getServerVersionCode() }
            pages.loadingDialog.dismiss()
            val content = "APP版本:${srcVersion}\n服务器版本:${result.value1}\n最低兼容版本:${result.value2}"
            if (result.ok) {
                if (srcVersion == result.value1) XToastUtils.success(content)
                else XToastUtils.warning(content)
            }
            else XToastUtils.error(content)
        }
    }

    // 签到
    @NewThread
    private fun signIn() {
        lifecycleScope.launch {
            pages.loadingDialog.show()
            val result = withContext(Dispatchers.IO) { API.UserAPI.signIn(Config.user_id, Config.user_pwd) }
            pages.loadingDialog.dismiss()
            if (result.ok) {
                pages.requestUserInfo()
                XToastUtils.success(result.value)
            }
            else XToastUtils.error(result.value)
        }
    }
}