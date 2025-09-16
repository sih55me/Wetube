package app.wetube.secret

import android.app.Activity
import android.app.Fragment
import android.app.FragmentTransaction
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedCallback
import app.wetube.MainActivity
import app.wetube.R
import app.wetube.TranslucentHelper
import app.wetube.core.isTablet
import app.wetube.core.setupTheme
import app.wetube.databinding.OnboardBinding
import app.wetube.page.setup.DummyInfo
import app.wetube.page.setup.Themer

class Intro : Activity() {


    private lateinit var acon: Any
    private var pg = 0
    private val tagIntent = Intent().apply {
        putExtra("intro",true)
    }
    val bin by lazy { OnboardBinding.inflate(layoutInflater) }
    val cuspref get()  = getSharedPreferences("cuspref", MODE_PRIVATE)
    val cpc get() = cuspref.edit()

    val t by lazy{ TranslucentHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val pm = baseContext.packageManager
        t.setTranslucentStatus(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (pm.hasSystemFeature("android.software.leanback")) {
            val p = PreferenceManager.getDefaultSharedPreferences(this).edit()
            cpc.putBoolean("first", false)
            cpc.apply()
            p.putBoolean("tv", true)
            p.apply()
            finish()
        }
        //to check if is from intro
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }else{
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        setContentView(bin.root)
        bin.prev.setOnClickListener {
            slide(0)
        }

        bin.next.setOnClickListener {
            slide(1)
        }
        if(savedInstanceState != null){
            pg = savedInstanceState.getInt("pg")
            bin.progress.progress = savedInstanceState.getInt("pg")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bin.root.setOnApplyWindowInsetsListener { v, insets ->
                var lu = WindowInsets.Type.navigationBars()
                if((resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) and !isTablet){
                    lu = lu or WindowInsets.Type.displayCutout()
                }
                val s = insets.getInsets(lu)
                bin.prev.layoutParams.let {
                    if(it is ViewGroup.MarginLayoutParams){
                        it.leftMargin = s.left
                        it.bottomMargin = s.bottom
                    }
                }
                bin.next.layoutParams.let {
                    if(it is ViewGroup.MarginLayoutParams){
                        it.rightMargin = s.right
                        it.bottomMargin = s.bottom
                    }
                }
                bin.content.layoutParams.let {
                    if(it is ViewGroup.MarginLayoutParams){
                        it.rightMargin = s.right
                        it.leftMargin = s.left
                    }
                }
                WindowInsets.CONSUMED
            }

        }else{
            if(t.hasNavBar){
                if (t.isNavigationAtBottom) {
                    bin.prev.layoutParams.let {
                        if(it is ViewGroup.MarginLayoutParams){
                            it.bottomMargin = t.navigationBarHeight
                        }
                    }
                    bin.next.layoutParams.let {
                        if(it is ViewGroup.MarginLayoutParams){
                            it.bottomMargin = t.navigationBarHeight
                        }
                    }
                } else {
                    bin.prev.layoutParams.let {
                        if(it is ViewGroup.MarginLayoutParams){
                            it.leftMargin = t.navigationBarHeight
                        }
                    }
                    bin.content.layoutParams.let {
                        if(it is ViewGroup.MarginLayoutParams){
                            it.leftMargin = t.navigationBarHeight
                            it.rightMargin = t.navigationBarHeight
                        }
                    }
                    bin.next.layoutParams.let {
                        if(it is ViewGroup.MarginLayoutParams){
                            it.rightMargin = t.navigationBarHeight
                        }
                    }
                }
            }
        }
        go()
        modifyButton()

    }

    fun modifyButton(){
        if(pg == 3){
            bin.next.setText(android.R.string.ok)
        }else{
            bin.next.setText(R.string.next)
        }
        if(pg == 0){
            bin.prev.setText(R.string.exit)
        }else{
            bin.prev.setText(R.string.prev)
        }
        setTitle("${pg + 1} / 4")
    }

    fun slide(flag:Int){
        if(flag == 1){
            if (pg != 3) {
                pg += 1
                go()

            } else {
                setResult(RESULT_OK, tagIntent)
                finish()
            }
        }
        else if(flag == 0){
            if (pg >= 1) {
                pg -= 1
                go()
            } else {
                setResult(RESULT_CANCELED, tagIntent)
                finish()
            }
        }
        bin.progress.progress = pg
        modifyButton()
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
        slide(0)
    }



}
