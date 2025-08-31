package app.wetube.manage

import android.app.Fragment
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import app.wetube.MainActivity
import app.wetube.R
import app.wetube.core.ActionModeCreator
import app.wetube.core.convertToId
import app.wetube.core.hideKeyBoard
import app.wetube.databinding.ActivityNewVidBinding
import app.wetube.manage.db.VidDB

class NewVideoPage(): Fragment() {



    private val db by lazy { VidDB(activity) }


    var onVideoSev = {

    }

    var onSave:((Bundle)->Unit) = {

    }
    var onRestore:((Bundle)->Unit) = {

    }



    private var videoId:String =""


    private val bin by lazy{ActivityNewVidBinding.inflate(activity.layoutInflater)}


    private val cam by lazy { ActionModeCreator().apply {
        onStart = {a,m->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                a.type = ActionMode.TYPE_PRIMARY
            }
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
    }.create() }

    override fun onStart() {
        super.onStart()
        setHasOptionsMenu(true)
        activity?.actionBar?.setTitle(R.string.add)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return bin.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            videoId = it.getString("id","")
        }
        try{
            bin.editTextText3.text = SpannableStringBuilder("Video ${db.listAsList().size + 1}")
            bin.editTextText4.text = SpannableStringBuilder(videoId)
            bin.editTextText4.customSelectionActionModeCallback = cam
        }catch (_: Exception){
            activity?.showDialog(MainActivity.DIALOG_DATA_CORRUPT)
            fragmentManager?.popBackStack()
        }
        super.onViewCreated(view, savedInstanceState)
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        with(menu){
            add("Convert To Id").setOnMenuItemClickListener {
                bin.editTextText4.convertToId()
                true
            }
            add(R.string.add).setIcon(R.drawable.check).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setOnMenuItemClickListener {
                add(
                    bin.editTextText3.text.toString(),
                    bin.editTextText4.text.toString()
                )
                true
            }

        }

        return super.onCreateOptionsMenu(menu, inflater)
    }


    private fun add(t:String, i:String){
        if (t.isNotEmpty() &&  i.isNotEmpty()) {
            db.doing {
                db.insert(t, i)
            }
            Toast.makeText(activity, "Saved!", Toast.LENGTH_LONG).show()
            if(activity is New_vid){
                activity?.finish()
            }else {
                fragmentManager?.popBackStackImmediate()
                onVideoSev()
            }

        } else { Toast.makeText(activity, "Cannot saving the video!", Toast.LENGTH_LONG).show() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.hideKeyBoard()
    }


    override fun onDetach() {
        view?.let { v->
            v.parent?.let {p->
                if (p is ViewGroup){
                    p.removeView(v)
                }
            }
        }
        super.onDetach()
        activity?.actionBar?.subtitle = null
    }









}