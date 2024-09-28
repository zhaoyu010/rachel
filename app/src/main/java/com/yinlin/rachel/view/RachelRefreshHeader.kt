package com.yinlin.rachel.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshKernel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import org.libpag.PAGFile
import org.libpag.PAGScaleMode
import org.libpag.PAGView

@SuppressLint("RestrictedApi")
class RachelRefreshHeader(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
    : FrameLayout(context, attrs, defStyleAttr), RefreshHeader {
    private val pic = PAGView(context)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context): this(context, null)

    init {
        val file = PAGFile.Load(context.resources.assets, "pag/refresh_header_rachel.pag")
        pic.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        pic.setRepeatCount(-1)
        pic.setScaleMode(PAGScaleMode.Stretch)
        pic.setCacheEnabled(true)
        pic.composition = file
        addView(pic)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val composition = pic.composition
        val picParams = pic.layoutParams
        picParams.height = MeasureSpec.getSize(widthMeasureSpec) * composition.height() / composition.width()
        pic.layoutParams = picParams
    }

    override fun onStateChanged(refreshLayout: RefreshLayout, oldState: RefreshState, newState: RefreshState) {
        when (newState) {
            RefreshState.PullDownToRefresh -> pic.progress = 0.0
            RefreshState.Refreshing -> pic.play()
            else -> {}
        }
    }

    override fun onFinish(refreshLayout: RefreshLayout, success: Boolean): Int = 500
    override fun getView(): View = this
    override fun getSpinnerStyle(): SpinnerStyle = SpinnerStyle.Translate
    override fun setPrimaryColors(vararg colors: Int) { }
    override fun onInitialized(kernel: RefreshKernel, height: Int, maxDragHeight: Int) { }
    override fun onMoving(isExpand: Boolean, p1: Float, p2: Int, p3: Int, p4: Int) {
        if (!isExpand && p1 == 0f && p2 == 0) pic.pause()
    }
    override fun onHorizontalDrag(p0: Float, p1: Int, p2: Int) { }
    override fun onReleased(refreshLayout: RefreshLayout, p1: Int, p2: Int) { }
    override fun onStartAnimator(refreshLayout: RefreshLayout, p1: Int, p2: Int) { }
    override fun isSupportHorizontalDrag(): Boolean = false
    override fun autoOpen(p0: Int, p1: Float, p2: Boolean): Boolean = false
}