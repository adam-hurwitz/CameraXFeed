package app.cameraxfeed.utils

import android.widget.ImageView
import app.cameraxfeed.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

fun ImageView.setProfileImage(image: Int) {
    val radius = context.resources.getDimensionPixelSize(R.dimen.feed_corner_radius)
    Glide.with(context)
        .load(image)
        .transform(CircleCrop())
        .placeholder(R.drawable.ic_user_placeholder_24)
        .error(R.drawable.ic_user_placeholder_24)
        .into(this)
}

fun ImageView.setPostImage(image: String) {
    val radius = context.resources.getDimensionPixelSize(R.dimen.feed_corner_radius)
    Glide.with(context)
        .load(image)
        .transform(CenterCrop(), RoundedCorners(radius))
        .placeholder(R.drawable.ic_content_placeholder)
        .error(R.drawable.ic_error)
        .into(this)
}