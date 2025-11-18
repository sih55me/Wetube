package app.wetube

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.ActivityGroup
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.FragmentBreadCrumbs
import android.app.FragmentManager
import android.app.Presentation
import android.app.ProgressDialog
import android.app.StatusBarManager
import android.app.TaskStackBuilder
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Point
import android.media.MediaRouter
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.util.SparseArray
import android.view.ActionMode
import android.view.Display
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowManager.InvalidDisplayException
import android.view.inputmethod.BaseInputConnection
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedCallback
import androidx.annotation.RequiresApi
import app.wetube.core.FirstReview
import app.wetube.core.getThemeId
import app.wetube.core.hideBackButton
import app.wetube.core.hideKeyBoard
import app.wetube.core.info
import app.wetube.core.isTablet
import app.wetube.core.isTv
import app.wetube.core.setupTheme
import app.wetube.core.showBackButton
import app.wetube.core.toView
import app.wetube.core.tryOn
import app.wetube.databinding.ActivityMainBinding
import app.wetube.databinding.KitemBinding
import app.wetube.page.DialogPass
import app.wetube.page.FavCha
import app.wetube.page.MySavedVideo
import app.wetube.page.Search
import app.wetube.page.dialog.SearchPeople
import app.wetube.page.TabAction
import app.wetube.page.dialog.NewVidDialog
import app.wetube.page.dialog.PreviewImgPage
import app.wetube.page.dialog.QRCodePage
import app.wetube.page.dialog.SearchDetail
import app.wetube.secret.Intro
import app.wetube.set.Settings
import app.wetube.window.ActivityContainerDialog
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.Runnable


@Suppress("DEPRECATION")
class MainActivity() : ActivityGroup(false), FragmentManager.OnBackStackChangedListener {
    val sp: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    val bin by lazy { ActivityMainBinding.inflate(layoutInflater) }
    protected val ls = listOf("s","f","m")
    val stringList = listOf(R.string.explore, R.string.favcha, R.string.library)
    private var openPage=0

    private var playingActivity: SparseArray<Pair<String, Intent>> = SparseArray()
    private var askLook = true
    private var storeAm = mutableSetOf<ActionMode>()
    private val actionMode: ActionMode? get() = storeAm.last()
    val roter by lazy { getSystemService(MEDIA_ROUTER_SERVICE) as MediaRouter }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private var popBack  = Any()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private var acmoFinBack = Any()

    private inner class MenuDrawer : BaseAdapter() {
        val items = listOf<String>(
            getString(R.string.explore),
            getString(R.string.favcha),
            getString(R.string.library)
        )
        val iitems = listOf<Int>(
            (R.drawable.explore),
            (R.drawable.favcha),
            (R.drawable.saved)
        )
        override fun getCount(): Int = items.size

        override fun getItem(p0: Int): Any = items[p0]

        override fun getItemId(p0: Int): Long = 0L

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {
            var v = p1
            var i  = v?.tag
            if(i !is KitemBinding){
                i  = KitemBinding.inflate(layoutInflater)
            }
            if(v == null){
                v = i.root
                i.root.tag = i
                i.text1.setText(items[p0])
                i.icon.setImageResource(iitems[p0])
            }else{
                i.text1.setText(items[p0])
                i.icon.setImageResource(iitems[p0])
            }
            return v
        }
    }
    private val roti by lazy{ FragmentBreadCrumbs(this) }
    var mPresentation : Presentation? = null
    val f by lazy { FirstReview(this) }
    companion object {
        var tag = ""
        const val EXPLORE = "explore"
        private const val LIBRARY = "library"
        private const val HISSAV = "hissav"
        val contentTarget = (R.id.hal)
        private const val MENU_DIALOG= 1
        var isNavGraph = !true
        const val SETUP_CODE = 1234567890
        const val DIALOG_BACKSTEP = 1
        const val DIALOG_DATA_CORRUPT = -2
        const val DIALOG_NO_CONNECT = -3
        const val DIALOG_MULTI_DOC = -0
        const val DIALOG_INFO = -4
        const val DIALOG_SEARCH= -5
    }




