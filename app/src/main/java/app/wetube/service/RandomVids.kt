package app.wetube.service

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.preference.PreferenceManager
import android.service.dreams.DreamService
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.TextView
import android.widget.Toast
import app.wetube.R
import app.wetube.core.Utils
import app.wetube.manage.db.VidDB
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlin.math.roundToInt

class RandomVids: DreamService() {
    private val db by lazy{ VidDB(this) }
    private val yt by lazy{ app.wetube.ytli.WTP(this) }
    private val prefs by lazy{ PreferenceManager.getDefaultSharedPreferences(this) }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if(isInteractive)return super.dispatchKeyEvent(event)
        finish()
        return true
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isFullscreen = true
        isScreenBright = false
        isInteractive = !prefs.getBoolean("allergy_touch_when_dreaming", true)
        if(!Utils(this).isConnected()){
            val inte = "No connection"
            try{
                AlertDialog.Builder(window.context).setTitle("Wetube").setMessage(inte).setPositiveButton(android.R.string.ok, null).setOnDismissListener {
                    finish()
                }.show()
            }catch (_: Exception){
                Toast.makeText(
                    try{ window?.context }catch (_: Exception) { this } ?: this,
                    inte,
                    Toast.LENGTH_SHORT
                ).show()
                Handler(mainLooper).postDelayed({finish()}, 500L)
            }
            return
        }
        if(db.listAsList().isNotEmpty()) {
            if(prefs.getBoolean("hearing_only_when_dreaming", false)){
                window.decorView.visibility = View.GONE
            }else {
                setContentView(yt)
            }
        }else{
            try{
                AlertDialog.Builder(window.context).setTitle("Wetube").setMessage("Try add video at least 1 / more").setPositiveButton(android.R.string.ok, null).setOnDismissListener {
                    finish()
                }.show()
            }catch (_: Exception){
                Toast.makeText(
                    try{ window?.context }catch (_: Exception) { this } ?: this,
                    (R.string.no_vid),
                    Toast.LENGTH_SHORT
                ).show()
                Handler(mainLooper).postDelayed({finish()}, 500L)
            }
        }
    }



    override fun onDreamingStarted() {
        super.onDreamingStarted()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.systemBars())
        }
        if(db.listAsList().isNotEmpty()){
            val l = object : AbstractYouTubePlayerListener() {
                var m = 0F
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    if(prefs.getBoolean("mute_dreaming", false)){
                        youTubePlayer.setVolume(0)
                    }
                    randomVid(youTubePlayer)
                }

                fun randomVid(player: YouTubePlayer) {
                    player.loadVideo(db.listAsList().random().videoId, 0f)
                }

                override fun onStateChange(
                    youTubePlayer: YouTubePlayer,
                    state: PlayerConstants.PlayerState
                ) {
                    if(state == PlayerConstants.PlayerState.ENDED){
                        randomVid(youTubePlayer)
                    }
                }

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    super.onCurrentSecond(youTubePlayer, second)

                }

                override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                    super.onVideoDuration(youTubePlayer, duration)
                    m = duration
                }


            }

            yt.initialize({
                it.addListener(l)
            }, IFramePlayerOptions.default)
        }
        else{
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        yt.destroy()
    }
}