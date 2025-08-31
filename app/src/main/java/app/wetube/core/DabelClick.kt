package app.wetube.core

import android.preference.Preference
import android.view.View
import app.wetube.core.ClickTime.DOUBLE_CLICK_TIME_DELTA

// equivalent to 300 ms
object ClickTime {
    //milliseconds
    const val DOUBLE_CLICK_TIME_DELTA: Long = 300
}
abstract class DabelClick : View.OnClickListener {
    var lastClickTime: Long = 0

    override fun onClick(v: View?) {
        val clickTime = System.currentTimeMillis()
        onOneClick(v)

        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v)
        }
        lastClickTime = clickTime
    }

    abstract fun onDoubleClick(v: View?)
    abstract fun onOneClick(v: View?)


}
abstract class DabelClickPreference : Preference.OnPreferenceClickListener {
    var lastClickTime: Long = 0

    override fun onPreferenceClick(preference: Preference?): Boolean {
        val clickTime = System.currentTimeMillis()
        onOneClick(preference)
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(preference)
        }
        lastClickTime = clickTime
        return true
    }

    abstract fun onDoubleClick(v: Preference?)
    abstract fun onOneClick(v: Preference?)


}

//                          is double click?
fun View.dabelClick(onClick : (Boolean) -> Unit,){
    setOnClickListener(object : DabelClick() {
        override fun onDoubleClick(v: View?) {
            onClick(true)
        }

        override fun onOneClick(v: View?) {
            onClick(false)
        }
    })
}