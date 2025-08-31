package app.wetube.window


import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.ActionMode
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toolbar
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedCallback
import app.wetube.core.getThemeId
import app.wetube.core.isTablet
import app.wetube.core.isTv
import app.wetube.core.setupTheme
import app.wetube.core.tryOn
import app.wetube.core.tul

/**
 * [Paper] is a [android.app.Activity]-like [android.app.Dialog] with a bit modify
 *
 * Feature :
 * * Can Make Menu
 * * Can Show and add listener for Back Button
 * * Can customize the toolbar using [tul]
 */
open class Paper @JvmOverloads constructor(context: Context, showActionBar : Boolean = true): Dialog(context, context.getThemeId()), AutoCloseable {

    var useBlur = false


    var winAttr set(value) {
        window?.attributes = value
    } get() = window?.attributes

    var winType set(value) {
        if (value != null) {
            winAttr?.type = value
        }
    } get() = winAttr?.type


    val decorView get() = window?.decorView

    @JvmField
    var type : Type = Type.ActivityLike

    @JvmField
    var skipMatchType = false

    val rollBack = java.lang.Runnable{
        requireNotNull(window?.decorView?.rootView?.rootView).apply {
            x = 0F
            1F.let {
                scaleX = it
                scaleY = it
                alpha = it
            }
        }
    }


    init {
        window?.requestFeature(Window.FEATURE_OPTIONS_PANEL)
        if(!showActionBar){
            unShowToolbar()
        }
    }

    @JvmField
    var windowAnimation = android.R.style.Animation_InputMethod


    var currentActionMode : ActionMode? = null
    private set

    val windowManager get()= window?.windowManager ?: context.getSystemService(Activity.WINDOW_SERVICE) as WindowManager

