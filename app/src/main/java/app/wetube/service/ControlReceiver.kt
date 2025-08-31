package app.wetube.service

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast

class ControlReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent) {
        val state = p1.getBooleanExtra("state", false)
        if (state) {
            Yt.pause()
        } else if (!state) {
            Yt.play()
        }
        val close = p1.getStringExtra("close")
        if(close == ""){
            if(p0 is Activity){
                p0.finish()
            }else{
                Toast.makeText(p0, "", Toast.LENGTH_SHORT).show()
            }
        }


    }

    override fun peekService(myContext: Context?, service: Intent?): IBinder {
        return super.peekService(myContext, service)
    }




}