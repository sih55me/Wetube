package app.wetube.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ActionMode
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.BaseAdapter
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SectionIndexer
import android.widget.TextView
import app.wetube.R
import app.wetube.VideoView
import app.wetube.core.ActionModeCreator
import app.wetube.core.ShareType
import app.wetube.core.changeEnHt
import app.wetube.core.dip
import app.wetube.core.initMenu
import app.wetube.core.tryOn
import app.wetube.item.ChannelDetail
import app.wetube.item.VideoDetail
import app.wetube.page.dialog.InfoVid
import app.wetube.service.FloatVideo
import app.wetube.service.Yt.atPip
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import java.util.Locale

class SearchAdap(
    val activity: Activity,
    val data: MutableList<VideoDetail>,
    val listener: OnAdapterListener,
)
    : BaseAdapter(), SectionIndexer{
    val p = Intent(activity, FloatVideo::class.java)
    var needCab: Boolean = true
    var mode : ActionMode? = null
    val selist :MutableList<VideoDetail> = mutableListOf()
    var mapIndex: HashMap<String, Int>? = linkedMapOf<String, Int>()
    var sections: Array<String?>? = null
    val infoTitle = AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog).create().apply {
        window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window!!.setWindowAnimations(android.R.style.Animation_Toast)
        window!!.attributes.apply{
            dimAmount = 0F
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }
    }
    init {
        for (x in data.indices) {
            val fruit = data[x]
            var ch = fruit.toString().substring(0, 1)
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



    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any? {
        return data[position]
    }

    val h get() = Handler(activity.mainLooper)

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


    fun onBindViewHolder(holder: ItemInList, position: Int) {
        val note = data[position]
        val title = note.title.changeEnHt()
        holder.view.findViewById<TextView>(R.id.title).text = title
        val checkInd = holder.view.findViewById<CompoundButton>(R.id.check)
        val d = holder.view.findViewById<ImageView>(R.id.thumbnail)
        holder.view.findViewById<TextView>(R.id.subtitle).text = note.channel.title
        holder.view.contentDescription = note.title

        holder.more.setOnLongClickListener {
            showInfo(note,it)
            true
        }
        holder.more.setOnHoverListener { it,m->

            showInfo(note,it)

            true
        }
        try{
            Handler(activity.mainLooper).postDelayed({
                Picasso.get().let {
                    it.load("https://i.ytimg.com/vi/${note.videoId}/hqdefault.jpg").placeholder(R.drawable.music_play)?.error(R.drawable.error)?.into(d)
                }
            }, 0L)
        }catch (e:Exception){
            e.printStackTrace()
        }
        val c = Runnable{
            checkInd.isChecked = selist.contains(note)
        }
        Runnable{
            val ac = ActionModeCreator().apply {
                onStart = { mode, menu ->
                    mode.title = "${selist.size} Selected"
                    mode.menuInflater.inflate(R.menu.home_item_menu, menu)
                    menu.add("Select all")
                    menu.add("Unselect all")
                    true
                }
                onDestroy = {
                    selist.clear()
                    mode = null
                    c.run()
                    tryOn(true) {
                        notifyDataSetChanged()
                    }
                }
                onItemClicked = { o ->
                    when (o.title) {
                        "Select all" -> {
                            if (selist.isNotEmpty()) {
                                selist.clear()
                            }
                            if (!selist.contains(note)) {
                                selist.add(note)
                            } else {
                                selist.remove(note)
                            }
                            c.run()
                            if (selist.isEmpty()) {
                                mode!!.finish()
                            }
                            selist.clear()
                            selist.addAll(data)
                            mode!!.title = "${selist.size} Selected"
                            tryOn { notifyDataSetChanged() }

                        }

                        "Unselect all" -> {
                            if (selist.isNotEmpty()) {
                                selist.clear()
                                mode!!.title = "${selist.size} Selected"
                                notifyDataSetChanged()
                            }
                        }
                    }
                    when (o.itemId) {
                        R.id.copyCI -> {
                            listener.tagChannel(note.channel, false)
                        }

                        R.id.copyCIU -> {
                            val clipboard: ClipboardManager =
                                activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("", note.getChannelUrl())
                            clipboard.setPrimaryClip(clip)
                        }

                        R.id.shareUrl -> {
                            if (selist.size == 1) {
                                listener.onOptionClick(
                                    note,
                                    Option.Share,
                                    ShareType.URL,
                                    null
                                )
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
                                listener.onOptionClick(
                                    note,
                                    Option.Share,
                                    ShareType.THUMB,
                                    holder.view.findViewById<ImageView>(R.id.thumbnail).drawable
                                )
                            }
                        }

                        R.id.shareA -> {
                            if (selist.size == 1) {
                                listener.onOptionClick(
                                    note,
                                    Option.Share,
                                    ShareType.ANY,
                                    holder.view.findViewById<ImageView>(R.id.thumbnail).drawable
                                )
                            }
                        }

                        R.id.addbtn -> {
                            if (selist.size == 1) {
                                listener.onOptionClick(
                                    note,
                                    Option.Save,
                                    ShareType.NO,
                                    null
                                )
                            } else {
                                listener.onAddList(selist)
                            }
                        }

                        R.id.info -> {
                            val list = arrayListOf<String>()
                            var order = 0
                            for (i in selist) {
                                order += 1
                                list.add(order.toString() + ". ${i.title.changeEnHt()}\n\n${i.description.changeEnHt()}\n")
                            }
                            AlertDialog.Builder(activity)
                                .setMessage(list.joinToString(separator = "\n"))
                                .setNegativeButton(android.R.string.cancel, null).show()
                                .apply {
                                    val size = ViewGroup.LayoutParams.MATCH_PARENT

                                    if (activity.resources.getBoolean(R.bool.tablet)) {
                                        window!!.setGravity(Gravity.CENTER_VERTICAL or Gravity.END)
                                        window!!.setLayout(
                                            (500F).dip(activity).toInt(),
                                            size
                                        )
                                    }
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
                tryOn {
                    notifyDataSetChanged()
                }
            }

            holder.view.setOnClickListener {
                if (needCab) {
                    if (mode != null) {
                        checkInd.toggle()
                    } else {
                        listener.onClick(note, position, holder.view)
                    }
                } else {
                    listener.onClick(note, position, holder.view)
                }
            }
            holder.view.setOnCreateContextMenuListener { c, v, _ ->
                MenuInflater(activity).inflate(R.menu.home_item_menu, c)
                c.setHeaderTitle(note.title)
                c.initMenu {
                    d.drawable?.also { d ->

                    }
                    true
                }
            }
        }
        holder.view.findViewById<ImageView>(R.id.overflow).setOnClickListener {
            popup(note, it, d.drawable)
        }
        Handler(activity.mainLooper).postDelayed(c, 100L)
    }

    val dismissTime = Runnable{
        infoTitle.dismiss()
    }

    private fun showInfo(note: VideoDetail, view: View) {
        infoTitle.setMessage(note.title)

        h.removeCallbacks(dismissTime)

        if(!infoTitle.isShowing){
            infoTitle.window?.attributes?.apply {
                gravity = Gravity.BOTTOM
                y = view.y.toInt()
                this.windowAnimations = android.R.style.Animation_Toast
            }
            infoTitle.show()
            h.postDelayed(dismissTime, 1000L)
        }
    }

    fun popup(note: VideoDetail, view: View, d: Drawable?){
        with(PopupMenu(view.context, view)) {
            inflate(R.menu.home_item_menu)


            setOnMenuItemClickListener{
                when (it.itemId) {
                    R.id.floating -> {
                        activity.atPip(note, i = p)
                    }

                    R.id.copyCI -> {
                        listener.tagChannel(note.channel, false)
                    }

                    R.id.copyCIU -> {
                        val clipboard: ClipboardManager =
                            activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("", note.getChannelUrl())
                        clipboard.setPrimaryClip(clip)
                    }

                    R.id.add_ch -> {
                        listener.addCh(note.channel)
                    }

                    R.id.shareUrl -> listener.onOptionClick(
                        note,
                        Option.Share,
                        ShareType.URL,
                        null
                    )

                    R.id.shareThum -> listener.onOptionClick(
                        note,
                        Option.Share,
                        ShareType.THUMB,
                        d
                    )

                    R.id.shareA -> listener.onOptionClick(
                        note,
                        Option.Share,
                        ShareType.ANY,
                        d
                    )

                    R.id.addbtn -> listener.onOptionClick(
                        note,
                        Option.Save,
                        ShareType.NO,
                        null
                    )

                    R.id.abc -> {
                        listener.tagChannel(note.channel, true)
                    }

                    R.id.qr -> {
                        Bundle().also {
                            it.putString("txt", note.videoId)
                            activity.showDialog(VideoView.QR_DIALOG, it)
                        }
                    }

                    R.id.info -> {
                        InfoVid(
                            activity,
                            Triple(
                                note.title.changeEnHt(),
                                note.description.changeEnHt(),
                                note.postDate.changeEnHt()
                            )
                        ).apply {
                            showBackButton {
                                dismiss()
                            }
                            show()
                        }

                    }

                    R.id.copy -> {
                        val clipboard: ClipboardManager =
                            activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("", note.getUrl())
                        clipboard.setPrimaryClip(clip)
                    }


                }
                true
            }
            show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<VideoDetail>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: VideoDetail, clearprev:Boolean = true) {
        this.data.add(data)
        notifyDataSetChanged()
    }
    fun clearData() {
        this.data.clear()
        notifyDataSetChanged()
    }
    override fun isEmpty()= data.isEmpty()
    interface OnAdapterListener {
        fun onClick(video: VideoDetail, position: Int, view: View?)
        fun onOptionClick(video: VideoDetail, option:Option, type:ShareType, drawable: Drawable?)
        fun tagChannel(channelDetail: ChannelDetail, newTab:Boolean)
        fun onAddList(list: MutableList<VideoDetail>)
        fun addCh(detail: ChannelDetail)


    }
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
    enum class Option{
        Share, Save
    }
    class SearchItemView(val view : View) :RecyclerView.ViewHolder(view)
}

