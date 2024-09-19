package com.yinlin.rachel.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewbinding.ViewBinding
import com.yinlin.rachel.rachelClick

@Suppress("UNCHECKED_CAST")
abstract class RachelHeaderAdapter<HeaderBinding : ViewBinding, ItemBinding : ViewBinding, Item>
    (var items: MutableList<Item>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val ITEM = 0
        private const val HEADER = 1
    }

    private var vHeader: HeaderBinding? = null
    val header: HeaderBinding get() = vHeader!!

    class RachelHeaderViewHolder<HeaderBinding : ViewBinding>(val v: HeaderBinding) : RecyclerView.ViewHolder(v.root)
    class RachelItemViewHolder<ItemBinding : ViewBinding>(val v: ItemBinding) : RecyclerView.ViewHolder(v.root)

    protected abstract fun bindingHeaderClass(): Class<HeaderBinding>
    protected abstract fun bindingItemClass(): Class<ItemBinding>
    protected open fun init(holder: RachelItemViewHolder<ItemBinding>, v: ItemBinding) { }
    protected open fun update(v: ItemBinding, item: Item, position: Int) { }
    protected open fun onItemClicked(v: ItemBinding, item: Item, position: Int) { }
    protected open fun onItemLongClicked(v: ItemBinding, item: Item, position: Int) { }
    protected open fun initHeader(v: HeaderBinding) { }

    constructor(): this(ArrayList())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == HEADER) {
            if (vHeader == null) {
                val cls: Class<HeaderBinding> = bindingHeaderClass()
                val method = cls.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.javaPrimitiveType)
                vHeader = method.invoke(null, LayoutInflater.from(parent.context), parent, false) as HeaderBinding
                initHeader(header)
            }
            RachelHeaderViewHolder(header)
        }
        else {
            val cls: Class<ItemBinding> = bindingItemClass()
            val method = cls.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.javaPrimitiveType)
            val v = method.invoke(null, LayoutInflater.from(parent.context), parent, false) as ItemBinding
            val holder = RachelItemViewHolder(v)
            val root = v.root
            root.rachelClick(300) {
                val position = holder.getBindingAdapterPosition() - 1
                onItemClicked(holder.v, items[position], position)
            }
            root.setOnLongClickListener {
                val position = holder.getBindingAdapterPosition() - 1
                onItemLongClicked(holder.v, items[position], position)
                true
            }
            init(holder, v)
            holder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RachelItemViewHolder<*>) update(holder.v as ItemBinding, items[position - 1], position - 1)
    }

    override fun getItemViewType(position: Int) = if (position == 0) HEADER else ITEM

    override fun getItemCount() = items.size + 1

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (getItemViewType(position) == HEADER) manager.spanCount else 1
                }
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        val lp = holder.itemView.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) lp.isFullSpan = holder.itemViewType == HEADER
    }

    fun getHolderPosition(holder: RachelItemViewHolder<ItemBinding>) = holder.bindingAdapterPosition - 1

    fun notifyChangedEx() {
        if (items.size > 0) notifyItemRangeChanged(1, items.size)
    }

    fun notifyChangedEx(position: Int) {
        if (items.size > 0) notifyItemChanged(position + 1)
    }

    fun notifyChangedEx(position: Int, count: Int) {
        if (items.size > 0) notifyItemRangeChanged(position + 1, count)
    }

    fun notifyInsertChangedEx(position: Int) {
        if (items.size > 0) notifyItemInserted(position + 1)
    }

    fun notifyInsertChangedEx(position: Int, count: Int) {
        if (items.size > 0) notifyItemRangeInserted(position + 1, count)
    }

    fun notifyRemoveChangedEx(position: Int) {
        notifyItemRemoved(position + 1)
    }

    fun notifyRemoveChangedEx(position: Int, count: Int) {
        notifyItemRangeRemoved(position + 1, count)
    }

    fun notifyMovedEx(fromPosition: Int, toPosition: Int) {
        notifyItemMoved(fromPosition, toPosition)
    }
}