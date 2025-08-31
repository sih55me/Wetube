package app.wetube.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class VideoService : Service() {
    private val binder = videobind()
    override fun onBind(p0: Intent?): IBinder {
        return binder
    }
    inner class videobind : Binder(){
        fun getService() : VideoService = this@VideoService
    }

}