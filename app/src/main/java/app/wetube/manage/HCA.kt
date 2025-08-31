package app.wetube.manage

import android.content.Context
import android.database.Cursor
import android.view.View
import android.widget.SimpleCursorAdapter
import android.widget.TextView
import app.wetube.core.tryOn
import app.wetube.manage.db.HistoryDB.Companion.COL_NAME

class HCA(context: Context, data: Cursor) : SimpleCursorAdapter(context, android.R.layout.simple_list_item_1, data, arrayOf("name"), intArrayOf(android.R.id.text1), 0){


    override fun bindView(view: View, context: Context?, cursor: Cursor) {
        // Find views
        tryOn{
            val textView = view.findViewById<TextView?>(android.R.id.text1)
            // Get data from cursor
            val data = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME))
            // Populate views
            textView.setText(data)
        }
    }
}