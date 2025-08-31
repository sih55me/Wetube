package app.wetube.core

import android.content.Context
import android.net.ConnectivityManager

class Utils(private val context : Context) {
    val density get() = context.resources.displayMetrics.density
    fun dpToPx(dp: Float): Float {
        return dp / density
    }
    fun pxToDp(px: Float): Float {
        return px * density
    }
    fun isConnected():Boolean{
        val cM = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nI = cM.activeNetworkInfo
        return nI != null && nI.isConnected
    }
}