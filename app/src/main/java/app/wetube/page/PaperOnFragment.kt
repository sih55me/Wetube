package app.wetube.page

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import app.wetube.window.Paper

open class PaperOnFragment(context: Context, val listener: TransPaper): Paper(context) {

    override fun show() {
        super.show()
        actionBar?.let { listener.onActionBarReady(it) }
        listener.onDialogShow()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        listener.connector.onCreateOptionsMenu(menu, MenuInflater(context))
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        listener.connector.onPrepareOptionsMenu(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return listener.connector.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listener.onDialogCreate(this)
    }

    override fun onStart() {
        super.onStart()
        listener.onDialogStart(this)
    }

}