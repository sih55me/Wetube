package app.wetube.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import app.wetube.R

class EmptyViewContainer(context: Context) {
    private val layinf = LayoutInflater.from(context)
    private val view = layinf.inflate(R.layout.empty_lay, null)
    val image = view.findViewById<ImageView>(R.id.image_add)
    val textView = view.findViewById<TextView>(R.id.empty_text)

    fun setIcon(icon: Any){
        if(icon is Int){
            image.setImageResource(icon)
        }else if(icon is Drawable){
            image.setImageDrawable(icon)
        }
    }

    fun setText(text: Any){
        if(text is Int){
            textView.setText(text)
        }else if(text is CharSequence) {
            textView.text = text
        }else {
            textView.text = text.toString()
        }
    }

    val root get() = view

}
