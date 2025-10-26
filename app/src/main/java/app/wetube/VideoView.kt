package app.wetube

import android.Manifest.permission.POST_NOTIFICATIONS
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.app.PictureInPictureParams
import android.app.Presentation
import android.app.RemoteAction
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ShortcutInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.media.MediaRouter
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.preference.PreferenceManager
import android.app.Notification
import android.graphics.drawable.BitmapDrawable
import app.wetube.manage.provide.FileProvider
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.util.Log
import android.util.Rational
import android.util.TypedValue
import android.view.Display
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.PointerIcon
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowManager.InvalidDisplayException
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Toast
import android.widget.Toolbar
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedCallback
import androidx.annotation.RequiresApi
import app.wetube.VideoView.Companion.VOLUME_DIALOG
import app.wetube.adapter.ColorizeAdapter
import app.wetube.adapter.FastSupportAdapter
import app.wetube.core.DabelClick
import app.wetube.core.Utils
import app.wetube.core.dabelClick
import app.wetube.core.fadeIn
import app.wetube.core.fadeOut
import app.wetube.core.getThemeId
import app.wetube.core.info
import app.wetube.core.isTv
import app.wetube.core.releaseParent
import app.wetube.core.setupTheme
import app.wetube.core.showBackButton
import app.wetube.core.tryOn
import app.wetube.databinding.VideoViewBinding
import app.wetube.databinding.VideoViewInfoBinding
import app.wetube.databinding.VolumeLayoutBinding
import app.wetube.databinding.ZoomBinding
import app.wetube.item.ChannelDetail
import app.wetube.item.Video
import app.wetube.item.VideoDetail
import app.wetube.manage.db.VidDB
import app.wetube.nothing.NothingPlayer
import app.wetube.p.FadeViewHelper
import app.wetube.p.formatTime
import app.wetube.page.dialog.NewVidDialog
import app.wetube.page.dialog.QRCodePage
import app.wetube.pryektr.PlayerProyektor
import app.wetube.service.ControlReceiver
import app.wetube.service.FloatVideo
import app.wetube.service.FloatVideo.Companion.checkOverlayDisplayPermission
import app.wetube.service.FloatVideo.Companion.isPipServiceRunning
import app.wetube.service.FloatVideo.Companion.requestOverlayDisplayPermission
import app.wetube.service.Yt
import com.cocosw.bottomsheet.BottomSheet
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlaybackRate
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt
import kotlin.random.Random


class VideoView : Activity(), PlayerProyektor.Connection{
    var isNotch = false
    private val roter by lazy{ getSystemService(Context.MEDIA_ROUTER_SERVICE) as MediaRouter }
    val db by lazy { VidDB(this) } ;
    var menu : Menu? = null
    var dialogStack = 0

    var qualityList = listOf<String>()
    var qualityNow = "default"

    var fulldia: Dialog? = null
    /**
     * Use in Vertical mode
     **/
    private var lockPot = false
    /**
     * Use in [VOLUME_DIALOG]
     **/
    private val volView by lazy { ScrollView(this) }
    /**
     * Use in [isInMultiWindowMode]
     **/
    private var lockFull = false
    private var mPresentation : Presentation? = null
    private var noteId : Int = 0
        set(value) {
            field = value
            Yt.link = value
        }
    private var video_id : String = ""
        set(value) {
            field = value
            Yt.videoId = value
        }
    private var uyt: YouTubePlayer? = null
    var play = false;
    private var mute = false;

    /**
     * "[now]" mean current time
     **/
    var now
        set(value){
            Yt.now = value
        }
        get() = Yt.now;

    val displeiyTvL = object : MediaRouter.SimpleCallback(){
        override fun onRouteSelected(
            router: MediaRouter?,
            type: Int,
            info: MediaRouter.RouteInfo?,
        ) {
            lookThisInTv()
        }

        override fun onRouteUnselected(
            router: MediaRouter?,
            type: Int,
            info: MediaRouter.RouteInfo?,
        ) {
            lookThisInTv()
        }

        override fun onRoutePresentationDisplayChanged(
            router: MediaRouter?,
            info: MediaRouter.RouteInfo?,
        ) {
            lookThisInTv()
        }
    }



