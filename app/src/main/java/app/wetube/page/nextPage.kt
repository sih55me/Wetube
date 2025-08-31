package app.wetube.page

import android.app.Activity
import android.app.Fragment
import app.wetube.MainActivity

fun Fragment.nextPage(next: Fragment, tag:String = "", backName: String?=null, breadName: Any? = null, con:Int = MainActivity.contentTarget) = activity.nextPage(this, next, tag, backName, breadName, con)


fun Activity.nextPage(used:Fragment?,next: Fragment, tag:String = "", backName: String?=null, breadName: Any? = null, con:Int = MainActivity.contentTarget){
    used?.let{ next.setTargetFragment(it, 0) }
    fragmentManager.beginTransaction()
        .let {
            if(used != null){
                it.detach(used)
            }else{
                it
            }
        }
        .add(con, next, tag)
        .addToBackStack(backName).apply {
            if(breadName is Int){
                setBreadCrumbTitle(breadName)
            }
            if(breadName is String){
                setBreadCrumbTitle(breadName)
            }
        }.commitAllowingStateLoss()
}