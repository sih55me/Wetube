package app.wetube.adapter

import android.app.Activity
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import app.wetube.R


class HistoryAdapter (
    val notes: ArrayList<String>,
    val activity: Activity,
    private val listener: HistoryListener,
): BaseAdapter(){
    class HViewHolder(val view : View) {
        val title get()=view.findViewById<TextView>(R.id.title)
    }

    interface HistoryListener{
        fun onClick(text:CharSequence)
        fun onUp(text: CharSequence)
        fun onDelete(text: CharSequence)
    }



    override fun getCount(): Int = notes.size
    override fun getItem(position: Int): Any? {
        return notes[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?,
    ): View? {
        val rowView: View
        val viewHolder: HViewHolder

        if (convertView == null) {
            // If convertView is null, inflate a new row layout.
            rowView = LayoutInflater.from(activity).inflate(
                R.layout.history_item,
                parent,
                false
            ) // Replace with your item layout

            // Create a ViewHolder to store references to the views within the item layout.
            viewHolder = HViewHolder(rowView)
            rowView.tag = viewHolder // Store the ViewHolder in the tag of the rowView.
        } else {
            // If convertView is not null, reuse the existing view.
            rowView = convertView
            viewHolder = rowView.tag as HViewHolder
        }
        onBindViewHolder(viewHolder, position)
        return rowView
    }
    fun clear(){
        notes.clear()
    }

    fun addAll(list: List<String>){
        notes.addAll(list)
        notifyDataSetChanged()
    }


    fun onBindViewHolder(holder: HViewHolder, position: Int) {
        val note = notes[position]
        holder.title?.text = note
        holder.view.setOnClickListener {
            listener.onClick(note)
        }
        holder.view.findViewById<View>(R.id.up)?.setOnClickListener {
            listener.onUp(note)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.view.setOnContextClickListener{
                listener.onDelete(note)
                true
            }
        }
        holder.view.setOnLongClickListener {
            listener.onDelete(note)
            true
        }
    }
}