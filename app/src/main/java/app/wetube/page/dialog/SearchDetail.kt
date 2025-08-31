package app.wetube.page.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.TextView
import app.wetube.R
import app.wetube.core.FirstReview
import app.wetube.core.releaseParent
import app.wetube.databinding.SearchAdvancedBinding
import app.wetube.manage.db.FavChaDB
import app.wetube.manage.db.HistoryDB
import app.wetube.page.Search

class SearchDetail(private val activity: Activity,private var arguments: Bundle = Bundle()): AlertDialog(activity){


    var onStart: (SearchDetail.() -> Unit) = {}
    var onQuery: (query: String, channelId: String, order: String, continueSearch: Boolean) -> Unit = { q, c, o, co ->
        try{ (activity.fragmentManager.findFragmentByTag("s") as Search).apply{
            this.filter = o
            search(q, c, co, o)

        } }catch (_: Exception){}
    }

    val f  = FirstReview(activity)

    val hdb by lazy{ HistoryDB(activity!!.applicationContext) }
    val fdb by lazy{ FavChaDB(activity!!.applicationContext) }
    val v by lazy { SearchAdvancedBinding.inflate(activity!!.layoutInflater) }
    companion object{
        const val QUERY = "query"
        const val CHANNELID = "channelId"
        const val TOKEN = "token"
        const val ORDER = "order"
    }
    var o = arguments.getString(ORDER) ?: ""
        set(value) {
            field = value
            arguments.putString(ORDER, value)
        }
    val canBeNext get() = arguments.getBoolean(TOKEN)
    init {
        window?.requestFeature(Window.FEATURE_OPTIONS_PANEL)
        window?.attributes?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
    }





    val h get() = Handler(Looper.getMainLooper())


    override fun onSaveInstanceState(): Bundle {
        val b = super.onSaveInstanceState()
        b.putBundle("a", arguments)
        return b
    }


