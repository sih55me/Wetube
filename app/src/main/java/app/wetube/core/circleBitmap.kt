package app.wetube.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect


fun Bitmap.makeCircle() : Bitmap{
    val output = Bitmap.createBitmap(
        getWidth(),
        getHeight(), Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(output)

    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(
        0, 0, getWidth(),
        getHeight()
    )

    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawCircle(
        getWidth().toFloat() / 2,
        getHeight().toFloat() / 2, getWidth().toFloat() / 2, paint
    )
    paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
    canvas.drawBitmap(this, rect, rect, paint)
    return output
}