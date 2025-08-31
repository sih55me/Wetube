package app.wetube.set

import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.Menu
import android.view.MenuItem
import app.wetube.R
import app.wetube.core.setupTheme

class RVSet: PreferenceActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.rv_cuz)
        actionBar?.apply {
            title = getString(R.string.set)
            subtitle = "For \"Some Random Video\""
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            it.add(R.string.close).setIcon(R.drawable.close).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setOnMenuItemClickListener {
                finish()
                true
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

}