package app.wetube.core

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.preference.PreferenceManager



fun getThemeColorInHex(
    context: Context,
    colorName: String?,
     attribute: Int
): String {
    val outValue = TypedValue()
    context.theme.resolveAttribute(attribute, outValue, true)
    return String.format("#%06X", (0xFFFFFF and outValue.data))
}