    fun lookThisInTv() {
        val info = roter.getSelectedRoute(
            MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        var displayShow : Display? = null
        if(info != null) {
            displayShow = info.presentationDisplay
            if (mPresentation != null && mPresentation?.display != displayShow) {
                mPresentation?.dismiss();
                info("Disconnect from tv")
                bin.videoview.let {
                    it.parent.let {p->
                        if(p is ViewGroup){
                            p.removeView(it)
                        }
                    }
                    bin.saverContainer.addView(it)
                }
            }
        }
        if (mPresentation == null && displayShow != null) {
            // Initialise a new Presentation for the Display
            mPresentation = Presentation(this, displayShow)
            bin.videoview.let {
                it.tag = it.layoutParams
                it.parent.let {p->
                    if(p is ViewGroup){
                        p.removeView(it)
                    }
                }
                mPresentation!!.setContentView(it)
            }

            mPresentation!!.setOnDismissListener{
                if (it == mPresentation) {
                    mPresentation = null;
                }
            }

            // Try to show the presentation, this might fail if the display has
            // gone away in the mean time
            try {
                mPresentation!!.show()

                info("Join with ${info?.getName(this)} ")
            } catch (ex: InvalidDisplayException) {
                // Couldn't show presentation - display was already removed
                mPresentation = null
            }
        }
    }
    /**
     * Use in [VOLUME_DIALOG]
     **/
    private val volumePhone by lazy{ VolumeLayoutBinding.inflate(layoutInflater)}
    /**
     * Use in [VOLUME_DIALOG]
     **/
    private val volumePlayer by lazy {   VolumeLayoutBinding.inflate(layoutInflater)}
    val bin by lazy { VideoViewBinding.inflate(layoutInflater) } ;var stvo = false
    var l:YouTubePlayer = NothingPlayer.newInstance()
        set(value) {
            field = value
            Yt.youTubePlayer = value
        }
    var fulskrinBek = Any()
    var ll :YouTubePlayerListener = object : AbstractYouTubePlayerListener(){} ;
    var thumb : Bitmap? = null
    private var isFullscreen = false
    private val audioManager: AudioManager
        get() = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    /**
     * selected video
     **/
    var noted : Video?
        get() = Yt.noted
        set(value){
            Yt.noted = value

        }

    var newCon:Configuration? = null
    /**
     * Use in [playercontrols]
     **/
    lateinit var nobu : Notification.Builder
    /**
     * Use in [playercontrols]
     **/
    val notifMan : NotificationManager get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    /**
     * value to set fullscreen state.
     * Use in [exitFullscreen] n' [enterFullscreen]
     **/
    var isInFullscreen = false
    var collape = false
    val sp by lazy { PreferenceManager.getDefaultSharedPreferences(applicationContext) }
    val ms : MediaSession by lazy { MediaSession(this, "Yt") }
    private var isnotifexist = false
    var p = 1;var max = 0f
    var isLoop = false
    val track by lazy{YouTubePlayerTracker()}
    var isSeeking = false
    var stillPip = false
    val playlist by lazy {
        FastSupportAdapter(
            this,
            R.layout.playlist_item,
            mutableListOf<String>()
        )
    }

    private var pipStand = Intent()



    override fun onDestroy() {

        bin.videoview.destroy()
        if(isnotifexist) {
            tryOn {
                notifMan.cancel(1)
            }
        }
        Yt.clear()
        ms.release()

        if (isPipServiceRunning(FloatVideo::class.java)) {
            stopService(pipStand)
        }
        super.onDestroy()
    }


    val translut by lazy{ TranslucentHelper(this) }


    override fun onBackPressed() {
        when{
            packageManager.hasSystemFeature("android.software.leanback") -> finishAndGoToMain()
            isInFullscreen -> if(!lockFull){
                exitFullscreen()
            }else{
                finishAndGoToMain()
            }
            else -> finishAndGoToMain()
        }

    }




    private fun exitFullscreen(anim: Boolean = true, setValue: Boolean = true) {
        bin.swipe.visibility = View.VISIBLE
        bin.swipe.requestLayout()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.show(WindowInsets.Type.systemBars())
        }
        bin.straing.setPadding(0,0,0,0)
        window?.decorView?.systemUiVisibility = 0
        window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR)
        if (!resources.getBoolean(R.bool.tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER)
        val view = bin.whoc
        val animatorh = ValueAnimator.ofInt( window.decorView.height, vidSize.first);
        animatorh.addUpdateListener { animation ->
            val animatedValue = animation.getAnimatedValue();
            if (animatedValue is Int) {
                view.layoutParams?.apply {
                    height = animatedValue
                    (bin.swipe.layoutParams as LinearLayout.LayoutParams).topMargin  = animatedValue - vidSize.first
                    bin.swipe.requestLayout()

                }
                view.requestLayout()
            }
        }
        val animatorw = ValueAnimator.ofInt( window.decorView.width, vidSize.second);
        animatorw.addUpdateListener { animation ->
            val animatedValue = animation.getAnimatedValue();
            if (animatedValue is Int) {
                view.layoutParams?.apply {
                    width = animatedValue
                }
                view.bringToFront()
                view.requestLayout()

            }
        }
        val s = AnimatorSet()
        s.playTogether(animatorh, animatorw,)
        s.setDuration(300L)



        Runnable{
            //        scrollView.setEnableScrolling(true);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            bin.videoLay.toggleFullScreen.contentDescription = getString(R.string.fullscreen)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                bin.videoLay.toggleFullScreen.tooltipText = getString(R.string.fullscreen)
            }
            bin.straing.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
            setUiState(true)
            bin.videoLay.toggleFullScreen.setImageResource(R.drawable.fullscreen)


//            bin.videoview.layoutParams?.apply {
//                val s = ViewGroup.LayoutParams.WRAP_CONTENT
//                height = s
//                width = s
//            }
            tryOn{ isImmersive = false }

            lockPot = false
            updateView()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                onBackInvokedDispatcher.unregisterOnBackInvokedCallback(fulskrinBek as OnBackInvokedCallback)
            }
        }.let {r->
            if(anim){
                s.addListener(object: Animator.AnimatorListener{
                    override fun onAnimationStart(animation: Animator) {
                        bin.videoLay.playlistBtn.apply {
                            visibility = View.VISIBLE
                            alpha = 1F
                            animate().alpha(0F).withEndAction {
                                visibility = View.GONE
                            }
                        }
                    }

                    override fun onAnimationEnd(animation: Animator) {

                        bin.whoc.layoutParams?.apply {
                            val s = ViewGroup.LayoutParams.WRAP_CONTENT
                            height = s
                            width = s
                        }
                        r.run()
                    }

                    override fun onAnimationCancel(animation: Animator) {

                    }

                    override fun onAnimationRepeat(animation: Animator) {

                    }
                })
                s.start()
            }else{
                r.run()
            }
        }
        if(setValue){
            isFullscreen = false
        }
        bin.videoLay.bottom.setPadding(0,0,0, 0)
        bin.videoLay.tul.setPadding(0,0,0, 0)
        actionBar?.setDisplayShowTitleEnabled(false)
        if(!isTablet){
            fadeIn(bin.videoLay.potraitFullscreen)
        }else{
            fadeOut(bin.videoLay.potraitFullscreen)
        }
    }

    var vidSize = Pair(0,0)

    
    private fun enterFullscreen(pot:Boolean = false, anim: Boolean = true, setValue :Boolean = true, addFlagToWin:Boolean = true) {
        if(bin.swipe.visibility != View.GONE){
            bin.swipe.translationY = 0F
            bin.swipe.alpha = 1f
        }
        fadeOut(bin.videoLay.potraitFullscreen)
        actionBar?.setDisplayShowTitleEnabled(true)
        val view = bin.whoc
        vidSize= Pair(view.height, view.width)

        val animatorh = ValueAnimator.ofInt( view.height, window.decorView.height);
        animatorh.addUpdateListener { animation ->
            val animatedValue = animation.getAnimatedValue();
            if (animatedValue is Int) {
                view.layoutParams?.apply {
                    height = animatedValue
                        (bin.swipe.layoutParams as LinearLayout.LayoutParams).topMargin  = animatedValue - vidSize.first
                        bin.swipe.requestLayout()

                }
                view.requestLayout()
            }
        }
        val animatorw = ValueAnimator.ofInt( vidSize.second, window.decorView.width);
        animatorw.addUpdateListener { animation ->
            val animatedValue = animation.getAnimatedValue();
            if (animatedValue is Int) {
                view.layoutParams?.apply {
                    width = animatedValue
                }
                view.bringToFront()
                view.requestLayout()

            }
        }
        val s = AnimatorSet()
        s.playTogether(animatorh, animatorw)
        s.setDuration(300L)

        val r = Runnable {

            if(isTablet){ bin.swipe.visibility = View.GONE }
            setUiState(false)
            bin.whoc.layoutParams?.apply {
                val s = ViewGroup.LayoutParams.MATCH_PARENT
                height = s
                width = s
            }
            bin.straing.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            //        scrollView.setEnableScrolling(false);

            requestedOrientation = if (pot) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }



            if(setValue){
                isFullscreen = true
            }

            lockPot = pot
            bin.videoLay.toggleFullScreen.setImageResource(R.drawable.exit_fullscreen)
            bin.videoLay.toggleFullScreen.contentDescription =
                getString(R.string.exit_fullscreen)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                bin.videoLay.toggleFullScreen.tooltipText = getString(R.string.exit_fullscreen)
            }
            if(addFlagToWin){
                window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR)
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                onBackInvokedDispatcher.registerOnBackInvokedCallback(
                    0,
                    fulskrinBek as OnBackInvokedCallback
                )
            }
            bin.videoLay.seekbarStock.visibility = View.VISIBLE

        }
        if(anim){
            s.addListener(object: Animator.AnimatorListener{
                override fun onAnimationStart(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {
                    r.run()
                    bin.videoLay.playlistBtn.apply {
                        visibility = View.VISIBLE
                        alpha = 0F
                        animate().alpha(1F)
                    }
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
            s.start()
        }else{
            r.run()
        }





        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.systemBars())
        }

        val flags =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    (View.SYSTEM_UI_FLAG_FULLSCREEN) or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_IMMERSIVE
        window?.decorView?.systemUiVisibility = flags
        updateView()
        try{
            isImmersive = true
        }catch (_: Exception){

        }
    }



    private fun requestNotif(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(POST_NOTIFICATIONS), 1)
        }
    }

    fun loadPIPR(){
        val u = til
        val sourceRectHint = Rect();
        bin.videoview.getGlobalVisibleRect(sourceRectHint);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val r = Rational(16, 9)
            val fi = openVideoNTicket(this@VideoView, noted?:return,p, intent = roundIntent)
            val ra = RemoteAction(
                Icon.createWithResource(this@VideoView,R.drawable.close),
                getString(R.string.close),getString(R.string.close),
                PendingIntent.getActivity(applicationContext, 3, fi.putExtra("finish", true),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            )
            val i = Intent(applicationContext, app.wetube.service.ControlReceiver::class.java)
            val s = if (play)getString(R.string.pause) else getString(R.string.play)
            val a = RemoteAction(
                Icon.createWithResource(this@VideoView,if(!play)R.drawable.play else R.drawable.pause), s,s,
                PendingIntent.getBroadcast(applicationContext, 2, i.putExtra("state", play),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            )
            setPictureInPictureParams(PictureInPictureParams.Builder()
                .setActions(mutableListOf(ra,a))
                .setAspectRatio(r).also{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.setExpandedAspectRatio(try{ Rational(bin.videoview.measuredWidth * 2, bin.videoview.height * 2) }catch (_: Exception){Rational(16*2, 18)})
                        it.setTitle(title)
                        it.setCloseAction(ra)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        it.setAutoEnterEnabled(false)
                        it.setSeamlessResizeEnabled(true)
                    }
                }.build())

        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.video_prefence, menu)
        menu?.apply{
            findItem(R.id.del_ic)?.also {
                it.setIcon(if (db.listAsList().map { it.videoId }.contains(video_id)) R.drawable.delete else R.drawable.add)
                it.setTitle(if (db.listAsList().map { it.videoId }.contains(video_id)) R.string.del else R.string.add)
            }
            add("Quality").setOnMenuItemClickListener {
                showDialog(QV_DIALOG)
                true
            }
            add("Zoom").setOnMenuItemClickListener {
                showDialog(200)
                true
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                add("Minimize").setOnMenuItemClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasPermission()) {
                        loadPIPR()
                        enterPictureInPictureMode()
                    } else {
                        val message = if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) "PIP need Android 8 (a.k.a Oreo) or high, but you're using Android ${Build.VERSION.RELEASE}" else if(!hasPermission()) "You need to grant the permission to use PIP" else "Unknown Error"
                        android.app.AlertDialog.Builder(this@VideoView).apply {
                            setTitle("Picture in Picture is not supported in your device")
                            setMessage(message)
                            setPositiveButton(android.R.string.ok,null)
                        }.show()
                    }
                    true
                }
            }
            add("Random Duration").setOnMenuItemClickListener {
                randomDur()
                true
            }
            add("Volume").setOnMenuItemClickListener {

                    showDialog(VOLUME_DIALOG)

                true
            }
        }

        return super.onCreateOptionsMenu(menu)

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        prepMenu(menu, true)
        menu?.findItem(R.id.del_ic)?.also {
            it.setTitle(if (db.listAsList().map { it.videoId }.contains(video_id)) R.string.del else R.string.add)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun prepMenu(menu: Menu?, fromActivity: Boolean) {
        menu?.findItem(R.id.loop)?.isChecked = sp.getBoolean("loop", false)

    }

    @SuppressLint("MissingInflatedId", "RestrictedApi", "ResourceType")


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when(keyCode){


            KeyEvent.KEYCODE_TAB -> {
                return true
            }


            KeyEvent.KEYCODE_DPAD_LEFT-> {
                try {
                    l.seekTo(now - skipDuraction)
                }catch(_:Exception){}
            }
            KeyEvent.KEYCODE_J -> {
                try {
                    l.seekTo(now - skipDuraction)
                }catch(_:Exception){}
            }
            KeyEvent.KEYCODE_0 -> {
                try {
                    l.seekTo(0f)
                }catch(_:Exception){}
            }
            KeyEvent.KEYCODE_DPAD_RIGHT  -> {
                try {
                    l.seekTo(now + skipDuraction)
                }catch(_:Exception){}
            }
            KeyEvent.KEYCODE_L -> {
                try {
                    l.seekTo(now + skipDuraction)
                }catch(_:Exception){}
            }
            KeyEvent.KEYCODE_VOLUME_UP ->{
                return super.onKeyDown(keyCode, event)
            }

            KeyEvent.KEYCODE_SPACE ->{
                if(play)l.pause() else l.play()
                return true
            }

            KeyEvent.KEYCODE_K ->{
                if(play)l.pause() else l.play()
            }



            KeyEvent.KEYCODE_F ->{

                if (isInFullscreen) {
                    exitFullscreen()
                } else {
                    enterFullscreen()
                }
                isInFullscreen = !isInFullscreen
            }
            KeyEvent.KEYCODE_VOLUME_DOWN ->{
                return super.onKeyDown(keyCode, event)
            }
            KeyEvent.KEYCODE_VOLUME_MUTE ->{
                if(!mute) {
                    l.mute()
                    mute = true
                }else{
                    l.unMute()
                    mute = false
                }
                snackBar(if(mute) "Mute player" else "Unmute player")
            }
            KeyEvent.KEYCODE_M ->{
                if(!mute) {
                    l.mute()
                    mute = true
                }else{
                    l.unMute()
                    mute = false
                }
                snackBar(
                    if(mute) "Mute player" else "Unmute player"
                )
            }
            KeyEvent.KEYCODE_DPAD_DOWN ->{
                window.openPanel(Window.FEATURE_OPTIONS_PANEL, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU))
            }

            KeyEvent.KEYCODE_DPAD_CENTER -> {
                if(play)l.pause() else l.play()
                return true
            }


            else -> {
                Toast.makeText(this, "Pressed key code $keyCode", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    fun prev(){
        val d = intent.getParcelableArrayExtra("playlist")
        if (0 != p) try{
            val note = d?.get(p - 1) as Video
            p -= 1
            craftVideo(note)
        }catch (e:Exception){
            Log.e("Yt", "No more videos")
        }
        refreshList()
        reCekList()
    }

    fun craftVideo(note: Video){
        video_id = note.videoId
        noteId = note.id
        Yt.link = note.id
        title = note.title
        noted = note
        runOnUiThread {
            l.loadVideo(note.videoId, 0f)
            title  = note.title
            setTaskDescription(ActivityManager.TaskDescription(note.title))
        }
        setupThumb
        cekCenel()
    }
    fun share(){
        val shareIntent = Intent()
        val stxt = "youtube.com/watch?v=${video_id}"
        shareIntent.apply{
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, stxt)
            putExtra(Intent.EXTRA_TITLE, "YouTube Video Link")
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
    }

    fun reCekList(){
        bin.playlist.clearChoices()
        bin.playlist.setItemChecked(p,true)
    }
    fun next(){
        val d = intent.getParcelableArrayExtra("playlist")
        if (d?.size?.minus(1)  != p) try{
            val note = d?.get(p + 1) as Video
            p += 1

            craftVideo(note)
        }catch (e:Exception){
            Log.e("Yt", "No more videos")
        }
        reCekList()
        refreshList()
    }

    private fun refreshList() {
        playlist.clear()
        nList()
        tryOn{
            playlist.notifyDataSetChanged()

        }
    }

    fun pip(){
        if(!isPipServiceRunning(FloatVideo::class.java)) {
            if (checkOverlayDisplayPermission()) {
                // If permission is granted, start the floating window service and finish the activity
                pipStand = Intent(this, FloatVideo::class.java)
                startService(pipStand.putExtra("id", video_id))
            } else {
                // If permission is not granted, request overlay permission
                requestOverlayDisplayPermission()
            }
        }else{
            stopService(pipStand)
            pip()
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat("time", now)
        outState.putParcelable("item", noted)
        outState.putBoolean("play", play)
        super.onSaveInstanceState(outState)
    }


    val c = object: MediaSession.Callback(){
        override fun onPlay() {
            l.play()
        }

        override fun onPause() {
            l.pause()
        }

        override fun onSeekTo(pos: Long) {
            l.seekTo((pos / 1000L).toFloat())
        }

        override fun onSkipToPrevious() {
            prev()
        }

        override fun onSkipToNext() {
            next()
        }

    }
    fun changeSeekbarColor(c:Int){
        val skbrs =arrayOf( bin.videoLay.seekbarStock, bin.videoLay.loadingVideo)
        for (sb in skbrs) {
            try{
                sb.getProgressDrawable().setTint(c);
                if (sb is SeekBar) {
                    sb.getThumb().setTint(c);
                }
            }catch (_: Throwable){

            }
        }
    }



    fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).roundToInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    fun changeButtonColor(i:Pair<Int,Int>){
        val btns =arrayOf( bin.videoLay.pausePlay, bin.videoLay.toggleFullScreen, bin.videoLay.playlistBtn)
        for (sb in btns) {
            try{

                sb.backgroundTintList = ColorStateList.valueOf(adjustAlpha(i.first, 0.3F))
                sb.imageTintList = ColorStateList.valueOf(i.second)
            }catch (_: Throwable){

            }
        }
    }
    val til by lazy { Utils(this) }
    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setupTheme()
        translut.setTranslucentStatus(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.fitInsetsTypes = 0
        }
        p = intent.getIntExtra("vi", 1)
        setContentView(bin.root)
        setActionBar(bin.videoLay.tul)
        updateView()
        window.navigationBarColor = Color.TRANSPARENT
        window.statusBarColor = Color.TRANSPARENT
        showBackButton()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        if(sp.getBoolean("show_playlist_control", false)){
            bin.playlistCon.visibility = View.VISIBLE
            bin.prevV.setOnClickListener {
                prev()
            }
            bin.nextV.setOnClickListener {
                next()
            }
        }
        actionBar?.setDisplayShowTitleEnabled(false)
        bin.videoLay.tul.popupTheme = getThemeId()
        fulldia = object :Dialog(this, getThemeId()){
            init {
                window?.setWindowAnimations(android.R.style.Animation_Dialog)
                window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCanceledOnTouchOutside(false)
                window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or (View.SYSTEM_UI_FLAG_FULLSCREEN) or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_IMMERSIVE
            }

            override fun show() {
                window?.let { configNotchWindow(it) }
                super.show()
            }
            override fun onBackPressed() {
                this@VideoView.onBackPressed()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            bin.root.setOnApplyWindowInsetsListener{v,i->
                val top = i.getInsets(WindowInsets.Type.statusBars() or WindowInsets.Type.displayCutout())
                val bot = i.getInsets(WindowInsets.Type.navigationBars())
                val insets = i.displayCutout
                bin.playlist.setPadding(0,0,0,bot.bottom)
                val normalLayout = fun(){
                    bin.videoLay.tul.layoutParams.also{
                        if(it is ViewGroup.MarginLayoutParams){
                            0.let{i->
                                it.topMargin = i
                                it.leftMargin = i
                                it.rightMargin = i
                            }
                        }
                    }

                    bin.videoLay.bottom.layoutParams.also{
                        if(it is ViewGroup.MarginLayoutParams){
                            0.let{i->
                                it.leftMargin = i
                                it.rightMargin = i
                            }
                        }
                    }
                }
                if(isFullscreen){
                    bin.vidlay.setPadding(0,0,0,0)
                    insets?.let { notch ->
                        bin.videoLay.tul.layoutParams.also{
                            if(it is ViewGroup.MarginLayoutParams){
                                it.topMargin = notch.safeInsetTop
                                it.leftMargin = notch.safeInsetLeft
                                it.rightMargin = if(dialogStack == 0)notch.safeInsetRight else 0
                            }
                        }

                        bin.videoLay.bottom.layoutParams.also{
                            if(it is ViewGroup.MarginLayoutParams){
                                it.leftMargin = notch.safeInsetLeft
                                it.rightMargin = if(dialogStack == 0)notch.safeInsetRight else 0
                            }
                        }

                    }
                }else{

                    bin.vidlay.setPadding(0,top.top,0,0)
                    normalLayout()
                }
                WindowInsets.CONSUMED
            }
        }else{
            bin.vidlay.setPadding(0,translut.mStatusBarHeight,0,0)
        }
        bin.title.setOnClickListener {
            showDialog(DESC_DIALOG)
        }
        window.decorView.isEnabled = !isTv
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                fulskrinBek = object :OnBackAnimationCallback{
                    override fun onBackInvoked() {
                        window.decorView.animate().y(800F).withEndAction{
                            exitFullscreen()
                            window.decorView.apply {
                                (0F).let {
                                    y = it
                                    scaleY = it + 1F
                                    scaleX = it + 1F
                                }
                            }
                        }
                    }

                    override fun onBackStarted(backEvent: BackEvent) {
                        window.decorView.animate().scaleX(0.8F).scaleY(0.8F).y(100F)


                    }




                    override fun onBackCancelled() {
                        (0F).let {
                            window.decorView.animate().y(it).scaleX(it + 1F).scaleY(it + 1F)
                        }
                    }

                }
            } else{
                fulskrinBek = OnBackInvokedCallback {
                    exitFullscreen()
                }
            }
        }



//        bin.toolbar.setNavigationOnClickListener {
//            collape = !collape
//            when(collape){
//                true -> {
//                    bin.videoview.visibility = View.INVISIBLE
//                    bin.play.show()
//                }
//                false -> {
//                    fadeIn(bin.videoview)
//                    bin.play.hide()
//                }
//            }
//
//        }


        val sp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        pipStand = Intent(this, FloatVideo::class.java)

        Yt.vintent = intent
        p = intent.getIntExtra("vi", 0)
        reloadPref()
        if(savedInstanceState != null) {
            val d = savedInstanceState.getParcelable<Video>("item")
            if(d is Video) {
                intent.putExtra("vid", d)
            }
        }
        val item = intent.getParcelableExtra<Video>("vid")
        if ((item is Video)) {
            noted = item
            if(noted != null) {
                noteId = noted?.id!!
            }
            if (item != null) {
                video_id = item.videoId
            }
            if (item != null) {
                Yt.videoId = item.videoId
            }
            if (item != null) {
                title = item.title
            }
            val l = db.listAsList().map { it.videoId }
            cekCenel()
            checkStateSave(l.contains(item.videoId))
        }





        bin.videoLay.next.setOnClickListener {
            next()
        }





        var isReadyToChange = false

        bin.videoview.isBackgroundPlaybackEnabled = (true)
        bin.videoview.isEnabled = !sp.getBoolean("tv", false)
        val fade = FadeViewHelper(bin.videoLay.container).apply {
            visibilityListener = {
                bin.videoLay.tul.animate().translationY(if(it) -bin.videoLay.tul.height.toFloat() else 0F)
                bin.videoLay.bottom.animate().translationY(if(it) bin.videoLay.tul.height.toFloat() else 0F)

            }
        }
        var changeABI = fun(p:PlayerState){}
        var erdialog = AlertDialog.Builder(this, getThemeId()).setTitle(android.R.string.VideoView_error_title).create()
        ms.setCallback(c)
        ll = object : AbstractYouTubePlayerListener() {
            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                super.onError(youTubePlayer, error)
                erdialog = AlertDialog.Builder(this@VideoView).setTitle(android.R.string.VideoView_error_title).setMessage(error.name).setPositiveButton(android.R.string.ok, null).create()
                erdialog.window!!.attributes!!.apply {
                    val s  = Point()
                    windowManager.defaultDisplay.getSize(s)
                    width = if(s.x > s.y) windowManager.defaultDisplay.height else windowManager.defaultDisplay.width
                    var g = Gravity.TOP
                    if(isTablet and !isInFullscreen) {
                        g = g or Gravity.START
                    }
                    gravity = g
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                erdialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                erdialog.window!!.attributes!!.dimAmount = 0F
                erdialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                try{
                    erdialog.show()
                    erdialog.setMessage(error.name)
                }catch (_: Throwable){
                    Toast.makeText(this@VideoView, error.name, Toast.LENGTH_SHORT).show()
                }
            }


            override fun onPlaybackQualityChange(
                youTubePlayer: YouTubePlayer,
                playbackQuality: PlayerConstants.PlaybackQuality
            ) {

                qualityNow = when(playbackQuality){
                    PlayerConstants.PlaybackQuality.SMALL -> "small"
                    PlayerConstants.PlaybackQuality.MEDIUM -> "medium"
                    PlayerConstants.PlaybackQuality.LARGE -> "large"
                    PlayerConstants.PlaybackQuality.HD720 -> "hd720"
                    PlayerConstants.PlaybackQuality.HD1080 -> "hd1080"
                    PlayerConstants.PlaybackQuality.HIGH_RES -> "highres"
                    PlayerConstants.PlaybackQuality.DEFAULT -> "default"
                    else -> "unknown" // Or handle other cases as needed
                }
                super.onPlaybackQualityChange(youTubePlayer, playbackQuality)
            }

            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
                super.onVideoId(youTubePlayer, videoId)
                if(erdialog.isShowing){
                    erdialog.dismiss()
                }
                fadeOut(bin.videoLay.errorContainer)
                db.doing {
                    val d = it.listAsList()
                    val itIs = d.map { it.videoId }.contains(videoId)
                    checkStateSave(itIs)
                    if(itIs) {
                        title = d.find { it.videoId == videoId }?.title
                    }
                }

                if(!Handler(mainLooper).post { bin.videoview.loadUrl("javascript:sendVideoQuality()") }){
                    Log.e("Quality", "not found")
                }

            }
            override fun onReady(youTubePlayer: YouTubePlayer) {


                l = youTubePlayer

                isReadyToChange = true
                youTubePlayer.addListener(track)
                youTubePlayer.addListener(fade)
                if(savedInstanceState != null){
                    if(!savedInstanceState.getBoolean("play")){
                        youTubePlayer.cueVideo(video_id, savedInstanceState.getFloat("time"))
                    }else{
                        youTubePlayer.loadVideo(video_id, savedInstanceState.getFloat("time"))
                    }
                }
                youTubePlayer.loadVideo(video_id, savedInstanceState?.getFloat("time") ?: now)
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                now = second
                try {
                    savedInstanceState!!.putFloat("time", second)
                } catch (_: NullPointerException) {
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ms.setPlaybackState(getPlayBackState())
                }
                setProgress(second.toInt())
                if (!isSeeking) {
                    bin.videoLay.seekbarStock.progress = second.toInt()
                }



                super.onCurrentSecond(youTubePlayer, second)
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                max = duration

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ms.setMetadata(
                        MediaMetadata.Builder()
                            .putLong(MediaMetadata.METADATA_KEY_DURATION, duration.toInt().toLong() * 1000L)
                            .build()
                    )
                }
                bin.videoLay.seekbarStock.setMax(duration.toInt())
                super.onVideoDuration(youTubePlayer, duration)
            }
            
            

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerState,
            ) {
                val r = fun(){
                    val v = bin.videoLay.replayCo
                    if(v.visibility != View.GONE){
                        v.alpha = 1f
                        v.translationY = 0F
                        v.animate().translationY(-700F).alpha(0f).withEndAction {
                                v.visibility = View.GONE
                        }
                    }

                    fade.isDisabled = false
                    val vd = bin.videoLay.duraction
                    if(vd.visibility != View.VISIBLE) {
                        vd.alpha = 0f
                        vd.visibility = View.VISIBLE
                        val t = 1F
                        vd.animate()
                            .alpha(t).scaleX(t).scaleY(t)
                    }
                    bin.videoLay.tul.animate().translationY(0F)
                    val te = bin.videoLay.theEnd
                    if(te.visibility == View.VISIBLE) {
                        val t = 0F
                        te.alpha = 1f
                        te.animate().alpha(t).scaleX(t).scaleY(t).withEndAction {
                            te.visibility = View.GONE
                        }
                    }

                    bin.videoLay.root.background = null
                }
                changeABI(state)
                play = when (state) {
                    PlayerState.PLAYING -> {
                        r()
                        true
                    }

                    PlayerState.ENDED -> {
                        val d = intent.getParcelableArrayExtra("playlist")
                        if (d?.size?.minus(1)  != p) tryOn{
                            val v = bin.videoLay.replayCo
                            v.alpha = 0f
                            v.translationY = -700F
                            v.visibility = View.VISIBLE

                            val note = d?.get(p + 1) as Video
                            val d = windowManager.defaultDisplay
                            Picasso.get().load("https://i.ytimg.com/vi/${note.videoId}/hqdefault.jpg").error(ColorDrawable(Color.BLACK)).into(object : Target{

                                override fun onBitmapLoaded(
                                    bitmap: Bitmap?,
                                    from: Picasso.LoadedFrom?
                                ) {
                                    (bin.videoLay.imageView4).setImageBitmap(bitmap)
                                }

                                override fun onBitmapFailed(
                                    e: java.lang.Exception?,
                                    errorDrawable: Drawable?
                                ) {

                                }

                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                    (bin.videoLay.imageView4).setImageDrawable(placeHolderDrawable)
                                }
                            })
                            v.animate().translationY(0F).alpha(1f)
                            val t = 0F

                            bin.videoLay.ntitle.text = note.title
                            fade.fade(1F)
                            bin.videoLay.tul.animate().translationY(-bin.videoLay.tul.height.toFloat())
                            fade.isDisabled = true
                            val vd = bin.videoLay.duraction
                            if(vd.visibility == View.VISIBLE) {
                                vd.alpha = 1f
                                vd.animate().alpha(t).scaleX(t).scaleY(t).withEndAction {
                                        vd.visibility = View.GONE
                                }
                            }

                        }
                        else{
                            val vd = bin.videoLay.theEnd
                            if(vd.visibility != View.VISIBLE) {
                                vd.alpha = 0f
                                vd.visibility = View.VISIBLE
                                val t = 1F
                                vd.animate()
                                    .alpha(t).scaleX(t).scaleY(t)
                            }
                            bin.videoLay.root.background = ColorDrawable(resources.getColor(R.color.black))
                        }
                        false
                    }

                    else -> {
                        r()
                        false
                    }
                }
                ms.isActive = play
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ms.setPlaybackState(getPlayBackState())
                }
                val normalCursor = Runnable{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        window.decorView.pointerIcon = PointerIcon.getSystemIcon(this@VideoView,
                            PointerIcon.TYPE_ARROW)
                    }
                }

                bin.videoLay.loadingVideo.visibility = View.GONE
                if (state == PlayerState.PLAYING) {
                    normalCursor.run()
                    fadeOut(bin.videoLay.errorContainer)
                    bin.videoLay.pausePlay.setImageResource(R.drawable.pause)
                    bin.videoLay.pausePlay.setContentDescription(
                        bin.videoLay.pausePlay.getContext().getString(R.string.pause)
                    )
                    bin.videoLay.pausePlay.also {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            it.tooltipText = bin.videoLay.pausePlay.getContext().getString(R.string.pause)
                        }
                    }

                } else if (state == PlayerState.ENDED) {
                    normalCursor.run()
                    if (!isLoop) {
                        bin.videoLay.pausePlay.setImageResource(R.drawable.replay)
                        bin.videoLay.pausePlay.setContentDescription(
                            bin.videoLay.pausePlay.getContext().getString(R.string.play)
                        )

                        bin.videoLay.pausePlay.also {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                it.tooltipText = getString(R.string.play)
                            }
                        }

                    } else {
                        l.seekTo(0f)
                    }
                } else if (state == PlayerState.PAUSED) {
                    normalCursor.run()
                    bin.videoLay.pausePlay.setImageResource(R.drawable.play)
                    bin.videoLay.pausePlay.setContentDescription(
                        bin.videoLay.pausePlay.getContext().getString(R.string.play)
                    )

                    bin.videoLay.pausePlay.also {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            it.tooltipText = getString(R.string.play)
                        }
                    }
                } else if (state == PlayerState.BUFFERING) {
                    bin.videoLay.loadingVideo.visibility = View.VISIBLE
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        window.decorView.pointerIcon = PointerIcon.getSystemIcon(this@VideoView,
                            PointerIcon.TYPE_WAIT)
                    }
                }
                playercontrols(play)
                loadPIPR()
                if (isFullscreen) {
                    window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LOW_PROFILE or
                                View.SYSTEM_UI_FLAG_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                (View.SYSTEM_UI_FLAG_FULLSCREEN) or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_IMMERSIVE
                }

                super.onStateChange(youTubePlayer, state)
            }
        }
        bin.videoview.setOnLongClickListener{
            fade.toggleVisibility()
            true
        }
        val changeTo = fun(position:Int,dol: Runnable){
            if(!isReadyToChange)return
            val d = intent.getParcelableArrayExtra("playlist")
            try {
                val note = d?.get(position) as Video
                p = position
                dol.run()
                craftVideo(note)
            } catch (e: Exception) {
                Log.e("Yt", "No more videos")
            }
            refreshList()
        }
        with(bin.videoLay) {

            val p  = View.OnClickListener{
                if (track.state == PlayerState.PLAYING) {
                    l.pause()
                    it.setContentDescription(getString(R.string.play))
                } else {
                    l.play()
                    it.setContentDescription(getString(R.string.pause))
                }
            }
            if(sp.getBoolean("showControllerSecondaryCenter", false)){
                controlStub.also { s ->
                    s.layoutResource = R.layout.controler_vid
                    s.setOnInflateListener { _, v ->
                        v.findViewById<View>(R.id.player_control_rewind)?.setOnClickListener {
                            now = now - skipDuraction
                            l.seekTo(now)
                        }
                        v.findViewById<View>(R.id.player_control_forward)?.setOnClickListener {
                            now = now + skipDuraction
                            l.seekTo(now)
                        }

                        v.findViewById<ImageView>(R.id.player_control_pp)?.also{
                            it.setOnClickListener(p)
                            changeABI = fun(p:PlayerState){
                                when(p){
                                    PlayerState.PLAYING -> {
                                        it.setImageResource(R.drawable.pause)
                                        it.setContentDescription(
                                            bin.videoLay.pausePlay.getContext().getString(R.string.pause)
                                        )
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            it.tooltipText = bin.videoLay.pausePlay.getContext().getString(R.string.pause)
                                        }

                                    }
                                    PlayerState.PAUSED -> {
                                        it.setImageResource(R.drawable.play)
                                        it.setContentDescription(
                                            getString(R.string.play)
                                        )
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            it.tooltipText = getString(R.string.play)
                                        }
                                    }
                                    PlayerState.ENDED -> {
                                        it.setImageResource(R.drawable.replay)
                                        it.setContentDescription(
                                            getString(R.string.play)
                                        )
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            it.tooltipText = getString(R.string.play)
                                        }

                                    }
                                    else -> Unit
                                }
                            }
                        }
                    }

                    s.inflate()
                }
            }
            potraitFullscreen.visibility = View.VISIBLE
            pausePlay.setOnClickListener(p)
            toggleFullScreen.setOnClickListener {
                if(!lockPot){
                    if (isInFullscreen) {
                        exitFullscreen()
                    } else {
                        enterFullscreen()
                    }
                    isInFullscreen = !isInFullscreen
                }else{
                    exitFullscreen()
                    isInFullscreen = false
                }

            }
            potraitFullscreen.setOnClickListener {
                showDialog(VF_DIALOG)
            }
            val o = object : DabelClick() {
                override fun onDoubleClick(v: View?) {
                    pausePlay.callOnClick()
                }

                override fun onOneClick(v: View?) {
                    fade.toggleVisibility()
                }
            }

            bin.fadet.setOnClickListener(o)
            with(root) {
                setOnClickListener(o)
            }

            with(container) {
                setOnClickListener(o)
                setOnLongClickListener {
                    pausePlay.callOnClick()
                    true
                }
            }
            with(tul){
                setOnClickListener {
                    fade.toggleVisibility()
                }
                setOnLongClickListener{
                    true
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setOnContextClickListener{
                        true
                    }
                }
            }
            val sk = object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    duraction.setText(
                        formatTime(progress.toFloat()) + " / " + formatTime(track.videoDuration)
                    )

                    fade.isDisabled = fromUser
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    isSeeking = true
                    bin.videoLay.duraction.animate().scaleX(1.5F).scaleY(1.5F).translationX(til.pxToDp(25F))
                    fade.fade(1F)
                    duraction.setText(
                        formatTime(seekBar.progress.toFloat()) + " / " + formatTime(
                            track.videoDuration
                        )
                    )
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    isSeeking = false
                    bin.videoLay.duraction.animate().scaleX(1F).scaleY(1F).translationX(0F)
                    l.seekTo(seekBar.progress.toFloat())
                    duraction.text =
                        formatTime(seekBar.progress.toFloat()) + " / " + formatTime(track.videoDuration)

                }

            }
            playlistBtn.setOnClickListener {
                val c = PopupMenu(it.context,it)
                intent.getParcelableArrayExtra("playlist")?.forEachIndexed { i,v->
                    if(v is Video){
                        c.menu.add(0,i,i, v.title).setCheckable(true).setChecked(v == noted).setOnMenuItemClickListener {im->
                            changeTo(im.order){}
                            true
                        }
                    }
                }
                c.show()
            }
            seekbarStock.setOnSeekBarChangeListener(sk)
        }
        val options: IFramePlayerOptions = IFramePlayerOptions.Builder().controls(0).ivLoadPolicy(3).ccLoadPolicy(0).origin("https://google").build()
        val s = Point()

        windowManager.defaultDisplay.getSize(s)
        if (!resources.getBoolean(R.bool.tablet)) {
            if (s.x > s.y) {
                isInFullscreen = true
                enterFullscreen()
            }
        }
        bin.videoview.also{t->
            t.isFocusable = false
            t.isFocusableInTouchMode = false
            t.isClickable = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                t.setAllowClickWhenDisabled(false)
            }
            //change user agent
            t.qualityListener = fun(l){
                qualityList = l
                Log.i("Quality","Quality found : ${l.size}")
            }
            t.settings.javaScriptEnabled = true
            t.initialize({
                it.addListener(ll)
            }, options)
        }
        bin.videoLay.playerControlForward.dabelClick {
            if (it) {
                now = now + skipDuraction
                l.seekTo(now)
            }
        }


        bin.videoLay.playerControlRewind.dabelClick {
            if (it) {
                now = now - skipDuraction
                l.seekTo(now)
            }
        }
        nList()
        bin.playlist.apply{
            adapter = playlist

            choiceMode = AbsListView.CHOICE_MODE_SINGLE;
            tryOn{
                setItemChecked(p, true)
            }


            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                changeTo(position){
                    clearChoices()
                    setItemChecked(position, true)
                }
            }
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    changeTo(position){}
                }
            }
        }

        Yt.onPip = {
            if(it){
                l.pause()
                tryOn{
                    notifMan.cancel(1)
                }
            }else{
                l.seekTo(now)
                l.play()
            }
        }

        initVolDia()

    }

    val skipDuraction get() = sp.getInt("skipdu", 10F.toInt()).toFloat()



    private fun cekCenel() {
        val de = intent.getParcelableArrayExtra("cplaylist")
        val p = intent.getParcelableArrayExtra("playlist")?.map { it as Video }

        if(!p.isNullOrEmpty()){
            de?.get(this.p)?.let {
                if (it !is ChannelDetail) return
                Runnable{
                    try {
                        val m = bin.videoLay.tul.menu
                        actionBar?.setSubtitle(it.title)
                        val item =
                            m.findItem(R.id.cenel_open) ?: m.add(
                                0,
                                R.id.cenel_open,
                                1,
                                it.title
                            )
                                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
                        item.isVisible = true
                        item.title = (getString(R.string.open) + " " + it.title)
                        item.intent =
                            Intent(this, ChannelInfo::class.java).putExtra("id", it.id)
                    } catch (e: Exception) {
                        info(e.message.orEmpty())
                    }
                }.let{
                    try{
                        Handler(mainLooper).postDelayed(it, 800L)
                    }catch (_: Exception){
                        it.run()
                    }
                }

            }
        }
    }



    private fun updateView() {


        val typedValue =  TypedValue();
        val sti =  TypedValue();
        val stm =  TypedValue();
        getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)
        getTheme().resolveAttribute(android.R.attr.textAppearanceSmall, sti, true)
        getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, stm, true)
        bin.videoLay.tul.setSubtitleTextAppearance(this, sti.data)
        bin.videoLay.tul.setTitleTextAppearance(this, stm.data)
        bin.videoLay.tul.layoutParams.height = TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration?) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)

    }



    override fun onKeyShortcut(keyCode: Int, event: KeyEvent?): Boolean {
        if(event?.isCtrlPressed == true){
            if (keyCode == KeyEvent.KEYCODE_W) {
                finishAndGoToMain()
                return true
            }
        }
        return super.onKeyShortcut(keyCode, event)
    }


    private fun lockNFull(isIt:Boolean){
        bin.videoLay.toggleFullScreen.visibility = when (isIt) {
            true -> View.GONE
            else -> View.VISIBLE
        }
        if(isIt){
            if (!isTablet) {
                isInFullscreen = true
                enterFullscreen()
            }
        }
    }

    fun nList(){
        print(intent.getParcelableArrayExtra("playlist")?.joinToString(separator = "\n"))
        intent.getParcelableArrayExtra("playlist")?.forEach {
            val text : String = when(it){
                is Video -> it.title
                else -> it.toString()
            }

            playlist.add(text)
        }
    }



    private fun randomDur() {
        try {
            l.seekTo(Random.nextInt(0, max.toInt()).toFloat())
        } catch (e: Exception) {
            snackBar("Controller not ready")
        }
    }
    val isTablet get() = resources.getBoolean(R.bool.tablet)

    fun checkStateSave(isSave:Boolean = false){
        val d=  if (isSave) R.drawable.delete else R.drawable.add
        invalidateOptionsMenu()
        bin.manage.apply{
            setCompoundDrawablesWithIntrinsicBounds(
                d,
                0,
                0,
                0
            )
            text = getString(if (isSave) R.string.del else R.string.add)
        }
    }

    val setupThumb : Unit get() {
        val transColor = sp.getBoolean("transColor", false)
        tryOn{
            val d = windowManager.defaultDisplay
            Picasso.get()
                .load("https://i.ytimg.com/vi/${video_id}/hqdefault.jpg")
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.unhappy)
                .priority(Picasso.Priority.HIGH)
                .into(object : Target {
                    override fun onBitmapLoaded(
                        resource: Bitmap?,
                        from: Picasso.LoadedFrom?
                    ) {
                        thumb = resource
                        setTaskDescription(ActivityManager.TaskDescription(noted?.title, resource, window.statusBarColor))
                    }

                    override fun onBitmapFailed(
                        e: java.lang.Exception?,
                        errorDrawable: Drawable?
                    ) {
                        info("Failed to load thumbnail")
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

                    }


                })
        }
    }

    override fun onStart() {
        super.onStart()
        setupThumb

    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun blubblub(){
        // Create a bubble intent.

        val bubbleIntent = PendingIntent.getActivity(this, 0, Intent(this, ActivityDialog::class.java).addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE)



// Create a sharing shortcut.


// Create a bubble metadata.
        val bubbleData = Notification.BubbleMetadata.Builder(bubbleIntent,
            Icon.createWithResource(this, R.drawable.video))
            .setDesiredHeight(600)
            .setSuppressNotification(true)
            .setAutoExpandBubble(true)
            .build()
        val chatPartner: Person = Person.Builder()
            .setName("Chat partner")
            .setImportant(true)
            .build()
        val shortcut =
            ShortcutInfo.Builder(this, "vid")
                .setCategories(setOf("vid"))
                .setIntent(Intent(Intent.ACTION_DEFAULT))
                .setLongLived(true)
                .setShortLabel("vid")
                .build()

// Create a notification, referencing the sharing shortcut.
        val builder = Notification.Builder(this, "1000")
            .setContentIntent(bubbleIntent)
            .setSmallIcon(R.drawable.video)
            .setBubbleMetadata(bubbleData)
            .setShortcutId("vid")
            .setStyle(Notification.MessagingStyle(chatPartner).setConversationTitle("Chat partner").apply {
                messages?.add(Notification.MessagingStyle.Message("mia", 12L, chatPartner))
            })
            .addPerson(chatPartner)
        val c =  NotificationChannel("1000", "Video", NotificationManager.IMPORTANCE_HIGH)
        c.description = "Wd";
        c.setAllowBubbles(true);

        notifMan.createNotificationChannel(c)
        builder.setChannelId("1000")
        notifMan.notify(2, builder.build())
    }


    private fun snackBar(t: String) {
        info(t)
    }

    inner class WatchOnThis() : Binder(){
        fun switchToVid(intent: Intent){
            this@VideoView.getAll(intent)
        }
    }

    companion object{

        const val  PLAY_ACTION = "pauseBtn"
        const val PAUSE_ACTION = "playBtn"
        const val REW_ACTION = "rewBtn"
        const val FOR_ACTION = "forBtn"
        const val VOLUME_DIALOG = 1
        const val QR_DIALOG = 2
        const val ADD_DIALOG = 3
        const val DEL_DIALOG = 4
        const val VF_DIALOG = 5
        const val QV_DIALOG = 6
        const val DESC_DIALOG = 7
        const val SHARE_OPTION_DIALOG = 8
    }

    fun View.fadeSize(much:Float){
        animate().scaleX(much).scaleY(much).alpha(much)
    }

    fun initVolDia(){
        val onSeek:((Int, ImageButton) -> Unit) = {i, b->
            if(i == 0){
                b.setImageDrawable(getDrawable(R.drawable.volume_off))
            }else{
                b.setImageDrawable(getDrawable(R.drawable.volume))
            }
        }
        volumePlayer.volumeseek.apply {
            max = (100)
            progress = plavol
            onSeek(progress, volumePlayer.icon)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    plavol = progress.toInt()
                    l.setVolume(plavol)
                    onSeek(progress, volumePlayer.icon)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    volumePlayer.icon.animate().scaleX(1.5F).scaleY(1.5F)
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    volumePlayer.icon.animate().scaleX(1F).scaleY(1F)
                }

            })
        }
        volumePhone.info.text = "My Phone"
        volumePlayer.info.text = "Player"
        volumePhone.volumeseek.apply {
            max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            onSeek(progress, volumePhone.icon)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress,0)
                    onSeek(progress, volumePhone.icon)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    volumePhone.icon.animate().scaleX(1.5F).scaleY(1.5F)
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    volumePhone.icon.animate().scaleX(1F).scaleY(1F)
                }

            })

        }
        val frame = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(volumePlayer.root)
            addView(volumePhone.root)
        }
        volView.addView(frame)
    }

    override fun onCreateDialog(id: Int, b: Bundle?): Dialog? {
        return when(id){
            QR_DIALOG -> {
                QRCodePage(this@VideoView, b?.getString("txt")?: return null).apply {

                    if(!lockPot){
                        window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                        if (!isInFullscreen) {
                            window!!.attributes?.height = bin.swipe.height
                            window!!.attributes?.width = bin.swipe.width
                            window!!.attributes?.x = bin.swipe.x.toInt()
                            window!!.attributes?.gravity = Gravity.BOTTOM
                            window!!.setWindowAnimations(android.R.style.Animation_InputMethod)
                        } else {
                            val s = Point()
                            windowManager.defaultDisplay.getSize(s)
                            val d = this@VideoView.windowManager.defaultDisplay
                            window!!.setWindowAnimations(android.R.style.Animation_Translucent)
                            window!!.attributes?.height =
                                ViewGroup.LayoutParams.MATCH_PARENT
                            window!!.attributes?.width = d.height
                            window!!.attributes?.gravity = Gravity.END
                        }
                    }
                }
            }
            else -> onCreateDialog(id)
        }
    }

    override fun onCreateDialog(id: Int): Dialog? {
        return when(id){
            SHARE_OPTION_DIALOG->{
                val i = ArrayAdapter(this, android.R.layout.simple_list_item_1,arrayOf("Video Link","Video Link using QR", "Video Thumbnail"))
                AlertDialog.Builder(this@VideoView).apply {
                    setTitle(R.string.share)
                    setIcon(R.drawable.share)
                    setOnDismissListener {
                        removeDialog(id)
                    }
                    setSingleChoiceItems(i, -1) { d, index ->
                        d.dismiss()
                        when(index){
                            0 ->{
                                val d = windowManager.defaultDisplay
                                Picasso.get()
                                    .load("https://i.ytimg.com/vi/${video_id}/hqdefault.jpg")
                                    .error(R.drawable.unhappy)
                                    .into(object : Target {
                                        override fun onBitmapLoaded(
                                            bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                                        ) {
                                            if(bitmap == null){
                                                info("Failed to share thumbnail")
                                                return
                                            }
                                            shareImageandText(bitmap)
                                        }

                                        override fun onBitmapFailed(
                                            e: java.lang.Exception?,
                                            errorDrawable: Drawable?
                                        ) {
                                            info("Failed to share thumbnail")
                                        }

                                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

                                        }


                                    })
                            }
                            1 ->{
                                runOnUiThread{
                                    Bundle().also {b->
                                        b.putString("txt", video_id)
                                        showDialog(QR_DIALOG,b)
                                    }
                                }
                            }
                            2 ->{
                                if (video_id.isNotEmpty()) {
                                    val shareIntent = Intent()
                                    val stxt = "youtube.com/watch?v=${video_id}"
                                    shareIntent.action = Intent.ACTION_SEND
                                    shareIntent.type = "text/plain"
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, stxt);
                                    startActivity(Intent.createChooser(shareIntent, "Share Via"))
                                }
                            }
                        }
                    }


                }.create()
            }
            200 -> {
                val t = Toolbar(this@VideoView)
                t.setTitle("Zoom Video")
                t.setNavigationIcon(R.drawable.close)

                AlertDialog.Builder(this@VideoView, if(!lockPot) this@VideoView.getThemeId() else 0).apply {
                    setCustomTitle(t)
                    val b = ZoomBinding.inflate(layoutInflater)
                    b.x.setOnZoomInClickListener {_->
                        bin.videoview.let{ y->
                            y.scaleX = (y.scaleX + 0.1F)
                        }
                    }
                    b.x.setOnZoomOutClickListener {_->
                        bin.videoview.let{ y->
                            y.scaleX = (y.scaleX - 0.1F)
                        }
                    }
                    b.y.setOnZoomInClickListener {_->
                        bin.videoview.let{ y->
                            y.scaleY = (y.scaleY + 0.1F)
                        }
                    }
                    b.y.setOnZoomOutClickListener {_->
                        bin.videoview.let{ y->
                            y.scaleY = (y.scaleY - 0.1F)
                        }
                    }
                    b.xy.setOnZoomInClickListener {_->
                        bin.videoview.let{ y->
                            if(y.scaleY != y.scaleX){
                                y.scaleY = y.scaleX
                            }
                            if(y.scaleX != y.scaleY){
                                y.scaleX = y.scaleY
                            }
                            y.scaleY = (y.scaleY + 0.1F)
                            y.scaleX = (y.scaleX + 0.1F)
                        }
                    }
                    b.xy.setOnZoomOutClickListener {_->
                        bin.videoview.let{ y->
                            if(y.scaleY != y.scaleX){
                                y.scaleY = y.scaleX
                            }
                            if(y.scaleX != y.scaleY){
                                y.scaleX = y.scaleY
                            }
                            y.scaleY = (y.scaleY - 0.1F)
                            y.scaleX = (y.scaleX - 0.1F)
                        }
                    }
                    b.px.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            bin.videoview.translationX = (progress - 1000).toFloat()
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {

                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {

                        }

                    })
                    b.py.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            bin.videoview.translationY = (progress - 1000).toFloat()
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {

                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {

                        }

                    })
                    b.root.releaseParent()
                    setView(b.root)
                    setTitle("Zoom Video")
                    setPositiveButton(R.string.close, null)
                    setNeutralButton("Reset") { _,_->
                        bin.videoview.animate().scaleX(1F).scaleY(1F).translationX(0F).translationY(0F)
                    }

                }.create().apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            window!!.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                        }else{
                            window!!.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        }
                    }
                    t.setNavigationOnClickListener {
                        dismiss()
                    }
                    if(!lockPot){
                        window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                        if (!isInFullscreen) {
                            window!!.setWindowAnimations(android.R.style.Animation_InputMethod)
                            window!!.attributes?.height = bin.swipe.height
                            window!!.attributes?.width = bin.swipe.width
                            window!!.attributes?.x = bin.swipe.x.toInt()
                            window!!.attributes?.gravity = Gravity.BOTTOM
                        } else {
                            val s = Point()
                            window!!.setWindowAnimations(android.R.style.Animation_Translucent)
                            windowManager.defaultDisplay.getSize(s)
                            val d = this@VideoView.windowManager.defaultDisplay
                            window!!.attributes?.height =
                                ViewGroup.LayoutParams.MATCH_PARENT
                            window!!.attributes?.width = d.height
                            window!!.attributes?.gravity = Gravity.END
                        }
                    }
                }
            }
            QV_DIALOG -> {
                AlertDialog.Builder(this@VideoView, if(!lockPot) this@VideoView.getThemeId() else 0).apply {
                    setTitle("Quality Video")
                    if(qualityList.isNotEmpty()){
                        setSingleChoiceItems(qualityList.toTypedArray(), -1) { _, index ->
                            val mainThreadHandler = Handler(mainLooper)
                            try {
                                val i = qualityList[index]
                                if (!mainThreadHandler.post { bin.videoview.loadUrl("javascript:setPlaybackQuality(\"$i\")") }) {
                                    info("Quality not set")
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                info("Quality not set")
                            }
                        }
                    }
                    else{
                        setMessage("Opps...\nTry again")
                        setPositiveButton("Fine", null)
                    }


                }.create().apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            window!!.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                        }else{
                            window!!.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        }
                    }
                    if(!lockPot){
                        window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                        if (!isInFullscreen) {
                            window!!.setWindowAnimations(android.R.style.Animation_InputMethod)
                            window!!.attributes?.height = bin.swipe.height
                            window!!.attributes?.width = bin.swipe.width
                            window!!.attributes?.x = bin.swipe.x.toInt()
                            window!!.attributes?.gravity = Gravity.BOTTOM
                        } else {
                            val s = Point()
                            window!!.setWindowAnimations(android.R.style.Animation_Translucent)
                            windowManager.defaultDisplay.getSize(s)
                            val d = this@VideoView.windowManager.defaultDisplay
                            window!!.attributes?.height =
                                ViewGroup.LayoutParams.MATCH_PARENT
                            window!!.attributes?.width = d.height
                            window!!.attributes?.gravity = Gravity.END
                        }
                    }
                }
            }
            VOLUME_DIALOG -> {
                object : AlertDialog(this@VideoView, this.getThemeId()){
                    init {
                        val t = Toolbar(this@VideoView)
                        t.setTitle("Volume")
                        t.setNavigationIcon(R.drawable.close)
                        t.setNavigationOnClickListener {
                            dismiss()
                        }
                        setCustomTitle(t)
                        volView.releaseParent()
                        setView(volView)
                        if(!lockPot){
                            window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                            if (!isInFullscreen) {
                                window?.setWindowAnimations(android.R.style.Animation_InputMethod)
                                window!!.attributes?.height = bin.swipe.height
                                window!!.attributes?.width = bin.swipe.width
                                window!!.attributes?.x = bin.swipe.x.toInt()
                                window!!.attributes?.gravity = Gravity.BOTTOM
                            } else {
                                val s = Point()
                                windowManager.defaultDisplay.getSize(s)
                                window?.setWindowAnimations(android.R.style.Animation_Translucent)
                                val d = this@VideoView.windowManager.defaultDisplay
                                window!!.attributes?.height = ViewGroup.LayoutParams.MATCH_PARENT
                                window!!.attributes?.width = d.height
                                window!!.attributes?.gravity = Gravity.END
                            }
                        }

                    }

                    override fun show() {
                        this.setCanceledOnTouchOutside(true)
                        super.show()
                    }


                    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
                        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
                            volumePhone.volumeseek.progress -= 10
                            return true
                        }
                        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
                            volumePhone.volumeseek.progress += 10
                            return true
                        }
                        return super.onKeyDown(keyCode, event)
                    }
                }
            }
            DESC_DIALOG -> D()
            ADD_DIALOG -> {
                NewVidDialog.new(this, video_id,){
                    db.doing {
                        val n =
                            it.listAsList().last()
                        noteId = n.id
                        p = it.listAsList().size - 1
                        Yt.apply {
                            pos = p
                            link = n.id
                        }
                        noted = n

                        this.title = n.title
                        setTitle(n.title)

                        snackBar("Added")

                        checkStateSave(true)
                        playlist.clear()
                        intent.getParcelableArrayExtra("playlist")?.forEach {
                            val text : String = when(it){
                                is Video -> it.title
                                else -> it.toString()
                            }

                            playlist.add(text)
                        }
                        playlist.notifyDataSetChanged()

                    }
                }.also {
                    it.bin.editTextText4.isEnabled = false
                    if(!lockPot){
                        it.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        it.window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                        if (!isInFullscreen) {
                            it.window!!.attributes?.height = bin.swipe.height
                            it.window!!.attributes?.width = bin.swipe.width
                            it.window!!.attributes?.x = bin.swipe.x.toInt()
                            it.window!!.attributes?.gravity = Gravity.BOTTOM
                            it.window?.setWindowAnimations(android.R.style.Animation_InputMethod)
                        } else {
                            val s = Point()
                            windowManager.defaultDisplay.getSize(s)
                            val d = this@VideoView.windowManager.defaultDisplay
                            it.window!!.attributes?.height = ViewGroup.LayoutParams.MATCH_PARENT
                            it.window!!.attributes?.width = d.height
                            it.window!!.attributes?.gravity = Gravity.END
                            it.window?.setWindowAnimations(android.R.style.Animation_Translucent)
                        }
                    }
                }
            }
            DEL_DIALOG -> {
                android.app.AlertDialog.Builder(this)
                    .setMessage(R.string.del_com)
                    .setNegativeButton(android.R.string.cancel,null)
                    .setPositiveButton(getString(R.string.del)){ _, _->
                        db.doing {
                            db.deleteVideoByVideoId(video_id)

                        }
                        snackBar("Video deleted!")
                        Yt.link = -1
                        Yt.pos = 1
                        checkStateSave(false)
                    }.create()
            }
            VF_DIALOG -> {
                val t = Toolbar(this@VideoView)
                AlertDialog.Builder(this@VideoView, if(!lockPot) this@VideoView.getThemeId() else 0).apply {
                    t.setTitle(R.string.vefu)
                    t.setNavigationIcon(R.drawable.back)
                    setCustomTitle(t)
                    setMessage(R.string.vefume)
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(R.string.enter) { _, _ ->
                        enterFullscreen(true)
                    }
                }.create().apply{
                    t.setNavigationOnClickListener {
                        dismiss()
                    }
                    if(!lockPot){
                        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        window?.setWindowAnimations(android.R.style.Animation_InputMethod)
                        window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                        if (!isInFullscreen) {
                            window!!.attributes?.height = bin.straing.height + bin.vidlay.paddingTop
                            window!!.attributes?.width = bin.straing.width
                            window!!.attributes?.x = bin.straing.x.toInt()
                            window!!.attributes?.gravity = Gravity.TOP
                        } else {
                            val s = Point()
                            windowManager.defaultDisplay.getSize(s)
                            val d = this@VideoView.windowManager.defaultDisplay
                            window!!.attributes?.height = ViewGroup.LayoutParams.MATCH_PARENT
                            window!!.attributes?.width = d.height
                            window!!.attributes?.gravity = Gravity.END
                        }
                    }
                }

            }

            else -> Dialog(this)
        }
    }

    override fun onPrepareDialog(id: Int, dialog: Dialog?) {
        val d = this@VideoView.windowManager.defaultDisplay
        val isSheet = (id == VOLUME_DIALOG) or (id == QR_DIALOG) or (id == 200) or (id == QV_DIALOG)  or (id == ADD_DIALOG)
        if(isSheet){
            dialog?.setOnDismissListener {
                dialogStack--
                if((dialogStack == 0) and isInFullscreen){
                    animatePadding(bin.straing, endPadding = 0, startPadding = d.height, right = true, duration = 300L)
                }
                removeDialog(id)
            }
        }else{
            dialog?.setOnDismissListener {
                removeDialog(id)
            }
        }
        dialog?.window?.let { configNotchWindow(it) }

        if(isSheet){
            dialog?.setOnShowListener {

                dialogStack++
                tryOn{
                    if(isInFullscreen and (dialogStack == 1)){
                        animatePadding(bin.straing, startPadding = 0, endPadding = d.height, right = true, duration = 300L)
                    }
                }
            }
        }



        super.onPrepareDialog(id, dialog)
    }


    public fun animatePadding(
        view: View,
        startPadding: Int = 0,
        endPadding: Int = 0,
        duration: Long = 500L,
        left: Boolean = false,
        right: Boolean = false,
        top: Boolean = false,
        bottom: Boolean = false
    ) {

        val animator = ValueAnimator.ofInt(startPadding, endPadding);
        animator.setDuration(duration);
        animator.addUpdateListener { animation ->
            val animatedValue = animation.getAnimatedValue();
            if (animatedValue is Int) {
                view.setPadding(
                    if(left)animatedValue else 0,
                    if(top)animatedValue else 0,
                    if(right)animatedValue else 0,
                    if(bottom)animatedValue else 0);
            }
        };
        animator.start();
    }

    private fun configNotchWindow(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if(isNotch) {
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }else{
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            }
        }
    }


    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return (if(isTv) {
            event?.let{
                onKeyDown(it.keyCode, it)
            }
        } else super.dispatchKeyEvent(event)) == true
    }

    override fun onStop() {
        super.onStop()

        if (mPresentation != null) {
            mPresentation?.dismiss();
            mPresentation = null;
        }
    }






    private fun setUiState(show:Boolean){

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        tryOn{
            savedInstanceState.apply {
                now = getFloat("time")
                noted = getParcelable("item")
            }
        }
    }


    fun getPlayBackState(): PlaybackState{

        return PlaybackState.Builder()
            .setState(
                if (play) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
               now.toLong() * 1000L, 1F
            )
            .setActions(PlaybackState.ACTION_SEEK_TO or PlaybackState.ACTION_SKIP_TO_NEXT or PlaybackState.ACTION_SKIP_TO_PREVIOUS or PlaybackState.ACTION_PLAY_PAUSE)
            .build()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        newCon = newConfig
        val v = false
        if(!v) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if(!resources.getBoolean(R.bool.tablet)) {
                    enterFullscreen()
                    setUiState(false)
                    isInFullscreen = true

                }
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                exitFullscreen()
                isInFullscreen = false
                setUiState(true)
            }
            updateView()
        }
        super.onConfigurationChanged(newConfig)
    }

    private val roundIntent get() = Intent(intent).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    @SuppressLint("UseLoadingForDrawables")
    private fun playercontrols(play : Boolean){
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            ms.setMetadata(
//                MediaMetadata.Builder()
//                    .putString(
//                        MediaMetadata.METADATA_KEY_DISPLAY_TITLE,
//                        noted?.title
//                    )
//                    .putBitmap(
//                        MediaMetadata.METADATA_KEY_DISPLAY_ICON,
//                        thumb
//                    ).putRating("android.media.metadata.RATING", Rating.newHeartRating(true))
//                    .build()
//            )
//        }
        val cI = "10000"
        val i = Intent(applicationContext, app.wetube.service.ControlReceiver::class.java)
        val fi = openVideoNTicket(this, noted?:return,p, intent = roundIntent)
        val a = Notification.Action(
            if(!play)R.drawable.play else R.drawable.pause, if (play)getString(R.string.pause) else getString(R.string.play),
            PendingIntent.getBroadcast(applicationContext, 2, i.putExtra("state", play),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        )

        val f = Notification.Action(
            R.drawable.close, getString(R.string.close),
            PendingIntent.getActivity(applicationContext, 3, fi.putExtra("finish", true),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        )
        nobu = Notification.Builder(this, cI)
            .setSmallIcon(R.drawable.music_play)
            .setContentTitle(title)
            .setContentText(title)
            .setAutoCancel(true)
            .setStyle(Notification.MediaStyle().setMediaSession(ms.sessionToken))
            .setLargeIcon(thumb)
            .setContentIntent(
                PendingIntent.getActivity(this, 1, roundIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            )
            .setOngoing(true)
            .addAction(a)
            .addAction(f)
            .setOnlyAlertOnce(true)

        Yt.youTubePlayer = l
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val c =  NotificationChannel(cI, "Video Control", NotificationManager.IMPORTANCE_DEFAULT)
            notifMan.createNotificationChannel(c)
            c.setShowBadge(false)
            c.description = "Control the played video"
            nobu.setChannelId(cI)
        }
        notifMan.notify(1, nobu.build())

        isnotifexist = true
    }

    override fun onNavigateUp(): Boolean {
        finishAndGoToMain()
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.title){

            getString(R.string.close) -> finishAndRemoveTask()
        }
        when(item.itemId){
            android.R.id.home -> {
                onNavigateUp()
            }
            R.id.share_ic -> showDialog(SHARE_OPTION_DIALOG)
            R.id.thumbnail ->{
                val d = windowManager.defaultDisplay
                Picasso.get()
                    .load("https://i.ytimg.com/vi/${video_id}/hqdefault.jpg")
                    .error(R.drawable.unhappy)
                    .into(object : Target {
                        override fun onBitmapLoaded(
                            bitmap: Bitmap?,
                            from: Picasso.LoadedFrom?
                        ) {
                            if(bitmap == null){
                                info("Failed to share thumbnail")
                                return
                            }
                            shareImageandText(bitmap)
                        }

                        override fun onBitmapFailed(
                            e: java.lang.Exception?,
                            errorDrawable: Drawable?
                        ) {
                            info("Failed to share thumbnail")
                        }

                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

                        }


                    })
            }
            R.id.qr ->{
                runOnUiThread{
                    Bundle().apply {
                        val b = this
                        putString("txt", video_id)
                        showDialog(QR_DIALOG,b)
                    }
                }
            }

            R.id.loop -> {
                val se = sp.edit()
                val c =  !sp.getBoolean("loop", false)
                se.putBoolean("loop", c).apply()
                isLoop = c.apply {
                    item.isChecked = this
                }
            }
            R.id.tooslow -> {
                l.setPlaybackRate(PlaybackRate.RATE_0_25)
            } R.id.slow -> {
                l.setPlaybackRate(PlaybackRate.RATE_0_5)
            }
            R.id.ns  ->{
                l.setPlaybackRate(PlaybackRate.RATE_1)
            }
            R.id.fast -> {
                l.setPlaybackRate(PlaybackRate.RATE_1_5)
            }
            R.id.toofast -> {
                l.setPlaybackRate(PlaybackRate.RATE_2)
            }

            R.id.shareUrl ->{
                if (video_id.isNotEmpty()) {
                    val shareIntent = Intent()
                    val stxt = "youtube.com/watch?v=${video_id}"
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_TEXT, stxt);
                    startActivity(Intent.createChooser(shareIntent, "Share Via"))
                }
            }
            R.id.del_ic -> {
                actionFile()
            }

            R.id.pip -> pip()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun actionFile() {
        val d = db.listAsList().map { it.videoId }
        if(d.contains(video_id)) {
            showDialog(DEL_DIALOG)
        }else{
            showDialog(ADD_DIALOG)
        }
    }

    fun reloadPref(){

        if(!sp.getBoolean("show_video", true)){
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }else{
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        if(sp.getBoolean("stay", false)){
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }else{
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(sp.getBoolean("keep_unlock", false))
            setTurnScreenOn(sp.getBoolean("stay", false))
        }
        isLoop = sp.getBoolean("loop", false)
        isNotch = sp.getBoolean("notch", false)

        configNotchWindow(window)

    }
    override fun onResume() {
        super.onResume()
        reloadPref()
        roter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, displeiyTvL)
        lookThisInTv()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        getAll(intent)
        if(intent.getBooleanExtra("finish", false)){
            finishAndRemoveTask()
        }
        val st = intent.getStringExtra("state")
        if(!st.isNullOrEmpty()){
            if(st == "playing"){
                l.pause()
            }else if(st == "not playing"){
                l.play()
            }
            moveTaskToBack(false)
        }
    }

    fun getAll(ie:Intent){
        val item = ie.getParcelableExtra<Parcelable>("vid")
        if(item !is Video) return
        if(item.videoId != video_id) {
            try {
                p = Yt.pos
                video_id = item.videoId
                noted = item
                l.loadVideo(item.videoId, 0f)
                title = item.title
                setTitle(item.title)
                setTaskDescription(ActivityManager.TaskDescription(item.title))
                setupThumb
            } catch (e: Exception) {
            }
            noteId = Yt.link

        }

    }
    /** PIP permission
     * (Need android oreo)
    * */
    private fun hasPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) appOps.checkOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE, android.os.Process.myUid(), packageName) == AppOpsManager.MODE_ALLOWED else false
    }
    @SuppressLint("InflateParams", "MissingInflatedId") @Suppress("SetJavaScriptEnabled")

    var plavol = 100


    private fun PIPP(): PictureInPictureParams? {
        val r = Rect()
        val I =  Intent(applicationContext, ControlReceiver::class.java)

        I.putExtra("state", play)
        val text = if(play)getString(R.string.pause) else getString(R.string.play)
            val l = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                listOf(
                    RemoteAction(Icon.createWithResource(this, R.drawable.play_pause), text, text, PendingIntent.getBroadcast(this, 1, I,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
                )
            else listOf()
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .setActions(l)
                    .build()
            }
            else null

        }


    @SuppressLint("SuspiciousIndentation")
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val pipfirst = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pipnow", false)

        if(pipfirst) {
            pip()
        }

    }

    private fun shareImageandText(bitmap: Bitmap) {
        val uri = getmageToShare(bitmap)
        val intent = Intent(Intent.ACTION_SEND)

        // putting uri of image to be shared
        intent.apply{
            putExtra(Intent.EXTRA_STREAM, uri)
            // adding text to share

            // Add subject Here
            putExtra(Intent.EXTRA_SUBJECT, "Subject Here")

            // setting type to image
            setType("image/png")

        }

        // calling startactivity() to share
        startActivity(Intent.createChooser(intent, "Share Via"))
    }
    private fun getmageToShare(bitmap: Bitmap): Uri? {
        val imagefolder = File(cacheDir, "images")
        var uri: Uri? = null
        try {
            imagefolder.mkdirs()
            val file = File(imagefolder, "shared_image.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            uri = FileProvider.getUriForFile(this, "app.wetube.image-share", file)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
        return uri
    }

    @SuppressLint("CheckResult")
    override fun onPause() {
        super.onPause()
    }

    override fun setTitle(title: CharSequence?) {
        try{
            bin.title.setText(title)
        }catch (_: java.lang.Exception){

        }
        super.setTitle(title)
    }




    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        if (isInPictureInPictureMode) {
            bin.swipe.visibility = View.GONE
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            enterFullscreen(setValue  = false, anim = false, addFlagToWin = false)
            setUiState(false)
        } else {

            window.decorView.scaleY = 0F
            val e = isFullscreen
            Handler(mainLooper).postDelayed({
                window.decorView.animate().scaleY(1F)
                if (e) {
                    //IS Fullscreen
                    enterFullscreen()
                } else {

                    exitFullscreen()
                }
                // Restore the full-screen UI.
                bin.videoLay.root.visibility = (View.VISIBLE)
                setUiState(!isFullscreen)
            } ,400L)

        }
        val e = Runnable{
            stillPip = isInPictureInPictureMode
        }
        try{
            Handler(mainLooper).postDelayed(e, 8000L)
        }catch (_:Exception){
            e.run()
        }
    }

    override fun getContextAble(): Context? {
        return this
    }

    override fun setYPlayer(yt: YouTubePlayer) {
        l = yt
    }

    override fun getYPlayer(): YouTubePlayer {
        return l
    }



    internal inner class D() : AlertDialog(this, this.getThemeId()){
        internal val d = VideoViewInfoBinding.inflate(layoutInflater)
        init {
            val t = Toolbar(this@VideoView)
            t.setTitle("Description")
            t.setNavigationIcon(R.drawable.close)
            t.setNavigationOnClickListener {
                dismiss()
            }
            setCustomTitle(t)
            setView(d.root)
            d.hint.setText(noted?.title)
            if(noted is VideoDetail){
                d.desc.setText((noted as VideoDetail).description)
                d.cenel.apply{
                    isEnabled = true
                    val c = (noted as VideoDetail).channel
                    setText(c.title)
                    setOnClickListener {
                        startActivity(Intent(this@VideoView, ChannelInfo::class.java).putExtra("id", c.id).putExtra("name", c.title))
                    }
                }
            }

            if(!lockPot){
                window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                if (!isInFullscreen) {
                    window?.setWindowAnimations(android.R.style.Animation_InputMethod)
                    window!!.attributes?.height = bin.swipe.height
                    window!!.attributes?.width = bin.swipe.width
                    window!!.attributes?.x = bin.swipe.x.toInt()
                    window!!.attributes?.gravity = Gravity.BOTTOM
                } else {
                    val s = Point()
                    windowManager.defaultDisplay.getSize(s)
                    window?.setWindowAnimations(android.R.style.Animation_Translucent)
                    val d = this@VideoView.windowManager.defaultDisplay
                    window!!.attributes?.height = ViewGroup.LayoutParams.MATCH_PARENT
                    window!!.attributes?.width = d.height
                    window!!.attributes?.gravity = Gravity.END
                }
            }

        }

        override fun show() {
            this.setCanceledOnTouchOutside(true)
            super.show()
        }
    }


}







