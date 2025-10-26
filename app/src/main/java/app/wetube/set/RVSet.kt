package app.wetube.set

import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import app.wetube.R
import app.wetube.core.setupTheme
import app.wetube.core.showBackButton

class RVSet: PreferenceActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)
        showBackButton()
        addPreferencesFromResource(R.xml.rv_cuz)
        title = "Dream video Settings"
        actionBar?.subtitle = "Wetube > ${getString(R.string.set)} > Dream video Settings"
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home)finish()
        return super.onOptionsItemSelected(item)
    }



}