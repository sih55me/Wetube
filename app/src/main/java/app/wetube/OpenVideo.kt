package app.wetube

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.view.View
import app.wetube.item.ChannelDetail
import app.wetube.item.Video
import app.wetube.item.VideoDetail
import app.wetube.service.Yt


/** Copyright Wetube*/


fun Intent.kembaliKe(used: Activity?){
    putExtra("backTo", used?.intent)
}
private fun getActivity(context: Context) = Intent(context, VideoView::class.java)

fun Activity.finishAndGoToMain(){
    val i = intent.getParcelableExtra<Intent>("backTo")
    finishAffinity()
    if(i is Intent) {
        val intent = Intent(i);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
 fun openVideo(
     context: Context,
     note: Video,
     position: Int = 0,
     playlist: Array<Video>? = null,
     view: View? = null,
     cplaylist: Array<ChannelDetail>? = null,
 ){
    val intent = openVideoNTicket(context, note, position, playlist, view, cplaylist)

    context.startActivity(intent)


}

@JvmOverloads
fun openVideoNTicket(
    context: Context,
    note: Video,
    position: Int = 0,
    playlist: Array<Video>? = null,
    view: View? = null,
    cplaylist: Array<ChannelDetail>? = null,
    intent : Intent= getActivity(context)
): Intent{
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    intent.putExtra("vid", note)
    intent.putExtra("vi", position)
    if(playlist != null) {
        intent.putExtra("playlist" , playlist)

    }
    if(note is VideoDetail ) {
        intent.putExtra("cplaylist" , playlist?.map { (it as VideoDetail).channel}?.toTypedArray())
    }

    if(context is Activity){
        intent.kembaliKe(context)
    }


//    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    Yt.link = note.id
    Yt.pos = position
    return setupFlags(context, intent)

}

fun setupFlags(context: Context, intent: Intent):Intent{
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
    if (sp.getBoolean("newtab", false)) {

        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    }
    if (!sp.getBoolean("allow_an", true)) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (sp.getBoolean("cav", false)) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
    return intent
}
 fun openVideo(
     context: Context,
     video: VideoDetail,
     playlist: Array<Video>? = null,
     view: View? = null,

     /** ^^ null to disable slide animation*/
 ){
    val intent = getActivity(context)
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    intent.putExtra("vid", video)
    intent.putExtra("exist", false)
     intent.putExtra("fromWidget", false)
     if(playlist != null) {
         intent.putExtra("playlist" , playlist)
     }



    Yt.apply {
        link = -1
        pos = 1
        videoId = video.videoId
    }


//    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(setupFlags(context, intent))

}
