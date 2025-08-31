package app.wetube.page

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Fragment
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import app.wetube.MainActivity
import app.wetube.MainActivity.Companion.DIALOG_DATA_CORRUPT
import app.wetube.R
import app.wetube.SupaContainer
import app.wetube.VideoView
import app.wetube.adapter.NoteAdap
import app.wetube.core.FirstReview
import app.wetube.core.convertToId
import app.wetube.core.fadeIn
import app.wetube.core.fadeOut
import app.wetube.core.hideKeyBoard
import app.wetube.core.info
import app.wetube.core.isTv
import app.wetube.core.releaseParent
import app.wetube.databinding.ActivityNewVidBinding
import app.wetube.databinding.VideosFragmentBinding
import app.wetube.item.Video
import app.wetube.manage.db.VidDB
import app.wetube.openVideo
import app.wetube.page.dialog.NewVidDialog
import app.wetube.page.dialog.ShowCaseDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.cocosw.undobar.UndoBarStyle
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Random

class MySavedVideo :
    Fragment(),
    NoteAdap.OnAdapterListener{
        val f by lazy { FirstReview(activity) }
    private val db by lazy { VidDB(activity!!.applicationContext, null) }
    private val sp by lazy { PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext) }
    private val bin by lazy { VideosFragmentBinding.inflate(activity!!!!.layoutInflater) }
    private var size = 0
    private var listview = false
    val listsample: ArrayList<String> = arrayListOf()
    val listAdap by lazy{ ArrayAdapter(activity!!!!, android.R.layout.simple_list_item_1, listsample) }
    private val noteAdap by lazy { NoteAdap(ArrayList(), (activity), this) }

    var data
        get() = noteAdap.getData()
        set(value) {noteAdap.setdata(value)}
    companion object{
        var seMo = false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance =false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        seMo = false
    }




    override fun onStart() {
        loadlist()
        super.onStart()
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
    }
    override fun onHiddenChanged(hidden: Boolean) {
        if(!seMo) {
            loadlist()
        }
        if(hidden){
            if(noteAdap.mode != null) {
                noteAdap.mode!!.finish()
            }
        }
        view?.hideKeyBoard()
        super.onHiddenChanged(hidden)
    }

    override fun onResume() {
        loadlist()
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return bin.root
    }
    
    val selLis = object : AbsListView.MultiChoiceModeListener{
        val listener = this@MySavedVideo
        val selist get() = noteAdap.selist
        val mine get() = noteAdap.getData()
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
            noteAdap.mode = a
            setTitle(a)
            a.subtitle = ""
            m?.let {m->
                m.add(0,R.id.del,0,R.string.del).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setIcon(R.drawable.delete)
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
                noteAdap.notifyDataSetChanged()
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
                noteAdap.notifyDataSetChanged()
                mode?.let { setTitle(it)}
                return true
            }
            if(it?.itemId == R.id.del){
                onDeleteList(selist)
                return true
            }
           return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            noteAdap.mode = null
            selist.clear()
        }

    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateView()
        bin.videosList.let {
            it.onItemClickListener = AdapterView.OnItemClickListener{a,_,i,_->
                val note = noteAdap.getData()[i]
                onClick(note, i, a)
            }
            it.adapter = noteAdap
            it.choiceMode = AbsListView.CHOICE_MODE_MULTIPLE_MODAL
            it.setMultiChoiceModeListener(selLis)
            val s  = Point()
            activity!!.windowManager.defaultDisplay.getSize(s)
            if(!resources.getBoolean(R.bool.tablet)){
                if(s.x > s.y) {
                    //land
                    it.numColumns = 2
                }else{
                    //pot
                    it.numColumns = 1
                }
            }else{
                //                                                                   pot    land
                it.numColumns = if(s.x < s.y) 2 else 3
            }
        }

        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun changeView(type : Boolean){
        if(type){
            fadeOut(bin.videosList)
            fadeIn(bin.list)
        }else{
            fadeOut(bin.list)
            fadeIn(bin.videosList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseParent()
    }


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
        val s = menu.findItem(R.id.app_bar_search)
        s.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {


            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                loadlist()

                return true
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }
        })
        if(!sp.getBoolean("forceWeIcon", false)){
            if(sp.getString("theme", "w") != "w"){
                s.setIcon(android.R.drawable.ic_menu_search)
            }
        }
        try {
            (s.actionView as SearchView).apply {
                queryHint = getString(android.R.string.search_go)
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        findOut(query)
                        return true
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        return true
                    }
                })
            }
        }catch (_:Exception){
            SearchView(activity!!).apply {
                queryHint = getString(android.R.string.search_go)
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        findOut(query)
                        return true
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        return true
                    }
                })
                s.actionView = this
            }
        }

        if(activity!!.isTv){
            menu.add(0, R.id.add, 0, R.string.add)
        }



        super.onCreateOptionsMenu(menu, inflater)
    }


    fun onItemClick(itemId: Int) {
        when(itemId){
            R.id.reload -> loadlist()
            R.id.add -> {
                activity?.showDialog(VideoView.ADD_DIALOG, NewVidDialog.newB(""){
                    loadlist()
                    activity?.info(R.string.vid_add)
                })
            }
            R.id.sel -> noteAdap.actionMode()
            R.id.shuffle ->{
                CoroutineScope(IO).launch {
                    val playlist = db.listAsList()
                    activity!!!!.runOnUiThread {
                        if (playlist.size >= 2) {
                            val i = Random().nextInt(playlist.size - 1)
                            onClick(playlist[i], i, bin.root)
                        } else {
                            info(getString(R.string.no_vid))
                        }
                    }
                }
            }

        }
    }


    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onItemClick(item.itemId)
        return true
    }


    private fun updateView(){

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val s  = Point()
        updateView()

        activity!!!!.windowManager.defaultDisplay.getSize(s)
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

        super.onConfigurationChanged(newConfig)
    }


    override fun onClick(note: Video, position: Int, view: View) {
        //ovp = open video page not activity!!
        Runnable{
            openVideo(
                activity!!,
                note,
                position,
                db.listAsList().toTypedArray(),
                view
            )

        }.let {
            if(activity?.isTv == true){
                val `in` = DialogInterface.OnClickListener{a, o->
                    when(o){
                        DialogInterface.BUTTON_POSITIVE -> {
                            it.run()
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                            this.onDelete(note)
                        }

                    }
                }
                AlertDialog.Builder(activity)
                    .setMessage(note.title)
                    .setPositiveButton (R.string.watch_trailer_1, `in`)
                    .setNegativeButton (R.string.del, `in`).show()
            }else{
                it.run()
            }
        }





    }
    private fun deleteAction(note: Video, fail:Boolean, quite:Boolean = false){
        if(!fail) {
            db.doing {
                it.deleteVideoByVideoId(note.videoId)
            }
            loadlist()
            if(!quite) {
                info(R.string.vid_del, UndoBarStyle(R.drawable.undo, R.string.undo), {
                    db.doing {
                        it.insert(note)
                    }
                    loadlist()
                })
            }

        }else{
            info("Video not delete")
        }
        noteAdap.mode?.finish()
    }
    override fun onDelete(note: Video) {
        val listener = DialogInterface.OnClickListener{d,i->
            if(sp.getBoolean("needOnAdd", false)){
                DialogPass.newInstance(activity!!){
                    deleteAction(note, !it)
                }.show()
            }else{
                deleteAction(note, false)
            }

        }
        AlertDialog.Builder(activity!!).apply {
            setTitle(note.title)
            setMessage(R.string.del_com)
            setPositiveButton(R.string.del, listener)
            setNegativeButton(android.R.string.cancel, null)
        }.show()
    }

    override fun onShare(note: Video, videoId: String?, drawable: Drawable?) {
        val i = Intent(Intent.ACTION_SEND)
        if(drawable != null){
            val imagefolder = File(activity!!!!.cacheDir, "images")
            var uri: Uri? = null
            try {
                imagefolder.mkdirs()
                val file = File(imagefolder, "shared_image.png")
                val outputStream = FileOutputStream(file)
                val bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
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
            // putting uri of image to be shared
            i.apply{
                putExtra(Intent.EXTRA_STREAM, uri)

                // setting type to image
                setType("image/png")

            }
        }
        if(videoId != null){
            i.apply {
                putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=$videoId")
                setType("text/plain")
            }
        }
        startActivity(Intent.createChooser(i, getString(R.string.share)))
    }
    private fun getOutside() = (activity!!!! as MainActivity)
    private fun shareBitmap(vid:String):Intent{
        val intent = Intent(Intent.ACTION_SEND)
        Glide.with(activity!!!!)
            .load("https://i.ytimg.com/vi/${vid}/hqdefault.jpg")
            .centerCrop()
            .error(R.drawable.info)
            .into<SimpleTarget<GlideDrawable>>(object :
                SimpleTarget<GlideDrawable>(100,100) {
                override fun onResourceReady(
                    drawable: GlideDrawable,
                    transition: GlideAnimation<in GlideDrawable>,
                ) {
                    val imagefolder = File(activity!!!!.cacheDir, "images")
                    var uri: Uri? = null
                    try {
                        imagefolder.mkdirs()
                        val file = File(imagefolder, "shared_image.png")
                        val outputStream = FileOutputStream(file)
                        val bitmap = Bitmap.createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888
                        )
                        val canvas: Canvas = Canvas(bitmap)
                        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
                        drawable.draw(canvas)
                        (bitmap).compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                        outputStream.flush()
                        outputStream.close()
                        uri = FileProvider.getUriForFile(activity!!!!, "app.wetube.image-share", file)
                    } catch (e: Exception) {
                        Toast.makeText(activity!!!!, e.message, Toast.LENGTH_LONG).show()
                    }

                    // putting uri of image to be shared
                    intent.apply{
                        putExtra(Intent.EXTRA_STREAM, uri)

                        // setting type to image
                        setType("image/png")

                    }

                }
            })
    return intent
    }

    override fun onDestroy() {

        super.onDestroy()
    }

    override fun onEdit(note: Video) {

        val v = ActivityNewVidBinding.inflate(activity!!.layoutInflater)
        v.textView3.visibility = View.GONE
        v.textView4.visibility = View.GONE
        v.editTextText3.setText(note.title)
        v.editTextText4.setText(note.videoId)
        v.editBtn.setOnClickListener {
            v.editTextText4.convertToId()
        }
        activity?.showDialog(MainActivity.DIALOG_INFO, object :MainActivity.DialogBlock(){
            init {
                listener = DialogInterface.OnClickListener{d,i->
                    if(i == DialogInterface.BUTTON_POSITIVE){
                        if (v.editTextText3.text.toString()
                                .isNotEmpty() && v.editTextText4.text.toString().isNotEmpty()
                        ) {
                            db.doing {
                                it.updateVideo(
                                    Video(
                                        note.id,
                                        v.editTextText3.text.toString(),
                                        v.editTextText4.text.toString()
                                    )
                                )
                            }
                            loadlist()
                            val dis = {}
                            val click: (() -> Unit) = {
                                CoroutineScope(IO).launch {
                                    db.doing {
                                        it.updateVideo(note)
                                    }
                                    activity!!!!.runOnUiThread {
                                        loadlist()
                                    }
                                }
                            }
                            try{
                                activity.info(
                                    R.string.vid_upd,
                                    UndoBarStyle(R.drawable.undo, R.string.undo),
                                    click,
                                    dis
                                )
                            }catch (_: Exception){
                                SupaContainer.dingDong(R.string.vid_upd)
                            }


                        } else {
                            try{
                                activity.info(R.string.vid_upd)
                            }catch (_: Exception){
                                SupaContainer.dingDong("Video didn't update")
                            }
                        }
                    }
                }
            }
            override fun onMaking(b: AlertDialog.Builder) {
                v.root.parent.let{
                    if(it is ViewGroup){
                        it.removeView(v.root)
                    }
                }

                b.apply{
                    setView(v.root)
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton("Update", listener)
                    setNeutralButton("Convert to id", null)
                }
            }

            override fun onDialogCreate(d: AlertDialog) {
                d.getButton(DialogInterface.BUTTON_NEUTRAL)?.setOnClickListener {
                    v.editTextText4.convertToId()
                }
            }
        }.toBundle())


    }
    fun addAction(s : Pair<String,String>, fail : Boolean){
        if(!fail){
            CoroutineScope(IO).launch {
                db.doing {
                    it.insert( s.first, s.second)
                }
                activity!!!!.runOnUiThread {
                    loadlist()
                    activity!!.info(R.string.vid_add)

                }
            }
        } else{
            activity!!.info(
                "Unable to permission to add video"
            )
        }
    }


    fun add(videoid : String?){
        val b = android.app.AlertDialog.Builder(activity!!!!)
        val v = ActivityNewVidBinding.inflate(activity!!!!.layoutInflater)
        v.textView4.visibility = View.GONE
        db.doing {
            val num = db.listAsList().size + 1
            activity!!!!.runOnUiThread{
                v.editTextText3.setText("Video ${num}" )
            }
        }
        val title = v.editTextText3
        val idv = v.editTextText4
        idv.setText(videoid)
        v.editBtn.setOnClickListener {
            idv.convertToId()
        }
        with(b) {
            setView(v.root)
            setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
            setPositiveButton(R.string.add) { d, i ->
                val isNotEmpty = title.text.toString().isNotEmpty() && idv.text.toString().isNotEmpty()
                if (isNotEmpty) {
                    if(sp.getBoolean("needOnAdd", false)){
                        DialogPass.newInstance(activity!!!!) {
                            addAction(Pair(title.text.toString(), idv.text.toString()), !it)
                        }.show()
                    }
                    else{
                        addAction(Pair(title.text.toString(), idv.text.toString()), false)
                    }

                }else activity!!.info(
                    "Unable to add video",

                )

            }
        }.create().apply{

            show()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun loadlist() {
        try{
            val notes = db.listAsList()

            listAdap.clear()
            noteAdap.setdata(notes)
            listAdap.addAll(notes.map { i -> i.title })
            if (notes.isEmpty()) {
                fadeOut(bin.videosList)
                fadeOut(bin.list)
                fadeIn(bin.emptyContainer)
            } else {
                fadeOut( bin.emptyContainer)
                fadeIn(bin.videosList)
            }
            size = notes.size
            Log.d("DB", notes.toString())
        }catch (_: Exception){
            activity?.showDialog(DIALOG_DATA_CORRUPT)
        }




    }


    private fun findOut(q : String) {
        val notes = db.listAsList()
        Log.d("DB", notes.toString())


        listAdap.clear()
        noteAdap.setdata(notes.filter { it.title.contains(q, true) })
        listAdap.addAll(notes.map { i -> i.title })
        if (notes.isEmpty()) {
            fadeOut(bin.videosList)
            fadeOut(bin.list)
            fadeIn(bin.emptyContainer)
        } else {
            fadeOut( bin.emptyContainer)
            fadeIn(bin.videosList)
        }
        size = notes.size

    }
    override fun onDeleteList(list: MutableList<Video>):Boolean {
        var isConfirm = false

        synchronized(this) {
            android.app.AlertDialog.Builder(activity!!).apply {
                setTitle(R.string.del_sel_vid)
                setSingleChoiceItems(
                    ArrayAdapter(
                        activity!!,
                        android.R.layout.simple_list_item_1,
                        list.map { it.title }), 0, null
                )
                setPositiveButton(R.string.del) { _, _ ->
                    Runnable {
                        scaryDel(list, false)
                        isConfirm = true
                    }.let { r ->
                        if (sp.getBoolean("needOnAdd", false)) {
                            DialogPass.newInstance(activity!!) {
                                r.run()
                            }.show()
                        } else {
                            r.run()
                        }
                    }

                }
                setNegativeButton(android.R.string.cancel, null)
            }.create().apply {
                show()
            }
        }

        return isConfirm
    }

    private fun scaryDel(list:MutableList<Video>, fail :Boolean){
        val delL = mutableSetOf<Video>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            noteAdap.mode?.hide((250*1000).toLong())
        }
        if (!fail) {
            list.forEach {v->
                Handler(activity!!.mainLooper).postDelayed({
                    db.doing {
                        it.deleteVideoByVideoId(v.videoId)
                        delL.add(v)
                    }
                    loadlist()
                },10L)
            }
            val dis:(() -> Unit) = {
                noteAdap.mode?.finish()
            }

            val ac:(() -> Unit) = {
                CoroutineScope(IO).launch {
                    try {
                        db.doing {
                            for (i in delL) {
                                Handler(activity!!!!.mainLooper ).postDelayed({
                                    if (db.listAsList().size < sp.getInt("limit", 50)) {
                                        db.insert(i)
                                        if(delL.isNotEmpty()) {
                                            if (i == delL.last()) {
                                                activity!!!!.runOnUiThread {
                                                    info("Video added")

                                                }
                                            }
                                        }
                                    }
                                },10L)

                            }
                        }
                    }catch (_:Exception){
                        info("Select videos corrupted")
                    }

                }
            }
            info(t = getString(R.string.vid_del), UndoBarStyle(R.drawable.undo, R.string.undo), ac, dis)

        }else{
            info("Canceled")
        }

    }

    override fun onSelectedList(pors: Int, list: MutableList<Video>) {
        val data = noteAdap.getData()[pors]
        val sl = noteAdap.selist
        if(sl.contains(data)) {
            sl.add(data)
        }else{
            sl.remove(data)
        }
        noteAdap.notifyDataSetChanged()
    }


    override fun onDetach() {
        super.onDetach()
        view?.hideKeyBoard()
    }

    fun secIn() {
        if(!f.isStorageFunctionReviewed){
            java.lang.Runnable{
                val o = object : ShowCaseDialog.OnToBuild{
                    override fun onToBuild(b: ShowcaseView.Builder) {
                        val d = activity?.actionBar?.height
                        val v = if(bin.videosList.visibility == View.VISIBLE) bin.videosList else bin.emptyContainer
                        b.setTarget(ViewTarget(v))
                            .setContentTitle("This is your library")
                            .setContentText("The place where you can save your videos")
                    }


                }
                ShowCaseDialog(activity,o).apply{
                    setOnDismissListener {
                        f.isStorageFunctionReviewed = true
                    }
                    show()
                }
            }.let {
                Handler(activity!!.mainLooper).postDelayed(it, 800L)
            }

        }
    }


}