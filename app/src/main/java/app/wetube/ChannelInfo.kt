package app.wetube

import android.app.ActionBar
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.ActionMode
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedCallback
import app.wetube.core.setupTheme
import app.wetube.core.showBackButton
import app.wetube.core.tryOn
import app.wetube.databinding.ActivityChannelInfoBinding
import app.wetube.item.ChannelDetail
import app.wetube.manage.db.FavChaDB
import app.wetube.page.AboutChannel
import app.wetube.page.ChannelVideo
import app.wetube.page.TabAction
import app.wetube.page.dialog.PreviewImgPage
import app.wetube.page.dialog.QRCodePage

class  ChannelInfo : Activity() {




    val db by lazy{ FavChaDB(applicationContext) }
    private lateinit var binding: ActivityChannelInfoBinding
    var acmoFinBack = Any()
    var actionMode : ActionMode? = null
    var openPage = 0
    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(android.preference.PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bylock", false))
        }

    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(android.preference.PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bylock", false))
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        updateView()
        super.onConfigurationChanged(newConfig)
    }

    override fun onActionModeStarted(mode: ActionMode?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mode?.type = ActionMode.TYPE_PRIMARY
        }
        super.onActionModeStarted(mode)
        actionMode = mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(2, acmoFinBack as OnBackInvokedCallback)
        }
    }

    override fun onActionModeFinished(mode: ActionMode?) {
        super.onActionModeFinished(mode)
        actionMode = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.unregisterOnBackInvokedCallback(acmoFinBack as OnBackInvokedCallback)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelInfoBinding.inflate(layoutInflater)
        setupTheme()
        setContentView(binding.root)
        updateView()
        showBackButton()
        setTaskDescription(ActivityManager.TaskDescription(intent.getStringExtra("name")))
        actionBar?.navigationMode = ActionBar.NAVIGATION_MODE_TABS;
        binding.close.setOnClickListener {
            finish()
        }

        binding.more.setOnClickListener {g->
            PopupMenu(this@ChannelInfo, g).apply {
                onCreateOptionsMenu(menu)
                onPrepareOptionsMenu(menu)
                setOnMenuItemClickListener {
                    onOptionsItemSelected(it)
                }
            }.show()

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            acmoFinBack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                object : OnBackAnimationCallback {
                    val t get() = window.decorView
                    override fun onBackInvoked() {
                        actionMode?.finish()
                        t.animate().y(0F)
                    }

                    override fun onBackStarted(backEvent: BackEvent) {
                        if(actionMode?.type == ActionMode.TYPE_PRIMARY) {
                            t.animate().y(-300F)
                        }
                    }






                    override fun onBackCancelled() {
                        t.animate().y(0F)
                    }
                }
            } else OnBackInvokedCallback{
                actionMode?.finish()

            }
        }
        setTitle(intent.getStringExtra("name"))

        val id = intent.getStringExtra("id")
        if(id.isNullOrEmpty()){
            finish()
        }
        val trans = fragmentManager.beginTransaction()

        trans.apply{
            try{
                //onRestart to prevent multi fragment
                if (fragmentManager.getFragment(savedInstanceState, "v") == null) {
                    add(android.R.id.content, ChannelVideo.newInstance(intent), "v")
                }
                if (fragmentManager.getFragment(savedInstanceState, "i") == null) {
                    add(android.R.id.content, AboutChannel.newInstance(intent), "i")
                }

            }catch (_:Exception){
                //onCreate
                add(android.R.id.content, ChannelVideo.newInstance(intent), "v").add(android.R.id.content, AboutChannel.newInstance(intent), "i")
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            trans.commitNowAllowingStateLoss()
        }else{
            trans.commitAllowingStateLoss()
        }
        Handler(mainLooper).postDelayed({

            changePage(openPage)
        },100L)
        val r = Runnable{
            actionBar?.apply {
                val s = Point()
                windowManager.defaultDisplay.getSize(s)
                setDisplayShowTitleEnabled(s.x < s.y)
                navigationMode = ActionBar.NAVIGATION_MODE_TABS
                addTab(addTab("Videos" , 0))
                addTab(addTab(getString(R.string.about), 1))


                Runnable{
                    if (savedInstanceState != null) {
                        setSelectedNavigationItem(savedInstanceState.getInt("page"))
                    }
                }.also {
                    Handler(mainLooper).postDelayed(it, 10L)
                }

            }
        }
        Handler(mainLooper).postDelayed(r, 200L)







    }

    private fun addTab(text: String, i: Int): ActionBar.Tab? {
        return actionBar?.newTab()?.setText(text)?.setTag(i)?.setTabListener(TabAction{_,_->
            changePage(i)
        })
    }

    private fun changePage(openPage: Int) {
        val l = listOf("v", "i")
        val d = fragmentManager.beginTransaction()
        for(i in l){
            if(l.indexOf(i) == openPage) {
                d.show(fragmentManager.findFragmentByTag(i))
                this.openPage = l.indexOf(i)
            }else{
                d.hide(fragmentManager.findFragmentByTag(i))
            }
        }
        d.commitAllowingStateLoss()
    }

    private fun updateView(){
        Point().apply {
            windowManager.defaultDisplay.getSize(this)
            actionBar?.setDisplayShowTitleEnabled(x < y)
        }
    }

    override fun onCreateDialog(id: Int, args: Bundle?): Dialog? {
        return when(id){
            VideoView.QR_DIALOG ->{
                val t = args?.getString("txt") ?: return null
                QRCodePage(this, t)
            }

            316109->{
                val dinfo = args?.getString("dinfo") ?: return null
                AlertDialog.Builder(this).setMessage("Some Info").setMessage(dinfo).setPositiveButton(android.R.string.ok, null).create()
            }
            PreviewImgPage.PREVIEW_IMAGE ->{
                PreviewImgPage(this, args?.getBinder(PreviewImgPage.PREVIEW_CODE))
            }
            else -> super.onCreateDialog(id, args)
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0,16,1,"Add").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS or MenuItem.SHOW_AS_ACTION_WITH_TEXT)
        menu.add(0,27,2,"Add to blacklist")
        menu.add(0,17,3,R.string.share)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            menu.add(0,28,2,"Minimize")
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val i = ChannelDetail(intent.getStringExtra("name").toString(), intent.getStringExtra("id").toString())
        val status = if(db.listAsList().contains(i)) R.string.del else R.string.add
        menu.findItem(16) .setTitle(status)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("page",openPage)
        fragmentManager.apply {
            fragmentManager.putFragment(outState, "v", findFragmentByTag("v"))
            fragmentManager.putFragment(outState, "i", findFragmentByTag("i"))
        }
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        action(item.itemId)
        return super.onOptionsItemSelected(item)
    }


    override fun onBackPressed() {
        if(actionMode != null){
            actionMode!!.finish()
            return
        }
        finishAndGoToMain()
    }
    
    private fun action(int:Int){
        when (int) {
            android.R.id.home -> {
                finishAndGoToMain()
            }
            16 -> {
                val i = ChannelDetail(
                    intent.getStringExtra("name").toString(),
                    intent.getStringExtra("id").toString()
                )
                db.doing {
                    if (it.listAsList().map { it.id }.contains(i.id)) {
                        it.deleteChaByName(i.title)
                        Toast.makeText(this, "Deleted", Toast.LENGTH_LONG).show()
                    } else {
                        it.insert(i)
                        Toast.makeText(this, "Added", Toast.LENGTH_LONG).show()
                    }
                    tryOn{
                        invalidateOptionsMenu()
                    }
                }
            }
            17 -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                val stxt = "https://www.youtube.com/channel/${intent.getStringExtra("id").toString()}"
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, stxt)
                startActivity(Intent.createChooser(shareIntent, ""))
            }
            28 ->{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val d = windowManager.defaultDisplay
                        enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                    } else {
                        enterPictureInPictureMode()
                    }
                }
            }
            27 -> {
                val sp = PreferenceManager.getDefaultSharedPreferences(this)
                val list = (sp.getString("bc", "") ?: "").split(",").map { it.trim() }.toMutableList();
                list.apply {
                    if(list[0].isEmpty()) {
                        removeAt(0)
                    }
                    with(intent.getStringExtra("id").toString()){
                        if(!this@apply.contains(this)) {
                            add(this)
                            Toast.makeText(this@ChannelInfo, "Added to blacklist", Toast.LENGTH_LONG).show()
                        }else{
                            remove(this)
                            Toast.makeText(this@ChannelInfo, "Remove from blacklist", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                sp.edit().putString("bc", list.joinToString(separator = ",")).apply()
            }
        }
    }

    override fun onCreateDialog(id: Int): Dialog? {
        if(id == -1000){
            return Dialog(this, android.R.style.Theme) .apply{
                this.requestWindowFeature(Window.FEATURE_NO_TITLE)
                this.setContentView(TextView(context).apply {
                    setText(this@ChannelInfo.title)
                    setTextSize(50F)
                    gravity = Gravity.CENTER
                }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }

        }
        return super.onCreateDialog(id)
    }

    override fun onPrepareDialog(id: Int, dialog: Dialog?) {
        if(id == -1000){
            dialog?.window?.attributes?.apply {
                WindowManager.LayoutParams.MATCH_PARENT.let {
                    width = it
                    height = it
                }
            }
        }
        super.onPrepareDialog(id, dialog)
    }

    var lastOri = 0


    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if(isInPictureInPictureMode){
            window.decorView.alpha = 0F
        }else{
            window.decorView.alpha = 1F
        }
    }


}