package app.wetube


import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.ViewConfiguration
import android.view.Window
import android.view.WindowManager
import kotlin.math.min

class TranslucentHelper(private val window: Window, val context: Context) {
    var mNavBarAvailable: Boolean = false
    var mStatusBarHeight: Int = 0
    private var mInPortrait: Boolean = false
    private var sNavBarOverride: String = ""
    private var mSmallestWidthDp: Float = 0F
    constructor(activity: Activity):this(activity.window, activity)


    init {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mInPortrait = (context.getResources()
            .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        try {
            val c = Class.forName("android.os.SystemProperties")
            val m = c.getDeclaredMethod("get", String::class.java)
            m.setAccessible(true)
            sNavBarOverride = m.invoke(null, "qemu.hw.mainkeys") as String
        } catch (e: Throwable) {
            sNavBarOverride = ""
        }

        // check theme attrs
        val `as` = intArrayOf(android.R.attr.windowTranslucentNavigation)
        val a = context.obtainStyledAttributes(`as`)
        try {
            mNavBarAvailable = a.getBoolean(0, false)
        } finally {
            a.recycle()
        }

        // check window flags
        if(context is Activity) {
            val winParams = window.attributes


            val bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            if ((winParams.flags and bits) != 0) {
                mNavBarAvailable = true
            }

            mSmallestWidthDp = getSmallestWidthDp(wm)
            if (mNavBarAvailable) setTranslucentStatus(true)
            mStatusBarHeight =
                getInternalDimensionSize(context.getResources(), STATUS_BAR_HEIGHT_RES_NAME)
        }
    }

    fun setTranslucentStatus(on: Boolean) {
        val win = window
        val winParams = win!!.getAttributes()
        val bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }

        win.setAttributes(winParams)
        // instance
        win.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )
    }

    private fun getSmallestWidthDp(wm: WindowManager): Float {
        val metrics = DisplayMetrics()
        wm.getDefaultDisplay().getRealMetrics(metrics)

        val widthDp = metrics.widthPixels / metrics.density
        val heightDp = metrics.heightPixels / metrics.density
        return min(widthDp.toDouble(), heightDp.toDouble()).toFloat()
    }

    val navigationBarHeight: Int get(){
        val res = context.getResources()
        if (hasNavBar) {
            val key: String?
            if (mInPortrait) {
                key = NAV_BAR_HEIGHT_RES_NAME
            } else {
//                if (!isNavigationAtBottom) return 0
                key = NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME
            }
            return getInternalDimensionSize(res, key)
        }else return 0

    }

    val hasNavBar: Boolean get(){
        val res = context.getResources()
        val resourceId = res.getIdentifier(SHOW_NAV_BAR_RES_NAME, "bool", "android")
        if (resourceId != 0) {
            var hasNav = res.getBoolean(resourceId)
            // check override flag (see static block)
            if ("1" == sNavBarOverride) {
                hasNav = false
            } else if ("0" == sNavBarOverride) {
                hasNav = true
            }
            return hasNav
        } else { // fallback
            return !ViewConfiguration.get(context).hasPermanentMenuKey()
        }
    }

    fun getInternalDimensionSize(res: Resources, key: String?): Int {
        var result = 0
        val resourceId = res.getIdentifier(key, "dimen", "android")
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId)
        }
        return result
    }

    val isNavigationAtBottom: Boolean
        /**
         * Should a navigation bar appear at the bottom of the screen in the current
         * device configuration? A navigation bar may appear on the right side of
         * the screen in certain configurations.
         *
         * @return True if navigation should appear at the bottom of the screen, False otherwise.
         */
        get() = (mSmallestWidthDp >= 600 || mInPortrait)

    companion object {



        // translucent support
        private const val STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height"
        private const val NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height"
        private const val NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape"
        private const val SHOW_NAV_BAR_RES_NAME = "config_showNavigationBar"
    }
}