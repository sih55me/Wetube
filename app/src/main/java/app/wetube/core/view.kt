package app.wetube.core

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.app.Fragment
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaRouter
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.preference.DialogPreference
import android.preference.Preference
import android.preference.PreferenceScreen
import android.util.Log
import android.util.TypedValue
import android.view.Display
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.Window.Callback
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import android.widget.Toolbar
import app.wetube.R
import com.cocosw.undobar.UndoBarController
import com.cocosw.undobar.UndoBarController.UndoBar
import com.cocosw.undobar.UndoBarStyle

private fun viewsCont(views: Int) : String{
    return when {
        views >= 1000000000 -> {
            "${views / 100000}M views"
        }
        views >= 1000000 -> {
            "${views / 10000}B views"
        }
        views >= 1000 -> {
            "${views / 10000}K views"
        }
        else -> {
            "$views views"
        }
    }
}

//Just shorcut
fun Window.toView() = decorView

fun Activity.playSFX(play : Int) = window.toView().playSoundEffect(play)

fun ViewGroup.forEach(get:(View) -> Unit) {
    for(i in 0 until childCount){
        get(getChildAt(i))
    }
}

fun Preference.performClick(){
    try{
        javaClass.getDeclaredMethod("performClick", PreferenceScreen::class.java).apply {
            isAccessible = true
        }.invoke(this, null)
    }catch (_: Exception){

    }
}

var Preference.selectDialog : Dialog?
    get() {
        return try{
            this::class.java.getDeclaredField("mDialog")?.let {
                it.isAccessible = true
                it.get(this) as? Dialog
            }
        }catch (_: Exception){
            if(this is DialogPreference){
                dialog
            }else if(this is PreferenceScreen){
                dialog
            }else{
                null
            }
        }
    }

    set(value) {
        try{
            this::class.java.getDeclaredField("mDialog")?.let {
                it.isAccessible = true
                it.set(this, value)
            }
        }catch (_: Exception){
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
        }
    }


fun ViewGroup.deepForEach(get:(View) -> Unit) {
    tryOn{
        forEach {
            if (it is ViewGroup) {
                deepForEach(get)
            }
            get(it)
        }
    }
}

val Context.isTv
    get() =  packageManager.hasSystemFeature("android.software.leanback")


fun Toolbar.resize(){
    val typedValue =  TypedValue();
    context.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)
    layoutParams.height = TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
    setupStyleText()

}

fun Toolbar.setupStyleText(){
    setupTitleStyle()
    setupSubTitleStyle()
}

fun Toolbar.setupTitleStyle(){
    val sti = TypedValue();
    context.getTheme().resolveAttribute(android.R.attr.titleTextAppearance, sti, true)
    setSubtitleTextAppearance(context, sti.data)
}

fun Toolbar.setupSubTitleStyle(){
    val sti = TypedValue();
    context.getTheme().resolveAttribute(android.R.attr.subtitleTextAppearance, sti, true)
    setSubtitleTextAppearance(context, sti.data)
}

val Context.isDarkTheme : Boolean get(){
    return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}


val Window.tul: Toolbar? get(){
    val decorView = decorView as FrameLayout
    for (i in 0 until decorView.childCount) {
        val it = decorView.getChildAt(i)
        if (it is ViewGroup) {
            for (j in 0 until it.childCount) {
                val v = it.getChildAt(j)
                if (v is ViewGroup) {
                    for (k in 0 until v.childCount) {
                        val v1 = v.getChildAt(k)
                        if (v1 is Toolbar) {
                            return v1
                        }
                        Log.i("window@$v", v1.javaClass.name)
                    }
                }
                Log.i("window@$it", v.javaClass.name)
            }
        }
        Log.i("window", it.javaClass.name)
    }
    return null
}
val Context.isTablet get() = resources.getBoolean(R.bool.tablet)

fun Drawable.drawableToBitmap(): Bitmap? {
    var bitmap: Bitmap? = null

    if (this is BitmapDrawable) {
        return getBitmap()
    }

    bitmap = if (getIntrinsicWidth() <= 0 || getIntrinsicHeight() <= 0) {
        Bitmap.createBitmap(
            1,
            1,
            Bitmap.Config.ARGB_8888
        ) // Single color bitmap will be created of 1x1 pixel
    } else {
        Bitmap.createBitmap(
            getIntrinsicWidth(),
            getIntrinsicHeight(),
            Bitmap.Config.ARGB_8888
        )
    }

    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
    draw(canvas)
    return bitmap
}


fun Activity.info(t: Any,  at:UndoBarStyle? = null, onClick:(() -> Unit)? =null, onDismiss:(() -> Unit)? = null) {
    UndoBar(this).message(if(t is Int) try {
        getString(t)
    }catch (_: Exception){
        t.toString()
    }  else t.toString()).style(at ?: UndoBarController.MESSAGESTYLE).listener(object : UndoBarController.AdvancedUndoListener{
        override fun onHide(token: Parcelable?) {
            onDismiss?.invoke()
        }

        override fun onClear(token: Array<out Parcelable?>) {

        }

        override fun onUndo(token: Parcelable?) {
            onClick?.invoke()
        }

    }).duration(2000L).show();
}

