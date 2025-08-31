package app.wetube.core

import android.content.Context

fun tryOut(context: Context, block : () -> Unit){
    try {
        block.invoke()
    }catch (e:Exception){
       showToastAsNotif(
           context,
           text = e.message ?: e.toString()
       )
        e.printStackTrace()

    }
}
inline fun tryOn(block : () -> Unit)=tryOn(false, block)

inline fun tryOn(quiet:Boolean, block : () -> Unit){
    try {
        block.invoke()
    }catch (e:Exception){
        if(!quiet) {
            e.printStackTrace()
        }
    }
}