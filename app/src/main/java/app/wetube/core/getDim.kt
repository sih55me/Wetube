package app.wetube.core

import android.content.Context


fun Context.getDimen(res:Int) = (resources.getDimension(res) / resources.displayMetrics.density)

fun Context.getDimenInt(res:Int) = getDimen(res).toInt()

fun android.app.Fragment.dime(res:Int): Int = activity.getDimen(res).toInt()