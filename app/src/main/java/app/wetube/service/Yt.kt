package app.wetube.service
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import app.wetube.VideoView
import app.wetube.core.isTv
import app.wetube.item.Video
import app.wetube.nothing.NothingPlayer
import app.wetube.service.FloatVideo.Companion.checkOverlayDisplayPermission
import app.wetube.service.FloatVideo.Companion.isPipServiceRunning
import app.wetube.service.FloatVideo.Companion.requestOverlayDisplayPermission
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer

//signal
object Yt {

    var vintent= Intent()

    //           isShow
    var onPip :((Boolean) -> Unit) ={

    }
    var noted : Video? = null
        set(value) {
            field = value
            link = value?.id ?: 0
            videoId = value?.videoId.toString()
        }
    val nullToPlay = NothingPlayer.newInstance()
    var pos = 1
    var link = 0
    var vid = ""
    var l = ""
    var now = 0F
    var isPLay = false
    var keeplay = false
    var youTubePlayer: YouTubePlayer = nullToPlay
    var videoId = ""
    fun pause() {
        youTubePlayer.pause()
        isPLay = false
    }

    fun play() {
        youTubePlayer.play()
        isPLay = true
    }

    fun clear(){
        isPLay = false
        keeplay = false
        videoId = ""

        now = 0F
        link = 0
        youTubePlayer = nullToPlay
        onPip = {}
    }


    fun Context.atPip(video: Video, listener :((Boolean) -> Unit) = {}, i: Intent = Intent(this, FloatVideo::class.java)){
        if(isTv){
            Toast.makeText(this, "PIP not available in tv", Toast.LENGTH_SHORT).show()
            return
        }
        if(checkOverlayDisplayPermission()) {
            if (!isPipServiceRunning(FloatVideo::class.java)) {
                this@Yt.noted = video
                now = 0F
                onPip = listener
                vintent = Intent(this, VideoView::class.java).apply {
                    putExtra("vid", video)
                }
                startService(i)
            }else{
                stopService(i)
                atPip(video,listener,i)
            }
        }else{
            if(this is Activity){
                requestOverlayDisplayPermission()
            }else{
                Toast.makeText(this, "Permission PIP Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }



}