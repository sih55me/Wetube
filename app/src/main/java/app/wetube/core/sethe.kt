package app.wetube.core

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.preference.PreferenceManager
import android.util.TypedValue
import android.view.View
import android.view.Window
import app.wetube.R
import java.lang.reflect.Method


public fun Context.setupTheme() = setupTheme(false)
public
fun Context.setupTheme(asDialog: Boolean){
    val p = PreferenceManager.getDefaultSharedPreferences(applicationContext)
    val t = p.getString("darkmode", "a")
    val cus =  valueTheme != "idk"

    if(cus){
        val th = if (asDialog) getThemeResDialog() else getThemeRes()
        theme.applyStyle(th, true)
        setTheme(th)
    }

    // If we're following the system, we just use the system default from the
    // application context

    //do some property color
    if(this is Activity){
        if(p.getBoolean("enco", false)) {
            val d =                 ColorDrawable(
                p.getInt(
                    "tbc",
                    resources.getColor(app.wetube.R.color.statusbarcolor)
                )
            )
            actionBar?.setBackgroundDrawable(d)
            actionBar?.setStackedBackgroundDrawable(d)
        }
        window.setupTheme()
    }

}

fun Window.setupTheme(){
    val p = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    val t = p.getString("darkmode", "a")
    val th = context.getThemeRes()
    val resources = context.resources
    val cus = context.valueTheme  != "idk"

    if(cus){
        if (th == app.wetube.R.style.Wetube_Sy_L) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                isNavigationBarContrastEnforced = t == app.wetube.DarkMode.LIGHT
            } else {
                navigationBarColor = resources.getColor(app.wetube.R.color.black)
            }
        }
        if (p.getBoolean("enco", false)) {
            apply {
                statusBarColor =
                    p.getInt("sbc", resources.getColor(app.wetube.R.color.statusbarcolor))
                navigationBarColor =
                    p.getInt("nbc", resources.getColor(app.wetube.R.color.navbarColor))
                setBackgroundDrawable(ColorDrawable(
                    p.getInt(
                        "wbc", resources.getColor(
                            app.wetube.R.color.background
                        )
                    )
                ))
            }
        }
    }
}

fun Dialog.setupTheme(){
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val resources = context.resources
    val cus = p.getBoolean("cus", true)
    if(cus){
        if (p.getBoolean("enco", false)) {
            actionBar?.setBackgroundDrawable(
                ColorDrawable(
                    p.getInt(
                        "tbc",
                        resources.getColor(app.wetube.R.color.statusbarcolor)
                    )
                )
            )
        }
    }
    window?.setupTheme()

}

fun Context.getThemeResDialog() :Int {
    return when(valueTheme){
        "w" -> R.style.Wetube_Sy_Dialog_L
        "wd" -> R.style.Wetube_Sy_Dialog_D
        "wa" -> R.style.Wetube_Sy_Dialog
        "d" -> android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
        "dd" -> android.R.style.Theme_DeviceDefault_Dialog_Alert
        else -> themeAlertDialog
    }
}


val Context.valueTheme get() = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("theme", "w")?:"w"


fun Context.getThemeRes() :Int {
     return when(valueTheme){
        "w" -> R.style.Wetube_Sy_L
        "wd" -> R.style.Wetube_Sy_D
        "wa" -> R.style.Wetube_Sy
         "wo" -> R.style.Wetube_Sy_L_O
         "wdo" -> R.style.Wetube_Sy_D_O
         "wao" -> R.style.Wetube_Sy_O
        "d" -> android.R.style.Theme_DeviceDefault_Light
        "dd" -> android.R.style.Theme_DeviceDefault
        "da" -> R.style.Sy
         else -> 0
     }

}

/**
 * Get a theme id that use in [Context]
 */
fun Context.getThemeId(): Int {
    try {
        val wrapper: Class<*> = this::class.java
        val method: Method = wrapper.getMethod("getThemeResId")
        method.isAccessible = true
        return method.invoke(this) as Int
    } catch (e: Exception) {
        e.printStackTrace()
        return android.R.style.Theme_DeviceDefault
    }
}


fun View.makeWindowBack(){
    val a = TypedValue()
    context.getTheme().resolveAttribute(android.R.attr.windowBackground, a, true)
    val c = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        a.isColorType
    } else {
        (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT)
    }
    if (c) {
        // windowBackground is a color
        val color = a.data
        setBackgroundDrawable(ColorDrawable(color))
    } else {
        // windowBackground is not a color, probably a drawable
        setBackgroundResource(a.resourceId)
    }
}


