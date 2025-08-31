package app.wetube.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.Window

class AlertController(val context: Context, window: Window, diin : DialogInterface) {
    @SuppressLint("PrivateApi")
    private val cz = Class.forName("com.android.internal.app.AlertController")

    private val alertController = cz.getDeclaredConstructor(Context::class.java, DialogInterface::class.java, Window::class.java).apply{
        isAccessible = true
    }.newInstance(
        context,
        diin,
        window
    )

    fun setTitle(title : CharSequence){
        cz.getMethod("setTitle", CharSequence::class.java).invoke(alertController, title)
    }

    fun setMessage(message : CharSequence){
        cz.getMethod("setMessage", CharSequence::class.java).invoke(alertController, message)
    }

    fun setView(view : android.view.View){
        cz.getMethod("setView", android.view.View::class.java).invoke(alertController, view)
    }

    fun setPositiveButton(text : CharSequence, listener : DialogInterface.OnClickListener?){
        setButton(DialogInterface.BUTTON_POSITIVE, text, listener, null)
    }

    fun setNegativeButton(text : CharSequence, listener : DialogInterface.OnClickListener?){
        setButton(DialogInterface.BUTTON_NEGATIVE, text, listener, null)
    }

    fun setNeutralButton(text : CharSequence, listener : DialogInterface.OnClickListener?){
        setButton(DialogInterface.BUTTON_NEUTRAL, text, listener, null)
    }

    fun setButton(whichButton : Int, text : CharSequence, listener : DialogInterface.OnClickListener?, message : android.os.Message?){
        cz.getMethod("setButton", Int::class.java, CharSequence::class.java, DialogInterface.OnClickListener::class.java, android.os.Message::class.java).invoke(alertController, whichButton, text, listener, message)
    }

    fun installContent(){
        cz.getMethod("installContent").invoke(alertController)
    }

}