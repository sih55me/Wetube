package app.wetube.manage.db

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.CursorAdapter
import app.wetube.R
import app.wetube.manage.db.VidDB.Companion.COL_NAME

class SearchCursor(context: Context?, c: Cursor?, autoRequery: Boolean):CursorAdapter(context, c, autoRequery) {

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(R.layout.menu_item, parent, false)
    }
    @SuppressLint("Range")
    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        view!!.findViewById<TextView>(R.id.menuTitle).text = cursor!!.getString(cursor.getColumnIndex(COL_NAME))
        view.findViewById<ImageView>(R.id.menuIcon).setImageResource(R.drawable.go)
    }
}