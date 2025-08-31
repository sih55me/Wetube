package app.wetube.page

import android.app.Activity
import android.app.DialogFragment
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import app.wetube.R
import app.wetube.core.OptionalItem
import app.wetube.core.OptionalMenu
import app.wetube.core.tryOn
import app.wetube.databinding.MenuDialogBinding
import app.wetube.page.MenuDialog.VMenu.Companion.toV
import app.wetube.page.MenuDialog.VMenuItem.Companion.toV
import java.io.ByteArrayOutputStream


class MenuDialog:DialogFragment() {
    private val bin by lazy{ MenuDialogBinding.inflate(activity!!.layoutInflater)}
    var listener : ((VMenuItem)-> Unit) = {
        dismissAllowingStateLoss()
    }
    var menu : VMenu? = null
    companion object{
        fun Activity.menuDialog(menu: VMenu, listener: (VMenuItem) -> Unit) = MenuDialog().apply {
            this.menu = menu
            this.listener = listener

        }
        fun Activity.menuDialog(menu: VMenu) = MenuDialog().apply {
            this.menu = menu

        }
    }





    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return bin.root
    }



    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(menu != null) {
            dialog?.setTitle(menu!!.headerTitle.ifEmpty { "Main Menu" })
            Log.i(this::class.java.simpleName, "Menu found")
            bin.list.adapter = MenuAdapter(menu!!.totalVisible, listener).apply {
                onSubMenuListener = {
                    activity!!.menuDialog(it.subMenu){
                        listener(it)
                        dismiss()
                    }.show(this@MenuDialog.fragmentManager, "mns")
                }
            }
        }
        view?.requestFocusFromTouch()
        view?.requestFocus()
    }

    class MenuAdapter(private val menu:VMenu, private val listener : ((VMenuItem)-> Unit)) : RecyclerView.Adapter<MenuItemView>() {
        var onSubMenuListener :((VMenuItem)->Unit)= {

        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MenuItemView(LayoutInflater.from(parent.context).inflate(R.layout.menu_item,parent, false))

        override fun getItemCount(): Int {
            return menu.size
        }

        override fun onBindViewHolder(holder: MenuItemView, position: Int) {
            val i = menu.getItem(position)
            if(i.iconRes != null) {
                val drawable = holder.itemView.context.resources.getDrawable(i.iconRes!!)
                val bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888
                )
                val canvas: Canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
                drawable.draw(canvas)
                i.setIcon(bitmap)
            }
            holder.itemView.findViewById<ImageView>(R.id.menuIcon).setImageBitmap(i.getIcon())
            if(i.titleRes != null ){
                i.title = holder.itemView.context.getString(i.titleRes!!)
            }
            holder.itemView.findViewById<Switch>(R.id.check).apply{
                visibility = if(i.isCheckable) View.VISIBLE else View.GONE
                isChecked = i.isChecked
                setOnCheckedChangeListener { buttonView, isChecked ->
                    i.isCheckable = isChecked
                }
            }
            if(i.customViewRes != null){
                i.customView = LayoutInflater.from(holder.itemView.context).inflate(i.customViewRes!!, null)
            }
            if(i.customView != null){
                val v = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val av =holder.itemView.findViewById<FrameLayout>(R.id.viewAction)
                if(i.customView!!.parent != null){
                    tryOn{
                        (i.customView!!.parent as ViewGroup).removeView(i.customView)
                    }
                }
                av.addView(i.customView, v)
                holder.itemView.findViewById<View>(R.id.more).apply{
                    visibility = View.VISIBLE
                    setOnClickListener {
                        if(av.visibility == View.GONE){
                            av.visibility = View.VISIBLE
                            this.rotation = 180F
                        }else{
                            av.visibility = View.GONE
                            this.rotation = 0F
                        }

                    }
                }
            }

            holder.itemView.findViewById<TextView>(R.id.menuTitle).text = i.title
            holder.itemView.visibility = when(i.isVisible){
                true -> View.VISIBLE
                false -> View.GONE
            }
            holder.itemView.setOnClickListener {
                if(i.customView != null){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                        holder.itemView.findViewById<View>(R.id.more).callOnClick()
                    }
                    return@setOnClickListener
                }
                if(i.isCheckable){
                    val c = holder.itemView.findViewById<Switch>(R.id.check)
                    c.isChecked = !c.isChecked
                    return@setOnClickListener
                }

                if(i.asAndroidMenuItem().hasSubMenu()){

                }

                if(i.onClick != null){
                    i.onClick?.let { it1 -> it1(i) }
                }
                listener(i)
            }
        }

    }
    class MenuItemView(view:View):RecyclerView.ViewHolder(view)
    class VMenu{
        private val items = mutableListOf<VMenuItem>()
        val totalVisible :VMenu
        get() = VMenu().also {new->
            for (i in items) {
                if (i.isVisible) new.add(i)
            }
        }
        var headerTitle = ""

        var onItemChangedList : ((VMenu)->Unit) = {

        }


        companion object{
            fun Menu.toV():VMenu{
                val ins = VMenu()
                if(size() == 0){
                    Log.i(javaClass.simpleName, "Menu (android) is empty")
                }
                for(i in 0..size()){
                    ins.add(getItem(i).toV())
                }
                return ins
            }
        }

        fun add(itemId: Int,title: CharSequence?): VMenuItem {
            return VMenuItem().apply {
                add(this)
                this.title = (title.toString())
                this.itemId = (itemId)
                onItemChangedList(this@VMenu)
            }
        }

        fun add(item: VMenuItem) {
            items.add(item)
            onItemChangedList(this)
        }


        fun removeItem(id: Int) {
            items.apply{
                remove(find { it.itemId == id } )
            }
            onItemChangedList(this)
        }

        val isEmpty get() = items.isEmpty()

        fun clear() {
            items.clear()
            onItemChangedList(this)
        }
        fun findItem(id: Int): VMenuItem {
            return items.find { id == it.itemId } ?: throw Exception("item with id $id not found")
        }

        val size: Int get()= items.size

        fun getItem(index: Int): VMenuItem = items[index]

        fun asAndroidMenu(): Menu = object:OptionalMenu(){
            val g = this@VMenu
            override fun add(
                groupId: Int,
                itemId: Int,
                order: Int,
                title: CharSequence?,
            ): MenuItem {
                return g.add(itemId, title).asAndroidMenuItem()
            }

            override fun setHeaderTitle(title: CharSequence?): SubMenu {
                this@VMenu.headerTitle = title.toString()
                return this

            }

            override fun add(title: CharSequence?): MenuItem {
                return add(0,0,0,title)
            }

            override fun add(titleRes: Int): MenuItem = g.add(0,"").also{ it.titleRes = titleRes }.asAndroidMenuItem()


            override fun size(): Int {
                return g.size
            }

            override fun removeItem(id: Int) {
                g.removeItem(id)
            }

            override fun findItem(id: Int): MenuItem {
                return g.findItem(id).asAndroidMenuItem()
            }


        }

    }
    class VMenuItem{
        companion object{
            fun MenuItem.toV():VMenuItem{
                return VMenuItem().apply {
                    title = this@toV.title.toString()
                    itemId = this@toV.itemId
                    val drawable = this@toV.icon
                    if(drawable != null){
                        val bitmap = Bitmap.createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888
                        )
                        val canvas = Canvas(bitmap)
                        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
                        drawable.draw(canvas)
                        setIcon(bitmap)
                    }
                    intent = this@toV.intent
                    this@toV.subMenu?.let{
                        subMenu =  it.toV()
                        subMenu.headerTitle = title.toString()
                    }
                    isCheckable = this@toV.isCheckable
                    isChecked = this@toV.isChecked
                    isVisible = this@toV.isVisible
                    isEnabled = this@toV.isEnabled
                    customView = this@toV.actionView
                    onClick = {
                        tryOn{
                            this@toV.javaClass.getDeclaredMethod("invoke").invoke(this@toV)
                        }
                    }
                }
            }
        }
        private val data = Bundle()
        var subMenu = VMenu()
        var onClick : ((VMenuItem) -> Unit) ?= null

        var itemId
            get() = data.getInt("itemId")
            set(id) =  data.putInt("itemId", id)

        var title
            set(title) = data.putString("title", title.toString())
            get() = data.getString("title")



        fun setIcon(icon: Bitmap?): VMenuItem {
            val baos = ByteArrayOutputStream()
            icon?.compress(Bitmap.CompressFormat.PNG, 100, baos) //bm is the bitmap object
            val b = baos.toByteArray()
            data.putByteArray("icon", b)
            return this
        }


        fun getIcon(): Bitmap? {
            val imageAsBytes = data.getByteArray("icon")
            return imageAsBytes?.size?.let {
                BitmapFactory.decodeByteArray(imageAsBytes, 0,
                    it
                )
            }
        }

        var titleRes:Int? = null
        var iconRes:Int? = null
        var customView:View? = null
        var customViewRes:Int? = null

        var intent
        set(intent) = data.putParcelable("intent", intent)
            get() = data.getParcelable<Intent>("intent")


        var isCheckable: Boolean
        get() = data.getBoolean("checkable", false)
        set(value) = data.putBoolean("checkable", value)

        var isChecked: Boolean get()= data.getBoolean("checked", false)
        set(value) = data.putBoolean("checked", value)


        var isVisible: Boolean get()= data.getBoolean("visible", true)
        set(value) = data.putBoolean("visible", value)


        var isEnabled: Boolean get()= data.getBoolean("enabled", true)
        set(value) = data.putBoolean("enabled", value)

        val anIt : Any = asAndroidMenuItem()

        fun asAndroidMenuItem() = object :OptionalItem(){
            val vi =  this@VMenuItem
            val vil = vi as Any
            override fun setIntent(intent: Intent?): MenuItem {
               vi.intent = intent
                return this
            }

            override fun equals(other: Any?): Boolean {
                return false
            }

            override fun getIcon(): Drawable {
                return BitmapDrawable(vi.getIcon())
            }

            override fun setOnMenuItemClickListener(menuItemClickListener: MenuItem.OnMenuItemClickListener?): MenuItem {
                val p = vi.anIt as MenuItem
                vi.onClick = {
                    menuItemClickListener?.onMenuItemClick(p)
                }
                return p
            }

            override fun getSubMenu(): SubMenu {
                return vi.subMenu.asAndroidMenu() as SubMenu

            }

            override fun setIcon(icon: Drawable?): MenuItem {
                if(icon != null){
                    val bitmap = Bitmap.createBitmap(
                        icon.getIntrinsicWidth(),
                        icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
                    icon.draw(canvas)
                    vi.setIcon(bitmap)
                }
                return this
            }
            override fun setIcon(iconRes: Int): MenuItem {
               vi.iconRes = iconRes
                return this
            }

            override fun hasSubMenu(): Boolean {
                return vi.subMenu.size != 0
            }


            override fun getItemId(): Int {
                return vi.itemId
            }

            override fun getTitle(): CharSequence? {
                return vi.title
            }

            override fun setCheckable(checkable: Boolean): MenuItem {
               vi.isCheckable = checkable
                return this
            }
            override fun setChecked(checked: Boolean): MenuItem {
               vi.isChecked = checked
                return this
            }

            override fun setVisible(visible: Boolean): MenuItem {
               vi.isVisible = visible
                return this
            }
            override fun setEnabled(enabled: Boolean): MenuItem {
               vi.isEnabled = enabled
                return this
            }

            override fun setTitle(title: CharSequence?): MenuItem {
               vi.title = title.toString()
                return this
            }
            override fun setTitle(titleRes: Int): MenuItem {
               vi.titleRes = titleRes
                return this
            }

            override fun setActionView(view: View?): MenuItem {
                customView = view
                return this
            }

            override fun setActionView(resId: Int): MenuItem {
                customViewRes = resId
                return this
            }

        }

    }

}