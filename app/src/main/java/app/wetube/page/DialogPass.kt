package app.wetube.page

import android.app.ActionBar
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.LayoutInflater
import app.wetube.core.tryOn
import app.wetube.databinding.PassBinding
import app.wetube.window.Paper


class DialogPass private constructor(context: Context) : Paper(context){
    private val p by lazy{ PassBinding.inflate(LayoutInflater.from(context)) }
    var onDone:((Boolean)-> Unit) = {}
    private var attempt = 0
    private var attempt_polic = 5

    var arguments = Bundle()
    companion object{
        fun newInstance(context: Context, onDone:((Boolean)-> Unit)): DialogPass {
            val args = Bundle()
            args.putString("pass", PreferenceManager.getDefaultSharedPreferences(context).getString("password", ""))
            val fragment = DialogPass(context)
            fragment.onDone = onDone
            fragment.arguments = args
            return fragment
        }
    }

    init {
        setContentView(p.root)
    }

    val h by lazy { Handler(Looper.getMainLooper()) }


    private val dorestoreTitle = Runnable{
        actionBar?.setTitle(app.wetube.R.string.app_name)
    }


    override fun setupActionBar(actionBar: ActionBar) {
        super.setupActionBar(actionBar)
        dorestoreTitle.run()
    }


    override fun onStart() {
        super.onStart()
        val pass = arguments.getString("pass")



        p.btnClose.setOnClickListener {
            dismiss()
            onDone.invoke(false)
        }
        p.ok.setOnClickListener   {
            if(attempt_polic != attempt){
                if (p.editTextTextPassword.text.toString() == pass) {
                    onDone.invoke(true)
                    dismiss()
                } else {
                    actionBar?.title = "Wrong password"
                    attempt += 1


                    val ifY = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        h.hasCallbacks(dorestoreTitle).not()
                    } else {
                        true
                    }
                    if(ifY){
                        try {
                            h.postDelayed(dorestoreTitle, 2000)
                        } catch (_: Exception) {
                            dorestoreTitle.run()
                        }
                    }else{
                        tryOn {
                            h.removeCallbacks(dorestoreTitle)
                        }
                    }
                }
            }else{
                tryOn {
                    h.removeCallbacks(dorestoreTitle)
                }
                it.isEnabled = false
                actionBar?.title = "Wait until your open again"
                AlertDialog.Builder(context).setTitle("Too many attempts").setMessage("Please try again later\nThe ${it.context.getString(android.R.string.ok)} button is ill rn").setPositiveButton(android.R.string.ok, null).setNegativeButton("ByPass") {_,_->
                    (context.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                }.show()
            }
        }
    }
}