package app.wetube

import android.app.Activity
import android.os.Bundle
import android.widget.FrameLayout
import app.wetube.page.NewTab

class ContentBrow: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val d = FrameLayout(this)
        setContentView(d)
        d.id = app.wetube.R.id.hal
        fragmentManager.beginTransaction().add(R.id.hal, NewTab()).commit()
    }
}