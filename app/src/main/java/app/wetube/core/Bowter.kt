package app.wetube.core

import android.media.MediaRouter.RouteInfo


/**
 * class to access hidden method for [RouteInfo]
 */
class Bowter(private val need: RouteInfo?) {
    val isSelected : Boolean get() {
        return try{
            getHideMet("isSelected")
        }catch (_: Exception){
            false
        }
    }

    val isDefault : Boolean get() {
        return try{
            getHideMet("isDefault")
        }catch (_: Exception){
            false
        }
    }
    /**
     * check if display connect using blutut
     */
    val isBluetooth : Boolean get() {
        return try{
            getHideMet("isBluetooth")
        }catch (_: Exception){
            false
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getHideMet(met: String):T{
        return invokeMet(met) as T
    }

    private fun invokeMet(met: String): Any {
        if(need == null){
            return Any()
        }
        return need::class.java.getDeclaredMethod(met).apply {
            isAccessible = true
        }.invoke(need)
    }
    /**
     * can be disconnect
     */
    fun select(){
        invokeMet("select")
    }
}