    private val parentClick = View.OnClickListener{
        do{
            fragmentManager.popBackStackImmediate()
        } while(fragmentManager.backStackEntryCount != 0)
    }



    fun toggleVisiBility(show: Boolean) {
//        if(show){
//            expand(bin.nav)
//            bin.container.updateLayoutParams<CoordinatorLayout.LayoutParams> {
//                Utils(this@MainActivity).apply {
//                    bottomMargin =pxToDp(57F).toInt()
//                }
//            }
//        }else{
//            collapse(bin.nav)
//            bin.container.updateLayoutParams<CoordinatorLayout.LayoutParams> {
//                Utils(this@MainActivity).apply {
//                    bottomMargin = pxToDp(0F).toInt()
//
//                }
//            }
//
//        }

    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        updateView()
        super.onConfigurationChanged(newConfig)
    }



    val cuspref by lazy { getSharedPreferences("cuspref", MODE_PRIVATE) }


    fun myti() {
        if(!isTv) {
            if (cuspref.getBoolean("first", true)) {
                val b = Bundle()
                b.putBinder("lil", LilInstance())
                startActivityForResult(
                    Intent(
                        this,
                        Intro::class.java
                    ).putExtra("bun", b).addFlags( Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                ,SETUP_CODE)
            }
        }
    }

    @SuppressLint("WrongConstant")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SETUP_CODE){
            if (data?.getBooleanExtra("intro", false) == true) {
                if (resultCode == RESULT_OK) {
                    cuspref.edit().putBoolean("first", false).apply()
                    val h = Handler(mainLooper)
                    val d = ProgressDialog(this)
                    val r = java.lang.Runnable{
                        d.dismiss()
                        recreate()
                    }
                    d.apply {
                        setMessage("Restarting in 2 second....")
                        setCancelable(false)
                        setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel)){_,_->
                            h.removeCallbacks(r)
                        }
                        show()
                    }
                    h.postDelayed(r, 2000L)
                }else{
                    finish()
                }
            }


        }
    }

    @SuppressLint("PrivateApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        return super.onCreateOptionsMenu(menu)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private var dBack = Any()



    fun toggleDrawer(){
        val b = bin.drawer
        if(b.visibility == View.GONE){
            tryOn {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    onBackInvokedDispatcher.registerOnBackInvokedCallback(
                        0,
                        dBack as OnBackInvokedCallback
                    )
                }
            }
            actionBar?.setHomeAsUpIndicator(R.drawable.back)
            b.translationX = -b.width.toFloat()
            b.visibility = View.VISIBLE
            b.animate().translationX(0F)
        }else {
            tryOn {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    onBackInvokedDispatcher.unregisterOnBackInvokedCallback(

                        dBack as OnBackInvokedCallback
                    )
                }
            }
            b.translationX = 0F
            actionBar?.setHomeAsUpIndicator(R.drawable.menu)
            b.animate().translationX(-b.width.toFloat()).withEndAction {
                b.visibility = View.GONE
            }
        }
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {

            showDialog(3234, null)
        }
        when (item.title) {
            "Back to..." -> {
                showDialog(1)
            }
            "Exit" ->{
                finish()
            }
            "Restart" -> {
                val d = ProgressDialog(this).apply {
                    setMessage("Restarting...")
                    setCancelable(false)
                    show()
                }
                Handler(Looper.myLooper()!!).postDelayed({
                    d.dismiss()
                    recreate()
                }, 1000L)

            }

            getString(R.string.set) -> {
                val b = Bundle()
                b.putBinder("lil", LilInstance())
                val s  = Intent(this, Settings::class.java).putExtra("bun",b)
                startActivity(s)

            }


        }
        return super.onOptionsItemSelected(item)
    }






    private fun addTab(string: Int, icon: Int, page: Int): ActionBar.Tab? {
        val p = Point()
        windowManager.defaultDisplay.getSize(p)
        return actionBar?.newTab()?.apply {
            when{
                isTablet -> setText(string)
                (p.x > p.y) -> setText(string)
                else -> setIcon(icon)
            }
        }?.setContentDescription(string)?.setTabListener(TabAction {t,d->
            if(fragmentManager.backStackEntryCount == 0) {
                actionBar?.title = t?.contentDescription
                changePage(page)
            }
        })

    }

    @SuppressLint("ResourceType")
    fun changePage(page: Int) {
        val l = listOf("s", "f", "m")
        val d = fragmentManager.beginTransaction()

        for(i in l){
            if(l.indexOf(i) == page) {
                d.show(fragmentManager.findFragmentByTag(i))
                openPage = l.indexOf(i)
            }else{
                d.hide(fragmentManager.findFragmentByTag(i))
            }
        }
        d.setBreadCrumbTitle(page.toString())
        d.commitAllowingStateLoss()

    }

    override fun startActionMode(callback: ActionMode.Callback?, type: Int): ActionMode? {
        return super.startActionMode(callback)
    }



    override fun onActionModeStarted(mode: ActionMode?) {

        super.onActionModeStarted(mode)
        val isMashAndFloat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mode?.type == ActionMode.TYPE_FLOATING
        } else {
            false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            popBack = OnBackInvokedCallback {
                mode?.finish()
            }
            tryOn {
                onBackInvokedDispatcher.registerOnBackInvokedCallback(
                    0,
                    popBack as OnBackInvokedCallback
                )
            }
        }
        mode?.let { storeAm.add(it) }
        if(!isMashAndFloat){
            actionBar?.navigationMode = ActionBar.NAVIGATION_MODE_STANDARD
        }
    }
    override fun onActionModeFinished(mode: ActionMode?) {
        super.onActionModeFinished(mode)
        val isMashAndFloat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mode?.type == ActionMode.TYPE_FLOATING
        } else {
            false
        }
        if(!isMashAndFloat){
            mainPage()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            tryOn {
                onBackInvokedDispatcher.unregisterOnBackInvokedCallback(
                    popBack as OnBackInvokedCallback
                )
            }
        }
        onBackStackChanged()
        mode?.let { storeAm.remove(it) }

    }


    private fun hideAll(){
        val l = listOf("s", "f", "m")
        val d = fragmentManager.beginTransaction()

        for(i in l) {
            d.hide(fragmentManager.findFragmentByTag(i))
        }
        d.commitAllowingStateLoss()
    }


    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        window.toView().hideKeyBoard()

    }







    fun checkNeedFullscreen(){
        val need = sp.getBoolean("main_fullscreen", false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(need){
                window.insetsController?.hide(WindowInsets.Type.systemBars())
            } else{
                window.insetsController?.show(WindowInsets.Type.systemBars())
            }
        }else{
            val flags =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            window?.decorView?.systemUiVisibility = if(need) flags else 0
        }

    }




    var caster : MenuItem? = null
    @SuppressLint("MissingInflatedId", "RestrictedApi", "WrongConstant")
    // app:layout_behavior="com.google.android.material.appbar.AppBarLayout$ScrollingViewBehavior" as scroll-custom
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setupTheme()

        actionBar?.navigationMode = ActionBar.NAVIGATION_MODE_STANDARD
        setContentView(bin.root)

        val trans = fragmentManager.beginTransaction()


        try{
            if (savedInstanceState == null) {
                trans?.add(app.wetube.MainActivity.contentTarget, Search(), "s")
                    ?.add(app.wetube.MainActivity.contentTarget, FavCha(), "f")
                    ?.add(app.wetube.MainActivity.contentTarget, MySavedVideo(), "m")
            }
        }catch (e: Exception){
            AlertDialog.Builder(this)
                .setTitle("Page error")
                .setMessage(e.message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
        trans?.commitAllowingStateLoss()

        if(fragmentManager.backStackEntryCount != 0){
            hideAll()
        }

        myti()
        showBackButton()
        actionBar?.setHomeAsUpIndicator(R.drawable.menu)
        updateView()

        lockThem(savedInstanceState)
        if(isTv){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                onBackInvokedDispatcher.registerOnBackInvokedCallback(0){
                    onBackPressed()
                }
            }
        }

        tryOn {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                dBack = object: OnBackAnimationCallback{
                    override fun onBackInvoked() {
                        toggleDrawer()
                    }

                    override fun onBackStarted(backEvent: BackEvent) {
                        bin.drawer.animate().translationX((-bin.drawer.width/2).toFloat())
                    }

                    override fun onBackCancelled() {
                        bin.drawer.animate().translationX(0F)
                    }
                }
            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                dBack = OnBackInvokedCallback{
                    toggleDrawer()
                }
            }
        }
        isNeedAdd(intent)
        fragmentManager.addOnBackStackChangedListener(this)
        roti.setActivity(this)
        bin.listt.apply{
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_activated_1, stringList.map { getString(it) })
            choiceMode = ListView.CHOICE_MODE_SINGLE
            onItemClickListener = AdapterView.OnItemClickListener{_,_,i,_->
                changePage(i)
                toggleDrawer()
            }
            setSelection(savedInstanceState?.getInt("page")?:0)
        }




        bin.set.setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
        }
        bin.exit.setOnClickListener {
            finish()
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        isNeedAdd(intent)
    }

    private fun isNeedAdd(intent: Intent?) {
        if(intent == null){
            return
        }
        val a = intent.action
        val type = intent.type
        if (Intent.ACTION_SEND == a && type != null) {
            showDialog(VideoView.ADD_DIALOG, NewVidDialog.newB(intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty().ifEmpty { "" },{}))
        }
    }

    private fun lockThem(savedInstanceState:Bundle? = null, onYes: Runnable = Runnable{}) {
        val mustAskLook = sp.getBoolean("needOnLogin", false)
        askLook = if(mustAskLook){
            savedInstanceState?.getBoolean("askLook") != false
        }else{
            false
        }
        if(mustAskLook){
            if (askLook) {
                DialogPass.newInstance(this) {
                    if (!it) {
                        finish()
                    }else{
                        onYes.run()
                        setVisible(true)
                    }
                    askLook = it.not()
                }.let { d->
                    Handler(mainLooper).postDelayed({
                        d.show()
                        setVisible(false)
                    },80L)
                }
            }
        }else{
            onYes.run()
        }
    }

    fun remoteKeyToPresent(pressKey: Int){
        if(mPresentation == null)
            return
        remoteKey(pressKey,mPresentation!!.window!!.decorView)
    }

    fun remoteKey(pressKey: Int, c: View){
        // Simulate pressing the Enter key
        val inputConnection: BaseInputConnection = BaseInputConnection(c, true)
        inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, pressKey))
        inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, pressKey)) // Send UP event for completeness
    }

    private val fullRunner = object : Runnable {
        @SuppressLint("DefaultLocale")
        override fun run() {
            val need = sp.getBoolean("main_fullscreen", false)
            val leftneed = sp.getBoolean("main_fullscreen_bleft", false)
            val rightneed = sp.getBoolean("main_fullscreen_bright", false)
            val nonotch = sp.getBoolean("main_fullscreen_notch", false).not()
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R){
                val flags = View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                window?.decorView?.systemUiVisibility = if (need) flags else 0
            }

            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) and nonotch.not() or !need){
                bin.hal.setOnApplyWindowInsetsListener(null)
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window?.attributes?.layoutInDisplayCutoutMode  = if(nonotch) WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window?.setDecorFitsSystemWindows(nonotch)

                    bin.hal.setOnApplyWindowInsetsListener { a,i->
                        (a.layoutParams as ViewGroup.MarginLayoutParams).apply {
                            bottomMargin = i.systemWindowInsetBottom
                            if(leftneed){
                                leftMargin = i.systemWindowInsetLeft
                            }
                            if(rightneed){
                                rightMargin = i.systemWindowInsetRight
                            }
                            topMargin = i.systemWindowInsetTop
                        }
                        WindowInsets.CONSUMED
                    }
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                } else {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
            Handler(mainLooper).postDelayed(this, 1500)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        changePage(savedInstanceState?.getInt("page")?:0)
        runOnUiThread(fullRunner)
    }

    override fun onDestroy() {
        super.onDestroy()

        if(isFinishing){
            Handler(mainLooper).removeCallbacks(fullRunner)

        }
    }



    override fun onTitleChanged(title: CharSequence?, color: Int) {
        super.onTitleChanged(title, color)
        roti.setParentTitle (title, null, parentClick)

    }

    private val checkPageState = Runnable{
        onBackStackChanged()
        if(fragmentManager.backStackEntryCount != 0){
            hideAll()
        }
    }







    override fun onStart() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(sp.getBoolean("bylock", false))
        }

        super.onStart()
        //to check

        Handler(mainLooper).postDelayed(checkPageState, 400L)


        val mustAskLook = sp.getBoolean("needOnLogin", false)

