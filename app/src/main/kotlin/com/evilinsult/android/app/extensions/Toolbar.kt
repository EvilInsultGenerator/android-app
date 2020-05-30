package com.evilinsult.android.app.extensions

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.evilinsult.android.app.R

fun Toolbar.tintMenu(
    @ColorInt color: Int =
        context.resolveColor(
            R.attr.colorOnPrimary,
            ContextCompat.getColor(context, R.color.onPrimary)
        )
) {
    overflowIcon?.tint(color)
    @SuppressLint("PrivateResource")
    val overflowDescription = context.getString(R.string.abc_action_menu_overflow_description)
    val outViews = ArrayList<View>()
    findViewsWithText(outViews, overflowDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION)
    if (outViews.isEmpty()) return
    val overflow = outViews[0] as? AppCompatImageView
    overflow?.setImageDrawable(overflow.drawable.tint(color))
}