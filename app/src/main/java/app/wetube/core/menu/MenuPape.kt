package app.wetube.core.menu

import android.content.Context
import android.os.Bundle
import app.wetube.widget.MenuList
import app.wetube.window.Paper

class MenuPape(context: Context) : Paper(context) {
    val listView = MenuList(context)



    val menu get() = listView.menu



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(listView)
    }

    companion object {
        const val KEY: String = "ceklistr"
    }
}