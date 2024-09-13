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
import androidx.fragment.app.FragmentTransaction
import com.chaychan.library.BottomBarLayout
import com.chaychan.library.TabData
import com.xuexiang.xui.utils.WidgetUtils
import com.xuexiang.xui.widget.dialog.LoadingDialog
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.R
import com.yinlin.rachel.fragment.FragmentDiscovery
import com.yinlin.rachel.fragment.FragmentMe
import com.yinlin.rachel.fragment.FragmentMsg
import com.yinlin.rachel.fragment.FragmentMusic
import com.yinlin.rachel.fragment.FragmentRes
import kotlin.random.Random


class RachelPages(activity: FragmentActivity, private val bbl: BottomBarLayout,
    private val items: Array<Item>, home: Item,
    @IdRes val mainFrame: Int, @IdRes val secondFrame: Int) {
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
    private var current: Int = -1
    private var isSecond: Boolean = false
    val handler: Handler = Handler(activity.mainLooper)
    val ril: RachelImageLoader = RachelImageLoader(activity)
    val loadingDialog: LoadingDialog = WidgetUtils.getLoadingDialog(activity, "加载中...")

    init {
        loadingDialog.setIconScale(0.4f)
        loadingDialog.setLoadingSpeed(8)
        loadingDialog.setCancelable(false)

        val tabSource: MutableList<TabData> = ArrayList()
        for (item in items) tabSource.add(TabData(item.title, item.iconNormal, item.iconActive))
        bbl.setData(tabSource)
        bbl.currentItem = home.index
        bbl.setOnItemSelectedListener { _, _, currentPos -> gotoMain(currentPos) }

        manager.addOnBackStackChangedListener {
            if (manager.backStackEntryCount == 0) {
                isSecond = false
                bbl.visibility = View.VISIBLE
                val transaction = manager.beginTransaction()
                createOrShowMain(current, transaction)
                transaction.commit()
            }
        }

        val transaction = manager.beginTransaction()
        createOrShowMain(home.index, transaction)
        transaction.commit()
    }

    private fun createOrShowMain(index: Int, transaction: FragmentTransaction) {
        current = index
        if (fragments[current] == null) { // 懒加载新页面
            try {
                val constructor = items[current].prototype.getConstructor(RachelPages::class.java)
                fragments[current] = constructor.newInstance(this) as RachelFragment<*>
                transaction.add(mainFrame, fragments[current]!!)
            }
            catch (ignored: Exception) { }
        }
        else transaction.show(fragments[current]!!)
    }

    val isMainFragment get() = !isSecond
    val isSecondFragment get() = isSecond

    fun isForeground(item: Item) = !isSecond && current == item.index

    private fun gotoMain(index: Int) {
        if (isSecond) { // 副页面状态 - 清空回退栈
            current = index
            manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        else if (current != index) { // 主页面状态
            // 隐藏当前页面
            val transaction = manager.beginTransaction()
            transaction.hide(fragments[current]!!)
            // 调起新页面
            createOrShowMain(index, transaction)
            transaction.commit()
        }
    }

    fun gotoSecond(des: RachelFragment<*>) {
        bbl.visibility = View.GONE
        if (!isSecond) { // 主页面状态
            // 隐藏主页面
            isSecond = true
            val transactionSrc = manager.beginTransaction()
            transactionSrc.hide(fragments[current]!!)
            transactionSrc.commit()
        }
        // 调起副页面
        val transactionDes = manager.beginTransaction()
        transactionDes.replace(secondFrame, des)
        transactionDes.addToBackStack(null)
        transactionDes.commit()
    }

    fun popSecond() {
        if (isSecond && manager.backStackEntryCount > 0) {
            manager.popBackStackImmediate()
        }
    }

    fun goBack(): Boolean {
        if (isSecond) {
            if (manager.backStackEntryCount > 0) {
                return (manager.fragments.last() as RachelFragment<*>).back()
            }
            return false
        }
        return fragments[current]?.back() ?: false
    }

    fun getResString(@StringRes id: Int) = context.getString(id)

    fun getResColor(@ColorRes id: Int) = context.getColor(id)

    fun sendMessage(item: Item, msg: RachelMessage, arg: Any?) = fragments[item.index]?.message(msg, arg)

    fun sendMessage(item: Item, msg: RachelMessage) = sendMessage(item, msg, null)

    fun sendMessageForResult(item: Item, msg: RachelMessage, arg: Any?) = fragments[item.index]?.messageForResult(msg, arg)

    fun sendMessageForResult(item: Item, msg: RachelMessage) = sendMessageForResult(item, msg, null)
}