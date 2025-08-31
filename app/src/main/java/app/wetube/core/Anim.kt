package app.wetube.core

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout

public fun expand(v: View, parent : Boolean = true) {
    val target = if(parent) (v.parent as View) else v
    val matchParentMeasureSpec =
        View.MeasureSpec.makeMeasureSpec(target.width, View.MeasureSpec.EXACTLY)
    val wrapContentMeasureSpec =
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    v.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
    val targetHeight = v.measuredHeight


    // Older versions of android (pre API 21) cancel animations for views with a height of 0.
    v.layoutParams.height = 1
    v.visibility = View.VISIBLE
    val a: Animation = object : Animation(
    ) {
        public override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            v.layoutParams.height = if (interpolatedTime == 1f
            ) LinearLayout.LayoutParams.WRAP_CONTENT
            else (targetHeight * interpolatedTime).toInt()
            v.requestLayout()
        }

        public override fun willChangeBounds(): Boolean {
            return true
        }
    }


    // Expansion speed of 1dp/ms
    a.setDuration((targetHeight / v.context.resources.displayMetrics.density).toLong())
    v.startAnimation(a)
}

public fun expandH(v: View, parent : Boolean = true) {
    val target = if(parent) (v.parent as View) else v
    val matchParentMeasureSpec =
        View.MeasureSpec.makeMeasureSpec(1, View.MeasureSpec.EXACTLY)

    v.measure(matchParentMeasureSpec, matchParentMeasureSpec)
    val targetHeight = v.measuredHeight


    // Older versions of android (pre API 21) cancel animations for views with a height of 0.
    v.layoutParams.height = 1
    v.visibility = View.VISIBLE
    val a: Animation = object : Animation(
    ) {
        public override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            v.layoutParams.height = if (interpolatedTime == 1f
            ) LinearLayout.LayoutParams.MATCH_PARENT
            else (targetHeight * interpolatedTime).toInt()
            v.requestLayout()
        }

        public override fun willChangeBounds(): Boolean {
            return true
        }
    }


    // Expansion speed of 1dp/ms
    a.setDuration((targetHeight / v.context.resources.displayMetrics.density).toLong())
    v.startAnimation(a)
}

public fun collapse(v: View) {
    val initialHeight = v.measuredHeight

    val a: Animation = object : Animation(
    ) {
        public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            if (interpolatedTime == 1f) {
                v.visibility = View.GONE
            } else {
                v.layoutParams.height =
                    initialHeight - (initialHeight * interpolatedTime).toInt()
                v.requestLayout()
            }
        }

        public override fun willChangeBounds(): Boolean {
            return true
        }
    }


    // Collapse speed of 1dp/ms
    a.duration =
        (initialHeight / v.context.resources.displayMetrics.density).toLong()
    v.startAnimation(a)
}
//Show
public fun fadeIn(v: View) {
    if(v.visibility == View.VISIBLE) return
    v.alpha = 0f
    v.visibility = View.VISIBLE
    v.animate()
        .alpha(1f)
}
//Hide
 fun fadeOut(v:View){
    if(v.visibility == View.GONE) return
    v.animate()
        .alpha(0f)
        .withEndAction { v.visibility = View.GONE }
}