package app.wetube.nothing

import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.ActionProvider
import android.view.ContextMenu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View

open class NothingItem: MenuItem {
    companion object{
        fun newInstance() = object  : NothingItem(){}
    }
    override fun getItemId(): Int = 0

    override fun getGroupId(): Int = 0

    override fun getOrder(): Int = 0

    override fun setTitle(title: CharSequence?): MenuItem = this

    override fun setTitle(title: Int): MenuItem = this

    override fun getTitle(): CharSequence = ""

    override fun setTitleCondensed(title: CharSequence?): MenuItem  = this

    override fun getTitleCondensed(): CharSequence = ""

    override fun setIcon(icon: Drawable?): MenuItem = this

    override fun setIcon(iconRes: Int): MenuItem = this

    override fun getIcon(): Drawable? = null

    override fun setIntent(intent: Intent?): MenuItem = this

    override fun getIntent(): Intent = Intent()

    override fun setShortcut(numericChar: Char, alphaChar: Char): MenuItem = this

    override fun setNumericShortcut(numericChar: Char): MenuItem = this

    override fun getNumericShortcut(): Char  = Char.MIN_VALUE

    override fun setAlphabeticShortcut(alphaChar: Char): MenuItem = this

    override fun getAlphabeticShortcut(): Char {
        TODO("Not yet implemented")
    }

    override fun setCheckable(checkable: Boolean): MenuItem = this

    override fun isCheckable(): Boolean = false

    override fun setChecked(checked: Boolean): MenuItem = this

    override fun isChecked(): Boolean = false

    override fun setVisible(visible: Boolean): MenuItem = this

    override fun isVisible(): Boolean = false

    override fun setEnabled(enabled: Boolean): MenuItem = this

    override fun isEnabled(): Boolean = false

    override fun hasSubMenu(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getSubMenu(): SubMenu? = null

    override fun setOnMenuItemClickListener(menuItemClickListener: MenuItem.OnMenuItemClickListener?): MenuItem = this

    override fun getMenuInfo(): ContextMenu.ContextMenuInfo? = null

    override fun setShowAsAction(actionEnum: Int) {
        TODO("Not yet implemented")
    }

    override fun setShowAsActionFlags(actionEnum: Int): MenuItem = this

    override fun setActionView(view: View?): MenuItem = this

    override fun setActionView(resId: Int): MenuItem = this

    override fun getActionView(): View? = null

    override fun setActionProvider(actionProvider: ActionProvider?): MenuItem = this

    override fun getActionProvider(): ActionProvider? = null

    override fun expandActionView(): Boolean = false

    override fun collapseActionView(): Boolean = false

    override fun isActionViewExpanded(): Boolean = false

    override fun setOnActionExpandListener(listener: MenuItem.OnActionExpandListener?): MenuItem = this
}