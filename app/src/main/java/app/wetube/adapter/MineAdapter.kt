package app.wetube.adapter

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import app.wetube.R
import app.wetube.item.Video
import com.bumptech.glide.Glide

class MineAdapter(
    private val context: Context,
) : BaseAdapter() {
    val yall: ArrayList<Video> = arrayListOf()
    private val lay = R.layout.item

    override fun isEnabled(position: Int): Boolean {
        return super.isEnabled(position)
    }

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    // 1. Returns the total number of items in the data set.
    override fun getCount(): Int {
        return yall.size
    }

    // 2. Returns the data item associated with the specified position.
    override fun getItem(position: Int): Any {
        return yall[position]
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
            viewHolder.title = rowView.findViewById(R.id.title) // Replace with your TextView ID
            viewHolder.subtitle = rowView.findViewById(R.id.subtitle) // Replace with your TextView ID
            viewHolder.imageView = rowView.findViewById(R.id.thumbnail) // Replace with your ImageView ID
            rowView.tag = viewHolder // Store the ViewHolder in the tag of the rowView.
        } else {
            // If convertView is not null, reuse the existing view.
            rowView = convertView
            viewHolder = rowView.tag as ViewHolder
        }

        // Get the data item for this position.
        val v = getVid(position)

        // Populate the views with data.
        viewHolder.title.text = v.title
        Glide.with(context)
            .load("https://i.ytimg.com/vi/${v.videoId}/hqdefault.jpg")
            .error(ColorDrawable(context.resources.getColor(R.color.black)))
            .into(viewHolder.imageView)

        return rowView
    }

    fun reAdd(new:ArrayList<Video>){
        yall.clear()
        yall.addAll(new)
        notifyDataSetChanged()
    }


    private fun getVid(position: Int) = getItem(position) as Video

    // ViewHolder pattern to improve performance by caching view lookups.
    private class ViewHolder {
        lateinit var title: TextView
        lateinit var imageView: ImageView
        lateinit var subtitle: TextView
    }
}