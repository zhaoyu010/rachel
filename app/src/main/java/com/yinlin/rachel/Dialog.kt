package com.yinlin.rachel

import android.content.Context
import android.text.InputType
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog


object Dialog {
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

    fun choice(context: Context, content: String, items: Collection<*>, callback: MaterialDialog.ListCallbackSingleChoice) {
        MaterialDialog.Builder(context).iconRes(R.mipmap.icon)
            .title(content).positiveText(R.string.ok).negativeText(R.string.cancel)
            .items(items).itemsCallbackSingleChoice(0, callback)
            .show()
    }
}