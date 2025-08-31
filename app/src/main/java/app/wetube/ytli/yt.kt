package app.wetube.ytli

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.VisibleForTesting
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayerBridge
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.toFloat
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


private class YouTubePlayerImpl3(private val webView: WebView) : YouTubePlayer {
    private val mainThread: Handler = Handler(Looper.getMainLooper())
    val listeners = mutableSetOf<YouTubePlayerListener>()

    override fun loadVideo(videoId: String, startSeconds: Float) = webView.invoke("loadVideo", videoId, startSeconds)
    override fun cueVideo(videoId: String, startSeconds: Float) = webView.invoke("cueVideo", videoId, startSeconds)
    override fun play() = webView.invoke("playVideo")
    override fun pause() = webView.invoke("pauseVideo")
    override fun mute() = webView.invoke("mute")
    override fun unMute() = webView.invoke("unMute")
    override fun setVolume(volumePercent: Int) {
        require(volumePercent in 0..100) { "Volume must be between 0 and 100" }
        webView.invoke("setVolume", volumePercent)
    }
    override fun seekTo(time: Float) = webView.invoke("seekTo", time)
    override fun setPlaybackRate(playbackRate: PlayerConstants.PlaybackRate) = webView.invoke("setPlaybackRate", playbackRate.toFloat())
    override fun toggleFullscreen() = webView.invoke("toggleFullscreen")
    override fun addListener(listener: YouTubePlayerListener) = listeners.add(listener)
    override fun removeListener(listener: YouTubePlayerListener) = listeners.remove(listener)

    fun release() {
        listeners.clear()
        mainThread.removeCallbacksAndMessages(null)
    }

    private fun WebView.invoke(function: String, vararg args: Any) {
        val stringArgs = args.map {
            if (it is String) {
                "'$it'"
            }
            else {
                it.toString()
            }
        }
        mainThread.post { loadUrl("javascript:$function(${stringArgs.joinToString(",")})") }
    }
}



/**
 * WebView implementation of [YouTubePlayer]. The player runs inside the WebView, using the IFrame Player API.
 */
@SuppressLint("RestrictedApi")
class WTP @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : WebView(context, attrs, defStyleAttr), YouTubePlayerBridge.YouTubePlayerBridgeCallbacks {

    /** Constructor used by tools */

    private val _youTubePlayer = YouTubePlayerImpl3(this)
    val youtubePlayer: YouTubePlayer get() = _youTubePlayer

    private lateinit var youTubePlayerInitListener: (YouTubePlayer) -> Unit

    var isBackgroundPlaybackEnabled = false

    fun initialize(initListener: (YouTubePlayer) -> Unit, playerOptions: IFramePlayerOptions?) {
        youTubePlayerInitListener = initListener
        initWebView(playerOptions ?: IFramePlayerOptions.default)
    }

    // create new set to avoid concurrent modifications
    override val listeners: Collection<YouTubePlayerListener> get() = _youTubePlayer.listeners.toSet()

    override fun getInstance(): YouTubePlayer = _youTubePlayer
    override fun onYouTubeIFrameAPIReady() = youTubePlayerInitListener(_youTubePlayer)

    override fun destroy() {
        _youTubePlayer.release()
        super.destroy()
    }

    var isIrregularSize = true

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if(isIrregularSize){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            val sixteenNineHeight = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec) * 9 / 16,
                MeasureSpec.EXACTLY
            )
            super.onMeasure(widthMeasureSpec, sixteenNineHeight)
        } else
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun computeHorizontalScrollRange(): Int {
        return 0
    }

    override fun computeHorizontalScrollOffset(): Int {
        return 0
    }

    override fun computeVerticalScrollRange(): Int {
        return 0
    }

    override fun computeVerticalScrollOffset(): Int {
        return 0
    }

    override fun computeVerticalScrollExtent(): Int {
        return 0
    }

    override fun computeScroll() {}

    override fun onHoverEvent(event: MotionEvent?): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        return false
    }

    override fun onTrackballEvent(event: MotionEvent?): Boolean {
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }

    override fun onKeyMultiple(keyCode: Int, repeatCount: Int, event: KeyEvent?): Boolean {
        return false
    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(playerOptions: IFramePlayerOptions) {


        settings.apply {
            javaScriptEnabled = true

            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        addJavascriptInterface(YouTubePlayerBridge(this), "YouTubePlayerBridge")

        val htmlPage = readHTMLFromUTF8File(resources.openRawResource(com.pierfrancescosoffritti.androidyoutubeplayer.R.raw.ayp_youtube_player))
            .replace("<<injectedPlayerVars>>", playerOptions.toString())

        loadDataWithBaseURL("https://www.youtube.com"
            , htmlPage, "text/html", "utf-8", null)

        webViewClient = object :WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String?,
            ): Boolean {
                return true
            }



            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                return true
            }
            override fun shouldOverrideKeyEvent(
                view: WebView?,
                event: KeyEvent?,
            ): Boolean {
                return true
            }

            override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?) {
                super.onUnhandledKeyEvent(view, event)
            }
        }
    }


    override fun onWindowVisibilityChanged(visibility: Int) {
        if (isBackgroundPlaybackEnabled && (visibility == View.GONE || visibility == View.INVISIBLE)) {
            return
        }

        super.onWindowVisibilityChanged(visibility)
    }
}

@VisibleForTesting
internal fun readHTMLFromUTF8File(inputStream: InputStream): String {
    inputStream.use {
        try {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream, "utf-8"))
            return bufferedReader.readLines().joinToString("\n")
        } catch (e: Exception) {
            throw RuntimeException("Can't parse HTML file.")
        }
    }
}