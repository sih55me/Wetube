package app.wetube.page.s

import android.os.Build
import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.View
import app.wetube.R

class VideoSettings: PreferenceFragment() {


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addPreferencesFromResource(R.xml.vidset)
        findPreference("notch")?.isEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }


}