//        for(i in 0..10){
//            remoteKey(KeyEvent.KEYCODE_ENTER, window.decorView)
//        }


    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("page",openPage)
        for (i in 0..playingActivity.size()) {
            playingActivity.get(i)?.let {
                Bundle().let { pa ->
                    pa.putString("name", it.first)
                    pa.putParcelable("intent", it.second)
                    outState.putBundle("pa", pa)
                }
            }
        }

        fragmentManager.apply {
            fragmentManager.putFragment(outState, "s", findFragmentByTag("s"))
            fragmentManager.putFragment(outState, "f", findFragmentByTag("f"))
            fragmentManager.putFragment(outState, "m", findFragmentByTag("m"))
        }
        outState.putBoolean("askLook", askLook)

        super.onSaveInstanceState(outState)

    }

    private fun updateView() {
        if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            bin.drawer.layoutParams.width = windowManager.defaultDisplay.height
        }
    }



    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onBackPressed() {

        val actions = Runnable{
//            val m = MenuBuilder.createFresh(this){
//                onOptionsItemSelected(it)
//            }
//            onCreatePanelMenu(Window.FEATURE_OPTIONS_PANEL,m)
//            onPreparePanel(Window.FEATURE_OPTIONS_PANEL, null,m)
//
//            tryOn{
//                MenuSheet(m).show()
//            }
           window.openPanel(Window.FEATURE_OPTIONS_PANEL, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU))
        }
        if(bin.drawer.visibility == View.VISIBLE){
            toggleDrawer()
            return
        }
        if(storeAm.isNotEmpty()){
            actionMode?.finish()
            return
        }
        if(isTv){
            actions.run()
            return
        }
        super.onBackPressed()
    }



    override fun onKeyShortcut(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_1) {
            actionBar?.setSelectedNavigationItem(0)
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_2) {
            actionBar?.setSelectedNavigationItem(1)
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_3) {
            actionBar?.setSelectedNavigationItem(2)
            return true
        }
        if(event?.isCtrlPressed == true){

            if (keyCode == KeyEvent.KEYCODE_W) {
                finish()
                return true
            }
        }
        return super.onKeyShortcut(keyCode, event)
    }







    override fun onResume() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(android.preference.PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bylock", false))
        }
        checkNeedFullscreen()

        super.onResume()




    }



    public fun lookThisInTv() {
        val info = roter.getSelectedRoute(
            MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        var displayShow : Display? = null
        if(info != null) {
            displayShow = info.presentationDisplay

            if (mPresentation != null && mPresentation?.getDisplay() != displayShow) {
                mPresentation?.dismiss();
                mPresentation = null;
                info("Disconnect from tv")
            }
        }
        if (mPresentation == null && displayShow != null) {
            // Initialise a new Presentation for the Display

            mPresentation = Presentation(this, displayShow)
            mPresentation!!.setOnDismissListener{
                mPresentation = null;
            }

            mPresentation?.setContentView(R.layout.search_start)
            mPresentation!!.window!!.decorView.setOnClickListener {
                Toast.makeText(mPresentation!!.context, "Boo", Toast.LENGTH_SHORT).show()
            }
            mPresentation?.findViewById<View>(R.id.cha)?.setOnClickListener {
                Toast.makeText(mPresentation!!.context, "Yay", Toast.LENGTH_SHORT).show()
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





    override fun onBackStackChanged() {
        if(fragmentManager.backStackEntryCount != 0) {
            otherPage()

        } else {
            mainPage()

        }

    }


    fun otherPage(){
        actionBar?.navigationMode = ActionBar.NAVIGATION_MODE_STANDARD
        actionBar?.setDisplayShowTitleEnabled(true)
        showBackButton()
    }

    override fun onPrepareNavigateUpTaskStack(builder: TaskStackBuilder?) {
        builder?.addParentStack(this)
    }

    override fun onPrepareDialog(id: Int, dialog: Dialog?, args: Bundle?) {
        if(id == DIALOG_MULTI_DOC){
            dialog?.setOnDismissListener {
                removeDialog(DIALOG_MULTI_DOC)
            }
        }
        if(id == DIALOG_INFO){
            val u = args?.getBinder(DIALOG_INFO.toString()) as DialogBlock
            u.onDialogCreate(dialog as AlertDialog)
        }
        super.onPrepareDialog(id, dialog, args)
    }

    override fun onCreateDialog(id: Int, args: Bundle?): Dialog? {
        return when(id){
            3234 -> AlertDialog.Builder(this, this.getThemeId()).setTitle(R.string.app_name).setSingleChoiceItems(
                ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_activated_1, stringList.map { getString(it) }.toMutableList().also{
                    it.add("Search other channel")
                    it.add(getString(R.string.set))
                }), openPage
            ){d,i->
                if(i == 3){
                    SearchPeople().show(fragmentManager, "sp")
                }
                else if(i == 4){
                    val b = Bundle()
                    b.putBinder("lil", LilInstance())
                    val s  = Intent(this, Settings::class.java).putExtra("bun",b)
                    startActivity(s)
                }else if (i < 3){
                    changePage(i)
                }
                removeDialog(id)
            }.setPositiveButton(android.R.string.cancel, null).setNeutralButton(R.string.exit){_,_->finish()}
                .create().also {
                it.window!!.also{w->
                    w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                    w.setDimAmount(0.2F)
                    w.setGravity(Gravity.START)
                    if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
                        w.setLayout(windowManager.defaultDisplay.height, ViewGroup.LayoutParams.MATCH_PARENT)
                    }
                    else if((resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) and isTablet){
                        w.setLayout(windowManager.defaultDisplay.width/2, ViewGroup.LayoutParams.MATCH_PARENT)
                    }
                    else if((resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) and !isTablet){
                        w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        w.setGravity(Gravity.TOP)
                    }
                    w.setWindowAnimations(android.R.style.Animation_InputMethod)
                }
            }
            DIALOG_SEARCH -> {
                SearchDetail(this, args?: Bundle()).apply {
                    setOnDismissListener {
                        removeDialog(id)
                    }
                }
            }


            DIALOG_INFO ->{
                val b = AlertDialog.Builder(this)
                val u = args?.getBinder(DIALOG_INFO.toString()) as DialogBlock
                u.onMaking(b)
                b.setOnDismissListener {
                    removeDialog(DIALOG_INFO)
                }
                b.create()
            }

            VideoView.ADD_DIALOG -> {
                NewVidDialog.new (this, args?: Bundle()).apply {
                    setOnDismissListener {
                        intent?.removeExtra(Intent.EXTRA_TEXT)
                        removeDialog(VideoView.ADD_DIALOG)
                    }
                }
            }
            DIALOG_MULTI_DOC -> {
                val b = args?.getBinder(DIALOG_MULTI_DOC.toString()) as SendToCon
                ActivityContainerDialog(this, ActivityContainerDialog.m(localActivityManager, b.intent, b.name), b.fullScreen).apply {
                    if(b.title.isEmpty()){
                        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    }else{
                        setTitle(b.title)
                    }
                    finishOnDismiss = true

                }
            }
            VideoView.QR_DIALOG ->{
                val t = args?.getString("txt") ?: return null
                QRCodePage(this, t, true).apply {
                    setOnDismissListener {
                        removeDialog(id)
                    }
                }

            }
            PreviewImgPage.PREVIEW_IMAGE ->{
                PreviewImgPage(this, args?.getBinder(PreviewImgPage.PREVIEW_CODE)).apply {
                    setOnDismissListener {
                        removeDialog(id)
                    }
                }
            }
            else ->  onCreateDialog(id)
        }


    }



    override fun onCreateDialog(id: Int): Dialog? {
        return if(id == DIALOG_BACKSTEP){
            Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog).apply {
                roti.let {r->
                    r.parent?.let{p->
                       if(p is ViewGroup){
                           p.removeView(r)
                       }
                    }
                    this.setContentView(r)
                    setTitle("Go back to....")
                }
            }
        }else when(id){
            DIALOG_DATA_CORRUPT -> {
                AlertDialog.Builder(this).apply {
                    setTitle(android.R.string.dialog_alert_title)
                    setMessage("Your saved video is corrupted, You cannot add, delete, and watch saved video.\nIf you want to hide this dialog forever, click reset button")
                    setPositiveButton(android.R.string.yes, null)
                    setNeutralButton ("Reset"){ _,_->
                        try{
                            AlertDialog.Builder(context).apply {
                                setTitle("Reset the app data?")
                                setMessage("If you reset the app data, your saved preference, saved videos, and history will be deleted.")
                                setIcon(R.drawable.delete)
                                setPositiveButton("Reset") { _, _ ->
                                    try {
                                        (getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                                    }catch (_: Exception){}
                                }
                                setNegativeButton(android.R.string.cancel, null)
                            }.show()

                        }catch (_: Exception){

                        }
                    }
                }.create()
            }
            else -> null
        }
    }


    fun mainPage(){
//        actionBar?.hideBackButton()
        val s = Point()

        windowManager.defaultDisplay.getSize(s)
//        actionBar?.apply{
//            navigationMode = when (sp.getBoolean("tabmod", true)) {
//                false -> ActionBar.NAVIGATION_MODE_LIST
//                true -> ActionBar.NAVIGATION_MODE_TABS
//            }
//            actionBar?.setTitle(try{
//                (stringList[selectedNavigationIndex])
//            }catch (_: Exception){
//                (R.string.app_name)
//            })
//            setDisplayShowTitleEnabled(!(isTablet or (s.x >= s.y) or (navigationMode == ActionBar.NAVIGATION_MODE_LIST)))
//        }
    }


    data class SendToCon @JvmOverloads constructor(val intent: Intent, val name: String = intent.component!!.shortClassName): Binder(){
        var fullScreen = true
        var title = ""
    }



    class MainActionBar(val actionBar: ActionBar) : Binder(){
        val from :Window.Callback? = null
    }



    inner class LilInstance: OutControlActivity(this)



    open class DialogBlock : Binder(){
        var title = ""
        var info = ""
        var listener = object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {

            }
        }

        open fun onMaking(b: AlertDialog.Builder){

        }

        open fun onDialogCreate(d: AlertDialog){

        }


        final fun toBundle(): Bundle{
            return Bundle().apply {
                putBinder(DIALOG_INFO.toString(), this@DialogBlock)
            }
        }
    }
}


