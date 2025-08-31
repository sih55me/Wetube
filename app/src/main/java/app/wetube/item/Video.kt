package app.wetube.item

import android.os.Parcel
import android.os.Parcelable
import android.util.Log

/**
 * [Video] is class that save info like [videoId] and [title].
 *
 * This class extend with [Parcelable], [java.lang.Cloneable] and [java.lang.Object]
 **/
@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
open class Video (
    open var id : Int,
    open var title : String,
    open var videoId : String,
):Parcelable, Object() {

    constructor() : this(0,"","")
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString()
    )

    fun trans(vid:VideoDetail){
        id = 0
        title = vid.title
        videoId = vid.videoId
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(videoId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Video> {
        override fun createFromParcel(parcel: Parcel): Video {
            return Video(parcel)
        }

        override fun newArray(size: Int): Array<Video?> {
            return arrayOfNulls(size)
        }
    }

    @JvmOverloads
    fun isSame(conTit:Boolean = true,conId:Boolean =true ,conVid:Boolean =true, other: Video): Boolean {
        //check characteristic of video is same
        var v = false
        var t = false
        var i = false
        if(!(conVid and conTit and conId)){
            Log.e("isSame", "must select one")
            return false
        }
        if(conVid){
            v = videoId == other.videoId
        }
        if(conTit){
            t = title == other.title
        }
        if(conId) {
            i = id == other.id
        }

        return v && t && i
    }

    /**
     * useful if this class is extend with [Video]
     * @return [Video]
     **/

    val actually:Video get() = this

    /**
     * Make a copycat of this
     *
     * @return [Video] that cosplay as [Any]
     **/
    public override fun clone(): Any {
        return Video(id,title,videoId)
    }


    var selected = false


}
