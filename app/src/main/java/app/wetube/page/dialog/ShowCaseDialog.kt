package app.wetube.page.dialog

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import app.wetube.window.Paper
import com.github.amlcurran.showcaseview.OnShowcaseEventListener
import com.github.amlcurran.showcaseview.ShowcaseView

class ShowCaseDialog(activity: Activity, private val oTB : OnToBuild): Paper(activity, false) {
    var hasNext = false
    private var showCase : ShowcaseView? = null
    init {
        setOwnerActivity(activity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window!!.attributes!!.layoutInDisplayCutoutMode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                } else WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        windowAnimation = (android.R.style.Animation_Toast)
        window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        decorView?.systemUiVisibility = activity.window.decorView.systemUiVisibility
        window?.setBackgroundDrawable(ColorDrawable(0))
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)
    }

    override fun setTitle(titleId: Int) {
        super.setTitle(titleId)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = Button(ownerActivity)
        if(hasNext){
            b.setText(app.wetube.R.string.next)
        }else{
            b.setText(app.wetube.R.string.close)
        }
        ShowcaseView.Builder(ownerActivity)
            .also {
                oTB.onToBuild(it)
            }
            .withHoloShowcase()
            .replaceEndButton(b)
            .setParent(window!!.decorView as ViewGroup, -1)
            .hideOnTouchOutside()
            .build().also { s ->
                showCase = s
                s.setOnShowcaseEventListener(object : OnShowcaseEventListener{
                    override fun onShowcaseViewHide(showcaseView: ShowcaseView?) {

                        dismiss()
                    }

                    override fun onShowcaseViewDidHide(showcaseView: ShowcaseView?) {

                    }

                    override fun onShowcaseViewShow(showcaseView: ShowcaseView?) {

                    }

                    override fun onShowcaseViewTouchBlocked(motionEvent: MotionEvent?) {

                    }

                })
                b.setOnClickListener {
                    s.hide()
                }
                s.fitsSystemWindows = true
            }
    }

    interface OnToBuild{
        fun onToBuild(b: ShowcaseView.Builder)

    }
}