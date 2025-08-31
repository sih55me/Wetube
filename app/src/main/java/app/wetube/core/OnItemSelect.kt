package app.wetube.core

import android.view.View
import android.widget.AdapterView

open class OnItemSelect: AdapterView.OnItemSelectedListener {

    open override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}