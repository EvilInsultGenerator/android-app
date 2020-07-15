package com.evilinsult.extensions

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

fun Drawable.tint(@ColorInt color: Int): Drawable {
    val drawable = DrawableCompat.wrap(mutate())
    DrawableCompat.setTint(drawable, color)
    return drawable
}
