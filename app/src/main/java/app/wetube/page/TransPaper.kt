package app.wetube.page

import android.app.ActionBar
import android.app.Fragment
import android.view.ActionMode
import android.view.Window
import app.wetube.window.Paper

interface TransPaper {

    fun onActionBarReady(actionBar:ActionBar){}

    companion object{
        val notDialogExep: IllegalStateException
        get() =
            IllegalStateException("DialogFragment not showing dialog, you must call DialogFragment.setShowDialog(boolean)")
    }

    fun onDialogShow(){}

    val currentActionMode : ActionMode?

    val window : Window? get() {
        return  paper.window
    }

    val actionBar : ActionBar? get() {
        return paper.actionBar
    }
    val paper : Paper

    val connector: Fragment




    fun onDialogCreate(p: Paper){}

    fun onDialogStart(p: Paper){}
}