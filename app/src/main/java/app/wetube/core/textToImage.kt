package app.wetube.core


import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable


fun textToDrawable(res:Resources,context: Context, text:CharSequence):Drawable?{
    val id: Int = res.getIdentifier(text.toString(), "drawable", context.applicationContext.packageName)
    return res.getDrawable(id)
}