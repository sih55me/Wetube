package app.wetube.page.s

import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.EditTextPreference
import android.preference.PreferenceFragment
import app.wetube.R
import app.wetube.page.DialogPass

class PasswordSet: PreferenceFragment() {

    val pref = listOf(
        "password",
        "needOnAdd",
        "needOnDelete",
        "needOnLogin",
        "off"
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pass)
        val pass = preferenceManager!!.sharedPreferences!!.getString("password", "")
        if(!pass.isNullOrEmpty()){
            set(false)
            preferenceScreen!!.summary = "You need to enter the password to change the settings"
            if(activity!=null){
                DialogPass.Companion.newInstance(activity) {
                    if (it) {
                        set(true)
                        preferenceScreen!!.summary = ""
                    }
                }.show()
            }
        }
        findPreference(pref.last())?.setOnPreferenceClickListener {
            (findPreference(pref.first()) as EditTextPreference).text = ""
            (findPreference(pref[1]) as CheckBoxPreference).isChecked = false
            (findPreference(pref[2]) as CheckBoxPreference)?.isChecked = false
            (findPreference(pref[3]) as CheckBoxPreference)?.isChecked = false
            true
        }
    }




    fun set(enable : Boolean){
        for (i in pref) {
            findPreference(i)?.isEnabled = enable
        }
    }




}