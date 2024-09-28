package com.yinlin.rachel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.textfield.TextInputEditText
import com.haibin.calendarview.Calendar
import com.yinlin.rachel.model.RachelImageLoader
import com.yinlin.rachel.model.RachelOnClickListener
import java.io.File


var View.backgroundColor: Int
    get() = background?.let { (it as? ColorDrawable)?.color ?: Color.TRANSPARENT } ?: Color.TRANSPARENT
    set(value) { setBackgroundColor(value) }

fun View.rachelClick(listener: View.OnClickListener) = setOnClickListener(RachelOnClickListener(listener))
fun View.rachelClick(delay: Long, listener: View.OnClickListener) = setOnClickListener(RachelOnClickListener(delay, listener))

@SuppressLint("ClickableViewAccessibility")
fun View.interceptScroll() {
    this.setOnTouchListener { view, _ ->
        view.parent.requestDisallowInterceptTouchEvent(true)
        false
    }
}

var TextInputEditText.content: String
    get() = text?.toString() ?: ""
    set(value) { setText(value) }

var AppCompatTextView.textColor: Int @ColorInt
    get() = currentTextColor
    set(value) { setTextColor(value) }
var AppCompatTextView.bold: Boolean
    get() = typeface == (context.applicationContext as RachelApplication).fontBold
    set(value) {
        val app = context.applicationContext as RachelApplication
        typeface = if (value) app.fontBold else app.fontNormal
    }

var TextView.textColor: Int @ColorInt
    get() = currentTextColor
    set(value) { setTextColor(value) }
var TextView.bold: Boolean
    get() = typeface == (context.applicationContext as RachelApplication).fontBold
    set(value) {
        val app = context.applicationContext as RachelApplication
        typeface = if (value) app.fontBold else app.fontNormal
    }

fun Paint.bold(context: Context, isBold: Boolean) {
    val app = context.applicationContext as RachelApplication
    typeface = if (isBold) app.fontBold else app.fontNormal
}

val Paint.textHeight: Float
    get() = this.fontMetrics.descent - this.fontMetrics.ascent

fun ImageView.load(loader: RachelImageLoader, @RawRes @DrawableRes resourceId: Int) {
    loader.glide.load(resourceId).apply(loader.options).into(this)
}

fun ImageView.load(loader: RachelImageLoader, path: String) {
    loader.glide.load(path).apply(loader.options).into(this)
}

fun ImageView.load(loader: RachelImageLoader, path: String, sign: Any) {
    loader.glide.load(path).signature(ObjectKey(sign)).apply(loader.options).into(this)
}

fun ImageView.load(loader: RachelImageLoader, file: File) {
    loader.glide.load(file).apply(loader.options).into(this)
}

fun ImageView.load(loader: RachelImageLoader, file: File, sign: Any) {
    loader.glide.load(file).signature(ObjectKey(sign)).apply(loader.options).into(this)
}

fun ImageView.clear(loader: RachelImageLoader) = loader.glide.clear(this)

var ImageView.pureColor: Int
    get() = (drawable as? ColorDrawable)?.color ?: Color.TRANSPARENT
    set(value) { setImageDrawable(if (value == Color.TRANSPARENT) null else ColorDrawable(value)) }

val Calendar.date: String get() = "${this.year}-${this.month}-${this.day}"