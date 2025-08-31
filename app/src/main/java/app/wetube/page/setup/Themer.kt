package app.wetube.page.setup

import android.app.Fragment
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import app.wetube.R
import app.wetube.databinding.Sp4Binding

class Themer:Fragment() {
    private val bin by lazy{Sp4Binding.inflate(activity!!.layoutInflater)}

    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return bin.root
    }



    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bin.apply {
            togthe.setOnCheckedChangeListener { buttonView, isChecked ->
                val result = if(isChecked)"w" else "d"
                PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).edit().putString("theme", result).apply()

            }
            val list = arrayOf(
                getString(R.string.light),
                getString(R.string.dark),
                "Auto"
            )
            val listV = arrayOf(
                "l",
                "d",
                "a"
            )
            spinner.apply {
                adapter = ArrayAdapter(activity!!, android.R.layout.simple_list_item_1, list)
                onItemSelectedListener = object:AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long,
                    ) {
                        PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).edit().putString("darkmode", listV[position]).apply()

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }
            }
        }
    }
}