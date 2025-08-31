package app.wetube.page

import android.app.ListFragment
import android.os.Bundle
import android.view.ContextMenu
import android.view.View
import app.wetube.adapter.MineAdapter
import app.wetube.item.Video
import app.wetube.manage.db.VidDB
import app.wetube.openVideo

class CollectionzPage: ListFragment() {
    val db by lazy { VidDB(activity!!) }
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listAdapter = MineAdapter(activity!!)
        load()
        listView.setOnItemClickListener { parent, view, position, id ->
            openVideo(activity!!, listAdapter.getItem(position) as Video ,position, db.listAsList().toTypedArray(), null)
        }
        registerForContextMenu(listView)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        menu?.add(0,0,0,"Delete")
        menu?.add(0,1,0,"Edit")
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        load()
    }


    private fun load(){
        if(listAdapter !is MineAdapter) return
        val ad = listAdapter as MineAdapter
        ad.reAdd(db.listAsList())
    }
}