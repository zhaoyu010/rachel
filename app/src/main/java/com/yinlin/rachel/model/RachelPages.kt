package com.yinlin.rachel.model

import android.content.Context
import android.os.Handler
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.chaychan.library.BottomBarLayout
import com.chaychan.library.TabData
import com.xuexiang.xui.utils.WidgetUtils
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.widget.dialog.LoadingDialog
import com.yinlin.rachel.Config
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelMessage.ME_UPDATE_USER_INFO
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.api.API
import com.yinlin.rachel.fragment.FragmentDiscovery
import com.yinlin.rachel.fragment.FragmentMe
import com.yinlin.rachel.fragment.FragmentMsg
import com.yinlin.rachel.fragment.FragmentMusic
import com.yinlin.rachel.fragment.FragmentRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random


class RachelPages(private val activity: FragmentActivity, private val bbl: BottomBarLayout,
    private val items: Array<Item>, home: Item, @IdRes val mainFrame: Int) {
    @JvmRecord
    data class Item(val index: Int, val prototype: Class<*>,
                    val title: String, @DrawableRes val iconNormal: Int,
                    @DrawableRes val iconActive: Int)
    companion object {
        private val isLuckyDog: Boolean = Random.nextInt(100) <= 1

        val msg = Item(0, FragmentMsg::class.java, "资讯", R.drawable.msg_normal, R.drawable.msg_active)
        val res = Item(1, FragmentRes::class.java, "美图", R.drawable.res_normal, R.drawable.res_active)
        val music = Item(2, FragmentMusic::class.java, "听歌", R.drawable.music_normal, R.drawable.music_active)
        val discovery = Item(3, FragmentDiscovery::class.java, "发现", R.drawable.discovery_normal, R.drawable.discovery_active)
        val me = Item(4, FragmentMe::class.java, "小银子", if (isLuckyDog) R.drawable.dog_normal else R.drawable.me_normal, if (isLuckyDog) R.drawable.dog_active else R.drawable.me_active)
    }

    val context: Context = activity
    private val manager: FragmentManager = activity.supportFragmentManager
    private val fragments = arrayOfNulls<RachelFragment<*>>(items.size)
    private var isMain: Boolean
    private var currentFragment: RachelFragment<*>
    private var currentMainFragment: RachelFragment<*>
    val handler: Handler = Handler(activity.mainLooper)
    val ril: RachelImageLoader = RachelImageLoader(activity)
    val loadingDialog: LoadingDialog = WidgetUtils.getLoadingDialog(activity, "加载中...")

    init {
        // 初始化加载框
        loadingDialog.setIconScale(0.4f)
        loadingDialog.setLoadingSpeed(8)
        loadingDialog.setCancelable(false)

        // 首页
        val homeFragment = items[home.index].prototype.getConstructor(RachelPages::class.java)
            .newInstance(this) as RachelFragment<*>
        fragments[home.index] = homeFragment
        currentFragment = homeFragment
        currentMainFragment = homeFragment
        isMain = true
        manager.beginTransaction().add(mainFrame, homeFragment).commit()

        // 回退操作
        manager.addOnBackStackChangedListener {
            if (manager.backStackEntryCount == 0) {
                isMain = true
                bbl.visibility = View.VISIBLE
                currentFragment = currentMainFragment
            }
        }

        // 初始化底部导航栏
        val tabSource: MutableList<TabData> = ArrayList()
        for (item in items) tabSource.add(TabData(item.title, item.iconNormal, item.iconActive))
        bbl.setData(tabSource)
        bbl.currentItem = home.index
        bbl.setOnItemSelectedListener { _, _, pos ->
            if (isMain) {
                val transaction = manager.beginTransaction().hide(currentFragment)
                var nextFragment = fragments[pos]
                if (nextFragment == null) {
                    nextFragment = items[pos].prototype.getConstructor(RachelPages::class.java)
                        .newInstance(this@RachelPages) as RachelFragment<*>
                    fragments[pos] = nextFragment
                    transaction.add(mainFrame, nextFragment)
                }
                else transaction.show(nextFragment)
                currentFragment = nextFragment
                currentMainFragment = nextFragment
                transaction.commit()
            }
        }

        if (Config.isLogin) requestUserInfo()
    }

    val isNotMain get() = !isMain

    fun checkBottomLayoutStatus() {
        if (isNotMain && bbl.visibility == View.VISIBLE) bbl.visibility = View.GONE
    }

    fun isForeground(item: Item) = isMain && currentFragment == fragments[item.index]

    fun isForeground(fragment: RachelFragment<*>) = isMain && currentFragment == fragment

    fun navigate(des: RachelFragment<*>) {
        if (isMain) {
            isMain = false
        }
        val transaction = manager.beginTransaction().hide(currentFragment).add(mainFrame, des).addToBackStack(null)
        currentFragment = des
        transaction.commit()
    }

    fun pop() {
        if (isNotMain && manager.backStackEntryCount > 0) {
            manager.popBackStackImmediate()
        }
    }

    fun goBack(): Boolean = currentFragment.back()

    fun getResString(@StringRes id: Int) = context.getString(id)

    fun getResColor(@ColorRes id: Int) = context.getColor(id)

    fun sendMessage(item: Item, msg: RachelMessage, vararg args: Any?) = fragments[item.index]?.message(msg, *args)

    @Suppress("UNCHECKED_CAST")
    fun <T> sendMessageForResult(item: Item, msg: RachelMessage, vararg args: Any?): T? = fragments[item.index]?.messageForResult(msg, *args) as T?

    // 请求用户信息
    @NewThread
    fun requestUserInfo() {
        activity.lifecycleScope.launch {
            Config.user = withContext(Dispatchers.IO) { API.UserAPI.getInfo(Config.user_id, Config.user_pwd) }
            if (!Config.user.ok) XToastUtils.error("网络异常")
            sendMessage(me, ME_UPDATE_USER_INFO)
        }
    }
}