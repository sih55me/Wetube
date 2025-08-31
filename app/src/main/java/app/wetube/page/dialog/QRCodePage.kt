package app.wetube.page.dialog

import android.content.Context
import app.wetube.core.getQrCodeFromString

class QRCodePage(context: Context, txt:String): PreviewImgPage(
    context,
    Get(requireNotNull(getQrCodeFromString("youtube.com/watch?v=$txt", 1100)),"Share using QR Code")
) {



    override fun show() {
        super.show()
    }
}