    private fun attachBack(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                object : OnBackAnimationCallback {
                    val v get() = requireNotNull(window?.decorView?.rootView?.rootView)
                    override fun onBackInvoked() {
                        v.animate().scaleX(0F).scaleY(0F).alpha(0F).withEndAction {
                            onBackPressed()
                        }
                    }

                    override fun onBackStarted(backEvent: BackEvent) {
                        val a = v.animate()

                        if (backEvent.swipeEdge == BackEvent.EDGE_LEFT) {
                            a.x(100F)
                        } else {
                            a.x(-100F)
                        }
                        a.scaleX(0.8F).scaleY(0.8F).alpha(0.7F)
                    }


                    override fun onBackCancelled() {
                        v.animate().x(0F).scaleX(1F).scaleY(1F).alpha(1F)
                    }
                }
            } else OnBackInvokedCallback {
                onBackPressed()
            }
            onBackInvokedDispatcher.registerOnBackInvokedCallback(0, b)
        }
    }


    final fun getString(resId: Int): String{
        return context.getString(resId)
    }

    override fun onKeyShortcut(keyCode: Int, event: KeyEvent): Boolean {
        if(event.isCtrlPressed == true){
            if (keyCode == KeyEvent.KEYCODE_W) {
                dismiss()
                return true
            }
        }
        return super.onKeyShortcut(keyCode, event)
    }


    override fun show() {

        if(!skipMatchType){
            tryOn {
                if (type is Type.ModalSheet) {
                    sizeOn()
                }
                if (type is Type.PersistentSheet) {
                    sizeLikeView()
                }
            }
        }
        winAttr?.let{
            it.windowAnimations = this@Paper.windowAnimation
        }

        super.show()
        if(window?.hasFeature(Window.FEATURE_ACTION_BAR) == true){
            actionBar?.let { setupActionBar(it) }
        }

        setupTheme()
    }

    @JvmOverloads
    fun createNLoad(s: Bundle? = null){
        onCreate(s)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if(context.isTv){
            menu.add("Back Function").setOnMenuItemClickListener {
                super.onBackPressed()
                true
            }
            for (i in 0..menu.size() - 1) {
                menu.getItem(i)?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }






    private fun sizeOn() {
        val p = Point()
        windowManager.defaultDisplay.getSize(p)
        var w = windowManager?.defaultDisplay?.width
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            w = windowManager.currentWindowMetrics.bounds.width()
        }
        if(p.x <= p.y){

            if (w != null) {
                window?.attributes?.width = w
            }
        }
        if(!context.isTablet){
            dimUp()
            if ((p.x > p.y)) {
                window?.attributes?.width = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    windowManager.currentWindowMetrics.bounds.height()
                }else{
                    windowManager.defaultDisplay.height
                }
                windowAnimation = android.R.style.Animation_Translucent
                window?.setGravity(Gravity.RIGHT)
            }else{
                window?.setGravity(Gravity.BOTTOM)
                window?.attributes?.height = WindowManager.LayoutParams.WRAP_CONTENT
            }
        }
        else if(context.isTablet){
            window?.setGravity(Gravity.RIGHT)
            if (w != null) {
                window?.attributes?.width = WindowManager.LayoutParams.WRAP_CONTENT
            }
            window?.setWindowAnimations(android.R.style.Animation_InputMethod)
            window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            window?.attributes?.height = WindowManager.LayoutParams.MATCH_PARENT

        }
    }


    private fun sizeLikeView() {
        val tc = type as Type.PersistentSheet
        val p = Point()
        windowManager.defaultDisplay.getSize(p)
        var w = tc.size.second

        if(p.x <= p.y){

            if (true) {
                window?.attributes?.width = w
            }
        }
        if(!context.isTablet){
            dimUp()
            if ((p.x > p.y)) {
                window?.attributes?.width = tc.size.first
                windowAnimation = android.R.style.Animation_Translucent
                window?.setGravity(Gravity.RIGHT)
            }else{
                window?.setGravity(Gravity.BOTTOM)
                window?.attributes?.height = WindowManager.LayoutParams.WRAP_CONTENT
            }
        }
        else if(context.isTablet){
            window?.setGravity(Gravity.RIGHT)
            if (w != null) {
                window?.attributes?.width = w
            }
            window?.setWindowAnimations(android.R.style.Animation_InputMethod)
            window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            window?.attributes?.height = WindowManager.LayoutParams.MATCH_PARENT

        }
        window?.attributes?.let { tc.matchPosition(it) }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }



    /**
     *  calling after Paper [show] up and [android.app.ActionBar] is set (except if set [Window.FEATURE_NO_TITLE] in [Window]'s flag)
     */
    open fun setupActionBar(actionBar: ActionBar){}

    /**
     *  show back button
     */
    fun showBackButton(resIcon:Int,onClick: View.OnClickListener) {
        tryOn {
            tul?.setNavigationIcon(resIcon)
            tul?.setNavigationOnClickListener(onClick)
        }
    }

    /**
     *  show back button
     */
    fun showBackButton(onClick: View.OnClickListener) {
        tryOn {
            val tv = TypedValue()
            context.theme.resolveAttribute(android.R.attr.homeAsUpIndicator, tv, true)
            showBackButton(tv.resourceId, onClick)
        }
    }

    fun destroyActionMode(){
        currentActionMode?.finish()

    }

    override fun onBackPressed() {
        if(currentActionMode != null){
            destroyActionMode()
            return
        }
        if(context.isTv){
            window?.openPanel(Window.FEATURE_OPTIONS_PANEL, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU))
            return
        }
        super.onBackPressed()
    }
    /**
     *  make the [Paper] dim behind
     *
     *  Importance : must call before calling [show]
     */
    fun dimUp(much: Float = 0.5F){
        tryOn(true){
            if (!isShowing) {

                if(useBlur){
                    window!!.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        window!!.setBackgroundBlurRadius(much.toInt())
                        winAttr?.blurBehindRadius = 100000
                    }
                }else{
                    window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                    window?.setDimAmount(much)
                }
                setCanceledOnTouchOutside(true)
            }
        }
    }
    /**
     *  show back button
     */
    fun showBackButton() = showBackButton {
        dismiss()
    }

    /**
     *
     *  return: Decor's [android.widget.Toolbar]
     *
     */
    val tul: Toolbar? get()= window?.tul


    /**
     *  make [Toolbar] or [ActionBar] gone in [Paper]
     *
     *  Importance : must call before calling [show] or [onCreate]
     */
    fun unShowToolbar(){
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return true
    }


    override fun onActionModeFinished(mode: ActionMode?) {
        currentActionMode = null
        super.onActionModeFinished(mode)
    }

    override fun onActionModeStarted(mode: ActionMode?) {
        super.onActionModeStarted(mode)
        currentActionMode = mode

    }

    override fun close() {
        dismiss()
    }


    sealed class Type(){
        object ActivityLike:Type()
        object ModalSheet:Type()
        class PersistentSheet(val view: View):Type(){
            val params: ViewGroup.LayoutParams get() = requireNotNull(view.layoutParams)

            /**
             * first = height
             *
             * second = width
             */
            val size get() = Pair(view.height, view.width)
            val gravity:Int get() = when(params){
                is LinearLayout.LayoutParams -> {
                    (params as LinearLayout.LayoutParams).gravity
                }

                is FrameLayout.LayoutParams -> {
                    (params as FrameLayout.LayoutParams).gravity
                }


                else -> throw IllegalStateException("Cannot find the gravity")
            }


            fun matchPosition(attr: WindowManager.LayoutParams){
                attr.apply{
                    x = view.x.toInt()
                    y = view.y.toInt()
                }
            }

        }
    }


}