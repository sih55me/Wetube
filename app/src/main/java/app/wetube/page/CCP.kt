package app.wetube.page

import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.View
import app.wetube.R

class CCP: PreferenceFragment() {

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addPreferencesFromResource(R.xml.cuco)
    }
}