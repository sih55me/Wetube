package app.wetube.core

import android.widget.EditText

fun EditText.convertToId(){
    if (this.text.startsWith("https://m.youtube.com/watch?v=", false)) {
        val d = this.text.toString().replace("https://m.youtube.com/watch?v=", "")
        this.setText(if (this.text.contains("&")) d.split("&")[0] else d)
    } else if (this.text.startsWith("https://youtube.com/watch?v=", false)) {
        val d = this.text.toString().replace("https://youtube.com/watch?v=", "")
        this.setText(if (this.text.contains("&")) d.split("&")[0] else d)
    } else if (this.text.startsWith("https://youtu.be/", false)) {
        val d = this.text.toString().replace("https://youtu.be/", "")
        this.setText(if (this.text.contains("&")) d.split("&")[0] else d)
    } else if (this.text.startsWith("https://www.youtube.com/watch?v=", false)) {
        val d = this.text.toString().replace("https://www.youtube.com/watch?v=", "")
        this.setText(if (this.text.contains("&")) d.split("&")[0] else d)
    } else if (this.text.startsWith("https://www.youtube.com/embed/", false)) {
        val d = this.text.toString().replace("https://www.youtube.com/embed/", "")
        this.setText(if (this.text.contains("&")) d.split("&")[0] else d)
    } else if (this.text.startsWith("https://www.youtube.com/shorts/", false)) {
        val d = this.text.toString().replace("https://www.youtube.com/shorts/", "")
        this.setText(if (this.text.contains("&")) d.split("&")[0] else d)
    } else if (this.text.startsWith("youtube.com/watch?v=", false)) {
        val d = this.text.toString().replace("youtube.com/watch?v=", "")
        this.setText(if (this.text.contains("&")) d.split("&")[0] else d)
    }
}