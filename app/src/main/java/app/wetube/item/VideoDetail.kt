package app.wetube.item

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable


/**
 * [VideoDetail] is class that save info like [videoId], [title],[description],[thumb], and [channel]
 *
 * This class is base on [Video]
 **/
data class VideoDetail(
    @Deprecated("This field is Unused") override var id: Int,
    override var title: String,
    override var videoId: String,
    var description: String,
    var thumb: String,
    var channel: ChannelDetail,
):Video(id,title, videoId), SmthFromInternet {


    constructor(
        videoId: String,
        title: String,
        description: String,
        thumb: String,
        channel: ChannelDetail,
    ) : this(0, title, videoId,description,thumb,channel)
    var postDate = ""
    override val bun: Bundle = Bundle()

    init {
        bun.putString("title", title)
        bun.putString("videoId", videoId)
        bun.putString("description", description)
        bun.putString("thumb", thumb)
        bun.putParcelable("channel", channel)
        bun.putString("postDate", postDate)
    }

    @SuppressLint("NewApi")
    override fun toString(): String = (
            "Video id : $videoId" +
                    "Title : $title" +
                    "Description : $description" +
                    "Thumb : $thumb" +
                    channel.toString()
            )

    override fun finalize() {
        videoId = ""
        title = ""
        description = ""
        thumb = ""
        channel = ChannelDetail("", "")
        super.finalize()
    }

    fun getUrl() = ("https://www.youtube.com/watch?v=$videoId")
    fun getChannelUrl() = ("https://www.youtube.com/channel/${channel.id}")


    constructor(parcel: Parcel):this(
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readParcelable(ChannelDetail::class.java.classLoader)!!
    ){
        postDate = parcel.readString().toString()
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(videoId)
        parcel.writeString(description)
        parcel.writeString(thumb)
        parcel.writeParcelable(channel,flags)
        parcel.writeString(postDate)
    }


    companion object CREATOR : Parcelable.Creator<VideoDetail> {
        override fun createFromParcel(parcel: Parcel): VideoDetail {
            return VideoDetail(parcel)
        }

        override fun newArray(size: Int): Array<VideoDetail?> {
            return arrayOfNulls(size)
        }
    }



}
