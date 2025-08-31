package app.wetube.page

import android.app.Dialog
import android.app.DialogFragment
import android.app.Fragment
import android.os.Bundle
import android.view.ActionMode
import app.wetube.core.releaseParent
import app.wetube.page.TransPaper.Companion.notDialogExep
import app.wetube.window.Paper


open class Sheet() :DialogFragment(), TransPaper{
    companion object{
        const val IsInDialog = "isInDialog"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return PaperOnFragment(activity!!, this)
    }

    override val currentActionMode: ActionMode? get() = paper.currentActionMode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = !true
    }

    override val connector: Fragment get() = this




    override val paper: Paper
        get() {
            if(!showsDialog) throw notDialogExep
            if(dialog is Paper) return dialog as Paper
            else throw IllegalStateException("Dialog is not Paper")
        }



    override fun onDestroyView() {

        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
            releaseParent()
        }
        super.onDestroyView()
    }




}