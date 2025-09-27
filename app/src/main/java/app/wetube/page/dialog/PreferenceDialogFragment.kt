package app.wetube.page.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import app.wetube.R
import app.wetube.core.getThemeId
import app.wetube.core.isTablet
import app.wetube.core.tryOn
import app.wetube.page.Sheet

open class PreferenceDialogFragment: DialogFragment() {


    companion object{
        const val FNAME = "fname"
        const val TITLE = "title"
    }


    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FrameLayout(activity).apply {
            id = app.wetube.R.id.container
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return if(activity?.isTablet == true){
            object : AlertDialog(activity) {
                override fun setContentView(view: View) {
                    super.setView(view)
                }

            }
        }else{
            object : AlertDialog(activity,activity.getThemeId()) {
                init {
                    window?.setWindowAnimations(android.R.style.Animation_Dialog)
                }
                override fun setContentView(view: View) {
                    super.setView(view)
                }
            }
        }.also {
            it.setTitle(arguments?.getString(TITLE))
            it.setButton(DialogInterface.BUTTON_POSITIVE,getString(android.R.string.ok)) { d, _ ->
                d.cancel()
            }
        }
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val n = arguments?.getString(FNAME)
        if(savedInstanceState == null){
            childFragmentManager.beginTransaction()
                .add(app.wetube.R.id.container, instantiate(activity, n), n).commit()
        }
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onDismiss(dialog)
    }
}