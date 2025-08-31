package app.wetube.core

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.Window
import app.wetube.databinding.LayoutDialogBinding

open class InfoDialog @JvmOverloads constructor(context: Context, themeResId: Int = 0, useCustomTheme: Boolean = true): Dialog(
    ContextThemeWrapper(context, THEME_ID), if(!useCustomTheme) THEME_ID else themeResId) {
    private val bin by lazy{ LayoutDialogBinding.inflate(layoutInflater) }
    companion object{
        const val THEME_ID = android.R.style.Theme_Holo_Dialog
    }
    var dismissOnButtonClick = true
    var title:String get() = bin.title.text.toString()
        set(value) {
            bin.title.text = value
            bin.title.visibility = View.VISIBLE
        }

    var message:String get() = bin.message.text.toString()
        set(value) {
            bin.message.text = value
            bin.message.visibility = View.VISIBLE
        }

    fun setButtonsText(yes:String? = null, no:String? = null){
        bin.btns.visibility = View.VISIBLE
        if(yes != null)
            bin.yes.apply{
                text = yes
                visibility = View.VISIBLE
            }
        if(no != null) {
            bin.no.apply {
                text = no
            }
            bin.no.visibility = View.VISIBLE
        }

    }


    var listener = object : DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) {

        }
    }

    var yesText:String get() = bin.yes.text.toString()
        set(value) {
            bin.yes.text = value
        }

    var noText:String get() = bin.no.text.toString()
        set(value) {
            bin.no.text = value
        }

    init {
        window?.requestFeature(Window.FEATURE_NO_TITLE)
        bin.apply {
            yes.setOnClickListener {
                listener.onClick(this@InfoDialog, DialogInterface.BUTTON_POSITIVE)
                initDis()
            }
            no.setOnClickListener {
                listener.onClick(this@InfoDialog, DialogInterface.BUTTON_NEGATIVE)
                initDis()
            }
        }
    }

    private fun initDis(){
        if(dismissOnButtonClick){
            dismiss()
        }
    }

    override fun show() {
        window?.setContentView(bin.root)
        super.show()
    }

    override fun setContentView(layoutResID: Int) {

    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {

    }

    override fun setContentView(view: View) {

    }

    override fun addContentView(view: View, params: ViewGroup.LayoutParams?) {

    }
}