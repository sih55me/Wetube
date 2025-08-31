package app.wetube.service

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget

class ThumbBit : SimpleTarget<GlideBitmapDrawable>(0,0) {

    val onGet:((GlideBitmapDrawable?) -> Unit)={}

    override fun onResourceReady(
        resource: GlideBitmapDrawable?,
        glideAnimation: GlideAnimation<in GlideBitmapDrawable>?,
    ) {
        onGet(resource)
    }

}