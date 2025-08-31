package app.wetube.preference

import android.app.Dialog
import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import android.preference.DialogPreference
import android.util.AttributeSet
import app.wetube.R
import app.wetube.core.selectDialog

class PaperShowPreference @JvmOverloads constructor(context: Context, attr: AttributeSet?=null, defStyleAttr: Int=android.R.attr.dialogPreferenceStyle, defStyleRes: Int=0): DialogPreference(context, attr, defStyleAttr,defStyleRes) {
    
    private var p : Dialog?
        get() = selectDialog

        set(value) {
            selectDialog = value
        }
    var classDialog=""
    init {
        val ta: TypedArray = context.obtainStyledAttributes(attr, R.styleable.PaperShowPreference)
        classDialog = ta.getString(R.styleable.PaperShowPreference_classDialog) ?: ""
        ta.recycle()
    }







    override fun showDialog(state: Bundle?) {
        val context = getContext()

        // Create the dialog
        val c = Class.forName(classDialog)
        p  = c.getConstructor(Context::class.java).newInstance(context) as Dialog


        dialog?.apply {
            if (state != null) {
                onRestoreInstanceState(state)
            }
            setOnDismissListener(this@PaperShowPreference)
            show()
        }
    }

}