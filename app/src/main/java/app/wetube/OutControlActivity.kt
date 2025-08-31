package app.wetube

import android.app.Activity
import android.os.Binder
import android.os.Process

open class OutControlActivity(val me : Activity): Binder() {
    open fun restart(){
        me.recreate()
    }

    open fun finishing(kill: Boolean) {
        me.finish()
        if(kill){
            android.os.Process.killProcess(Process.myPid())
        }
    }





}