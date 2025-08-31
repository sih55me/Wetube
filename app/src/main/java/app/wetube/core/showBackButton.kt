package app.wetube.core

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog


public fun Activity.showBackButton(){
    actionBar?.apply {
        setHomeButtonEnabled(true)
        setDisplayHomeAsUpEnabled(true)
    }
}

fun ActionBar.hideBackButton(){
    setHomeButtonEnabled(!true)
    setDisplayHomeAsUpEnabled(!true)
}

public fun Dialog.showBackButton(){
    actionBar?.apply {
        setHomeButtonEnabled(true)
        setDisplayHomeAsUpEnabled(true)
    }
}