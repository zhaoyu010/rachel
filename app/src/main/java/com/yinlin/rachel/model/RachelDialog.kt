package com.yinlin.rachel.model

import android.content.Context
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import com.yinlin.rachel.R

abstract class RachelDialog<Binding : ViewBinding>(private val context: Context) {
    protected abstract fun bindingClass(): Class<Binding>
    protected open fun init() { }
    protected open fun ok() { }
    protected open fun cancel() { }

    private var _binding: Binding? = null
    val v: Binding get() = _binding!!
    private val builder = MaterialDialog.Builder(context)
        .positiveText(R.string.ok).negativeText(R.string.cancel).cancelable(false)
        .onPositive { _, _ -> ok() }.onNegative { _, _ -> cancel() }
        .dismissListener { _binding = null }

    fun show() {
        val cls: Class<Binding> = bindingClass()
        val method = cls.getMethod("inflate", LayoutInflater::class.java)
        @Suppress("UNCHECKED_CAST")
        _binding = method.invoke(null, LayoutInflater.from(context)) as Binding
        init()
        builder.customView(v.root, true).show()
    }

    fun interface NoArgCallback { fun onCall() }
    fun interface InputCallback { fun onCall(input: String) }
    fun interface ChoiceCallback { fun onCall(position: Int, text: String) }

    class CallbackWrapper(private val callback: Any) :
        MaterialDialog.SingleButtonCallback, MaterialDialog.InputCallback,
        MaterialDialog.ListCallbackSingleChoice {
        override fun onClick(p0: MaterialDialog, p1: DialogAction) = (callback as NoArgCallback).onCall()
        override fun onInput(p0: MaterialDialog, p1: CharSequence) = (callback as InputCallback).onCall(p1.toString())
        override fun onSelection(p0: MaterialDialog, p1: View, p2: Int, p3: CharSequence): Boolean {
            (callback as ChoiceCallback).onCall(p2, p3.toString())
            return true
        }
    }

    companion object {
        fun confirm(context: Context, content: String, callback: NoArgCallback) {
            MaterialDialog.Builder(context).iconRes(R.mipmap.icon)
                .title("二次确认").content(content)
                .positiveText(R.string.yes).negativeText(R.string.no)
                .onPositive(CallbackWrapper(callback)).show()
        }

        fun input(context: Context, content: String, maxLength: Int, callback: InputCallback) {
            MaterialDialog.Builder(context).iconRes(R.mipmap.icon)
                .title("输入").positiveText(R.string.ok).negativeText(R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT).inputRange(1, maxLength)
                .input(content, "", false, CallbackWrapper(callback))
                .cancelable(false).show()
        }

        fun inputMultiLines(context: Context, content: String, maxLength: Int, maxLine: Int, callback: InputCallback) {
            val dialog = MaterialDialog.Builder(context).iconRes(R.mipmap.icon)
                .title("输入").positiveText(R.string.ok).negativeText(R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT).inputRange(1, maxLength)
                .input(content, "", false, CallbackWrapper(callback))
                .cancelable(false).build()
            dialog.inputEditText?.apply {
                textSize = 12f
                gravity = Gravity.START
                isSingleLine = false
                maxLines = maxLine
                setLines(maxLine)
            }
            dialog.show()
        }

        fun inputNumber(context: Context, content: String, callback: InputCallback) {
            MaterialDialog.Builder(context).iconRes(R.mipmap.icon)
                .title("输入").positiveText(R.string.ok).negativeText(R.string.cancel)
                .inputType(InputType.TYPE_CLASS_NUMBER).inputRange(1, 1)
                .input(content, "", false, CallbackWrapper(callback))
                .cancelable(false).show()
        }

        fun choice(context: Context, content: String, items: Collection<*>, callback: ChoiceCallback) {
            MaterialDialog.Builder(context).iconRes(R.mipmap.icon)
                .title(content).positiveText(R.string.ok).negativeText(R.string.cancel)
                .items(items).itemsCallbackSingleChoice(0, CallbackWrapper(callback))
                .show()
        }
    }
}