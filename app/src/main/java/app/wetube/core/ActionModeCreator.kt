package app.wetube.core

import android.os.Build
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem

class ActionModeCreator {
    var onStart: ((ActionMode, Menu) -> Boolean) = {_,_->true}
    var onPrepare: ((ActionMode, Menu) -> Boolean) = {_,_->true}
    var onItemClicked: ((MenuItem) -> Boolean) = {true}
    var onDestroy: (() -> Unit) = {}

    fun create():ActionMode.Callback{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            object : ActionMode.Callback2() {
                override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                    return onStart.invoke(mode, menu)
                }

                override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                    return onPrepare.invoke(mode,menu)
                }

                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
                    return onItemClicked.invoke(item)
                }

                override fun onDestroyActionMode(mode: ActionMode?) {
                    onDestroy.invoke()
                }

            }
        } else {
            object : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                    return onStart.invoke(mode, menu)
                }

                override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                    return onPrepare.invoke(mode,menu)
                }

                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
                    return onItemClicked.invoke(item)
                }

                override fun onDestroyActionMode(mode: ActionMode?) {
                    onDestroy.invoke()
                }

            }
        }
    }
}