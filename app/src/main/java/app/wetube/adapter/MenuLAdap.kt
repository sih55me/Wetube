package app.wetube.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import app.wetube.core.menu.ActionMenuItem
import app.wetube.core.menu.ArrayMenu

class MenuLAdap (
    private val context: Context,
) : BaseAdapter() {
    val menu: ArrayMenu = ArrayMenu(context)
    private val lay = android.R.layout.simple_list_item_1
    var onClick = MenuItem.OnMenuItemClickListener{
        true
    }


    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    // 1. Returns the total number of items in the data set.
    override fun getCount(): Int {
        return menu.visibleOnly.size
    }

    // 2. Returns the data item associated with the specified position.
    override fun getItem(position: Int): Any {
        return menu.visibleOnly[position]
    }

    // 3. Returns the row id associated with the specified position.
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // 4. Returns a View that displays the data at the specified position.
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            // If convertView is null, inflate a new row layout.
            rowView = inflater.inflate(
                lay,
                parent,
                false
            ) // Replace with your item layout

            // Create a ViewHolder to store references to the views within the item layout.
            viewHolder = ViewHolder()
            viewHolder.title =
                rowView.findViewById(android.R.id.text1) // Replace with your TextView ID
            rowView.tag = viewHolder // Store the ViewHolder in the tag of the rowView.
        } else {
            // If convertView is not null, reuse the existing view.
            rowView = convertView
            viewHolder = rowView.tag as ViewHolder
        }
        rowView.setOnClickListener {
            menu.getItem(position)?.let { it1 -> onClick.onMenuItemClick(it1) }
        }

        // Get the data item for this position.
        val v = getVid(position)

        // Populate the views with data.
        viewHolder.title.text = v.title
        return rowView
    }

    fun reAdd(new: ArrayList<MenuItem>) {
        menu.clear()
        menu.mana.addAll(new)
        notifyDataSetChanged()
    }


    private fun getVid(position: Int) = getItem(position) as ActionMenuItem

    // ViewHolder pattern to improve performance by caching view lookups.
    private class ViewHolder {
        lateinit var title: TextView
    }
}