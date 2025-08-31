package app.wetube.service

import android.app.AlertDialog
import android.app.PendingIntent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import app.wetube.manage.db.VidDB
import app.wetube.openVideoNTicket
import kotlin.random.Random.Default.nextInt

@RequiresApi(api = Build.VERSION_CODES.N)
class RandomQS : TileService() {
    // Called when the user adds your tile.
    override fun onTileAdded() {
        super.onTileAdded()
    }

    // Called when your app can update your tile.
    override fun onStartListening() {
        if(qsTile != null){
            if (isEmpty) {
                qsTile.state = Tile.STATE_UNAVAILABLE
                qsTile.subtitle = "Empty"
            }else if (t.size < 2) {
                qsTile.state = Tile.STATE_UNAVAILABLE
                qsTile.subtitle = " > 1"
            } else {
                qsTile.state = Tile.STATE_ACTIVE
                qsTile.subtitle = null
            }
            qsTile.updateTile()
        }
        super.onStartListening()
    }

    // Called when your app can no longer update your tile.
    override fun onStopListening() {
        super.onStopListening()
    }

    // Called when the user taps on your tile in an active or inactive state.
    override fun onClick() {
        super.onClick()

        randomVid()
    }
    var r = 0
    val isEmpty get() = d.listAsList().isEmpty()
    val t get() = d.listAsList()
    val d by lazy{VidDB(baseContext)}

    fun randomVid() {
        if (isEmpty) {
            showDialog(AlertDialog.Builder(this).setMessage(app.wetube.R.string.no_vid).create())
            return
        }
        if(t.size < 2){

            showDialog(AlertDialog.Builder(this).setMessage("Must have at least 2 / more videos").create())
            return
        }
        try {
            r = nextInt(0, t.size - 1)
            val rv = t[r]
            val p = PendingIntent.getActivity(
                this,
                0,
                openVideoNTicket(this, rv, r, t.toTypedArray()),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(p)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        qsTile?.updateTile()
    }

    // Called when the user removes your tile.
    override fun onTileRemoved() {
        super.onTileRemoved()
    }
}