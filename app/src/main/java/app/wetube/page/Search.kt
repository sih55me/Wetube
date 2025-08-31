package app.wetube.page


import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.Fragment
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.provider.SearchRecentSuggestions
import android.support.v4.content.FileProvider
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.ActionMode
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.PointerIcon
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListPopupWindow
import android.widget.SearchView
import android.widget.Toast
import app.wetube.ActivityDialog
import app.wetube.ChannelInfo
import app.wetube.MainActivity
import app.wetube.R
import app.wetube.adapter.HistoryAdapter
import app.wetube.adapter.SearchAdap
import app.wetube.adapter.SearchAdap.Option
import app.wetube.core.FirstReview
import app.wetube.core.Search
import app.wetube.core.ShareType
import app.wetube.core.fadeIn
import app.wetube.core.fadeOut
import app.wetube.core.hideKeyBoard
import app.wetube.core.info
import app.wetube.core.isTv
import app.wetube.core.tryOut
import app.wetube.core.tul
import app.wetube.databinding.FragmentLsBinding
import app.wetube.databinding.TokenmanagerBinding
import app.wetube.item.ChannelDetail
import app.wetube.item.Video
import app.wetube.item.VideoDetail
import app.wetube.kembaliKe
import app.wetube.manage.SearchManager
import app.wetube.manage.db.FavChaDB
import app.wetube.manage.db.HistoryDB
import app.wetube.manage.db.VidDB
import app.wetube.openVideo
import app.wetube.page.dialog.SearchDetail.Companion.CHANNELID
import app.wetube.page.dialog.SearchDetail.Companion.ORDER
import app.wetube.page.dialog.SearchDetail.Companion.QUERY
import app.wetube.page.dialog.SearchDetail.Companion.TOKEN
import app.wetube.page.dialog.ShowCaseDialog
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import kotlinx.coroutines.Runnable
import java.io.File
import java.io.FileOutputStream


class Search() : Fragment(), SearchAdap.OnAdapterListener {
    lateinit var sp: SharedPreferences
    var pT = ""
    var openSearch = Runnable{}
    var searchToNewTab = true
    val myPage = mutableListOf<String>()
    var showItem : (Boolean)-> Unit = {

    }

