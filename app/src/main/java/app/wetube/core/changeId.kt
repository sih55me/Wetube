package app.wetube.core

import android.text.SpannableStringBuilder
import android.widget.EditText

fun CharSequence.convertToId(): String{
    val d = if (startsWith("https://m.youtube.com/watch?v=", false)) {
        val d = toString().replace("https://m.youtube.com/watch?v=", "")
        (if (contains("&")) d.split("&")[0] else d)
    } else if (startsWith("https://youtube.com/watch?v=", false)) {
        val d = toString().replace("https://youtube.com/watch?v=", "")
        (if (contains("&")) d.split("&")[0] else d)
    } else if (startsWith("https://youtu.be/", false)) {
        val d = toString().replace("https://youtu.be/", "")
        (if (contains("&")) d.split("&")[0] else d)
    } else if (startsWith("https://www.youtube.com/watch?v=", false)) {
        val d = toString().replace("https://www.youtube.com/watch?v=", "")
        (if (contains("&")) d.split("&")[0] else d)
    } else if (startsWith("https://www.youtube.com/embed/", false)) {
        val d = toString().replace("https://www.youtube.com/embed/", "")
        (if (contains("&")) d.split("&")[0] else d)
    } else if (startsWith("https://www.youtube.com/shorts/", false)) {
        val d = toString().replace("https://www.youtube.com/shorts/", "")
        (if (contains("&")) d.split("&")[0] else d)
    } else if (startsWith("youtube.com/watch?v=", false)) {
        val d = toString().replace("youtube.com/watch?v=", "")
        (if (contains("&")) d.split("&")[0] else d)
    } else {
        this
    }

    return d.split("?")[0]
}