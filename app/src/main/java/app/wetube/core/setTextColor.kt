package app.wetube.core

import android.text.Html

fun setTextColor(text:String,color:String): CharSequence = Html.fromHtml("<font color='${color}'>${text}</font>")
