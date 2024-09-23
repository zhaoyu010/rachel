package com.yinlin.rachel.model

import android.content.Context
import android.graphics.Paint
import android.view.View
import android.widget.TextView
import com.xuexiang.xui.adapter.listview.BaseListAdapter
import com.xuexiang.xui.widget.popupwindow.popup.XUIListPopup
import com.yinlin.rachel.R
import com.yinlin.rachel.bold
import com.yinlin.rachel.toDP

object RachelPopMenu {
    @JvmRecord
    data class Item(val title: String, val callback: () -> Unit)

    class ViewHolder(val view: TextView)

    class MenuAdapter(context: Context, items: List<Item>) : BaseListAdapter<Item, ViewHolder>(context, items) {
        override fun newViewHolder(view: View) = ViewHolder(view as TextView)
        override fun getLayoutId() = R.layout.item_pop_menu
        override fun convert(holder: ViewHolder, item: Item, position: Int) { holder.view.text = item.title }
    }

    fun showDown(view: View, items: List<Item>) {
        val context = view.context
        val adapter = MenuAdapter(context, items)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.bold(context, false)
        paint.textSize = context.resources.getDimension(R.dimen.sm)
        var maxWidth = 80f.toDP(context)
        for (item in items) {
            val width = paint.measureText(item.title)
            if (width > maxWidth) maxWidth = width
        }
        val menu = XUIListPopup<XUIListPopup<*>>(context, adapter)
        menu.create(maxWidth.toInt(), 0) { _, _, position, _ ->
            menu.dismiss()
            adapter.items[position].callback()
        }.setHasDivider(true).showDown(view)
    }
}