package app.wetube.service

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import app.wetube.core.Utils
import app.wetube.core.dabelClick
import app.wetube.core.setupTheme
import app.wetube.databinding.FloatwinBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import kotlinx.coroutines.Runnable


/**
 * A Floating Video that using [Service]
 */
class FloatVideo: Service() {
    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private val binding by lazy{FloatwinBinding.inflate(LayoutInflater.from(this))}
    private lateinit var floatWindowLayoutParam: WindowManager.LayoutParams
    private var isPlaying = false


    companion object{


        /**
         * If the SDK version is below Marshmallow (API 23), overlay permission is not needed and will return true
         *
         * return: if the overlay permission is granted, false otherwise.
         **/

        fun Context.checkOverlayDisplayPermission(): Boolean {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)
        }


        /** Check if a service is currently running
         *
         * return: true if the service is running, false otherwise
         */
        fun Context.isPipServiceRunning(serviceClass: Class<*>): Boolean {
            val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            @Suppress("DEPRECATION")
            return manager.getRunningServices(Int.MAX_VALUE).any {it.service.className == serviceClass.name }
        }

        /** Request the user to enable the "Display over other apps" permission from system settings*/
        @RequiresApi(Build.VERSION_CODES.M)
        fun Context.requestOverlayDisplayPermission() {
            AlertDialog.Builder(this)
                .setTitle("Screen Overlay Permission Needed") // Title of the dialog
                .setMessage("Enable 'Display over other apps' from System Settings.") // Message in the dialog
                .setCancelable(true)
                .setIconAttribute(android.R.attr.alertDialogIcon)// Allow the dialog to be canceled
                .setPositiveButton("Open Settings") { _, _ ->
                    // If the user clicks "Open Settings", open the system settings for overlay permission
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent) // Launch the settings activity
                }.show() // Show the dialog
        }
    }



    override fun onCreate() {
        super.onCreate()
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        setupTheme()
        // Get the WindowManager system service for handling windows on the screen

        // Inflate the floating window layout using the LayoutInflater system service
        Yt.onPip(true)
        val ov = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY  else WindowManager.LayoutParams.TYPE_TOAST
        Utils(this) .apply{
            floatWindowLayoutParam = WindowManager.LayoutParams(
                // Set width to 60% of screen width
                (pxToDp(405F).toInt() / 1.45).toInt(),
                // Set height to 50% of screen height
                (pxToDp(305F).toInt() / 1.45).toInt(),
                // Type for overlay window
                ov,
                // Make it not focusable
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS  or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                // Set transparent background for the window
                PixelFormat.TRANSLUCENT
            ).apply {
                // Position the window at the center of the screen
                gravity = Gravity.CENTER
                x = 0
                y = 0
                windowAnimations = android.R.style.Animation_Dialog

            }
        }
        binding.v.initialize(object : AbstractYouTubePlayerListener() {
                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    super.onCurrentSecond(youTubePlayer, second)
                    Yt.now = second
                }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState,
            ) {
                isPlaying = when(state){
                    PlayerConstants.PlayerState.PLAYING -> {
                        true
                    }

                    else -> {
                        false
                    }
                }
            }

                override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                    super.onError(youTubePlayer, error)
                    Toast.makeText(this@FloatVideo, "Video not valid", Toast.LENGTH_SHORT).show()
                    stopSelf()
                }
            }, false, IFramePlayerOptions.Builder().controls(0).build())
        binding.v.getChildAt(0).also {
            //find legacy
            if (it is FrameLayout) {
                it.isFocusableInTouchMode = false
                it.isClickable = false
                it.isFocusable = false
                it.setHapticFeedbackEnabled(false)
                it.setLongClickable(false)
                //get view
                val t = it.getChildAt(0);
                //check if view is webview
                if(t is WebView) {
                    t.isFocusableInTouchMode = false
                    t.isClickable = false
                    t.isFocusable = false
                    t.setHapticFeedbackEnabled(false)
                    t.setLongClickable(false)
                    t.setOnTouchListener(object : OnTouchListener {
                        override fun onTouch(v: View?, event: MotionEvent): Boolean {
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                event.setLocation((t.getWidth() + 1).toFloat(),
                                    (t.getHeight() + 1).toFloat()
                                )
                            }
                            return event.getAction() == MotionEvent.ACTION_UP
                        }
                    })
                    //change user agent
                    t.settings.userAgentString = "Mozilla/5.0 (X11; Linux x86_64; rv:138.0) Gecko/20100101 Firefox/138.0"
                }
            }
        }

        // Add the floating view to the window using the WindowManager
        windowManager.addView(binding.root, floatWindowLayoutParam)
        binding.v.getYouTubePlayerWhenReady(object: YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                Yt.noted?.videoId?.let { youTubePlayer.loadVideo(it,Yt.now) }
                binding.action.dabelClick {
                    if(it){
                        if (isPlaying) {
                            youTubePlayer.pause()
                        } else {
                            youTubePlayer.play()
                        }
                    }
                }
            }

        })

        binding.close.setOnClickListener {
            stopSelf()
            windowManager.removeView(binding.root)
        }

        binding.open.setOnClickListener {
            val r = Runnable{
                stopSelf()  // Stop the service
                windowManager.removeView(binding.root)  // Remove the floating window
                if (Yt.noted != null) {
                    startActivity(Intent(Yt.vintent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
            r.run()
        }


        setupFloatingWindowMovement()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingWindowMovement() {
        var initialX = 0.0
        var initialY = 0.0
        var initialTouchX = 0.0
        var initialTouchY = 0.0
        // Set a touch listener to detect dragging
        binding.s.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Record the initial position and touch points when the touch starts
                    initialX = floatWindowLayoutParam.x.toDouble()
                    initialY = floatWindowLayoutParam.y.toDouble()
                    initialTouchX = event.rawX.toDouble()
                    initialTouchY = event.rawY.toDouble()
                }
                MotionEvent.ACTION_MOVE -> {
                    // Calculate the new position of the window based on the movement of the touch
                    floatWindowLayoutParam.x = ((initialX + event.rawX) - initialTouchX).toInt()
                    floatWindowLayoutParam.y = ((initialY + event.rawY) - initialTouchY).toInt()
                    // Update the layout
                    windowManager.updateViewLayout(binding.root, floatWindowLayoutParam)
                }
            }
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Yt.onPip(false)
        binding.v.release()
        try {
            // Remove the floating window from the screen
            windowManager.removeView(binding.root)
        } catch (e: Exception) {
            // Handle any potential errors
            e.printStackTrace()
        }
    }



    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}