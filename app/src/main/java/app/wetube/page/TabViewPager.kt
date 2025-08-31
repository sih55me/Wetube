package app.wetube.page

import android.app.ActionBar.Tab
import android.app.FragmentTransaction


class TabAction(val select : ((Tab?, FragmentTransaction?) -> Unit), val unselect : ((Tab?,FragmentTransaction?) -> Unit)):android.app. ActionBar.TabListener {

    constructor(select: (Tab?,FragmentTransaction?) -> Unit) : this(select, {_,_->})
    override fun onTabSelected(
        tab: android.app.ActionBar.Tab?,
        ft: android.app.FragmentTransaction?
    ) {
        select(tab, ft)

    }

    override fun onTabUnselected(
        tab: android.app.ActionBar.Tab?,
        ft: android.app.FragmentTransaction?
    ) {
        unselect(tab, ft)
    }

    override fun onTabReselected(
        tab: android.app.ActionBar.Tab?,
        ft: android.app.FragmentTransaction?
    ) {
        
    }
}