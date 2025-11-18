package app.wetube.page.dialog

import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.ActionMode
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.TextView
import app.wetube.R
import app.wetube.TranslucentHelper
import app.wetube.core.FirstReview
import app.wetube.core.isTablet
import app.wetube.core.isTv
import app.wetube.core.releaseParent
import app.wetube.databinding.SearchAdvancedBinding
import app.wetube.manage.db.FavChaDB
import app.wetube.manage.db.HistoryDB
import app.wetube.page.Search
import app.wetube.window.Paper

class SearchDetail(private val activity: Activity,private var arguments: Bundle = Bundle()): Paper(activity){


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


    init {
        val w = window!!
        val resources = activity.resources
        w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        w.setDimAmount(0.2F)
        w.setGravity(Gravity.END)
        if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            w.setLayout(activity.windowManager.defaultDisplay.height, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        w.setWindowAnimations(android.R.style.Animation_InputMethod)
    }



    var isMaximizze = false
    var isPinned = false


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
        setContentView(v.root)
        setTitle("Request")
        v.openq.setOnClickListener {
            pyCom(HistoryDB::class.java)
        }
        v.openc.setOnClickListener {
            pyCom(FavChaDB::class.java)
        }

        super.onCreate(savedInstanceState)
        showBackButton()
        with(v.query) {
            if(context.isTv) {
                imeOptions = EditorInfo.IME_ACTION_GO
            }
        }
        with(v.channelId) {
            if(context.isTv) {
                imeOptions = EditorInfo.IME_ACTION_GO
            }
        }
        v.cf.setOnClickListener {_->
            v.order.setSelection(0)
            v.channelId.text = null
            v.query.text = null
        }

        if(canBeNext){
            v.npl.visibility = View.VISIBLE
        }
        v.np.setOnClickListener {
            onQuery(v.query.text.toString(), v.channelId.text.toString(), o, true)
            if(!isPinned){ dismiss() }
        }
        v.search.setOnClickListener {
            onQuery(v.query.text.toString(), v.channelId.text.toString(), o, false)
            if(!isPinned){ dismiss() }
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

        v.query.customSelectionActionModeCallback = makeCustomAcMo(HistoryDB::class.java,onMenuClick(v.query))
        v.channelId.customSelectionActionModeCallback = makeCustomAcMo(FavChaDB::class.java, onMenuClick(v.channelId))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            v.query.customInsertionActionModeCallback = makeCustomAcMo(HistoryDB::class.java,onMenuClick(v.query))
            v.channelId.customInsertionActionModeCallback = makeCustomAcMo(FavChaDB::class.java,onMenuClick(v.channelId))
        }
        v.query.setText(arguments.getString(QUERY))
        v.channelId.setText(arguments.getString(CHANNELID))


    }

    val n = TranslucentHelper(window!!, context)


    override fun dismiss() {
        activity.window.decorView.setPadding(0,0,0,0)
        super.dismiss()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if(activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            menu.add(0, 0, 0, "Pin").setOnMenuItemClickListener {
                isPinned = !isPinned
                if(!isPinned){
                    window!!.setDimAmount(0.2F)
                    activity.window.decorView.setPadding(0,0,0,0)
                    window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                }else{
                    window!!.setDimAmount(0F)
                    window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                    if(winAttr!!.x < 0) {
                        activity.window.decorView.setPadding(activity.windowManager.defaultDisplay.height - n.navigationBarHeight, 0, 0, 0)
                    }else{
                        activity.window.decorView.setPadding(0,0,activity.windowManager.defaultDisplay.height, 0)
                    }
                }
                windowManager.updateViewLayout(window!!.decorView, winAttr)
                true
            }.setIcon(R.drawable.pin).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

            menu.add(0, 0, 0, R.string.fullscreen).setOnMenuItemClickListener {
                isMaximizze = !isMaximizze
                if (isMaximizze) {
                    it.setIcon(R.drawable.restore_full)
                    animateWidth(activity.windowManager.defaultDisplay.height,activity.windowManager.defaultDisplay.width+ n.navigationBarHeight)
                } else {
                    it.setIcon(R.drawable.maximize)
                    animateWidth(activity.windowManager.defaultDisplay.width + n.navigationBarHeight, activity.windowManager.defaultDisplay.height)
                }
                windowManager.updateViewLayout(window!!.decorView, winAttr)
                true
            }.setIcon(R.drawable.maximize).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        return true
    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            dismiss()
        }
        return super.onOptionsItemSelected(item)
    }



    fun animateWidth(s:Int, e:Int){
        val animator = ValueAnimator.ofInt(s, e);
        animator.setDuration(200);
        animator.addUpdateListener { animation ->
            val animatedValue = animation.getAnimatedValue();
            if (animatedValue is Int) {
                window!!.setLayout(
                    animatedValue,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        };
        animator.start();
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