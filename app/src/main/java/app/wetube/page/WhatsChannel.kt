package app.wetube.page

import android.app.ActionBar
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import app.wetube.R
import app.wetube.core.info
import app.wetube.core.makeDismissSender
import app.wetube.databinding.WhcaBinding
import app.wetube.item.ChannelDetail
import app.wetube.manage.db.FavChaDB

class WhatsChannel: Sheet() {
    private val con by lazy{ WhcaBinding.inflate(activity!!.layoutInflater) }
    val db by lazy{ FavChaDB(activity!!) }
    private var channelDetail : ChannelDetail?= null
    private val p by lazy{ PreferenceManager.getDefaultSharedPreferences(activity) }
    var defEv = 0.0F
    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        return con.root
    }

    override fun onActionBarReady(actionBar: ActionBar) {
        super.onActionBarReady(actionBar)
        if(showsDialog){
            paper.showBackButton{
                dismiss()
            }
            defEv = actionBar.elevation
            con.tabs.elevation = defEv
            actionBar.elevation = 0F
        }
        actionBar.title = channelDetail?.title
        activity?.actionBar?.elevation = 0F
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putFloat("defEv", defEv)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(isRemoving and !showsDialog){
            activity?.actionBar?.elevation = defEv
        }
    }



    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        var n  = arguments?.getBinder("navToThis")
        arguments?.let {
           channelDetail = it.getParcelable<ChannelDetail>("cenel")

        }

        val cus = p.getBoolean("cus", true)

        if(!showsDialog){
            val r = try{
                activity?.actionBar?.elevation
            }catch (_: Exception){
                null
            }?:0F
            defEv = savedInstanceState?.getFloat("defEv") ?: r
            activity?.actionBar?.let { onActionBarReady(it) }
        }
        val inc = Bundle().apply {
            putString("id", channelDetail?.id)
            putString("name", channelDetail?.title)
            putBoolean("showMenu", true)
            putBinder("navToThis", n)
            putBoolean(IsInDialog, showsDialog)
            makeDismissSender(this)
        }
        con.tabs.elevation = defEv
        setHasOptionsMenu(true)
        con.root.apply{
            mContainerId = con.tabcontent.id
            setup(activity!!, childFragmentManager)
            newTabSpec("vids").setContent(R.id.tab1).setIndicator("Videos").let {
                addTab(it, ChannelVideo::class.java,inc)
            }
            newTabSpec("about").setContent(R.id.tab2).setIndicator(getString(R.string.about)).let {
                addTab(it, AboutChannel::class.java, inc)
            }
        }
    }


    override fun onDestroyView() {
        if(!showsDialog){
            activity.actionBar?.subtitle = null
        }
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.add(0,16,1,"Add").setOnMenuItemClickListener {
            db.doing {
                if (it.listAsList().map { it.id }.contains(channelDetail?.id)) {
                    channelDetail?.title?.let { name -> it.deleteChaByName(name) }
                    info("Channel Deleted")
                } else {
                    channelDetail?.let { it1 -> it.insert(it1) }
                    info("Channel Added")
                }
            }
            true
        }
        menu.add(0,27,2,"Add to blacklist").setOnMenuItemClickListener {
            val sp = PreferenceManager.getDefaultSharedPreferences(activity!!)
            val list = (sp.getString("bc", "") ?: "").split(",").map { it.trim() }.toMutableList();
            list.apply {
                if(list[0].isEmpty()) {
                    removeAt(0)
                }
                channelDetail?.id?.apply{
                    if(!list.contains(this)) {
                        add(this)
                    }else{
                        info("Already in blacklist")
                    }
                }
            }
            sp.edit().putString("bc", list.joinToString(separator = ",")).apply()
            true
        }
        menu.add(0,17,3,"Share").setOnMenuItemClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            val stxt = "https://www.youtube.com/channel/${channelDetail?.id}"
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, stxt)
            startActivity(Intent.createChooser(shareIntent, ""))
            true
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val i = channelDetail
        val status = if(db.listAsList().contains(i)) "Delete from my favorite channel" else "Add to my favorite channel"
        menu.findItem(16) .setTitle(status)


        super.onPrepareOptionsMenu(menu)
    }




}