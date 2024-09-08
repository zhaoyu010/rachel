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

    private var _binding: Binding? = null
    val v: Binding get() = _binding!!

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val cls: Class<Binding> = bindingClass()
            val method = cls.getMethod("inflate", LayoutInflater::class.java)
            @Suppress("UNCHECKED_CAST")
            _binding = method.invoke(null, layoutInflater) as Binding
        }
        catch (ignored: Exception) { }
        val view = v.root
        setContentView(view)
        setOnDismissListener {
            quit()
            _binding = null
        }
        if (maxHeightPercent > 0f) {
            val screenHeight = root.pages.context.resources.displayMetrics.heightPixels
            val maxHeight = (screenHeight * maxHeightPercent).toInt()
            val behavior = BottomSheetBehavior.from(view.parent as View)
            behavior.maxHeight = maxHeight
            behavior.peekHeight = maxHeight
        }
        init()
    }
}