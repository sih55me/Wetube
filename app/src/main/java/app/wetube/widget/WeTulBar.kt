package app.wetube.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.PopupMenu
import app.wetube.core.Utils
import app.wetube.databinding.TulBinding
import app.wetube.page.MenuDialog
import app.wetube.page.MenuDialog.VMenu.Companion.toV
import app.wetube.page.MenuDialog.VMenuItem.Companion.toV

class WeTulBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0 , defStyleRes: Int = 0):LinearLayout(context, attrs, defStyleAttr,defStyleRes){


    private val tulBinding  = TulBinding.inflate(LayoutInflater.from(context))
    var title set(value) {
        titleView.text = value
    }
    get() = titleView.text.toString()
    var onItemClick :((MenuDialog.VMenuItem) -> Unit) = {

    }
    private var pmenu = MenuDialog.VMenu()
    private val backBtn = tulBinding.close
    val titleView get()= tulBinding.textView5
    var titleColor
        set(value) {
            titleView.setTextColor(value)
        }
        get() = titleView.currentTextColor
    init {
        orientation = LinearLayout.HORIZONTAL
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val tv = TypedValue()
        context.theme.resolveAttribute(android.R.attr.homeAsUpIndicator, tv, true)
        backBtn.setImageResource(tv.resourceId)
        val u = Utils(context)
        val s = u.pxToDp(50F).toInt()
        addView(tulBinding.root, LayoutParams.MATCH_PARENT, s)
        pmenu.onItemChangedList = {
            tulBinding.more.visibility =  VISIBLE
        }
        tulBinding.more.setOnClickListener {
            PopupMenu(it.context, it).apply {
                for (i in 0..<pmenu.totalVisible.size){
                    val its = pmenu.totalVisible.getItem(i)
                    this.menu.add(0, 0, i, its.title)
                }
                setOnMenuItemClickListener {
                    onItemClick(pmenu.totalVisible.getItem(it.order))
                    true
                }
                show()
            }
        }
    }


    var showBackButton = false
        set(value) {
            field = value
            backBtn.visibility = if(value) VISIBLE else GONE
        }
    fun setOnBackClick(onClickListener: OnClickListener){
        backBtn.setOnClickListener(onClickListener)
    }

    fun setupWithActivity(activity: Activity) {
        pmenu.let {
            activity.apply{
                val m = it.asAndroidMenu()
                onCreateOptionsMenu(m)
                onPrepareOptionsMenu(m)
                onItemClick = {
                    onOptionsItemSelected(it.asAndroidMenuItem())
                }
            }
        }
    }


    val menu get() = pmenu


    fun changeMenu(target:Any?){
        when(target){
            is MenuDialog.VMenu -> pmenu = target
            is Menu -> pmenu = target.toV()
            else -> Log.e(this::class.java.simpleName, "Invalid target type")
        }
    }

    fun loadFrom(menu: Menu?) {
        menu?.let {
            pmenu.clear()
            for(i in 0..<it.size()){
                pmenu.add(it.getItem(i).toV())
            }
        }
    }

    fun click(target: Any?){
        when(target) {
            is MenuDialog.VMenuItem -> onItemClick(target)
            is MenuItem -> onItemClick(target.toV())
        }

    }




}