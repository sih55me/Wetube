package app.wetube.page.dialog

import android.content.Context
import android.view.Menu
import app.wetube.core.getQrCodeFromString

class QRCodePage(context: Context, txt:String, asDialog: Boolean = false): PreviewImgPage(
    context,
    Get(requireNotNull(getQrCodeFromString("youtube.com/watch?v=$txt", 1100)),"Share using QR Code"),
    asDialog
) {


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }


    override fun show() {
        super.show()
    }
}
