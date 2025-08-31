package app.wetube.window.call

import android.R
import android.view.Menu
import android.view.MenuItem
import android.view.Window


/**
 * Alternative to navigation button for window.
 *
 * @param c Window's callback
 */
class XButtonWC(c: Window.Callback, private val onClick : Runnable): WindowCallbackWrapper(c) {
    override fun onCreatePanelMenu(featureId: Int, menu: Menu): Boolean {
        menu.add(0, R.id.home, 0, app.wetube.R.string.close)?.let {
            it.setIcon(app.wetube.R.drawable.close)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        return super.onCreatePanelMenu(featureId, menu)
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        if(item.itemId == R.id.home){
            onClick.run()
            return true
        }
        return super.onMenuItemSelected(featureId, item)
    }
}