    var checkOnce  : Runnable = Runnable{

    }
    override fun onCreate(savedInstanceState: Bundle?) {

        if(savedInstanceState != null){
            arguments = savedInstanceState.getBundle("a") ?: arguments
        }
        v.root.releaseParent()
        setView(v.root)
        setTitle("Search Advanced")
        val d = DialogInterface.OnClickListener{ _, i->
            when(i){
                BUTTON_POSITIVE -> {
                    onQuery(v.query.text.toString(), v.channelId.text.toString(), o, false)
                    dismiss()
                }

                BUTTON_NEUTRAL ->{
                    onQuery(v.query.text.toString(), v.channelId.text.toString(), o, true)
                    dismiss()
                }

            }
        }
        setButton(BUTTON_POSITIVE, activity.getString(android.R.string.search_go),d)
        setButton(BUTTON_NEGATIVE, activity.getString(android.R.string.cancel), d)
        if(canBeNext){
            setButton(BUTTON_NEUTRAL, "Next key", d)
        }
        super.onCreate(savedInstanceState)
        with(v.query) {
            setAdapter(ArrayAdapter(context, android.R.layout.simple_list_item_1, hdb.listAsList().toMutableList()))
            addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                    
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    v.openq.let {
                        if (s.isNullOrEmpty()) {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                it.tooltipText = context.getString(R.string.history_search)
                            }
                            it.contentDescription = context.getString(R.string.history_search)
                            it.setImageResource(R.drawable.arrow_drop)
                            it.setOnClickListener {
                                v.query.showDropDown()
                            }
                        }else{
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                                it.tooltipText = context.getString(R.string.clear)
                            }
                            it.contentDescription = context.getString(R.string.clear)
                            it.setImageResource(R.drawable.close)
                            it.setOnClickListener {
                                v.query.setText(null)
                            }
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    
                }

            })
            threshold = 1
        }
        with(v.channelId) {
            val l = fdb.listAsList()
            setAdapter(ArrayAdapter(context, android.R.layout.simple_list_item_1, l.map { it.title }.toMutableList()))
            threshold = 1
            onItemClickListener =  AdapterView.OnItemClickListener{_,_,position,_->
                v.channelId.setText(l[position].id)
            }
            addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {

                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    v.openc.let {

                        if (s.isNullOrEmpty()) {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                it.tooltipText = context.getString(R.string.favcha)
                            }
                            it.contentDescription = context.getString(R.string.favcha)
                            it.setImageResource(R.drawable.arrow_drop)
                            it.setOnClickListener {
                                v.channelId.showDropDown()
                            }
                        }else{
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                it.tooltipText = context.getString(R.string.clear)
                            }
                            it.contentDescription = context.getString(R.string.favcha)
                            it.setImageResource(R.drawable.close)
                            it.setOnClickListener {
                                v.channelId.setText(null)
                            }
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })
        }
        v.cf.setOnClickListener {_->
            v.order.setSelection(0)
            v.channelId.text = null
            v.query.text = null
        }

        val onMenuClick = fun(t: TextView): MenuItem.OnMenuItemClickListener{
            return MenuItem.OnMenuItemClickListener{
                t.onTextContextMenuItem(it.itemId)
                true
            }
        }
        val list = arrayOf(
            activity.getString(R.string.none),
            activity!!.getString(R.string.rating),
            activity!!.getString(R.string.nv),
            activity!!.getString(R.string.date),
            activity!!.getString(R.string.vidcount),
            activity!!.getString(R.string.viewcount)
        )
        v.order.adapter = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, list)
        v.order.setSelection(
            try{
                list.indexOf(o)
            }catch (_: Exception){
                0
            }
        )

        v.order.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                o = list[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        v.openo.setOnClickListener {
            v.order.performClick()
        }
        v.query.customSelectionActionModeCallback = makeCustomAcMo(HistoryDB::class.java,onMenuClick(v.query))
        v.channelId.customSelectionActionModeCallback = makeCustomAcMo(FavChaDB::class.java, onMenuClick(v.channelId))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            v.query.customInsertionActionModeCallback = makeCustomAcMo(HistoryDB::class.java,onMenuClick(v.query))
            v.channelId.customInsertionActionModeCallback = makeCustomAcMo(FavChaDB::class.java,onMenuClick(v.channelId))
        }
        v.query.setText(arguments.getString(QUERY))
        v.channelId.setText(arguments.getString(CHANNELID))


    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            dismiss()
        }
        return super.onOptionsItemSelected(item)
    }




    fun makeCustomAcMo(sel: Class<*>, o: MenuItem.OnMenuItemClickListener): ActionMode.Callback{
        return  object : ActionMode.Callback{
            override fun onCreateActionMode(
                mode: ActionMode?,
                menu: Menu?,
            ): Boolean {
                menu?.add("Get from..")
                return true
            }

            override fun onPrepareActionMode(
                mode: ActionMode?,
                menu: Menu?,
            ): Boolean {
                return true
            }

            override fun onActionItemClicked(
                mode: ActionMode?,
                item: MenuItem?,
            ): Boolean {
                item?.let { o.onMenuItemClick(it) }
                if(item?.title == "Get from.."){
                    pyCom(sel)
                    mode?.finish()
                }
                return true
            }



            override fun onDestroyActionMode(mode: ActionMode?) {

            }

        }
    }

    private fun pyCom(sel: Class<*>) {
        val dev = if(sel.name == HistoryDB::class.java.name){
            v.query
        }
        else if(sel.name == FavChaDB::class.java.name){
            v.channelId
        }else throw IllegalStateException("Nothing select")
        val p = PopupMenu(context, dev)

        val l = if(sel.name == HistoryDB::class.java.name){
            hdb.listAsList().toList()
        }else if(sel.name == FavChaDB::class.java.name){
            fdb.listAsList().map { it.title }.toList()
        }else  listOf<String>();
        l.forEach {
            p.menu.add(0,l.indexOf(it), l.indexOf(it), it)
        }
        p.setOnMenuItemClickListener {
            if(sel.name == HistoryDB::class.java.name){
                v.query.setText(it.title)
            }
            else if(sel.name == FavChaDB::class.java.name){
                try{
                    v.channelId.setText(fdb.listAsList()[it.order].id)
                }catch (_: Throwable){

                }
            }
            true
        }
        p.show()
    }





}