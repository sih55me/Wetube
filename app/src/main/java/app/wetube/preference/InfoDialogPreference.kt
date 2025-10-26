package app.wetube.preference

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.DialogPreference
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.widget.TextView
import app.wetube.core.isTv
import app.wetube.core.tryOn

class InfoDialogPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null): DialogPreference(context, attrs){

    var setClick:(() -> Unit)? = null

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
        super.onPrepareDialogBuilder(builder)
    }

    override fun showDialog(state: Bundle?) {
        super.showDialog(state)
        val d = (dialog as AlertDialog)
        tryOn{
            d.findViewById<TextView>(android.R.id.message)?.apply{
                setTextIsSelectable(true)
                isFocusable = context.isTv.not()
                setMovementMethod(LinkMovementMethod.getInstance());
                isFocusable = context.isTv.not()
            }
        }
        setClick?.let{
            d.getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
                it()
            }
        }
    }



}