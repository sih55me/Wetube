package app.wetube.page


import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceFragment
import android.view.PointerIcon
import android.widget.Toast
import app.wetube.OutControlActivity
import app.wetube.R
import app.wetube.core.tryOn
import app.wetube.manage.db.HistoryDB
import app.wetube.manage.db.VidDB


class SettingsPage : PreferenceFragment() {
    companion object{
        const val IMGPICKER = 1516
    }
    private val db by lazy { VidDB(activity.applicationContext) }
    private val hdb by lazy { HistoryDB(activity.applicationContext) }
    val cuspref by lazy { activity.getSharedPreferences("cuspref", Context.MODE_PRIVATE) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            if(activity == null){
                if(it.context != null) {
                    Toast.makeText(it.context, "Error", Toast.LENGTH_SHORT).show()
                }
                return@setOnPreferenceClickListener true
            }
            if(l.isEmpty())return@setOnPreferenceClickListener true
            val d = ProgressDialog(activity).apply {
                setMessage("Deleting...")
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                setCancelable(false)
                progress = 0
                max = l.size - 1
                setOnDismissListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        view?.pointerIcon = PointerIcon.getSystemIcon(context, PointerIcon.TYPE_ARROW)
                    }
                }
                show()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    window?.decorView?.pointerIcon = PointerIcon.getSystemIcon(context, PointerIcon.TYPE_WAIT)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view?.pointerIcon = PointerIcon.getSystemIcon(context, PointerIcon.TYPE_WAIT)
            }
            try{
                Thread {
                    l.forEach { i ->
                        activity.runOnUiThread {
                            d.isIndeterminate = false
                            d.progress = l.indexOf(i) + 1
                            d.max = l.size - 1
                            d.setMessage("Delete $i")
                            hdb.deleteByName(i)
                            Thread.sleep(1000L)
                        }
                        if (i == l.last()) {
                            Handler(activity.mainLooper).postDelayed({
                                activity.runOnUiThread {
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
                Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
                d.dismiss()
            }

            true
        }
        findPreference("rese")?.setOnPreferenceClickListener {
            showDialog(4)
            true
        }

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
                    activity.setShowWhenLocked(b)
                    tryOn(true){
                        intent.getBundleExtra("bun")?.getBinder("lil")?.let {
                            if(it is OutControlActivity){
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
        findPreference("res")?.setOnPreferenceClickListener { _->
            showDialog(3)
            true
        }
        findPreference("notch")?.isEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    fun showDialog(id: Int){
        activity?.showDialog(id)
    }




    private fun restartHomePage() {

        tryOn(true){
            activity.recreate()
            activity.intent.getBundleExtra("bun")?.getBinder("lil")?.let {
                if(it is OutControlActivity){
                    it.restart()
                }
            }
        }
    }
    private fun recreate() {
        restartHomePage()
    }

    private fun flushThen(function: () -> Unit) {
        function.invoke()
    }

}






