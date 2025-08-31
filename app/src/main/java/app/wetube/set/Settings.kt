package app.wetube.set

import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.Fragment
import android.app.LocalActivityManager
import android.app.ProgressDialog
import android.content.Intent
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
import app.wetube.window.CekList


private const val TITLE_TAG = "settingsActivityTitle"
private const val MDI_TAG = "MDI"

class Settings : PreferenceActivity()  {


    private val db by lazy { VidDB(applicationContext) }
    private val hdb by lazy { HistoryDB(applicationContext) }
    val mdi by lazy { LocalActivityManager(this, false) }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0,4622,1,app.wetube.R.string.title_activity_pass)?.intent = Intent(this@Settings, PasswordSet::class.java)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)
        showBackButton()
        savedInstanceState?.getBundle("s")?.let {
            mdi.dispatchCreate(it)
        }

    }

    override fun startWithFragment(
        fragmentName: String?,
        args: Bundle?,
        resultTo: Fragment?,
        resultRequestCode: Int,
        titleRes: Int,
        shortTitleRes: Int
    ) {
        super.startWithFragment(
            fragmentName,
            args,
            resultTo,
            resultRequestCode,
            titleRes,
            shortTitleRes
        )
    }

    override fun onBuildHeaders(target: List<Header?>?) {
        loadHeadersFromResource(R.xml.header, target)
    }

    override fun isValidFragment(fragmentName: String?): Boolean {
        return true
    }

    fun old(){
        window.decorView.apply {
            alpha = 1F
            rotation = 0F
        }
        if (preferenceScreen == null) {
            addPreferencesFromResource(R.xml.set)
        }
        if(preferenceScreen != null) {
            if (preferenceScreen.preferenceCount == 0) {
                addPreferencesFromResource(R.xml.set)
            }
        }

        findPreference("delhis")?.setOnPreferenceClickListener {
            val l = hdb.listAsList()
            if(l.isEmpty())return@setOnPreferenceClickListener true
            val d = ProgressDialog(this).apply {
                setMessage("Deleting...")
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                setCancelable(false)
                progress = 0
                max = l.size - 1
                setOnDismissListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        this@Settings.window.decorView.pointerIcon = PointerIcon.getSystemIcon(context, PointerIcon.TYPE_ARROW)
                    }
                }
                show()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    window?.decorView?.pointerIcon = PointerIcon.getSystemIcon(context, PointerIcon.TYPE_WAIT)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                window.decorView.pointerIcon = PointerIcon.getSystemIcon(this, PointerIcon.TYPE_WAIT)
            }
            try{
                Thread {
                    l.forEach { i ->
                        runOnUiThread {
                            d.isIndeterminate = false
                            d.progress = l.indexOf(i) + 1
                            d.max = l.size - 1
                            d.setMessage("Delete $i")
                            hdb.deleteByName(i)
                            Thread.sleep(1000L)
                        }
                        if (i == l.last()) {
                            Handler(mainLooper).postDelayed({
                                runOnUiThread {
                                    d.dismiss()
                                    l.clear()
                                }
                            }, 2000L)
                        }
                        tryOn{
                            Thread.sleep(1000L)
                        }

                    }
                }.start()
            }catch(_:Exception){
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                d.dismiss()
            }

            true
        }
        findPreference("rese")?.setOnPreferenceClickListener {
            showDialog(4)
            true
        }

        findPreference("versi")?.summary = getVersionName(this)
        findPreference("vergen")?.summary = getVersionCode(this).toString()
        findPreference("bylock")?.apply {
            //if not using android 8.1
            val d = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O).also {
                isEnabled = !it
            }
            if(d) {
                summary = "This feature need android 8.1"
            }
            setOnPreferenceChangeListener { a, d->
                val b = d as Boolean
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    setShowWhenLocked(b)
                    tryOn(true){
                        intent.getBundleExtra("bun")?.getBinder("lil")?.let {
                            if(it is MainActivity.LilInstance){
                                it.me.setShowWhenLocked(b)
                            }
                        }
                    }
                }

                true
            }
        }


        findPreference("theme")?.setOnPreferenceChangeListener { _, t->
            flushThen{
                restartHomePage()
                recreate()
            }
            true
        }

        findPreference("total")?.apply{
            summary = try{
                db.listAsList().size.toString()
            }catch (_: Exception){
                "0"
            }
        }
        findPreference("f")?.customDialog
        findPreference("cuco")?.customDialog
        findPreference("res")?.setOnPreferenceClickListener { _->
            showDialog(3)
            true
        }
        findPreference("notch")?.isEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    }




    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mdi.saveInstanceState()?.let{state->
            outState.putBundle("s", state)
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

    override fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
        tryOn(false){
            intent?.putExtra(
                "bun",
                Bundle().apply {
                    putBinder("lil", SetCon())
                }
            )
        }
        super.startActivityForResult(intent, requestCode, options)
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





    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bylock", false))
        }
    }






    override fun onResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bylock", false))
        }
        mdi.dispatchResume()
        super.onResume()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            4622 ->{
                startActivity(Intent(this@Settings, PasswordSet::class.java))

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


    override fun onPause() {
        super.onPause()
        mdi.dispatchPause(isFinishing)
    }




    override fun onStop() {
        super.onStop()
        mdi.dispatchStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mdi.dispatchDestroy(isFinishing)
    }








}