package app.wetube.set

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.app.Fragment
import android.app.FragmentTransaction
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.Preference
import android.preference.PreferenceManager
import android.preference.PreferenceScreen
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import app.wetube.OutControlActivity
import app.wetube.R
import app.wetube.core.getVersionCode
import app.wetube.core.getVersionName
import app.wetube.core.isTablet
import app.wetube.core.setupTheme
import app.wetube.core.showBackButton
import app.wetube.core.tryOn
import app.wetube.manage.db.HistoryDB
import app.wetube.manage.db.VidDB
import app.wetube.page.s.SettingsPage
import app.wetube.page.s.VideoSettings
import app.wetube.page.dialog.ResetDialog
import app.wetube.page.dialog.RestartDialog
import app.wetube.page.s.PasswordSet
import app.wetube.window.CekList


private const val TITLE_TAG = "settingsActivityTitle"
private const val MDI_TAG = "MDI"

class Settings : Activity()  {


    val pg = listOf("general","pass","vid")
    private val db by lazy { VidDB(applicationContext) }
    private val hdb by lazy { HistoryDB(applicationContext) }
    val sp get() = PreferenceManager.getDefaultSharedPreferences(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)
        showBackButton()
        val p = fun(i: String): Fragment{
            return fragmentManager.findFragmentByTag(i)
        }
        val f :List<Fragment>
        val t = fragmentManager.beginTransaction()
        if(savedInstanceState == null){
            f = listOf(SettingsPage(), PasswordSet(), VideoSettings())
            t
                .add(android.R.id.content, f[0], pg[0])
                .add(android.R.id.content, f[1], pg[1])
                .add(android.R.id.content, f[2], pg[2])
        }else{
            f = pg.map { p(it) }
        }
        for(i in f){
            t.detach(i)
        }
        t.commit()
        actionBar?.also {
            it.setDisplayShowTitleEnabled(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            it.navigationMode = ActionBar.NAVIGATION_MODE_TABS
            it.addTab(
                it.newTab()
                    .setText(R.string.general)
                    .setTabListener(object : ActionBar.TabListener{
                        override fun onTabSelected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {
                            ft?.attach(f[0])

                        }

                        override fun onTabUnselected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {
                            ft?.detach(f[0])
                        }

                        override fun onTabReselected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {

                        }

                    })
            )
            it.addTab(
                it.newTab()
                    .setText(R.string.title_activity_pass)
                    .setTabListener(object : ActionBar.TabListener{
                        override fun onTabSelected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {
                            ft?.attach(f[1])
                        }

                        override fun onTabUnselected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {
                            ft?.detach(f[1])
                        }

                        override fun onTabReselected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {}

                    })
            )
            it.addTab(
                it.newTab()
                    .setText(R.string.title_activity_video)
                    .setTabListener(object : ActionBar.TabListener{
                        override fun onTabSelected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {
                            ft?.attach(f[2])
                        }

                        override fun onTabUnselected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {
                            ft?.detach(f[2])
                        }

                        override fun onTabReselected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {}

                    })
            )
        }
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBar?.setSelectedNavigationItem(savedInstanceState?.getInt("page")?: 0)
    }






    override fun recreate() {
        restartHomePage()
        if(parent==null){
            super.recreate()
        }
    }


    inner class SetCon: OutControlActivity(this){

        override fun restart() {
            tryOn(true){
                me.intent.getBundleExtra("bun")?.getBinder("lil")?.let {
                    if(it is OutControlActivity){
                        it.restart()
                    }
                }
            }
            super.restart()
        }
    }



    private fun restartHomePage() {

    }

    val h get() = Handler(Looper.getMainLooper())

    val Preference.customDialog : Unit get() {
        if(this is PreferenceScreen){
            onPreferenceClickListener = Preference.OnPreferenceClickListener{_->

                dialog?.dismiss()
                val b = Bundle()

                b.putBinder(CekList.KEY, ListBind(this))
                showDialog(1,b)
                true
            }
        }
    }

    class ListBind(val p: PreferenceScreen): Binder(){
        fun whenList(l: ListView){
            p.bind(l)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(menu == null)return false
        menu.add("Dream video settings").intent = Intent(this, RVSet::class.java)
        menu.addSubMenu("About").also{
            it.add("Version : ${getVersionName(this)}")
            it.add("Gen ver. : ${getVersionCode(this)}")
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreateDialog(id: Int, args: Bundle?): Dialog? {
        when(id){
            1 -> {

                return CekList(this).apply {
                    showBackButton()
                    setOnDismissListener {
                        removeDialog(1)
                    }
                    if(isTablet){
                        this@Settings.window.attributes?.let {
                            window?.setLayout(it.width,it.height)
                        }

                    }
                }
            }
        }
        return super.onCreateDialog(id, args)
    }

    override fun onPrepareDialog(id: Int, dialog: Dialog?, args: Bundle?) {
        if(id ==1){
            val l = args?.getBinder(CekList.KEY) as ListBind
            (dialog as CekList).apply {
                l.whenList(listView)
                setOnShowListener {_->
                    this.actionBar?.let{a->
                        a.title = this@Settings.title
                        a.subtitle = l.p.title
                    }
                }
            }
        }
        super.onPrepareDialog(id, dialog, args)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("page", actionBar?.selectedNavigationIndex?:0)
        super.onSaveInstanceState(outState)
    }





    override fun onStart() {
        super.onStart()
        cekLok()
    }
    
    fun cekLok(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(sp.getBoolean("bylock", false))
        }
    }






    override fun onResume() {
        cekLok()
        super.onResume()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                onBackPressed()
                return true
            }

        }

        return super.onOptionsItemSelected(item)
    }

    fun flushThen(r: Runnable){
        window.decorView.animate().rotation(100000F).alpha(0F).withEndAction(r)
    }


    override fun onCreateDialog(id: Int): Dialog? {
        return when(id){
            3 -> RestartDialog(this)
            4 -> ResetDialog(this)
            else -> super.onCreateDialog(id)
        }
    }










}