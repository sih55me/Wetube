package app.wetube.service

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.preference.PreferenceManager
import android.service.dreams.DreamService
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.wetube.R
import app.wetube.core.Utils
import app.wetube.manage.db.VidDB
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class RandomVids: DreamService() {
    private val db by lazy{ VidDB(this) }
    private val yt by lazy{ YouTubePlayerView(this) }
    private val prefs by lazy{ PreferenceManager.getDefaultSharedPreferences(this) }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        finish()
        return true
    }
    override fun onAttachedToWindow() {
        isFullscreen = true
        super.onAttachedToWindow()
        if(!Utils(this).isConnected()){
            showText("THIS FEATURE NEED INTERNET")
            return
        }
        if(db.listAsList().isNotEmpty()) {
            if(prefs.getBoolean("ao_rv", false)){
                showText("Playing as audio....")
            }else {
                setContentView(yt)
            }
        }else{
            showText(getText(R.string.no_vid))
        }
    }


    private fun showText(text: CharSequence){
        setContentView(TextView(this).apply {
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            gravity = Gravity.CENTER
            setTextColor(Color.RED)
            setText(text)
            textSize = 20F
        }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        window.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        if(db.listAsList().isNotEmpty()){
            val l = object : AbstractYouTubePlayerListener() {
                var m = 0
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    if(prefs.getBoolean("mute_rv", false)){
                        youTubePlayer.setVolume(0)
                    }
                    randomVid(youTubePlayer)
                }

                fun randomVid(player: YouTubePlayer) {
                    val t = db.listAsList().random()
                    player.loadVideo(t.videoId, 0f)
                }

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    super.onCurrentSecond(youTubePlayer, second)
                    second.toInt().let {
                        if (it == m) randomVid(youTubePlayer)
                    }
                }

                override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                    super.onVideoDuration(youTubePlayer, duration)
                    m = duration.toInt()
                }

                override fun onStateChange(
                    youTubePlayer: YouTubePlayer,
                    state: PlayerConstants.PlayerState
                ) {
                    if(prefs.getBoolean("ao_rv", false)){
                        val t = when(state){
                            PlayerConstants.PlayerState.ENDED -> "The end"
                            PlayerConstants.PlayerState.PLAYING -> "Playing"
                            PlayerConstants.PlayerState.PAUSED -> "Paused"
                            PlayerConstants.PlayerState.BUFFERING -> "Loading...."
                            else -> "IDK what happened\n$state"
                        }
                        showText(t)
                    }
                }
            }

            yt.addYouTubePlayerListener(l)
        }
        else{
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        yt.release()
    }
}