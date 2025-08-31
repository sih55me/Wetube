package app.wetube.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.TextView

class ColorizeAdapter <T> @JvmOverloads constructor(context: Context, resId:Int = android.R.layout.simple_list_item_1, objects: MutableList<T>): FastSupportAdapter<T>(context,resId, objects){
    var color = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = super.getView(position, convertView, parent)
        if(color != 0){
            v.findViewById<TextView>(tvid)?.let{
                it.setTextColor(color)
                if(it is CheckedTextView){
                    it.checkMarkTintList = ColorStateList.valueOf(color)
                }
            }
        }
        return v
    }


}