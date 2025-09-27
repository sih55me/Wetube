package app.wetube.ytli

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer

/**
 * quality definitton control for player
 */
class Qwali (private val callback: Callback){
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())
    @JavascriptInterface
    fun sendVideoQuality(quality: String) {
        mainThreadHandler.post {
            callback.onVideoQuality(callback.yt, quality)
        }
    }


    interface Callback{
        fun onVideoQuality(instance: YouTubePlayer, quality: String)
        val yt: YouTubePlayer
    }
}
