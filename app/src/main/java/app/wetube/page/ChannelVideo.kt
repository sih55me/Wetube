package app.wetube.page

import android.app.AlertDialog
import android.app.Fragment
import android.app.ProgressDialog
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
import android.support.v4.content.FileProvider
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.PointerIcon
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import app.wetube.ChannelInfo
import app.wetube.R
import app.wetube.VideoView
import app.wetube.adapter.SearchAdap
import app.wetube.adapter.SearchAdap.Option
import app.wetube.core.Search
import app.wetube.core.ShareType
import app.wetube.core.info
import app.wetube.databinding.ChannelvidBinding
import app.wetube.item.ChannelDetail
import app.wetube.item.Video
import app.wetube.item.VideoDetail
import app.wetube.manage.db.FavChaDB
import app.wetube.manage.db.VidDB
import app.wetube.openVideoNTicket
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import kotlinx.coroutines.Runnable
import java.io.File
import java.io.FileOutputStream

class ChannelVideo:Fragment(), SearchAdap.OnAdapterListener {
    val bin by lazy{ChannelvidBinding.inflate(activity!!.layoutInflater)}
    val sp: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(activity!!!!) }
    val db by lazy { VidDB(activity!!!!) }
    val cdb by lazy { FavChaDB(activity!!!!) }
    val adap by lazy { SearchAdap(activity!!!!, arrayListOf(), this) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        return bin.root
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        val s  = Point()
        activity!!!!.windowManager.defaultDisplay.getSize(s)
        if(!resources.getBoolean(R.bool.tablet)){
            when(newConfig.orientation){
                Configuration.ORIENTATION_LANDSCAPE -> {
                    bin.videos.numColumns=2
                }

                else -> {
                    bin.videos.numColumns = 1
                }
            }
        }else{
            bin.videos.numColumns = if(s.x < s.y) 2 else 3
        }

        super.onConfigurationChanged(newConfig)
    }

    val selLis = object : AbsListView.MultiChoiceModeListener{
        val listener = this@ChannelVideo
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
                bin.videos.clearChoices()
                adap.notifyDataSetChanged()
                mine.forEach {
                    bin.videos.setItemChecked(mine.indexOf(it), true)
                }
                if(selist.isEmpty()){
                    selist.addAll(mine)
                }
                mode?.let { setTitle(it) }
                return true
            }
            if(it?.title == "Unselect all"){
                selist.clear()
                bin.videos.clearChoices()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val s = Point()
        activity!!!!.windowManager.defaultDisplay.getSize(s)
        if(!resources.getBoolean(R.bool.tablet)){
            if(s.x > s.y) {
                bin.videos.numColumns = 2
            }else{
                bin.videos.numColumns = 1
            }
        }else{
            bin.videos.numColumns = if(s.x < s.y) 2 else 3
        }
        bin.videos.apply{
            isFastScrollEnabled = true
            choiceMode = AbsListView.CHOICE_MODE_MULTIPLE_MODAL
            setMultiChoiceModeListener(this@ChannelVideo.selLis)
            activity!!.windowManager.defaultDisplay.getSize(s)

            onItemClickListener = AdapterView.OnItemClickListener { a, _, i, _ ->
                val note = adap.data[i]
                onClick(note, i, a)
            }
            adapter = adap
        }
        if(savedInstanceState != null){
            val vd = savedInstanceState.getParcelableArray("vlist")!!.map { (it as VideoDetail) }
            adap.setData(vd)
        }
        if(adap.isEmpty()) {

            ProgressDialog(activity!!).apply {
                setMessage("Loading...")
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel") { _, _ ->
                    if(activity is ChannelInfo){
                        activity.finish()
                        return@setButton
                    }
                    activity.fragmentManager.popBackStack()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val e = PointerIcon.getSystemIcon(activity, PointerIcon.TYPE_WAIT)
                    window?.decorView?.pointerIcon = e
                    view.pointerIcon = e
                }
                var isSetd = false
                isIndeterminate = true
                setOnDismissListener {
                    if(!isSetd){
                        activity?.finish()
                    }
                }
                Handler(activity!!!!.mainLooper).postDelayed({
                    Search(activity!!!!).searchVideo(
                        channelId = arguments.getString("id").toString(),
                        max = 40,
                        order = Search.DATE,
                        onGet = {
                            adap.setData(it)
                        },
                        onDone = {
                            isSetd = true
                            this.dismiss()
                            if(activity == null)return@searchVideo
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                val e = PointerIcon.getSystemIcon(activity, PointerIcon.TYPE_ARROW)
                                window?.decorView?.pointerIcon = e
                                view.pointerIcon = e
                            }

                        }
                    )
                }, 10L)
                show()
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }


    companion object{
        fun newInstance(intent: Intent): ChannelVideo {
            val args = Bundle().apply {
                intent.also{i->
                    putString("id", i.getStringExtra("id"))
                }
            }
            val fragment = ChannelVideo()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArray("vlist", adap.data.toTypedArray())//video
        super.onSaveInstanceState(outState)
    }

    override fun onDetach() {
        super.onDetach()

    }



    override fun onClick(video: VideoDetail, position: Int, view: View?) {
        val d = mutableListOf<Video>()
        adap.data.forEach {
            d.add(Video(
                adap.data.indexOf(it),
                it.title,
                it.videoId
            ))
        }
        val i = openVideoNTicket(activity!!, video,position, adap.data.toTypedArray(), view, adap.data.map { it.channel }.toTypedArray())
        startActivity(i)
        arguments?.getBinder("navToThis")?.let {
            if(it is VideoView.WatchOnThis){
                it.switchToVid(i)
                arguments?.getBinder("dismiss")?.let {d->
                    if(d is DialogInterface){
                        d.dismiss()
                    }
                }
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
                        shareIntent.apply{
                            putExtra(Intent.EXTRA_STREAM, uri)
                            // adding text to share

                            // Add subject Here

                            // setting type to image
                            setType("image/png")

                        }
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
                            setType("image/png")

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



    private fun shareBitmap(vid:String):Intent{
        val intent = Intent(Intent.ACTION_SEND)
        Glide.with(activity!!!!)
            .load("https://i.ytimg.com/vi/${vid}/hqdefault.jpg")
            .centerCrop()
            .error(R.drawable.cover_color)
            .into<SimpleTarget<GlideDrawable>>(object : SimpleTarget<GlideDrawable>(0,0) {


                override fun onResourceReady(
                    resource: GlideDrawable?,
                    glideAnimation: GlideAnimation<in GlideDrawable>?,
                ) {
                    val imagefolder = File(activity!!!!.cacheDir, "images")
                    var uri: Uri? = null
                    try {
                        val d = resource!!
                        imagefolder.mkdirs()
                        val file = File(imagefolder, "shared_image.png")
                        val outputStream = FileOutputStream(file)
                        val bitmap = Bitmap.createBitmap(
                            d.getIntrinsicWidth(),
                            d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888
                        )
                        val canvas = Canvas(bitmap)
                        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
                        d.draw(canvas)
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
                        // adding text to share

                        // Add subject Here
                        putExtra(Intent.EXTRA_SUBJECT, "Subject Here")

                        // setting type to image
                        setType("image/png")

                    }
                }

            })
        return intent
    }

    override fun tagChannel(channelDetail: ChannelDetail, newTab: Boolean) {
        val clipboard: ClipboardManager = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("", channelDetail.id)
        clipboard.setPrimaryClip(clip)
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