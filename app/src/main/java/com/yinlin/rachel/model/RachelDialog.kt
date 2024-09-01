package com.yinlin.rachel.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yinlin.rachel.R


abstract class RachelDialog<Binding : ViewBinding, F : RachelFragment<*>>
    (val root: F, private val maxHeightPercent: Float):
    BottomSheetDialog(root.pages.context, R.style.RachelBottomDialog) {

    protected abstract fun bindingClass(): Class<Binding>
    protected open fun init() { }
    protected open fun quit() { }

    lateinit var v: Binding

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val cls: Class<Binding> = bindingClass()
            val method = cls.getMethod("inflate", LayoutInflater::class.java)
            @Suppress("UNCHECKED_CAST")
            v = method.invoke(null, layoutInflater) as Binding
        }
        catch (ignored: Exception) { }
        val view = v.root
        setContentView(view)
        setOnDismissListener { quit() }
        if (maxHeightPercent > 0f) {
            val screenHeight = root.requireContext().resources.displayMetrics.heightPixels
            val maxHeight = (screenHeight * maxHeightPercent).toInt()
            val behavior = BottomSheetBehavior.from(view.parent as View)
            behavior.maxHeight = maxHeight
            behavior.peekHeight = maxHeight
        }
        init()
    }
}