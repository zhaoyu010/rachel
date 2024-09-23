package com.yinlin.rachel.model

import android.content.Context
import android.text.InputType
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog.InputCallback
import com.yinlin.rachel.R


object RachelDialog {
    fun confirm(context: Context, content: String, callback: MaterialDialog.SingleButtonCallback) {
        MaterialDialog.Builder(context).iconRes(R.mipmap.icon)
            .title("二次确认").content(content)
            .positiveText(R.string.yes).negativeText(R.string.no)
            .onPositive(callback).show()
    }

    fun input(context: Context, content: String, maxLength: Int, callback: MaterialDialog.InputCallback) {
        MaterialDialog.Builder(context).iconRes(R.mipmap.icon)
            .title("输入").positiveText(R.string.ok).negativeText(R.string.cancel)
            .inputType(InputType.TYPE_CLASS_TEXT).inputRange(1, maxLength)
            .input(content, "", false, callback)
            .cancelable(false).show()
    }

    fun inputMultiLines(context: Context, content: String, maxLength: Int, maxLine: Int, callback: InputCallback) {
        val dialog = MaterialDialog.Builder(context).iconRes(R.mipmap.icon)
            .title("输入").positiveText(R.string.ok).negativeText(R.string.cancel)
            .inputType(InputType.TYPE_CLASS_TEXT).inputRange(1, maxLength)
            .input(content, "", false, callback)
            .cancelable(false).build()
        dialog.inputEditText?.apply {
            isSingleLine = false
            maxLines = maxLine
            setLines(maxLine)
        }
        dialog.show()
    }

    fun inputNumber(context: Context, content: String, callback: MaterialDialog.InputCallback) {
        MaterialDialog.Builder(context).iconRes(R.mipmap.icon)
            .title("输入").positiveText(R.string.ok).negativeText(R.string.cancel)
            .inputType(InputType.TYPE_CLASS_NUMBER).inputRange(1, 1)
            .input(content, "", false, callback)
            .cancelable(false).show()
    }

    fun choice(context: Context, content: String, items: Collection<*>, callback: MaterialDialog.ListCallbackSingleChoice) {
        MaterialDialog.Builder(context).iconRes(R.mipmap.icon)
            .title(content).positiveText(R.string.ok).negativeText(R.string.cancel)
            .items(items).itemsCallbackSingleChoice(0, callback)
            .show()
    }
}