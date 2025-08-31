package app.wetube.core

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import app.wetube.R


fun sharePopup(a: Activity, v: View, shareIntent: Intent) {
    val pm: PackageManager = a.packageManager
    val list = pm.queryIntentActivities(shareIntent, 0)
    with(PopupMenu(a, v)) {
        for (i in list.indices) {
            menu.add(0, 0, i, list[i].loadLabel(pm)).setIcon(list[i].loadIcon(pm))
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            setForceShowIcon(true)
        }
        setOnMenuItemClickListener {
            val activityInfo = list[it.order].activityInfo
            val named = ComponentName(
                activityInfo.applicationInfo.packageName,
                activityInfo.name
            )
            val newIntent = shareIntent.clone() as Intent
            newIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
            newIntent.setComponent(named)
            a.startActivity(newIntent)
            true
        }
        show()
    }
}

    fun share(a: Activity, stxt: String) {
        val shareIntent = Intent()
        shareIntent.apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, stxt)
            putExtra(Intent.EXTRA_TITLE, "YouTube Video Link")
        }
        val s = Intent.createChooser(
            shareIntent,
            a.getString(R.string.share)
        )
        a.startActivity(s)
    }

//    fun getId(a: Activity, vid: String) = InfoSheet().show(a) {
//        val t = "<b>$vid</b>"
//        title("Share id")
//        displayCloseButton(false)
//        content(Html.fromHtml(t))
//        onNegative("${a.resources.getString(android.R.string.copy)} id") {
//            copyId(a, vid)
//        }
//    }

    fun copyId(a: Activity, vid: String) {
        val clipboardManager = a.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clipData = android.content.ClipData.newPlainText("id", vid)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(a, "Copied", Toast.LENGTH_SHORT).show()
    }
private fun infoId(a: Activity, i: Intent){
    val text = i.getStringExtra(Intent.EXTRA_TEXT) ?: "Nothing"
    val title = i.getStringExtra(Intent.EXTRA_TITLE) ?: "Info"
    AlertDialog.Builder(a).apply {
        setMessage(text)
        setNegativeButton(android.R.string.cancel, null)
        show()
    }
}

