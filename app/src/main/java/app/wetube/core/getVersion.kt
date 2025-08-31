package app.wetube.core

import android.content.Context
import android.content.pm.PackageManager

fun getVersionName(co: Context): String {
    try {
        val pInfo = co.pack
        return(pInfo.versionName.toString())
    } catch (e: PackageManager.NameNotFoundException) {
        return e.message.toString()
    }
}
fun getVersionCode(co: Context): Int {
    try {
        val pInfo = co.pack
        return(pInfo.versionCode)
    } catch (e: PackageManager.NameNotFoundException) {
        return 0
    }
}


val Context.apk get() = applicationContext.packageManager
val Context.pack get() = apk.getPackageInfo(applicationContext.packageName, 0)
