package app.wetube

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedCallback
import app.wetube.core.AlertController
import app.wetube.core.setupTheme

open class ActivityDialog : Activity(),DialogInterface {

    open var listenerSet = DialogCallback()

    companion object {
        fun make(context: Context,title : String, message: String, callback: DialogCallback? =null)  {
            context.startActivity(pack(context, title,message,callback))
        }

        fun pack(context: Context,title : String, message: String, callback: DialogCallback? =null): Intent  {
            val i = Intent(context, ActivityDialog::class.java)
            i.putExtra("message", message)
            val b = Bundle()
            b.putBinder("callback", callback)
            if(i.extras != null){
                b.putAll(i.extras)
            }
            i.replaceExtras(b)
            i.putExtra("title", title)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return i
        }
    }
    
    interface ButtonDialog{
        fun setNegative(text: Any, listener:DialogInterface.OnClickListener?)
        fun setPositive(text: Any, listener:DialogInterface.OnClickListener?)
        fun setNeutral(text: Any, listener:DialogInterface.OnClickListener?)
    }
    
    open class DialogCallback:Binder(){
        open fun onButtonClick(select: Int){}
        
        open fun onMakeButton(buttons: ButtonDialog){
            buttons.setPositive ("OK", null)
        }

        open val stringsInfo : Pair<String?, String?>?  get() = null
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setWindowAnimations(android.R.style.Animation_Activity)
        setupTheme(true)
        super.onCreate(savedInstanceState)
        val callback = intent.extras?.getBinder("callback") ?: listenerSet
        try{
            AlertController(this, window, this).apply {
                setTitle(intent.getStringExtra("title") ?: "Info")
                setMessage(intent.getStringExtra("message") ?: "Info")
                val bi = object: ButtonDialog{

                    val listener:((Int, Any, DialogInterface.OnClickListener?)->Unit) = {i,a,c->
                        val addonLis = object:DialogInterface.OnClickListener{
                            override fun onClick(
                                dialog: DialogInterface?,
                                which: Int,
                            ) {
                                c?.onClick(dialog, which)
                                if(callback is DialogCallback){
                                    callback.onButtonClick(i)
                                }

                            }

                        }
                        if(a is Int) {
                            setButton(i, getString(a), addonLis, null)
                        }else{
                            setButton(i, a.toString(), addonLis, null)
                        }

                    }
                override fun setNegative(
                    text: Any,
                    listener: DialogInterface.OnClickListener?,
                ) {
                    listener(DialogInterface.BUTTON_NEGATIVE, text, listener)
                }

                override fun setPositive(
                    text: Any,
                    listener: DialogInterface.OnClickListener?,
                ) {
                    listener(DialogInterface.BUTTON_POSITIVE, text, listener)
                }

                override fun setNeutral(
                    text: Any,
                    listener: DialogInterface.OnClickListener?,
                ) {
                    listener(DialogInterface.BUTTON_NEUTRAL, text, listener)
                }

            }
                if(callback is DialogCallback){
                    callback.onMakeButton(bi)
                }
                installContent()
            }

        }
        catch(e:Exception){
            e.printStackTrace()
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.emdi)
            findViewById<TextView>(R.id.title).text = (intent.getStringExtra("title") ?: "Info")


            findViewById<TextView>(R.id.message).text = intent.getStringExtra("message")
            findViewById<Button>(R.id.ok).setOnClickListener {
                finish()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                object : OnBackAnimationCallback {
                    val v get() = requireNotNull(window?.decorView?.rootView?.rootView)
                    override fun onBackInvoked() {
                        v.animate().scaleX(0F).scaleY(0F).alpha(0F).withEndAction {
                            cancel()
                        }
                    }

                    override fun onBackStarted(backEvent: BackEvent) {
                        val a = v.animate()
                        a.scaleX(0.8F).scaleY(0.8F).alpha(0.6F)
                    }


                    override fun onBackCancelled() {
                        v.animate().scaleX(1F).scaleY(1F).alpha(1F)
                    }
                }
            } else OnBackInvokedCallback {
                cancel()
            }
            onBackInvokedDispatcher.registerOnBackInvokedCallback(0, b)
        }
    }

    override fun onBackPressed() {
        cancel()
    }

    override fun cancel() {
        finish()
    }

    override fun dismiss() {
        finish()
    }


}