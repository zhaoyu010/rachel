package com.yinlin.rachel.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.yinlin.rachel.RachelMessage

abstract class RachelFragment<Binding : ViewBinding>(val pages: RachelPages) : Fragment() {
    private var _binding: Binding? = null
    val v: Binding get() = _binding!!

    protected abstract fun bindingClass(): Class<Binding>
    protected open fun init() { }
    protected open fun update() { }
    protected open fun hidden() { }
    protected open fun quit() { }
    open fun back(): Boolean = false
    open fun message(msg: RachelMessage, vararg args: Any?) { }
    open fun messageForResult(msg: RachelMessage, vararg args: Any?): Any? = null

    final override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, bundle: Bundle?): View {
        val cls: Class<Binding> = bindingClass()
        val method = cls.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.javaPrimitiveType)
        @Suppress("UNCHECKED_CAST")
        _binding = method.invoke(null, inflater, parent, false) as Binding
        return v.root
    }

    final override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, null)
        init()
        update()
    }

    final override fun onHiddenChanged(isHidden: Boolean) {
        super.onHiddenChanged(isHidden)
        if (isHidden) hidden()
        else update()
    }

    final override fun onDestroyView() {
        super.onDestroyView()
        quit()
    }

    final override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun post(r: Runnable) = pages.handler.post(r)
    fun postDelay(delay: Long, r: Runnable) = pages.handler.postDelayed(r, delay)
    fun removePost(r: Runnable) = pages.handler.removeCallbacks(r)
}