fun Fragment.info(t: Any,  at:UndoBarStyle? = null, onClick:(() -> Unit)? =null, onDismiss:(() -> Unit)? = null) {
    if(this is DialogFragment && this.showsDialog){
        AlertDialog.Builder(activity).apply {
            when(t){
                is Int -> try{
                    setMessage(t)
                }catch (_: Exception){
                    setMessage(t.toString())
                }

                is CharSequence -> setMessage(t)
            }
            if(at != null){
                try{
                    val fitr = at.javaClass.getDeclaredField("titleRes").apply{
                        isAccessible = true
                    }.getInt(at)
                    setPositiveButton(fitr) { _, _ ->
                        onClick?.invoke()
                    }
                }catch (_: Exception){
                    setPositiveButton("Action") { _, _ ->
                        onClick?.invoke()
                    }
                }
            }
            setOnDismissListener {
                onDismiss?.invoke()
            }
        }.create().also {d->
            d.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            d.window?.attributes?.let {
                it.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                it.dimAmount = 0F
                it.gravity = Gravity.BOTTOM
            }
            d.show()
            Handler(activity.mainLooper).postDelayed({
                d.dismiss()
            },2000L)
        }
    }
    activity?.info(t, at, onClick, onDismiss)
}



fun View.hideKeyBoard(){
    try {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }catch (e:Exception){
        Log.e("Wtb@MSV", "hideKeyBoard: where?",e)
    }
}

fun View.showKeyBoard(){
    try {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.isFullscreenMode
        imm.showSoftInputFromInputMethod(windowToken, 0)
    }catch (e:Exception){
        Log.e("Wtb@MSV", "hideKeyBoard: where?",e)
    }
}

val Context.wm : WindowManager
get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager


val Context.showAt : Display
    get() {
        val d= if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display
        } else {
            wm.defaultDisplay
        }
        return requireNotNull(d)
    }





val Context.themeAlertDialog : Int get() {
    val outValue = TypedValue()
    theme.resolveAttribute(android.R.attr.alertDialogTheme, outValue, true)
    return outValue.resourceId
}

val Context.themeDialog : Int get() {
    val outValue = TypedValue()
    theme.resolveAttribute(android.R.attr.dialogTheme, outValue, true)
    return outValue.resourceId
}

fun Fragment.releaseParent(){
    view.releaseParent()
}

fun View?.releaseParent(){
    this?.let {
        it.parent?.let {p->
            if(p is ViewGroup) {
                p.removeView(it)
            }
        }
    }
}

fun View.getBitmapFromView(): Bitmap {
    val bitmap = Bitmap.createBitmap(
        width, height,
        Bitmap.Config.ARGB_8888
    )
    buildDrawingCache()
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}



fun DialogFragment.makeDismissSender(b: Bundle) {
    val bi = DsmBnd(this)
    b.putBinder("dismiss", bi)
}

fun Boolean.toViewVisibility(): Int{
    return if(this) View.VISIBLE else View.GONE
}

class DsmBnd(val d: DialogFragment): Binder(), DialogInterface{

    override fun cancel() {
        d.dismiss()
    }

    override fun dismiss() {
        d.dismiss()
    }

}

fun WindowManager.LayoutParams.copyFromView(v: View){
    x = v.x.toInt()
    y = v.y.toInt()
    alpha = v.alpha
    width = v.width
    height = v.height

}

fun View.print(waile:(Bitmap)-> Unit){
    // measure the webview
    measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED)
    )
//layout of webview
    layout(0, 0, measuredWidth, getMeasuredHeight())

    isDrawingCacheEnabled = true
    buildDrawingCache()
//create Bitmap if measured height and width >0
    val b = if (measuredWidth> 0 && measuredHeight> 0)Bitmap.createBitmap(
        measuredWidth,
        measuredHeight, Bitmap.Config.ARGB_8888
    )
    else null
// Draw bitmap on canvas
    b?.let {
        Canvas(b).apply {
            drawBitmap(it, 0f, b.height.toFloat(), Paint())
            draw(this)
            waile(it)
        }
    }
}

/**
 * android 15 will force the app to destroy the system bar border
 *
 * so we make a custom border by padding the content
 */
fun Window?.make15Old(){
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM){
        this?.findViewById<View>(android.R.id.content)?.setOnApplyWindowInsetsListener { v, i ->
            v.setPadding(
                i.systemWindowInsetLeft
                , i.systemWindowInsetTop
                , i.systemWindowInsetRight
                , i.systemWindowInsetBottom
            )
            WindowInsets.CONSUMED
        }
    }
}

fun Menu.initMenu(c: MenuItem.OnMenuItemClickListener){
    for(d in 0..size() - 1){
        getItem(d)?.let {
            it.setOnMenuItemClickListener(c)
            it.subMenu?.initMenu(c)
        }
    }
}

val Callback.gotWindow: Window get() {
    try{
        return this::class.java.getDeclaredMethod("getWindow").apply {
            isAccessible = true
        }.invoke(this) as Window
    }catch (e: Throwable){
        throw IllegalStateException("function not found",e)
    }
}

fun Activity.whenCasting(listener :((Boolean)-> Unit)){
    val m = getSystemService(Activity.MEDIA_ROUTER_SERVICE) as MediaRouter

    Toast.makeText(this, m.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO).status?:"Y", Toast.LENGTH_SHORT).show()
    m.selectRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, m.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO))
    listener(m.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO).isEnabled)
}

const val ANDROID_INTERNAL = "com.android.internal"



