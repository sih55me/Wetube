package app.wetube.page

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import app.wetube.ChannelInfo
import app.wetube.R
import app.wetube.core.dime
import app.wetube.core.tryOn
import app.wetube.kembaliKe
import app.wetube.manage.db.FavChaDB


class FavCha():android.app.ListFragment(){
    val ad by lazy{ ArrayAdapter(activity!!,android.R.layout.simple_list_item_1, arrayListOf<String>()) }
    val db by lazy{ FavChaDB(activity!!.applicationContext) }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEmptyText("No favorite channels")
        listAdapter = ad
        listView.apply{
            isFastScrollEnabled = true
            load()
            setOnCreateContextMenuListener { menu, v, menuInfo ->
                val l = db.listAsList()[(menuInfo as AdapterView.AdapterContextMenuInfo).position]
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
                    addSubMenu(R.string.del) .also{s->
                        s.setHeaderTitle("Delete this?")
                        s.add(R.string.del).setOnMenuItemClickListener {
                            db.deleteChaByName(l.title.toString())
                            load()
                            true
                        }
                        s.add(android.R.string.cancel)
                    }
                }
            }
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val s = db.listAsList()
                tryOn {
                    val l = s[position]
                    val i = Intent(activity!!, ChannelInfo::class.java).putExtra("id", l.id).putExtra("name", l.title)
                    i.kembaliKe(activity)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(
                        i
                    )
                    
                }
            }

        }
        updateView()
    }


    private fun updateView(){

        try{
            (view?.layoutParams as FrameLayout.LayoutParams).apply {
                leftMargin = dime(R.dimen.marginLayout).toInt()
                rightMargin = dime(R.dimen.marginLayout).toInt()

            }
        }catch (_:Exception){

        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateView()
    }
    override fun onResume() {
        load()
        super.onResume()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        load()
        super.onHiddenChanged(hidden)
    }

    fun load(){
        ad.clear()
        ad.addAll(db.listAsList().map { it.title })
        ad.notifyDataSetChanged()
    }


}

