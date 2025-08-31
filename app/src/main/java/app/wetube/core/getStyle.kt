package app.wetube.core

import android.app.Activity
import android.util.TypedValue


fun Float.dip(activity: Activity): Float{
    val metrics = activity.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, metrics)
}

