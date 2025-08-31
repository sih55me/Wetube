package app.wetube.core.menu

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu

class ArrayMenu(private val context: Context):  Menu {
    val mana = ArrayList<MenuItem>()
    override fun add(title: CharSequence?): MenuItem? {
        return add(0,0,0,title)
    }

    var lC = Runnable{}
    override fun add(titleRes: Int): MenuItem? {
        return add(context.getString(titleRes))
    }

    val visibleOnly get() = mana.filter { it.isVisible }

    override fun add(
        groupId: Int,
        itemId: Int,
        order: Int,
        title: CharSequence?,
    ): MenuItem? {
        return ActionMenuItem(context, groupId, itemId, order,title).also { 
            mana.add(it)
            onItemsChanged()
        }
    }

    override fun add(
        groupId: Int,
        itemId: Int,
        order: Int,
        titleRes: Int,
    ): MenuItem? {
        return add(0,0,0,context.getString(titleRes))
    }

    override fun addSubMenu(title: CharSequence?): SubMenu? {
        return null
    }

    override fun addSubMenu(titleRes: Int): SubMenu? {
        return addSubMenu("")
    }

    override fun addSubMenu(
        groupId: Int,
        itemId: Int,
        order: Int,
        title: CharSequence?,
    ): SubMenu? {
        return null
    }

    override fun addSubMenu(
        groupId: Int,
        itemId: Int,
        order: Int,
        titleRes: Int,
    ): SubMenu? {
        return null
    }

    override fun addIntentOptions(
        groupId: Int,
        itemId: Int,
        order: Int,
        caller: ComponentName?,
        specifics: Array<out Intent?>?,
        intent: Intent?,
        flags: Int,
        outSpecificItems: Array<out MenuItem?>?,
    ): Int {
        return 0
    }

    override fun removeItem(id: Int) {
        findItem(id)?.let {
            mana.remove(it)
        }
        onItemsChanged()
    }

    override fun removeGroup(groupId: Int) {
        
    }

    override fun clear() {
        mana.clear()
        onItemsChanged()
    }

    private fun onItemsChanged() {
        lC.run()
    }

    override fun setGroupCheckable(
        group: Int,
        checkable: Boolean,
        exclusive: Boolean,
    ) {
        
    }

    override fun setGroupVisible(group: Int, visible: Boolean) {
        
    }

    override fun setGroupEnabled(group: Int, enabled: Boolean) {
        
    }

    override fun hasVisibleItems(): Boolean {
        return false
    }

    override fun findItem(id: Int): MenuItem? {
        return mana.find { it.getItemId() == id }
    }

    override fun size(): Int {
        return mana.size
    }



    override fun getItem(index: Int): MenuItem? {
        return mana[index]
    }

    override fun close() {
        onItemsChanged()
    }

    override fun performShortcut(
        keyCode: Int,
        event: KeyEvent?,
        flags: Int,
    ): Boolean {
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
}