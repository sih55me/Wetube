package app.wetube

import android.content.Context
import android.content.res.Configuration


data object DarkMode {
    const val LIGHT = "l"
    const val DARK = "d"
    const val BATTERY = "b"
    const val AUTO = "a"
    @JvmStatic
    fun checkDarkMode(c:Context) = c.getResources().getConfiguration().uiMode and Configuration.UI_MODE_NIGHT_MASK



//    ^^int result =

//    when (nightModeFlags) {
//        Configuration.UI_MODE_NIGHT_YES -> doStuff()
//        Configuration.UI_MODE_NIGHT_NO -> doStuff()
//        Configuration.UI_MODE_NIGHT_UNDEFINED -> doStuff()
//    }

}