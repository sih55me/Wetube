package app.wetube.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.app.NotificationCompat
import app.wetube.R

fun showMessage(context: Context, block : (NotificationCompat.Builder) -> Unit){
    val notifMan = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val n = NotificationCompat.Builder(context.applicationContext, "2")
    block.invoke(n)
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        val c =  NotificationChannel("2", "Other", NotificationManager.IMPORTANCE_DEFAULT)
        notifMan.createNotificationChannel(c)
        n.setChannelId("2")
    }
    notifMan.notify(1, n.build())
}
public fun showToastAsNotif(
    context: Context,
    title : String = "Info",
    text : String,
    delay : Long
){
    val notifMan = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val n = NotificationCompat.Builder(context.applicationContext, "2")
    n.apply {
        setStyle(
            NotificationCompat.BigTextStyle().bigText(text)
        )
        setContentText(text)
        setContentTitle(title)
        setSmallIcon(R.drawable.info)
    }

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        val c =  NotificationChannel("2", "Other", NotificationManager.IMPORTANCE_HIGH)
        notifMan.createNotificationChannel(c)
        n.setChannelId("2")
    }
    notifMan.notify(1, n.build())
    if(delay > 1L){
        Handler(Looper.getMainLooper()).postDelayed({
            notifMan.cancel(1)
        }, delay)
    }
}
fun showToastAsNotif(
    context: Context,
    title : String = "Info",
    text : String
    ) = showToastAsNotif(context, title, text, 5000L)