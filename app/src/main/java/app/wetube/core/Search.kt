package app.wetube.core

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import app.wetube.ActivityDialog
import app.wetube.C
import app.wetube.C.KEY
import app.wetube.C.SNIPPET
import app.wetube.item.ChannelDetail
import app.wetube.item.VideoDetail
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.net.URLEncoder

class Search(val activity : Activity) {
    companion object{
        //ORDER VALUE
        const val DATE = "date"
        const val TITLE = "title"
        const val RATING = "rating"
        const val RELEVANCE = "relevance"
        const val VIDEO_COUNT = "videoCount"
        const val VIEW_COUNT = "viewCount"
        //SAFESEARCH VALUE
        const val MODERATE = "moderate"
        const val STRICT = "strict"
    }
    fun searchVideo(
        query:String ="",
        channelId:String="",
        max:Int = 30,
        pT:String="",
        safeSearch:String="strict",
        order:String="",
        onGet : ((VideoDetail) -> Unit) = {},
        onDone:((String)->Unit) = {}
    ){

        val requestQueue = Volley.newRequestQueue(activity.applicationContext)
        var urlT = "${C.BASE_URL}search?part=snippet&q=${URLEncoder.encode(query, "UTF-8")}&type=video&key=${KEY}&maxResults=$max&safeSearch=$safeSearch&videoEmbeddable=true"
        if(channelId.isNotEmpty()){
            urlT += "&channelId=$channelId"
        }
        if(pT.isNotEmpty()){
            urlT += "&pageToken=$pT"
        }
        if(order.isNotEmpty()){
            urlT += "&order=$order"
        }

        var key =""
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            try {
                Log.i("I URL", it)
                val xml = JSONObject(it)
                val list = xml.getJSONArray("items")
                for (i in 0 until list.length()) {
                    val item = list.getJSONObject(i)
                    val id = item.getJSONObject("id")
                    val snippet = item.getJSONObject(SNIPPET)
                    tryOn{
                        onGet.invoke(
                            VideoDetail(
                                id.getString("videoId"),
                                snippet.getString("title").changeEnHt(),
                                snippet.getString("description").changeEnHt(),
                                snippet.getJSONObject("thumbnails").getJSONObject("high")
                                    .getString("url"),
                                ChannelDetail(
                                    snippet.getString("channelTitle"),
                                    snippet.getString("channelId")
                                )
                            ).apply {
                                tryOn{
                                    postDate = snippet.getString("publishTime")
                                }
                            }
                        )
                    }
                }
                try {
                    key = xml.getString("nextPageToken")

                }catch (e:Exception){
                    Log.e("E URL", "Videos of the channel is limit", e)
                }
                onDone.invoke(key)
            } catch (e: JSONException) {
                //if quota exceeded
                try{
                    val xml = JSONObject(it)
                    val err = xml.getJSONObject("error")
                    val c = err.getInt("code")
                    val info = err.getString("message")
                    val wrapped = "Error Code = $c\nReason = $info"
                    error(activity,wrapped)
                }catch (_: Exception) {
                    error(activity, e.message + "\n" + it)
                    Log.e("E URL", e.message ?: "?")
                }
                onDone.invoke(key)
            }

        }, {e->
            error(activity, "Message :\n${e.localizedMessage}\nCuz : \n${e.stackTrace.joinToString(separator = "<-\n")}\nSuppress E:\n${e.suppressedExceptions.joinToString(separator = "<-\n")}\nSuppress:\n${
                e.suppressed.joinToString(
                    separator = "<-\n"
                )
            }")
            onDone.invoke(pT)
        })
        requestQueue.add(stringRequest)
    }
    fun searchChannel(
        query:String ="",
        max:Int = 30,
        pT:String="",
        safeSearch:String="strict",
        order:String="",
        onGet : ((ChannelDetail) -> Unit) = {},
        onDone:((String)->Unit) = {}
    ){

        val requestQueue = Volley.newRequestQueue(activity.applicationContext)
        var urlT = "${C.BASE_URL}search?part=snippet&q=${URLEncoder.encode(query, "UTF-8")}&type=channel&key=${KEY}&maxResults=$max&safeSearch=$safeSearch#"
        if(pT.isNotEmpty()){
            urlT += "&pageToken=$pT"
        }
        if(order.isNotEmpty()){
            urlT += "&order=$order"
        }

        var key =""
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            try {
                Log.i("I URL", it)
                val xml = JSONObject(it)
                val list = xml.getJSONArray("items")
                for (i in 0 until list.length()) {
                    val item = list.getJSONObject(i)
                    val snippet = item.getJSONObject(SNIPPET)
                    tryOn{
                        onGet.invoke(
                            ChannelDetail(
                                snippet.getString("title"),
                                snippet.getString("channelId")
                            ).apply {
                                tryOn{
                                    description = snippet.getString("description")
                                }
                            }
                        )
                    }
                }
                try {
                    key = xml.getString("nextPageToken")

                }catch (e:Exception){
                    Log.e("E URL", "Videos of the channel is limit", e)
                }
                onDone.invoke(key)
            } catch (e: JSONException) {
                //if quota exceeded
                try{
                    val xml = JSONObject(it)
                    val err = xml.getJSONObject("error")
                    val c = err.getInt("code")
                    val info = err.getString("message")
                    val wrapped = "Error Code = $c\nReason = $info"
                    error(activity,wrapped)
                }catch (_: Exception) {
                    error(activity, e.message + "\n" + it)
                    Log.e("E URL", e.message ?: "?")
                }
                onDone.invoke(key)
            }

        }, {e->
            error(activity, "Message :\n${e.localizedMessage}\nCuz : \n${e.stackTrace.joinToString(separator = "<-\n")}\nSuppress E:\n${e.suppressedExceptions.joinToString(separator = "<-\n")}\nSuppress:\n${
                e.suppressed.joinToString(
                    separator = "<-\n"
                )
            }")
            onDone.invoke(pT)
        })
        requestQueue.add(stringRequest)
    }
    fun findChannelById(
        id:String,
        onGet : ((ChannelDetail) -> Unit) = {},
        onDone:(()->Unit) = {}
    ){
        val requestQueue = Volley.newRequestQueue(activity.applicationContext)
        val urlT = "https://www.googleapis.com/youtube/v3/channels?part=snippet&id=$id&key=$KEY"
        val stringRequest = StringRequest(Request.Method.GET, urlT, {
            try {
                Log.i("I URL", it)
                val xml = JSONObject(it)
                val list = xml.getJSONArray("items")
                val item = list.getJSONObject(0)
                val snippet = item.getJSONObject(SNIPPET)
                onGet.invoke(
                    ChannelDetail(
                        snippet.getString("title"),
                        id
                    ).apply {
                        tryOn{
                            val hdt = snippet.getJSONObject("thumbnails").getJSONObject("high")
                            tryOn {
                                description = snippet.getString("description")
                            }
                            tryOn { born = snippet.getString("publishedAt") }
                            tryOn { lived = snippet.getString("country") }
                            tryOn { genzid = snippet.getString("customUrl") }
                            tryOn {
                                thumbnail = hdt
                                    .getString("url")
                                size = Pair(
                                    hdt.getInt("width"),
                                    hdt.getInt("height")
                                )
                            }
                        }
                    }
                )
                onDone.invoke()
            } catch (e: JSONException) {
                error(activity, e.message.toString())
                Log.e("E URL", e.message ?: "?")
                onDone.invoke()
            }

        }, {e->
            error(activity, e.message.toString())
            onDone.invoke()
        })
        requestQueue.add(stringRequest)
    }
    private fun error(activity:Activity, message:String){
        val notifMan = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val n = NotificationCompat.Builder(activity.applicationContext, "2")
        val i = object  :ActivityDialog.DialogCallback(){
            override fun onMakeButton(buttons: ActivityDialog.ButtonDialog) {
                buttons.setPositive ("Dismiss"){ d,_->
                    d.dismiss()
                }
            }
        }

        n.apply {
            setStyle(
                NotificationCompat.BigTextStyle().bigText(message)
            )
            setAutoCancel(true)
            setContentIntent(PendingIntent.getActivity(activity, 1, ActivityDialog.pack(activity, "Error", message, i), PendingIntent.FLAG_IMMUTABLE))
            setContentText(message)
            setContentTitle("Error in search@wetube")
            setSmallIcon(android.R.drawable.ic_dialog_alert)
            priority = NotificationCompat.PRIORITY_LOW
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val c =  NotificationChannel("2", "Other", NotificationManager.IMPORTANCE_HIGH)
            notifMan.createNotificationChannel(c)
            n.setChannelId("2")
            n.priority = NotificationCompat.PRIORITY_LOW
        }
        notifMan.notify(1, n.build())
    }
}