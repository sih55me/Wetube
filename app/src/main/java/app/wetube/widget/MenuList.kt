package app.wetube.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Menu
import android.widget.ListAdapter
import android.widget.ListView
import app.wetube.adapter.MenuLAdap

class MenuList : ListView {
    private val dap by lazy { MenuLAdap(context) }
    @JvmOverloads
    constructor(c: Context, a: AttributeSet? = null, da: Int = android.R.attr.listViewStyle , dr: Int = 0):super(c, a, da, dr){

    }

    public val menu get() = dap.menu as Menu

    override fun onFinishInflate() {
        super.onFinishInflate()
        if(!isInEditMode){
            super.setAdapter(dap)
        }
    }

    override fun setAdapter(adapter: ListAdapter?) {
        //dont care
    }


}