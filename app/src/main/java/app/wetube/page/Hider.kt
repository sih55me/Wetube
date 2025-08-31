package app.wetube.page

import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.View
import app.wetube.R

class Hider: PreferenceFragment() {

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (preferenceScreen == null) {
            addPreferencesFromResource(R.xml.hider)
        }
        if(preferenceScreen != null) {
            if (preferenceScreen.preferenceCount == 0) {
                addPreferencesFromResource(R.xml.hider)
            }
        }
    }
}