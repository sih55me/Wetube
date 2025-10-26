package app.wetube

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import android.widget.Toast

class SupaContainer:Application(){
    companion object {
        @JvmField
        var main: SupaContainer? = null
        @JvmStatic
        fun Context.d(){
            Thread.setDefaultUncaughtExceptionHandler(object : Thread.UncaughtExceptionHandler {
                override fun uncaughtException(thread: Thread, e: Throwable) {
                    val intent: Intent = Intent(applicationContext, RIP::class.java)
                    e.printStackTrace()
                    val em = "Wetube crash\nLogs :\n${e.stackTrace.joinToString(separator = "\n")}\n\nBecause :\n${e.message.toString().ifEmpty { e.localizedMessage.toString() }}"
                    intent.putExtra("msg", em)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            })
        }
        @JvmStatic
        fun restart(){
            android.os.Process.killProcess(android.os.Process.myPid())

            if(main != null) {
                main!!.startActivity(Intent(main, MainActivity::class.java))
            }
        }
        @JvmStatic
        fun dingDong(t: CharSequence){
            if(main == null)return
            main?.unknownInfo(t)
        }
        @JvmStatic
        fun dingDong(t: Int){
            if(main == null)return
            main?.unknownInfo(main!!.getText(t))
        }
    }

    val sp get() = PreferenceManager.getDefaultSharedPreferences(this)
    val mapA = mutableMapOf<String, Activity>()

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }


    override fun onCreate() {

        main = this
        
        super.onCreate()
        d()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
                WebView.enableSlowWholeDocumentDraw()
                if(sp.getBoolean("hardware_accelerated", true)){
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
                }
            }
            override fun onActivityCreated(
                activity: Activity,
                savedInstanceState: Bundle?,
            ) {
                activity.window.requestFeature(Window.FEATURE_OPTIONS_PANEL)
                activity.application
                d()
                activity.d()
            }


            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
                
            }

            override fun onActivityPaused(activity: Activity) {
                
            }

            override fun onActivityStopped(activity: Activity) {
                
            }

            override fun onActivitySaveInstanceState(
                activity: Activity,
                outState: Bundle,
            ) {
                
            }

            override fun onActivityDestroyed(activity: Activity) {

            }

        })
    }

    override fun onTerminate() {
        mapA.clear()
        main = null
        super.onTerminate()
    }


    /**
     * for android 12 >=, it will not show the icon like the before android 12 did it
     */
    fun unknownInfo(text: CharSequence){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    /**
     * start activity by index the stack activity
     */
    fun startActivityAt(index: Int){
        val d = mapA.values.toList()
        if(index >= d.size){
            unknownInfo("Invalid index")
            return
        }
        val i = d[index]
        startActivity(i.intent)
    }






    override fun onLowMemory() {
        mapA.clear()
        super.onLowMemory()
    }

}