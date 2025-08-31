package app.wetube.page

import android.app.Activity
import android.content.res.Configuration
import app.wetube.DarkMode

fun Activity.theme() = when(DarkMode.checkDarkMode(this)){
    Configuration.UI_MODE_NIGHT_YES -> android.R.style.Theme_Holo
    else -> android.R.style.Theme_Holo_Light
}

fun Activity.theme2() = when(DarkMode.checkDarkMode(this)){
    Configuration.UI_MODE_NIGHT_YES -> android.R.style.Theme_DeviceDefault
    else -> android.R.style.Theme_DeviceDefault_Light
}