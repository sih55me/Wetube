package app.wetube.core.menu

import android.os.IBinder
import android.view.Menu
import app.wetube.core.ClassOpen

class MenuSheet(private val menu: Menu): ClassOpen("com.android.internal.view.menu.MenuDialogHelper") {
    init {
        if(menu.javaClass.name != "com.android.internal.view.menu.MenuBuilder"){
            throw IllegalArgumentException("menu must be a MenuBuilder")
        }
    }


    override val result: Any?
        get() = cz.getDeclaredConstructor(Class.forName("com.android.internal.view.menu.MenuBuilder")).apply{
            isAccessible = true
        }.newInstance(menu)


    fun show(token: IBinder? = null){
        cz.getDeclaredMethod("show", IBinder::class.java).apply {
            isAccessible = true
            invoke(result, token)
        }
    }
}