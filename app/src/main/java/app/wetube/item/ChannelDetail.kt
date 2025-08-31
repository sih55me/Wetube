package app.wetube.item

import android.os.Parcel
import android.os.Parcelable

data class ChannelDetail(
    val title: String,
    val id: String
) :Parcelable{
    var description = ""
    var thumbnail = ""
    var born = ""
    var lived = ""
    var size = Pair(0,0)

    var genzid = ""
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
        description = parcel.readString().toString()
        thumbnail = parcel.readString().toString()
        born = parcel.readString().toString()
        lived = parcel.readString().toString()
        size = Pair(
            parcel.readInt(),
            parcel.readInt()
        )
        genzid = parcel.readString().toString()
    }

    override fun toString(): String = ("Channel title : $title\nChannel id : $id")
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(id)
        parcel.writeString(description)
        parcel.writeString(thumbnail)
        parcel.writeString(born)
        parcel.writeString(lived)
        parcel.writeInt(size.first)
        parcel.writeInt(size.second)
        parcel.writeString(genzid)
    }




    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChannelDetail> {
        override fun createFromParcel(parcel: Parcel): ChannelDetail {
            return ChannelDetail(parcel)
        }

        override fun newArray(size: Int): Array<ChannelDetail?> {
            return arrayOfNulls(size)
        }
    }


}