package app.wetube.core

import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.ActionProvider
import android.view.ContextMenu
import android.view.KeyEvent
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import app.wetube.nothing.NothingItem

abstract class OptionalMenu:SubMenu{
    
    private val nome get()= NothingItem.newInstance()
    
    override fun add(title: CharSequence?): MenuItem {
        return nome
    }

    override fun add(titleRes: Int): MenuItem {
        return nome
    }

    override fun add(groupId: Int, itemId: Int, order: Int, title: CharSequence?): MenuItem {
        return nome
    }

    override fun add(groupId: Int, itemId: Int, order: Int, titleRes: Int): MenuItem {
        return nome
    }

    override fun addSubMenu(title: CharSequence?): SubMenu {
        return this
    }

    override fun addSubMenu(titleRes: Int): SubMenu {
        return this
    }

    override fun addSubMenu(groupId: Int, itemId: Int, order: Int, title: CharSequence?): SubMenu {
        return this
    }

    override fun addSubMenu(groupId: Int, itemId: Int, order: Int, titleRes: Int): SubMenu {
        return this
    }

    override fun addIntentOptions(
        groupId: Int,
        itemId: Int,
        order: Int,
        caller: ComponentName?,
        specifics: Array<out Intent>?,
        intent: Intent?,
        flags: Int,
        outSpecificItems: Array<out MenuItem>?,
    ): Int {
        return 0
    }

    override fun removeItem(id: Int) {
        
    }

    override fun removeGroup(groupId: Int) {
        
    }

    override fun clear() {
        
    }

    override fun setGroupCheckable(group: Int, checkable: Boolean, exclusive: Boolean) {
        
    }

    override fun setGroupVisible(group: Int, visible: Boolean) {
        
    }

    override fun setGroupEnabled(group: Int, enabled: Boolean) {
        
    }

    override fun hasVisibleItems(): Boolean {
        return false
    }

    override fun findItem(id: Int): MenuItem {
        return nome
    }

    override fun size(): Int {
        return 0
    }

    override fun getItem(): MenuItem {
        return getItem(0)
    }

    override fun getItem(index: Int): MenuItem {
        return nome
    }

    override fun close() {
        
    }

    override fun performShortcut(keyCode: Int, event: KeyEvent?, flags: Int): Boolean {
        return false
    }

    override fun isShortcutKey(keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }

    override fun performIdentifierAction(id: Int, flags: Int): Boolean {
        return false
    }

    override fun setQwertyMode(isQwerty: Boolean) {

    }

    override fun setHeaderTitle(titleRes: Int): SubMenu {
        return this
    }

    override fun setHeaderTitle(title: CharSequence?): SubMenu {
        return this
    }

    override fun setHeaderIcon(iconRes: Int): SubMenu {
        return this
    }

    override fun setHeaderIcon(icon: Drawable?): SubMenu {
        return this
    }

    override fun setHeaderView(view: View?): SubMenu {
        return this
    }

    override fun clearHeader() {
        return
    }

    override fun setIcon(iconRes: Int): SubMenu {
        return this
    }

    override fun setIcon(icon: Drawable?): SubMenu {
        return this
    }

}
abstract class OptionalItem:MenuItem{
    override fun getItemId(): Int {
        return 0
    }

    override fun getGroupId(): Int {
        return 0
    }

    override fun getOrder(): Int {
        return 0
    }

    override fun setTitle(title: CharSequence?): MenuItem {
        return this
    }

    override fun setTitle(title: Int): MenuItem {
        return this
    }

    override fun getTitle(): CharSequence? {
        return ""
    }

    override fun setTitleCondensed(title: CharSequence?): MenuItem {
        return this
    }

    override fun getTitleCondensed(): CharSequence? {
        return ""
    }

    override fun setIcon(icon: Drawable?): MenuItem {
        return this
    }

    override fun setIcon(iconRes: Int): MenuItem {
        return this
    }

    override fun getIcon(): Drawable? {
        return null
    }

    override fun setIntent(intent: Intent?): MenuItem {
        return this
    }

    override fun getIntent(): Intent? {
        return null
    }

    override fun setShortcut(numericChar: Char, alphaChar: Char): MenuItem {
        return this
    }

    override fun setNumericShortcut(numericChar: Char): MenuItem {
        return this
    }

    override fun getNumericShortcut(): Char {
        return 's'
    }

    override fun setAlphabeticShortcut(alphaChar: Char): MenuItem {
        return this
    }

    override fun getAlphabeticShortcut(): Char {
        return 'a'
    }

    override fun setCheckable(checkable: Boolean): MenuItem {
        return this
    }

    override fun isCheckable(): Boolean {
        return false
    }

    override fun setChecked(checked: Boolean): MenuItem {
        return this
    }

    override fun isChecked(): Boolean {
        return false
    }

    override fun setVisible(visible: Boolean): MenuItem {
        return this
    }

    override fun isVisible(): Boolean {
        return false
    }

    override fun setEnabled(enabled: Boolean): MenuItem {
        return this
    }

    override fun isEnabled(): Boolean {
        return false
    }

    override fun hasSubMenu(): Boolean {
        return false
    }

    override fun getSubMenu(): SubMenu? {
        return null
    }

    override fun setOnMenuItemClickListener(menuItemClickListener: MenuItem.OnMenuItemClickListener?): MenuItem {
        return this
    }

    override fun getMenuInfo(): ContextMenu.ContextMenuInfo? {
        return null
    }

    override fun setShowAsAction(actionEnum: Int) {
        return
    }

    override fun setShowAsActionFlags(actionEnum: Int): MenuItem {
        return this
    }

    override fun setActionView(view: View?): MenuItem {
        return this
    }

    override fun setActionView(resId: Int): MenuItem {
        return this
    }

    override fun getActionView(): View? {
        return null
    }

    override fun setActionProvider(actionProvider: ActionProvider?): MenuItem {
        return this
    }

    override fun getActionProvider(): ActionProvider? {
        return null
    }

    override fun expandActionView(): Boolean {
        return false
    }

    override fun collapseActionView(): Boolean {
        return false
    }

    override fun isActionViewExpanded(): Boolean {
        return false
    }

    override fun setOnActionExpandListener(listener: MenuItem.OnActionExpandListener?): MenuItem {
        return this
    }

}
