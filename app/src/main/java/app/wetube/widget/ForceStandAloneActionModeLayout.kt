package app.wetube.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ActionMode
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RequiresApi

class ForceStandAloneActionModeLayout: FrameLayout {
    @JvmOverloads
    constructor(c: Context, a: AttributeSet? = null, da: Int = 0 , dr: Int = 0):super(c, a, da, dr){
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun startActionMode(callback: ActionMode.Callback?, type: Int): ActionMode? {
        return super.startActionMode(callback, ActionMode.TYPE_PRIMARY)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun startActionModeForChild(
        originalView: View?,
        callback: ActionMode.Callback?,
        type: Int
    ): ActionMode? {
        return super.startActionModeForChild(originalView, callback, ActionMode.TYPE_PRIMARY)
    }


}