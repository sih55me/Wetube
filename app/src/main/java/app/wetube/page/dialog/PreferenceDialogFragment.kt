package app.wetube.page.dialog

import android.app.ActionBar
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
import android.widget.Toolbar
import app.wetube.R
import app.wetube.core.getThemeId
import app.wetube.core.isTablet
import app.wetube.core.tryOn
import app.wetube.page.Sheet
import app.wetube.window.Paper

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
        return object : Paper(activity) {
            init {
                setTitle(arguments?.getString(TITLE))
            }

            override fun setupActionBar(actionBar: ActionBar) {
                super.setupActionBar(actionBar)
                showBackButton {
                    this@PreferenceDialogFragment.onCancel(this)
                }
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