    val f by lazy { FirstReview(activity) }
    var onSearch = false
    var d : ListPopupWindow? = null
    private var query = ""
    var channelId = ""
    var filter = ""
    val hisadap by lazy{ HistoryAdapter(hidb.listAsList(), activity, keyHistory)}
    val db by lazy{VidDB(activity!!.applicationContext)}
    val cdb by lazy{ FavChaDB(activity!!.applicationContext)}
    val hidb by lazy{HistoryDB(activity!!.applicationContext)}
    var search: SearchView? = null
    protected val suggestions: SearchRecentSuggestions by lazy {
        SearchRecentSuggestions(
            activity!!,
            SearchManager.AUTHORITY,
            SearchManager.MODE
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    val selLis = object : AbsListView.MultiChoiceModeListener{
        val listener = this@Search
        val selist get() = adap.selist
        val mine get() = adap.data
        override fun onItemCheckedStateChanged(
            mode: ActionMode?,
            position: Int,
            id: Long,
            checked: Boolean,
        ) {
            val note = mine[position]
            if(checked and !selist.contains(note)){
                selist.add(note)
            }else {
                selist.remove(note)
            }
            mode?.let { setTitle(it) }
        }

        fun setTitle(a: ActionMode){
            a.title = "${selist.size} Selected"
        }

        override fun onCreateActionMode(
            a: ActionMode?,
            m: Menu?,
        ): Boolean {
            if(a == null)return false
            adap.mode = a
            setTitle(a)
            a.subtitle = ""
            m?.let {m->
                m.add(0,R.id.add,0,R.string.add).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setIcon(
                    R.drawable.playlist_add
                )
                m.add("Select all").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
                m.add("Cek").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
                m.add("Unselect all").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
            }
            return true
        }

        override fun onPrepareActionMode(
            mode: ActionMode?,
            menu: Menu?,
        ): Boolean {
            mode?.let { setTitle(it) }
            return true
        }

        override fun onActionItemClicked(
            mode: ActionMode?,
            it: MenuItem?,
        ): Boolean {
            if(it?.title == "Cek"){
                if(activity == null)return false
                AlertDialog.Builder(activity).apply {
                    setSingleChoiceItems(ArrayAdapter(activity, android.R.layout.simple_list_item_1, selist.map { it.title }),0,null)
                    setTitle("Selected")
                }.show()
                return true
            }
            if(it?.title == "Select all"){
                selist.clear()
                bin.videosList.clearChoices()
                adap.notifyDataSetChanged()
                mine.forEach {
                    bin.videosList.setItemChecked(mine.indexOf(it), true)
                }
                if(selist.isEmpty()){
                    selist.addAll(mine)
                }
                mode?.let { setTitle(it) }
                return true
            }
            if(it?.title == "Unselect all"){
                selist.clear()
                bin.videosList.clearChoices()
                adap.notifyDataSetChanged()
                mode?.let { setTitle(it)}
                return true
            }
            if(it?.itemId == R.id.add){
                onAddList(selist)
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            adap.mode = null
            selist.clear()
        }

    }



    var titleBar = ""
    
    

    protected val searchManager: android.app.SearchManager by lazy {
        activity!!.getSystemService(Context.SEARCH_SERVICE) as android.app.SearchManager
    }

    companion object{
        const val CREATE_TO_SEARCH = "createToSearch"

        /**
         * add this to NOT show option menu
         */
        const val KIOSK = "kiosk"


        fun newTab(query : String = "", channelId : String= "", tok: String = "", filter: String = ""): Fragment{
            val n = Search()
            n.arguments = Bundle().apply {
                putString(QUERY, query)
                putString(CHANNELID, channelId)
                putString(TOKEN, tok)
                putString(ORDER, filter)
                putBoolean(CREATE_TO_SEARCH, true)
                putBoolean(KIOSK, true)
            }
            return n
        }
    }
    var s2 = Search.MODERATE
    val bin by lazy { FragmentLsBinding.inflate(activity!!.layoutInflater) }
    val adap by lazy { SearchAdap(activity!!, ArrayList(), this) }
    override fun onDestroy() {
        super.onDestroy()
        titleBar = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance =false
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        val s  = Point()
        activity!!.windowManager.defaultDisplay.getSize(s)
        if(!resources.getBoolean(R.bool.tablet)){
            when(newConfig.orientation){
                Configuration.ORIENTATION_LANDSCAPE -> {
                    bin.videosList.numColumns = 2
                }

                else -> {
                    bin.videosList.numColumns = 1
                }
            }
        }else{
            bin.videosList.numColumns =  if(s.x < s.y) 2 else 3
        }
        updateView()

        super.onConfigurationChanged(newConfig)
    }

    override fun tagChannel(channelDetail: ChannelDetail, newTab: Boolean) {

        if(newTab.not()) {

            info("Channel id copy")
            val clipboard: ClipboardManager = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", channelDetail.id)
            clipboard.setPrimaryClip(clip)

        }else {
            val i = Intent(activity!!, ChannelInfo::class.java).putExtra("id", channelDetail.id).putExtra("name", channelDetail.title)
            i.kembaliKe(activity)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(
                i
            )
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View{
        return bin.root
    }

    val si by lazy{searchManager.getSearchableInfo(activity!!?.componentName)}
    override fun onDetach() {
        d?.dismiss()
        d = null
        super.onDetach()
    }
    override fun onStart() {
        filter = getString(R.string.none)
        super.onStart()
        setHasOptionsMenu((arguments?.getBoolean(KIOSK))?.not() ?: true)

    }
    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search, menu)
        showItem = {
            val ids = listOf(R.id.reskey, R.id.nexkey, R.id.del)
            for(i in ids){
                try {
                    menu.findItem(i).setVisible(it)
                }catch (_:Exception){}
            }

        }
        tryOut(activity!!) {
            if (true) {
                search = SearchView(activity.actionBar?.themedContext ?: activity).apply {
                    queryHint = getString(android.R.string.search_go)
                    setSearchableInfo(si)
                }
            }
            menu.findItem(R.id.app_bar_search).apply {
                openSearch = Runnable {
                    this.expandActionView()
                }
                isVisible = true
                if (!sp.getBoolean("forceWeIcon", false)) {
                    if (sp.getString("theme", "w") != "w") {
                        setIcon(android.R.drawable.ic_menu_search)
                    }
                }
                actionView = search
                setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW or MenuItem.SHOW_AS_ACTION_ALWAYS)
                setOnActionExpandListener(searchBehav)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    val keyHistory = object : HistoryAdapter.HistoryListener{
        override fun onClick(text: CharSequence) {
            d?.dismiss()
            d = null
            activity?.window?.tul?.collapseActionView()
            search(text.toString(), channelId, false, filter)
        }

        override fun onUp(text: CharSequence) {
            search?.setQuery(text, false)
        }

        override fun onDelete(text: CharSequence) {
            AlertDialog.Builder(activity!!).setTitle(text).setMessage("Delete this from history?").setPositiveButton(R.string.del){_,_->
                info(if(hidb.deleteByName(text.toString()))"Deleted" else "Fail to deleted")
                loadHistory()
            }.setNegativeButton(android.R.string.cancel, null).show()
        }


    }

    val searchBehav = object : MenuItem.OnActionExpandListener {

        override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
            onSearch = false
            d?.dismiss()
            return true
        }

        override fun onMenuItemActionExpand(item: MenuItem): Boolean {
            onSearch = true
            search?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    search(query, channelId, false, filter)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    this@Search.withQuery(newText, item)
                    return true
                }
            })
            Handler(activity!!.mainLooper).postDelayed({
                search?.setQuery(query, false)
            }, 30L)
            withQuery("", item)
            return true
        }


    }
    fun withQuery(string: String, item: MenuItem) {
        if(!onSearch)return

        if(string.isNotEmpty()){
            hisadap.clear()
            val r = hidb.listAsList().filter { it.contains(string, true) }

            hisadap.addAll(r)
        }else{
            loadHistory()
        }
        if(hisadap.isEmpty){
            d?.dismiss()
            return
        }
        if(d == null){

            d = ListPopupWindow(activity!!).apply {
                anchorView = item.actionView
                isModal = false

                setInputMethodMode(ListPopupWindow.INPUT_METHOD_NEEDED)
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                setOnDismissListener {
                    d = null
                }
                Handler(Looper.myLooper()!!).postDelayed({
                    try {
                        show()
                        listView?.setOverScrollMode(View.OVER_SCROLL_NEVER)
                    } catch (_: Exception) {
                        d = null
                    }
                }, 50L)
            }
        }
        d?.setAdapter(hisadap)

    }

    fun manageDialog(){
        val view = TokenmanagerBinding.inflate(activity!!.layoutInflater)
        Dialog(activity!!).apply {
            window!!.requestFeature(Window.FEATURE_NO_TITLE)
            setContentView(view.root)
            window!!.setGravity(Gravity.TOP)
            show()
        }
        view.apply {
            reset.setOnClickListener {
                pT = ""
                myPage.apply {
                    clear()
                }
                Toast.makeText(activity!!, "Done", Toast.LENGTH_LONG).show()
                search(query, channelId, true, filter)
            }
            back.setOnClickListener{
                if(myPage.size >= 2) {
                    try{
                        val num = myPage.indexOf(pT) - 1
                        pT = myPage[num]
                        Toast.makeText(activity!!, "Previous", Toast.LENGTH_LONG).show()
                        search(query, channelId, false, filter)
                    }catch (e:Exception){
                        AlertDialog.Builder(activity!!).setMessage("\"Prev key\" and \"Next key\" button  is broken, please try again").setPositiveButton(android.R.string.ok,null).show()
                    }
                }else if(myPage.size == 1) {
                    Toast.makeText(activity!!, "You touch the end", Toast.LENGTH_LONG).show()
                }
            }
            forward.setOnClickListener{
                if(myPage.last() == pT) {
                    try{
                        Toast.makeText(activity!!, "Forward", Toast.LENGTH_LONG).show()
                        search(query, channelId, true, filter)
                    }catch (e:Exception){
                        AlertDialog.Builder(activity!!).setMessage("\"Prev key\" and \"Next key\" button  is broken, please try again").setPositiveButton(android.R.string.ok,null).show()
                    }
                }else if(myPage.last() != pT){
                    try{
                        val num = myPage.indexOf(pT) + 1
                        pT = myPage[num]
                        Toast.makeText(activity!!, "Forward", Toast.LENGTH_LONG).show()
                        search(query, channelId, false, filter)
                    }catch (e:Exception){
                        AlertDialog.Builder(activity!!).setMessage("\"Prev key\" and \"Next key\" button  is broken, please try again").setPositiveButton(android.R.string.ok,null).show()
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.manage -> manageDialog()
            R.id.reskey -> {
                pT = ""
                Toast.makeText(activity!!, "Done", Toast.LENGTH_LONG).show()
                search(query, channelId, true, filter)
            }
            R.id.channelId -> {
                AlertDialog.Builder(activity!!).apply {
                    setTitle("Channel Id")
                    val text = EditText(activity!!)
                    text.text = SpannableStringBuilder(channelId)
                    setView(text)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        channelId = text.text.toString()
                    }
                    setNegativeButton(android.R.string.cancel, null)
                }.show()
            }
            R.id.del -> {
                deleteResult()
            }
            R.id.app_bar_search ->{

            }
            R.id.nexkey -> {
                Toast.makeText(activity!!, "Next page....", Toast.LENGTH_LONG).show()
                search(query, channelId, true, filter)
            }

            R.id.filter ->{
                android.app.AlertDialog.Builder(activity!!).apply {
                    setTitle(R.string.order)
                    var num = 0
                    val fil = arrayOf(
                        getString(R.string.none),
                        activity!!.getString(R.string.rating),
                        activity!!.getString(R.string.nv),
                        activity!!.getString(R.string.date),
                        activity!!.getString(R.string.vidcount),
                        activity!!.getString(R.string.viewcount)
                    )
                    setSingleChoiceItems(fil, fil.indexOf(filter)) { _, i ->
                        num = i
                    }
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        filter = fil[num]
                        info("Filter changed!", )
                    }
                }.show()
            }

            R.id.cusSea ->{
                Bundle().apply {
                    putString(QUERY, query)
                    putString(CHANNELID, channelId)
                    putBoolean(TOKEN, pT.isNotEmpty())
                    putString(ORDER, filter)
                }.let {
                    activity?.showDialog(MainActivity.DIALOG_SEARCH, it)
                }

            }
        }
        return true
    }




    override fun onViewCreated(view:View, savedInstanceState: Bundle?) {
        var isHere = false
        if(savedInstanceState != null){
            try {
                val da = savedInstanceState.getParcelableArray("list")?.map { it as VideoDetail }?.toMutableList()
                val ce = savedInstanceState.getParcelableArray("cenels")?.map { it as ChannelDetail }?.toMutableList()
                val de = savedInstanceState.getStringArray("descs")?.toMutableList()
                val th = savedInstanceState.getStringArray("thumbs")?.toMutableList()
                query = savedInstanceState.getString("query", "")
                filter = savedInstanceState.getString("filter", "")
                pT = savedInstanceState.getString("key", "")
                channelId = savedInstanceState.getString("channelId", "")
                if (da != null) {
                    val e = mutableListOf<VideoDetail>()
                    da.forEach { v ->
                        val index = da.indexOf(v)
                        e.add(
                            VideoDetail(v.videoId, v.title, de?.get(index) ?: "", th?.get(index) ?:"", ce?.get(index) ?: ChannelDetail("", ""))
                        )
                    }
                    adap.setData(da)
                }
                showItem(true)
            }
            catch (e:Exception){
                ActivityDialog.make(activity!!, "Cannot load search property", e.message ?: "Unknown")
            }
//            Handler(activity!!.mainLooper).postDelayed({
//                fadeIn(bin.videosList)
//                fadeOut(bin.emptyContainer)
//            },100L)

        }else{
            showItem(false)
        }

        loadHistory()
        bin.insturcBtn.apply {

            setOnClickListener {
                if(context.isTv){
                    Bundle().apply {
                        putString(QUERY, query)
                        putString(CHANNELID, channelId)
                        putBoolean(TOKEN, pT.isNotEmpty())
                        putString(ORDER, filter)
                    }.let {
                        activity?.showDialog(MainActivity.DIALOG_SEARCH, it)
                    }
                }else{
                    openSearch.run()
                }
            }
            setOnLongClickListener{
                it.callOnClick()
                true
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setOnContextClickListener{
                    it.callOnClick()
                    true
                }
            }

        }
        sp = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
        s2 = if(!sp.getBoolean("safe", true)){
            "moderate"
        }else{
            "strict"
        }
        bin.videosList.apply {
            val s = Point()
            isFastScrollEnabled = true

            choiceMode = AbsListView.CHOICE_MODE_MULTIPLE_MODAL
            setMultiChoiceModeListener(this@Search.selLis)
            activity!!.windowManager.defaultDisplay.getSize(s)
            if(!resources.getBoolean(R.bool.tablet)){
                if(s.x > s.y) {
                    bin.videosList.numColumns =  2
                }else{
                    bin.videosList.numColumns = 1
                }
            }else{
                bin.videosList.numColumns =if(s.x < s.y) 2 else 3
            }
            onItemClickListener = AdapterView.OnItemClickListener{a,_,i,_->
                val note = adap.data[i]
                onClick(note, i, a)
            }
            adapter = adap

        }
        if (adap.isEmpty().not()) {
            fadeIn(bin.videosList)
            fadeOut(bin.emptyContainer)
        } else {
            fadeOut(bin.videosList)
            fadeIn(bin.emptyContainer)
        }

        updateView()
        super.onViewCreated(view, savedInstanceState)




        if(arguments?.getBoolean(CREATE_TO_SEARCH) == true){
            arguments?.let {
                query = it.getString(QUERY, "")
                channelId = it.getString(CHANNELID, "")
                filter = it.getString(ORDER, "")
                pT = it.getString(TOKEN, "")
            }
            search(query, channelId, true, filter)
        }
        if(!f.isSearchFunctionReviewed){
            java.lang.Runnable{
                val o = object : ShowCaseDialog.OnToBuild{
                    override fun onToBuild(b: ShowcaseView.Builder) {
                        b.setTarget(ViewTarget(bin.insturcBtn))
                            .setContentTitle("Easy to search video with one click")
                            .setContentText("Click \"Open search\" to open the searchbar")
                    }

                }
                ShowCaseDialog(activity,o).apply{
                    hasNext = true
                    setOnDismissListener {
                        f.isSearchFunctionReviewed = true
                        activity?.actionBar?.setSelectedNavigationItem(2)
                        activity?.fragmentManager?.findFragmentByTag("m").let {
                            if(it is MySavedVideo){
                                it.secIn()
                            }
                        }
                    }
                    show()
                }
            }.let {
                Handler(activity!!.mainLooper).postDelayed(it, 800L)
            }

        }
    }



    private fun loadHistory() {
        hisadap.clear()
        hisadap.addAll(hidb.listAsList())
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("channelId", channelId)
        outState.putString("key", pT)
        outState.putString("query", query)
        outState.putParcelableArray("list", adap.data.toTypedArray())
        outState.putParcelableArray("cenels", adap.data.map{it.channel}.toTypedArray())
        outState.putStringArray("descs", adap.data.map{it.description}.toTypedArray())
        outState.putStringArray("thumbs", adap.data.map{it.thumb}.toTypedArray())
        outState.putString("filter", filter)
        super.onSaveInstanceState(outState)
    }



    @SuppressLint("RestrictedApi")
    fun updateView(){


    }


    @SuppressLint("RestrictedApi")
    override fun onHiddenChanged(hidden: Boolean) {
        try {
            if (hidden) {
                d?.dismiss()
                d = null
                titleBar = ""
                try{
                    dismissISP()
                    view?.hideKeyBoard()
                    adap.mode?.finish()
                }catch (_:Exception){}
            } else {
                titleBar = query
                if(search != null){
                    search!!.setQuery(query, false)
                }
                if(sp.getBoolean("clear_cache", false)){
                    deleteResult()
                }

            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun dismissISP() {
        if(bin.insturcBtn.tag is ActionMode){
            (bin.insturcBtn.tag as ActionMode).finish()
        }
    }

    private fun deleteResult() {
        titleBar = ""
        adap.clearData()
        showItem(false)
        fadeIn(bin.emptyContainer)
        fadeOut(bin.videosList)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        showItem(adap.data.isNotEmpty())
        super.onPrepareOptionsMenu(menu)
    }

    override fun onDestroyView() {
        bin.videosList.adapter = null
        bin.root.apply {
            if(parent is ViewGroup){ (parent as ViewGroup).removeView(this) }
        }
        super.onDestroyView()
    }
    override fun onClick(video: VideoDetail, position: Int, view: View?) {
        Runnable{
            var r: Video? = video
            if (sp.getBoolean("auto_add", false)) {
                db.doing {
                    r = db.listAsList().find {
                        it.videoId == video.videoId
                    }
                    if (r == null) {
                        db.insert(
                            video
                        )
                        r = video
                    }
                }
            }
            openVideo(
                activity!!,
                r!!,
                position,
                adap.data.toTypedArray(),
                view,
                adap.data.map { it.channel }.toTypedArray()
            )
        }.let {
            if(activity?.isTv == true){
                val `in` = DialogInterface.OnClickListener{a, o->
                    when(o){
                        DialogInterface.BUTTON_POSITIVE -> {
                            it.run()
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                            (this as SearchAdap.OnAdapterListener).onOptionClick(video, Option.Save, ShareType.NO, null)
                        }

                    }
                }
                AlertDialog.Builder(activity)
                    .setTitle(video.title)
                    .setMessage(video.description)
                    .setPositiveButton (R.string.watch_trailer_1, `in`)
                    .setNegativeButton (R.string.add, `in`).show()
            }else{
                it.run()
            }
        }
    }
    override fun onOptionClick(video: VideoDetail, option: Option, type:ShareType, drawable: Drawable?) {
        when (option) {
            Option.Share -> {
                val shareIntent = Intent()
                when(type){
                    ShareType.URL -> {
                        val stxt = "youtube.com/watch?v=${video.videoId}"
                        shareIntent.action = Intent.ACTION_SEND
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(Intent.EXTRA_TEXT, stxt)
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, video.title)
                        startActivity(Intent.createChooser(shareIntent, "Share"))
                    }
                    ShareType.THUMB -> {
                        val imagefolder = File(activity!!!!.cacheDir, "images")
                        var uri: Uri? = null
                        try {
                            imagefolder.mkdirs()
                            val file = File(imagefolder, "shared_image.png")
                            val outputStream = FileOutputStream(file)
                            val bitmap = Bitmap.createBitmap(
                                drawable!!.getIntrinsicWidth(),
                                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888
                            )
                            val canvas = Canvas(bitmap)
                            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
                            drawable.draw(canvas)
                            (bitmap).compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                            outputStream.flush()
                            outputStream.close()
                            uri = FileProvider.getUriForFile(activity!!!!, "app.wetube.image-share", file)
                        } catch (e: Exception) {
                            Toast.makeText(activity!!!!, e.message, Toast.LENGTH_LONG).show()
                        }
                        shareIntent.apply{
                            putExtra(Intent.EXTRA_STREAM, uri)
                            // adding text to share

                            // Add subject Here

                            // setting type to image
                            setType("image/*")

                        }
                        shareIntent.action = Intent.ACTION_SEND
                        startActivity(Intent.createChooser(shareIntent, "Share"))
                    }
                    ShareType.ANY -> {
                        val imagefolder = File(activity!!!!.cacheDir, "images")
                        var uri: Uri? = null
                        try {
                            imagefolder.mkdirs()
                            val file = File(imagefolder, "shared_image.png")
                            val outputStream = FileOutputStream(file)
                            val bitmap = Bitmap.createBitmap(
                                drawable!!.getIntrinsicWidth(),
                                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888
                            )
                            val canvas = Canvas(bitmap)
                            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
                            drawable.draw(canvas)
                            (bitmap).compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                            outputStream.flush()
                            outputStream.close()
                            uri = FileProvider.getUriForFile(activity!!!!, "app.wetube.image-share", file)
                        } catch (e: Exception) {
                            Toast.makeText(activity!!!!, e.message, Toast.LENGTH_LONG).show()
                        }
                        shareIntent.apply{
                            putExtra(Intent.EXTRA_STREAM, uri)
                            // adding text to share

                            // Add subject Here

                            // setting type to image
                            setType("image/*")

                        }
                        val stxt = "youtube.com/watch?v=${video.videoId}"
                        shareIntent.action = Intent.ACTION_SEND
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(Intent.EXTRA_TEXT, stxt)
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, video.title)
                        startActivity(Intent.createChooser(shareIntent, "Share"))
                    }

                    ShareType.NO -> {}
                }
            }
            Option.Save -> {
                db.doing {
                    if (db.listAsList().size < sp.getInt("limit", 50)) {

                        db.insert(video.title, video.videoId)
                        activity!!!!.runOnUiThread {
                            Toast.makeText(activity!!!!,
                                "Video added",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }else Toast.makeText(activity!!!!,
                        "Unable to add video",
                        Toast.LENGTH_SHORT
                    ).show()



                }

            }
        }
    }


    fun search(query : String, channelId : String, continu : Boolean, order:String, nsh:Boolean = false){
        dismissISP()
        val c = activity ?: view?.context ?: bin.root.context
        if(false){
            Log.e("search", "activity!! is null, using view activity!!")
        }
        if(c == null){
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view?.pointerIcon = PointerIcon.getSystemIcon(c, PointerIcon.TYPE_WAIT)
        }
        this@Search.query = query
        this@Search.filter = order
        this@Search.channelId = channelId
        titleBar = query
        if(bin.videosList.visibility == View.VISIBLE){
            fadeOut(bin.videosList)
        }
        fadeOut(bin.emptyContainer)
        fadeIn(bin.progressBar2)
        if (adap.data.isNotEmpty()) {
            adap.clearData()
        }
        Log.i("Key Manager", myPage.joinToString())
        if (!continu) {
            pT = ""
        }
        if(search != null){
            search!!.setQuery(query, false)
        }

        if(c !is Activity){
            Toast.makeText(c, "No interaction", Toast.LENGTH_LONG).show()
            return
        }
        hidb.listAsList().apply {
            if (!contains(query)) {
                hidb.doing{
                    it.insert(query)
                }
            }
        }
        val sp = PreferenceManager.getDefaultSharedPreferences(activity!!)
        val list = (sp.getString("bc", "") ?: "").split(",").map { it.trim() }

        Search(c).searchVideo(
            query = query,
            max = 45,
            pT = pT,
            onGet = { video ->
                if(!list.contains(video.channel.id)){
                    adap.setData(video)
                }
            },
            order = when (order) {
                c.getString(R.string.rating) -> Search.RATING
                c.getString(R.string.nv) -> Search.TITLE
                c.getString(R.string.date) -> Search.DATE
                c.getString(R.string.vidcount) -> Search.VIDEO_COUNT
                c.getString(R.string.viewcount) -> Search.VIEW_COUNT
                else -> ""
            },
            channelId = channelId,
            onDone = { key ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    view?.pointerIcon = PointerIcon.getSystemIcon(c, PointerIcon.TYPE_ARROW)
                }
                myPage.add(key)
                pT = key
                fadeIn(bin.videosList)
                fadeOut(bin.emptyContainer)
                fadeOut(bin.progressBar2)
                loadHistory()
                showItem(true)
                if(adap.data.isEmpty()){
                    info("Not video available")
                }
            }
        )
    }

    override fun onAddList(list: MutableList<VideoDetail>) {
        android.app.AlertDialog.Builder(activity!!).apply {
            setTitle("Add selected video to ${getString(R.string.library)} ?")
            setSingleChoiceItems(ArrayAdapter(activity!!, android.R.layout.simple_list_item_1, list.map { it.title }), 0, null)
            setPositiveButton(R.string.add){_,_->
                db.doing {
                    Runnable{
                        for (i in list) {
                            Handler(Looper.myLooper()!!).postDelayed({
                                db.insert(i)
                            },10L)
                        }
                    }.let{r->
                        if (sp.getBoolean("needOnAdd", false)) {
                            DialogPass.newInstance(activity!!) {
                                if(it){
                                    r.run()
                                }
                            }.show()
                        } else {
                            r.run()
                        }
                    }
                    info("Selected videos added. Check out @ ${getString(R.string.library)}",)
                }

            }
            setNegativeButton(android.R.string.cancel, null)
        }.create().apply{
            
            show()
        }
    }

    override fun addCh(detail: ChannelDetail) {
        cdb.insert(detail)
        info("${detail.title} added to Favorite Channel")
    }
}