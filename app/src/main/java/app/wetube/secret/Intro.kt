package app.wetube.secret

import android.app.Activity
import android.app.Fragment
import android.app.FragmentTransaction
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedCallback
import app.wetube.MainActivity
import app.wetube.R
import app.wetube.page.NothingDI
import app.wetube.page.setup.DummyInfo
import app.wetube.page.setup.Themer

class Intro : Activity() {


    private lateinit var acon: Any
    private var pg = 0
    private val tagIntent = Intent().apply {
        putExtra("intro",true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val pm = baseContext.packageManager
        val cuspref = getSharedPreferences("cuspref", MODE_PRIVATE)
        val cpc = cuspref.edit()
        val con = FrameLayout(this).apply {
            id = R.id.content
        }
        if (pm.hasSystemFeature("android.software.leanback")) {
            val p = PreferenceManager.getDefaultSharedPreferences(this).edit()
            cpc.putBoolean("first", false)
            cpc.apply()
            p.putBoolean("tv", true)
            p.apply()
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
        //to check if is from intro

        try {
            val ac = Class.forName("com.android.internal.app.AlertController")
            val c = ac.getDeclaredConstructor(
                Context::class.java,
                DialogInterface::class.java,
                Window::class.java
            )
            c.isAccessible = true
            acon = c.newInstance(this@Intro, NothingDI(), window)
            val sv = acon.javaClass.getMethod("setView", View::class.java)
            sv.isAccessible = true
            sv.invoke(acon, con)
            val sBN = acon.javaClass.getMethod(
                "setButton",
                Int::class.javaPrimitiveType,
                CharSequence::class.java,
                DialogInterface.OnClickListener::class.java,
                Message::class.java
            )
            sBN.isAccessible = true
            sBN.invoke(
                acon,
                DialogInterface.BUTTON_POSITIVE,
                getString(R.string.next),
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    if (pg != 3) {
                        pg += 1
                        go()
                    } else {

                        intent.getBundleExtra("bun")?.let {
                            val i = it.getBinder("lil")
                            if (i is MainActivity.LilInstance){
                                i.restart()
                            }
                        }
                        cpc.putBoolean("first", false)
                        cpc.apply()
                        setResult(RESULT_OK, tagIntent)
                        finish()
                    }
                },
                null
            )
            val sBP = acon.javaClass.getMethod(
                "setButton",
                Int::class.javaPrimitiveType,
                CharSequence::class.java,
                DialogInterface.OnClickListener::class.java,
                Message::class.java
            )
            sBP.isAccessible = true
            sBP.invoke(
                acon,
                DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.prev),
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    if (pg >= 1) {
                        pg -= 1
                        go()
                    } else {
                        setResult(RESULT_CANCELED, tagIntent)
                        finish()
                    }
                },
                null
            )
            val install = acon.javaClass.getMethod("installContent")
            install.isAccessible = true
            install.invoke(acon)
        } catch (e: Exception) {
            setContentView(con)
            e.printStackTrace()
        }

        if(savedInstanceState != null){
            pg = savedInstanceState.getInt("pg")
        }
        go()
        isImmersive = false
        super.onCreate(savedInstanceState)

    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("pg", pg)
    }
    fun go(){
        Log.i("pg", pg.toString())
        val f = when(pg){
            0 -> DummyInfo().apply {
                laid = R.layout.sp1
            }
            1 -> DummyInfo().apply {
                laid = R.layout.sp2
            }
            2 -> DummyInfo().apply {
                laid = R.layout.sp3
            }
            3 -> Themer()
            else -> Fragment()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                object : OnBackAnimationCallback {

                    override fun onBackStarted(backEvent: BackEvent) {
                        window.decorView.animate().setDuration(500L).scaleY(0.5F).scaleX(0.5F).alpha(0.5F)
                    }
                    override fun onBackInvoked() {
                        window.decorView.animate().alpha(0F).setDuration(1000L).withEndAction {
                            onBackPressed()
                        }
                    }

                    override fun onBackCancelled() {
                        window.decorView.animate().setDuration(500L).scaleY(1F).scaleX(1F).alpha(1F)
                    }
                }
            } else {
                OnBackInvokedCallback{
                    onBackPressed()
                }
            }

            onBackInvokedDispatcher.registerOnBackInvokedCallback(0,b)
        }

        fragmentManager.beginTransaction().replace(R.id.content, f).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
    }


    override fun onBackPressed() {
        setResult(RESULT_CANCELED, tagIntent)
        finish()
    }



}
