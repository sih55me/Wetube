package app.wetube.page

import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.View
import app.wetube.R
import app.wetube.core.getVersionCode
import app.wetube.core.getVersionName

class Bout: PreferenceFragment() {

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addPreferencesFromResource(R.xml.bout)
        if(activity != null){
            findPreference("versi").summary = getVersionName(activity)
            findPreference("vergen").summary = getVersionCode(activity).toString()
        }
    }
}