package com.yinlin.rachel

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.textfield.TextInputEditText
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.model.RachelOnClickListener
import java.io.File

var View.backgroundColor: Int
    get() = background?.let { (it as? ColorDrawable)?.color ?: Color.TRANSPARENT } ?: Color.TRANSPARENT
    set(value) { setBackgroundColor(value) }

var TextInputEditText.content: String
    get() = text?.toString() ?: ""
    set(value) { setText(value) }

var AppCompatTextView.textColor: Int @ColorInt
    get() = currentTextColor
    set(value) { setTextColor(value) }

var TextView.textColor: Int @ColorInt
get() = currentTextColor
    set(value) { setTextColor(value) }

fun View.rachelClick(listener: View.OnClickListener) = setOnClickListener(RachelOnClickListener(listener))
fun View.rachelClick(delay: Long, listener: View.OnClickListener) = setOnClickListener(RachelOnClickListener(delay, listener))

@SuppressLint("ClickableViewAccessibility")
fun View.interceptScroll() {
    this.setOnTouchListener { view, _ ->
        view.parent.requestDisallowInterceptTouchEvent(true)
        false
    }
}

fun ImageView.load(loader: RachelImageLoader, @RawRes @DrawableRes resourceId: Int) {
    loader.glide.load(resourceId).apply(loader.options).into(this)
}

fun ImageView.load(loader: RachelImageLoader, path: String) {
    loader.glide.load(path).apply(loader.options).into(this)
}

fun ImageView.load(loader: RachelImageLoader, path: String, sign: Int) {
    loader.glide.load(path).signature(ObjectKey(sign)).apply(loader.options).into(this)
}

fun ImageView.load(loader: RachelImageLoader, file: File) {
    loader.glide.load(file).apply(loader.options).into(this)
}

fun ImageView.load(loader: RachelImageLoader, file: File, sign: Int) {
    loader.glide.load(file).signature(ObjectKey(sign)).apply(loader.options).into(this)
}

fun ImageView.clear(loader: RachelImageLoader) = loader.glide.clear(this)