package app.wetube.set

import android.app.ActionBar
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.Fragment
import android.app.FragmentTransaction
import android.app.LocalActivityManager
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.preference.PreferenceScreen
import android.view.Menu
import android.view.MenuItem
import android.view.PointerIcon
import android.widget.ListView
import android.widget.Toast
import app.wetube.MainActivity
import app.wetube.OutControlActivity
import app.wetube.R
import app.wetube.SupaContainer
import app.wetube.core.getVersionCode
import app.wetube.core.getVersionName
import app.wetube.core.isTablet
import app.wetube.core.setTextColor
import app.wetube.core.setupTheme
import app.wetube.core.showBackButton
import app.wetube.core.tryOn
import app.wetube.manage.db.HistoryDB
import app.wetube.manage.db.VidDB
import app.wetube.page.Bout
import app.wetube.page.SettingsPage
import app.wetube.page.VideoSettings
import app.wetube.window.CekList


private const val TITLE_TAG = "settingsActivityTitle"
private const val MDI_TAG = "MDI"

class Settings : Activity()  {


    private val db by lazy { VidDB(applicationContext) }
    private val hdb by lazy { HistoryDB(applicationContext) }
    val sp get() = PreferenceManager.getDefaultSharedPreferences(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)
        showBackButton()
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
                            ft?.replace(android.R.id.content, SettingsPage())
                        }

                        override fun onTabUnselected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {}

                        override fun onTabReselected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {}

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
                            ft?.replace(android.R.id.content, PasswordSet())
                        }

                        override fun onTabUnselected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {}

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
                            ft?.replace(android.R.id.content, VideoSettings())
                        }

                        override fun onTabUnselected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {}

                        override fun onTabReselected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {}

                    })
            )
            it.addTab(
                it.newTab()
                    .setText(R.string.about)
                    .setTabListener(object : ActionBar.TabListener{
                        override fun onTabSelected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {
                            ft?.replace(android.R.id.content, Bout())
                        }

                        override fun onTabUnselected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {}

                        override fun onTabReselected(
                            tab: ActionBar.Tab?,
                            ft: FragmentTransaction?
                        ) {}

                    })
            )
            actionBar?.setSelectedNavigationItem(savedInstanceState?.getInt("page")?: 0)
        }
        if(savedInstanceState == null){
            
        }
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
            3 -> AlertDialog.Builder(this)
                .setMessage("Restart the app?")
                .setNegativeButton(getString(android.R.string.cancel),null)
                .setPositiveButton(setTextColor("Restart", "#EF0D0D")){ _, _->
                    SupaContainer.Companion.restart()
                }.create()
            4 -> AlertDialog.Builder(this).apply {
                setTitle("Reset the app data?")
            setMessage("If you reset the app data, your saved preference, saved videos, and history will be deleted.")
                setPositiveButton("Reset") { _, _ ->
                    try {
                        (getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                    }catch (_: Exception){}
                }
                setNegativeButton(android.R.string.cancel, null)
            }.create()
            else -> super.onCreateDialog(id)
        }
    }










}