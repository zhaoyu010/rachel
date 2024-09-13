package com.yinlin.rachel.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.yinlin.rachel.rachelClick


abstract class RachelAdapter<Binding : ViewBinding, Item>(val items: MutableList<Item>)
    : RecyclerView.Adapter<RachelAdapter.RachelViewHolder<Binding>>() {
        class RachelViewHolder<Binding : ViewBinding>(val v: Binding) : RecyclerView.ViewHolder(v.root)

    protected abstract fun bindingClass(): Class<Binding>
    protected open fun init(holder: RachelViewHolder<Binding>) { }
    protected open fun update(v: Binding, item: Item, position: Int) { }
    protected open fun onItemClicked(v: Binding, item: Item, position: Int) { }
    protected open fun onItemLongClicked(v: Binding, item: Item, position: Int) { }

    constructor(): this(ArrayList())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RachelViewHolder<Binding> {
        val cls: Class<Binding> = bindingClass()
        val method = cls.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.javaPrimitiveType)
        @Suppress("UNCHECKED_CAST")
        val v = method.invoke(null, LayoutInflater.from(parent.context), parent, false) as Binding
        val holder = RachelViewHolder(v)
        val root: View = v.root
        root.rachelClick(300) {
            val position = holder.getBindingAdapterPosition()
            onItemClicked(holder.v, items[position], position)
        }
        root.setOnLongClickListener {
            val position = holder.getBindingAdapterPosition()
            onItemLongClicked(holder.v, items[position], position)
            true
        }
        init(holder)
        return holder
    }

    override fun onBindViewHolder(holder: RachelViewHolder<Binding>, position: Int) = update(holder.v, items[position], position)

    override fun getItemCount() = items.size
}