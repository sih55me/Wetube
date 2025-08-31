package app.wetube

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button


class MenuActivity : Activity() {



    private var mIdList: MutableList<String> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("Presentasi")

        AlertDialog.Builder(this).apply {
            setTitle("Keperluan video editing")
            setSingleChoiceItems (ArrayAdapter(this@MenuActivity, android.R.layout.simple_list_item_1, arrayOf("1. Handphone / laptop", "2. App video editing [FREE/BAYAR]", "3. Ide")), 0, null)
            setPositiveButton("NEXT", null)
        }.show()

    }

    fun a(b : Button) : Button{
        b.text = "Button from ${b.context}"
        return b
    }


}



