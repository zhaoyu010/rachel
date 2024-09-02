package com.yinlin.rachel.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.xuexiang.xui.widget.flowlayout.BaseTagAdapter
import com.xuexiang.xui.widget.flowlayout.FlowTagLayout
import com.yinlin.rachel.R


class TagView(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
    : FlowTagLayout(context, attrs, defStyleAttr) {
    class Adapter(context: Context) : BaseTagAdapter<String, TextView>(context) {
        override fun newViewHolder(convertView: View): TextView {
            return convertView.findViewById(R.id.tag)
        }
        override fun getLayoutId() = R.layout.item_tag
        override fun convert(view: TextView, item: String, position: Int) {
            view.text = item
        }
    }

    fun interface OnTagClickListener {
        fun onTagClick(text: String)
    }

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context): this(context, null)

    private val adapter = Adapter(context)

    init {
        setAdapter(adapter)
    }

    fun setOnTagClickListener(listener: OnTagClickListener) {
        setOnTagClickListener { _, _, position -> listener.onTagClick(adapter.getItem(position)) }
    }
}