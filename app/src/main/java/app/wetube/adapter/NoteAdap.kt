package app.wetube.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ActionMode
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.BaseAdapter
import android.widget.CompoundButton
import android.widget.PopupMenu
import android.widget.SectionIndexer
import android.widget.Toast
import app.wetube.R
import app.wetube.TranslucentHelper
import app.wetube.VideoView
import app.wetube.core.ActionModeCreator
import app.wetube.core.fadeIn
import app.wetube.core.fadeOut
import app.wetube.core.tryOn
import app.wetube.item.Video
import app.wetube.service.FloatVideo
import app.wetube.service.Yt.atPip
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import java.util.Locale


@Suppress("INACCESSIBLE_TYPE")
open class NoteAdap(
    private val notes: ArrayList<Video>,
    val activity: Activity,
    private val listener: OnAdapterListener,

) : BaseAdapter(), SectionIndexer {
    var colorize = true
    val p = Intent(activity, FloatVideo::class.java)
    var mode : ActionMode? = null
    private val r by lazy { Glide.with(activity.application) }
    val selist :MutableList<Video> = mutableListOf()
    val tlc by lazy{ TranslucentHelper(activity.window, activity) }
    var infoShowing = false
    val infoTitle = AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog).create().apply {
        window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window!!.setWindowAnimations(android.R.style.Animation_Toast)
        window!!.attributes.apply{
            dimAmount = 0F
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }
    }
    var mapIndex: HashMap<String, Int>? = linkedMapOf<String, Int>()
    var sections: Array<String?>? = null
    val h get() = Handler(activity.mainLooper)
    init {

        for (x in notes.indices) {
            val fruit = notes[x].title
            var ch = fruit.substring(0, 1)
            ch = ch.toUpperCase(Locale.US)

            // HashMap will prevent duplicates
            mapIndex?.set(ch, x)
        }

        val sectionLetters = mapIndex?.keys

// create a list from the set to sort
        val sectionList = sectionLetters?.toList()

        Log.d("sectionList", sectionList.toString())
        val sortedSectionList = sectionList?.sorted()

        sections = sortedSectionList?.toTypedArray()
    }

    val dismissTime = Runnable{
        infoTitle.dismiss()
    }

    @SuppressLint("SetTextI18n")
    fun onBindViewHolder(holder: ItemInList, position: Int) {


       if(true) {
           val note = notes[position]
           val checkInd = holder.view.findViewById<CompoundButton>(R.id.check)
            holder.title.text = note.title
            holder.subtitle.text = "Saved"
            val d = holder.imageView
            holder.view.contentDescription = note.title
           holder.more.setOnLongClickListener {
               showInfo(note,it)
               true
           }
           holder.more.setOnHoverListener { it,m->

                showInfo(note,it)

               true
           }


           val ac = ActionModeCreator().apply {
                onStart = { a, m ->
                    a.title = "${selist.size} Selected"
                    a.subtitle = ""
                    a.menuInflater.inflate(R.menu.menu_scrolling, m)
                    m.add("Select all")
                    m.add("Unselect all")

                    if (selist.size >= 2) {
                        val it = m.findItem(R.id.editvid)
                        it.isEnabled = false
                        it.isVisible = false

                    }
                    true
                }
                onPrepare = { a, m ->
                    a.title = "${selist.size} Selected"
                    true
                }
                onItemClicked = {
                    when (it.title) {
                        "Select all" -> {
                            if (selist.isNotEmpty()) {
                                selist.clear()
                            }
                            if (!selist.contains(note)) {
                                selist.add(note)
                                fadeIn(checkInd)
                            } else {
                                fadeOut(checkInd)
                                selist.remove(note)
                            }
                            if (selist.isEmpty()) {
                                mode?.finish()
                            }
                            selist.clear()
                            selist.addAll(notes)
                            mode?.title = "${selist.size} Selected"
                            tryOn{ notifyDataSetChanged() }

                        }

                        "Unselect all" -> {
                            if (selist.isNotEmpty()) {
                                selist.clear()
                                mode?.title = "${selist.size} Selected"
                                tryOn{ notifyDataSetChanged() }
                            }
                        }
                    }
                    when (it.itemId) {
                        R.id.sel -> {
                            checkInd.toggle()
                        }
                        R.id.delbtn -> {
                            if (selist.size >= 2) {
                                listener.onDeleteList(selist)
                            } else {
                                listener.onDelete(note)
                            }

                        }

                        R.id.shareUrl -> {
                            if (selist.size == 1) {
                                listener.onShare(note, note.videoId, null)
                            } else {
                                val list = arrayListOf<String>()
                                var order = 0
                                for (i in selist) {
                                    order += 1
                                    list.add(order.toString() + ". youtube.com/watch?v=${i.videoId}")
                                }
                                val shareIntent = Intent()
                                shareIntent.action = Intent.ACTION_SEND
                                shareIntent.type = "text/plain"
                                shareIntent.putExtra(
                                    Intent.EXTRA_TEXT,
                                    list.joinToString(separator = "\n")
                                )
                                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "DoubleVids")
                                activity.startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        "Share Via"
                                    )
                                )
                            }
                        }

                        R.id.shareThum -> {
                            if (selist.size == 1) {
                                listener.onShare(
                                    note,
                                    null,
                                    d.drawable
                                )
                            } else {
                                Toast.makeText(
                                    activity,
                                    "Cannot share more that one",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        R.id.shareA -> if (selist.size == 1) {
                            listener.onShare(
                                note,
                                note.videoId,
                                d.drawable
                            )
                        } else {
                            Toast.makeText(
                                activity,
                                "Cannot share more that one",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        R.id.editvid -> if (selist.size == 1) {
                            listener.onEdit(note)
                        } else {
                            Toast.makeText(
                                activity,
                                "Cannot edit more that one",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        R.id.editbtn -> {
                            if (selist.size == 1) {

                            } else {
                                Toast.makeText(
                                    activity,
                                    "Cannot share more that one",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        R.id.copy -> {
                            val clipboard: ClipboardManager =
                                activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            if (selist.size == 1) {
                                val clip = ClipData.newPlainText(
                                    "",
                                    "youtube.com/watch?v=${note.videoId}"
                                )
                                clipboard.setPrimaryClip(clip)
                            } else {
                                val list = arrayListOf<String>()
                                var order = 0
                                for (i in selist) {
                                    order += 1
                                    list.add(order.toString() + ". youtube.com/watch?v=${i.videoId}")
                                }
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText(
                                        "",
                                        list.joinToString(separator = "\n")
                                    )
                                )
                            }

                        }
                    }
                    true
                }
                onDestroy = {
                    selist.clear()
                    mode = null
                    tryOn(true) {
                        notifyDataSetChanged()
                    }
                }
            }
           try{
               Handler(activity.mainLooper).postDelayed({
                   r?.let {
                       it.load("https://i.ytimg.com/vi/${note.videoId}/hqdefault.jpg").asBitmap().also{
                           tryOn{ it.override(d.width, d.height) }
                       }.placeholder(R.drawable.music_play)?.centerCrop()?.error(R.drawable.error)
                           ?.into(object : BitmapImageViewTarget(holder.imageView){
                               override fun setResource(resource: Bitmap?) {
                                   super.setResource(resource)
                               }
                           })
                   }
               }, 0L)
           }catch (e:Exception){
               e.printStackTrace()
           }
            checkInd.setOnCheckedChangeListener { d, b ->
                if (b) {
                    if (!(selist.contains(note))) {
                        selist.add(note)
                        if (selist.size <= 1) {
                            mode = holder.view.startActionMode(ac.create())
                        }
                    }
                } else {

                    if ((selist.contains(note))) {
                        selist.remove(note)
                    }
                    if (selist.isEmpty()) {
                        mode?.finish()
                    }
                }
                mode?.title = "${selist.size} Selected"
            }

            val draw = d.drawable

            holder.more.setOnClickListener {
                popup(note, it, draw)
            }
        }
    }

    private fun showInfo(note: Video, ir: View) {
        infoTitle.setMessage(note.title)
        h.removeCallbacks(dismissTime)

        if(!infoTitle.isShowing){
            infoTitle.window?.attributes?.apply {
                gravity = Gravity.BOTTOM
                y = ir.y.toInt()
                this.windowAnimations = android.R.style.Animation_Toast
            }
            infoTitle.show()
            h.postDelayed(dismissTime, 1000L)
        }
    }

    override fun getCount(): Int {
        return notes.size
    }

    override fun getItem(position: Int): Any? {
        return notes[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?,
    ): View? {
        val rowView: View
        val viewHolder: ItemInList

        if (convertView == null) {
            // If convertView is null, inflate a new row layout.
            rowView = LayoutInflater.from(activity).inflate(
                R.layout.item,
                parent,
                false
            ) // Replace with your item layout

            // Create a ViewHolder to store references to the views within the item layout.
            viewHolder = ItemInList(rowView)
            rowView.tag = viewHolder // Store the ViewHolder in the tag of the rowView.
        } else {
            // If convertView is not null, reuse the existing view.
            rowView = convertView
            viewHolder = rowView.tag as ItemInList
        }
        onBindViewHolder(viewHolder, position)
        return rowView
    }

    override fun getItemViewType(position: Int): Int {

        return 0
    }

    fun r() {
        Runnable {
//                holder.view.setOnLongClickListener {
//                    if (!it.context.isTv) {
//                        if (!selist.contains(note)) {
//                            selist.add(note)
//                            fadeIn(checkInd)
//                        } else {
//                            fadeOut(checkInd)
//                            selist.remove(note)
//                        }
//                        if (mode == null) {
////                        mode = it.startActionMode(c.create())
//
//                        } else {
//                            mode!!.title = "${selist.size} Selected"
//                        }
//                        if (selist.isEmpty()) {
//                            mode?.finish()
//                        }
//                    } else {
//                        popup(
//                            note,
//                            it,
//                            holder.view.findViewById<ImageView>(R.id.thumbnail).drawable
//                        )
//                    }
//                    true
//                }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    holder.view.setOnContextClickListener {
//                        val draw = holder.view.findViewById<ImageView>(R.id.thumbnail).drawable
//                        popup(note, it, draw)
//                        Toast.makeText(activity, "Got a ${note.title}", Toast.LENGTH_SHORT).show()
//                        true
//                    }
//                }
//            }
        }
    }

    private fun popupToolbar(note: Video, view: View, draw:Drawable){
        val c = object : ActionMode.Callback{
            override fun onCreateActionMode(
                mode: ActionMode?,
                menu: Menu?,
            ): Boolean {
                mode?.menuInflater?.inflate(R.menu.menu_scrolling, menu)
                menu?.add(0,0,0,"X")?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
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
                when (item?.itemId) {
                    R.id.delbtn -> listener.onDelete(note)
                    R.id.shareUrl -> listener.onShare(note, note.videoId, null)
                    R.id.shareThum -> listener.onShare(note, null, draw)
                    R.id.shareA -> listener.onShare(note, note.videoId, draw)
                    R.id.editvid -> listener.onEdit(note)
                    R.id.editbtn -> {
                        Bundle().also {
                            it.putString("txt", note.videoId)
                            activity.showDialog(VideoView.QR_DIALOG, it)
                        }

                    }
                    R.id.copy -> {
                        val clipboard: ClipboardManager =
                            activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip =
                            ClipData.newPlainText("", "youtube.com/watch?v=${note.videoId}")
                        clipboard.setPrimaryClip(clip)
                    }
                    R.id.floating -> {
                        activity.atPip(note, i = p)
                    }
                }
                mode?.finish()
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode?) {

            }

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.startActionMode(c, ActionMode.TYPE_FLOATING)
        }
    }




    private fun popup(note: Video, view: View, draw:Drawable?){

        with(
            PopupMenu(
                view.context,
                view
            )
        ) {
            inflate(R.menu.menu_scrolling)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.delbtn -> listener.onDelete(note)
                    R.id.shareUrl -> listener.onShare(note, note.videoId, null)
                    R.id.shareThum -> listener.onShare(note, null, draw)
                    R.id.shareA -> listener.onShare(note, note.videoId, draw)
                    R.id.editvid -> listener.onEdit(note)
                    R.id.editbtn -> {
                        Bundle().also {
                            it.putString("txt", note.videoId)
                            activity.showDialog(VideoView.QR_DIALOG, it)
                        }

                    }
                    R.id.copy -> {
                        val clipboard: ClipboardManager =
                            activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip =
                            ClipData.newPlainText("", "youtube.com/watch?v=${note.videoId}")
                        clipboard.setPrimaryClip(clip)
                    }
                    R.id.floating -> {
                        activity.atPip(note, i = p)
                    }
                }
                true
            }
            show()
        }

    }


    class NoteViewHolder(val view : View) : RecyclerView.ViewHolder(view){}

    class CardHolder(val view : View) : RecyclerView.ViewHolder(view){}

    fun actionMode(){



    }
    @SuppressLint("NotifyDataSetChanged")
    fun setdata(list : List<Video>){
        notes.clear()
        notes.addAll(list)
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun clearData(){
        notes.clear()
        notifyDataSetChanged()
    }


    interface OnAdapterListener {
        fun onClick(note: Video, position: Int, view :View)
        fun onDelete(note : Video)
        fun onShare(note: Video, videoId: String?, drawable: Drawable?)
        fun onEdit(note: Video)
        fun onSelectedList(pors:Int, list : MutableList<Video>)
        fun onDeleteList(list: MutableList<Video>):Boolean
    }



    fun getData() = notes
    override fun getSections(): Array<out Any?>? {
        return sections
    }

    override fun getPositionForSection(section: Int): Int {
        Log.d("section", "" + section);
        return mapIndex?.get(sections?.get(section)) ?: 0;
    }

    override fun getSectionForPosition(position: Int): Int {
        Log.d("position", "" + position);
        return 0;
    }


}