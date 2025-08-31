package app.wetube.widget

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.FrameLayout
import app.wetube.window.Kertas

class WindowView: FrameLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)
    var themeId = 0
    var win : Kertas? = null

    var themedContext : Context? = null
        private set
    var makeTC = true
    fun setup(){
        if(makeTC){
            themedContext = ContextThemeWrapper(context, themeId)
        }
        win = Kertas(themedContext ?: context, themeId)
        addView(childWindow?.decorView)
    }

    fun addViewToChild(v : View){
        childWindow?.setContentView(v)
    }

    val childWindow get() = win?.window


    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
    }


    fun release(){
        win = null
    }


}