package app.wetube.page.dialog


import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.AdapterViewFlipper
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Gallery
import android.widget.GridView
import android.widget.ListView
import android.widget.StackView
import android.widget.Toast
import app.wetube.ChannelInfo
import app.wetube.R
import app.wetube.core.Search
import app.wetube.core.info
import app.wetube.core.tryOn
import app.wetube.databinding.FragmentSearchResultBinding
import app.wetube.item.ChannelDetail
import app.wetube.kembaliKe
import app.wetube.manage.db.FavChaDB
import app.wetube.page.PaperOnFragment
import app.wetube.page.Sheet

class SearchPeople: Sheet() {
    val db by lazy{ FavChaDB(activity!!.applicationContext) }
    var data = mutableListOf<ChannelDetail>()
    val bin by lazy { FragmentSearchResultBinding.inflate(activity.layoutInflater) }
    val adap by lazy {
        ArrayAdapter(
            activity,
            android.R.layout.simple_list_item_1,
            mutableListOf<String>()
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }



    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return bin.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bin.list.adapter = adap
        paper.showBackButton()
        if(savedInstanceState != null){
            val d = savedInstanceState.getParcelableArray("data")
            if(d != null) {
                data = d.filterIsInstance<ChannelDetail>().toMutableList()
                reload()
            }
        }
        bin.back.setOnClickListener {
            dismiss()
        }
        bin.search.setOnEditorActionListener { v, actionId, event ->
            v?.text?.let {
                search(it)
            }
            true
        }
        bin.list.setOnCreateContextMenuListener { menu, v, menuInfo ->
            val l = data[(menuInfo as AdapterView.AdapterContextMenuInfo).position]
            menu?.apply {
                setHeaderTitle(l.title)
                add(getString(android.R.string.copy) + " id").setOnMenuItemClickListener {
                    val clipboard: ClipboardManager =
                        activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip =
                        ClipData.newPlainText("yt channel id", l.id)
                    clipboard.setPrimaryClip(clip)
                    true
                }
                add(R.string.add).setOnMenuItemClickListener {
                    db.insert(l)
                    info("Saved")
                    true
                }

            }
        }
        bin.list.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            tryOn {
                val l = data[position]
                val i = Intent(activity!!, ChannelInfo::class.java).putExtra("id", l.id).putExtra("name", l.title)
                i.kembaliKe(activity)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(
                    i
                )

            }
        }
        dialog.setTitle("Search Other Channel")
    }

    private fun search(text: CharSequence) {
        val s = Search(activity)
        val n = ProgressDialog(activity)
        n.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        n.setMessage("Getting data....")
        n.setIcon(R.drawable.search)
        data.clear()
        var done=false
        s.searchChannel(
            query = text.toString(),
            max = 35,
            onGet = {
                data.add(it)
            },
            onDone = {
                done = true
                n.dismiss()
                reload()
            }
        )
        n.setOnDismissListener {
            if(!done){
                s.s()
                Toast.makeText(activity, "Searching cancelled",0).show()
            }
        }
        n.show()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelableArray("data", data.toTypedArray())
    }

    fun reload(){
        adap.clear()
        adap.addAll(data.map{it.title})
        adap.notifyDataSetChanged()
    }



    private fun requestSm(closeOnCancel: Boolean) {
        if(activity == null) return
        val t = EditText(activity)
        t.isSingleLine = true
        t.imeOptions = EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI


        val d = object : AlertDialog(activity){
            init {
                setTitle("Request query")
                setView(t)
                setButton(getString(android.R.string.search_go)){ _, _->
                    search(t.text)
                }
                setButton2(getString(android.R.string.cancel)){ _, _->
                    if(closeOnCancel){
                        this@SearchPeople.dismiss()
                    }
                }
            }
        }
        d.setCancelable(false)
        t.setOnEditorActionListener { t,_,_->
            d.dismiss()
            search(t.text)
            true
        }
        d.show()
    }
}