package app.wetube

import android.app.Dialog
import android.content.Context
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.View

class ActionModeDialog(private val context: Context): ActionMode() {
    private var svdT = ""
    val d = Dialog(context)

    override fun setTitle(title: CharSequence?) {
        d.setTitle(title)
    }

    override fun setTitle(resId: Int) {
        d.setTitle(resId)
    }

    override fun setSubtitle(subtitle: CharSequence?) {

    }

    override fun setSubtitle(resId: Int) {

    }

    override fun setCustomView(view: View?) {

    }

    override fun invalidate() {
        TODO("Not yet implemented")
    }

    override fun finish() {
        d.dismiss()
    }

    override fun getMenu(): Menu? {
        TODO("Not yet implemented")
    }

    override fun getTitle(): CharSequence? {
        return svdT
    }

    override fun getSubtitle(): CharSequence? {
        return ""
    }

    override fun getCustomView(): View? {
        return null
    }

    override fun getMenuInflater(): MenuInflater? {
        return MenuInflater(context)
    }
}