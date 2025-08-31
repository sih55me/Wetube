package app.wetube.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import app.wetube.R

class ItemInList(val view: View) {
    val title: TextView  get() = view.findViewById(R.id.title)
    val imageView: ImageView get() = view.findViewById(R.id.thumbnail)
    val subtitle: TextView get() = view.findViewById(R.id.subtitle)
    val more: View get() = view.findViewById<View>(R.id.overflow)


}