package app.wetube.page.dialog

import android.content.Context
import android.os.Binder
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import app.wetube.MainActivity
import app.wetube.R
import app.wetube.core.ActionModeCreator
import app.wetube.core.convertToId
import app.wetube.core.isTv
import app.wetube.databinding.ActivityNewVidBinding
import app.wetube.manage.db.VidDB
import app.wetube.window.Paper

class NewVidDialog private constructor(context : Context, private val arguments: Bundle = Bundle()): Paper(context) {

    private val db by lazy { VidDB(context) }


    var onVideoSev = OnSave{

    }

    override fun onKeyShortcut(keyCode: Int, event: KeyEvent): Boolean {
        if(event.isCtrlPressed == true){
            if (keyCode == KeyEvent.KEYCODE_S) {
                try{
                    add(
                        bin.editTextText3.text.toString(),
                        bin.editTextText4.text.toString()
                    )
                }catch (_: Exception){}
                return true
            }
        }
        return super.onKeyShortcut(keyCode, event)
    }


    companion object{
        const val VIID = "id"
        const val ONSAVE ="onSave"
        @JvmStatic
        fun newB(id:String, onSave: Runnable) = Bundle().also{a->
            a.putString(VIID, id)
            a.putBinder(ONSAVE, OnSave(onSave))
        }
        @JvmStatic
        fun new(context: Context,id:String, onSave: Runnable): NewVidDialog {
            val a = newB(id, onSave)
            return new(context, a)
        }
        @JvmStatic
        fun new(context: Context,a: Bundle): NewVidDialog {
            return NewVidDialog(context, a)
        }
    }




    var videoId:String =""


    private val bin by lazy{ActivityNewVidBinding.inflate(layoutInflater)}


    private val cam by lazy { ActionModeCreator().apply {
        onStart = {a,m->

            m.add(0,123,0,"Convert to id").setOnMenuItemClickListener {
                bin.editTextText4.convertToId()
                a.finish()
                true
            }
            true
        }

        onPrepare = {_,m->
            m.findItem(123)?.isEnabled = bin.editTextText4.text.isNotEmpty()
            true
        }

        onItemClicked = {
            bin.editTextText4.onTextContextMenuItem(it.itemId)
        }
    }.create()
    }


    override fun onStart() {
        showBackButton()
        super.onStart()

    }

    fun regonize(b: Bundle){
        b.let {
            it.getString(VIID,"")?.let {
                videoId = it
            }
            it.getBinder(ONSAVE)?.let { b ->
                if (b is OnSave) {
                    onVideoSev = b
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(bin.root)
        regonize(savedInstanceState?:arguments)
        try{
            bin.editTextText3.text = SpannableStringBuilder("Video ${db.listAsList().size + 1}")
            bin.editTextText4.text = SpannableStringBuilder(videoId)
            bin.editTextText4.customSelectionActionModeCallback = cam
        }catch (_: Exception){
            ownerActivity?.showDialog(MainActivity.DIALOG_DATA_CORRUPT)
        }
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        with(menu){
            add("Convert To Id").setOnMenuItemClickListener {
                bin.editTextText4.convertToId()
                true
            }
            add(R.string.add).setIcon(R.drawable.check).setShowAsActionFlags(if(context.isTv) MenuItem.SHOW_AS_ACTION_NEVER else MenuItem.SHOW_AS_ACTION_ALWAYS).setOnMenuItemClickListener {
                add(
                    bin.editTextText3.text.toString(),
                    bin.editTextText4.text.toString()
                )
                true
            }

        }

        return super.onCreateOptionsMenu(menu)
    }


    private fun add(t:String, i:String){
        if (t.isNotEmpty() &&  i.isNotEmpty()) {
            db.doing {
                if(it.listAsList().map { it.videoId }.contains(i)){
                    Toast.makeText(context, "This video already saved!", Toast.LENGTH_LONG).show()
                }else{
                    it.insert(t, i)
                }
            }
            Toast.makeText(context, "Saved!", Toast.LENGTH_LONG).show()
            dismiss()
            onVideoSev.run()

        } else { Toast.makeText(context, "Cannot saving the video!", Toast.LENGTH_LONG).show() }
    }

    class OnSave(private val r: Runnable): Binder(), Runnable{
        override fun run() {
            r.run()
        }

    }


    override fun onSaveInstanceState(): Bundle {
        return super.onSaveInstanceState().apply {
            putAll(arguments)
        }
    }


    override fun onStop() {
        decorView?.let { v->
            v.parent?.let {p->
                if (p is ViewGroup){
                    p.removeView(v)
                }
            }
        }
        super.onStop()
    }
}