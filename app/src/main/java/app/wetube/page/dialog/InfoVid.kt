package app.wetube.page.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import app.wetube.R
import app.wetube.core.isTv
import app.wetube.core.setTextColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class InfoVid(context: Context, private val script : Triple<String, String, String>): AlertDialog(context) {
    init {
        val array = context.obtainStyledAttributes(null, intArrayOf(android.R.attr.colorAccent))
        try {
            val hexColor = java.lang.String.format("#%06X", (0xFFFFFF and array.getColor(0, 0)))
            val info = "${setTextColor(context.getString(R.string.nv), hexColor)} : ${script.first}\n" +
                    "\n${setTextColor(context.getString(R.string.desc), hexColor)} : ${script.second}\n" +
                    "\n${setTextColor("Post on", hexColor)} : ${script.third}"
            setMessage(info)
            setButton2(context.getString(R.string.close)) { _, _ -> }
        } finally {
            array.recycle()
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<View>(android.R.id.message)?.apply{
            if(this !is TextView)return
            setTextIsSelectable(true)
            isFocusable = context.isTv.not()
            setMovementMethod(LinkMovementMethod.getInstance());
            isFocusable = context.isTv.not()
        }
    }

}