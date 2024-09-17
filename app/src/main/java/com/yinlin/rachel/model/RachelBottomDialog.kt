package com.yinlin.rachel.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yinlin.rachel.R

abstract class RachelBottomDialog<Binding : ViewBinding, F : RachelFragment<*>>
    (val root: F, maxHeightPercent: Float) {
    val context = root.pages.context
    private var _binding: Binding? = null
    val v: Binding get() = _binding!!
    private var dialog: BottomSheetDialog? = null
    private val maxDialogHeight = (context.resources.displayMetrics.heightPixels * maxHeightPercent).toInt()

    protected abstract fun bindingClass(): Class<Binding>

    @CallSuper
    open fun init() {
        val cls: Class<Binding> = bindingClass()
        val method = cls.getMethod("inflate", LayoutInflater::class.java)
        @Suppress("UNCHECKED_CAST")
        _binding = method.invoke(null, root.layoutInflater) as Binding
    }

    fun show() {
        dialog = BottomSheetDialog(context, R.style.RachelBottomDialog)
        dialog?.setContentView(v.root)
        dialog?.setOnDismissListener {
            (v.root.parent as ViewGroup).removeView(v.root)
            dialog = null
        }
        dialog?.behavior?.apply {
            maxHeight = maxDialogHeight
            peekHeight = maxDialogHeight
        }
        dialog?.show()
    }

    fun hide() {
        dialog?.dismiss()
    }

    fun release() {
        dialog?.dismiss()
        _binding = null
    }
}