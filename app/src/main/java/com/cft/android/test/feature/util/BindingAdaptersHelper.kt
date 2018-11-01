package com.cft.android.test.feature.util

import android.databinding.BindingAdapter
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.squareup.picasso.Picasso


/**
 * Created by administrator <dshumkov@icerockdev.com> on 28.10.18.
 */
object BindingAdaptersHelper {

    @JvmStatic
    @BindingAdapter("imageUri", "placeholder", requireAll = false)
    fun setImageUri(view: ImageView, uri: Uri?, placeholder: Drawable?) {
        view.post {
            uri?.let {
                Picasso.get()
                        .load(it)
                        .resize(view.measuredWidth, view.measuredHeight)
                        .centerCrop()
                        .into(view)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("invisibleOrGone")
    fun setInvisibleOrGone(view: View, invisible: Boolean) {
        view.visibility = if (invisible) View.INVISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("visibleOrGone")
    fun setVisibleOrGone(view: View, value: Boolean) {
        view.visibility = if (value) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("visibleOrInvisible")
    fun setVisibleOrInvisible(view: View, value: Boolean) {
        view.visibility = if (value) View.VISIBLE else View.INVISIBLE
    }
}
