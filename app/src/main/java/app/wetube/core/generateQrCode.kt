package app.wetube.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder

public fun getQrCodeFromString(text : String, width : Int = 400, height : Int = 400): Bitmap?{
    val writer = MultiFormatWriter()
    try {
        val bm = writer.encode(text, BarcodeFormat.QR_CODE, width, height)
        val encoder = BarcodeEncoder()
        return encoder.createBitmap(bm)
    }catch (e : WriterException){
        return null
    }
}

public fun getQrCodeFromString(text : String, size : Int = 400): Bitmap? = getQrCodeFromString(text, size, size)


fun getQrCodeAsDrawableFromString(context: Context,text : String, width : Int = 400, height : Int = 400 ): Drawable? =
    BitmapDrawable(context.resources,getQrCodeFromString(text, width, height))
