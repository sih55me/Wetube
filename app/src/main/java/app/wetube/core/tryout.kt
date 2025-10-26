package app.wetube.core

import android.content.Context
import android.widget.Toast

fun tryOut(context: Context, block : () -> Unit){
    try {
        block.invoke()
    }catch (e:Exception){
        Toast.makeText(
           context, e.message ?: e.toString(),
            Toast.LENGTH_SHORT
       ).show()
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