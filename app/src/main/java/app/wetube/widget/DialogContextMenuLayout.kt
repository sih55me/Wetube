package app.wetube.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class DialogContextMenuLayout: FrameLayout {
    @JvmOverloads
    constructor(c: Context, a: AttributeSet? = null, da: Int = 0 , dr: Int = 0):super(c, a, da, dr){
    }


    override fun showContextMenu(x: Float, y: Float): Boolean {
        return super.showContextMenu()
    }


}