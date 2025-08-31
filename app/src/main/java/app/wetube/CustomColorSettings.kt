package app.wetube

import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.View

class CustomColorSettings:PreferenceFragment() {

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addPreferencesFromResource(R.xml.cusco)
    }
}