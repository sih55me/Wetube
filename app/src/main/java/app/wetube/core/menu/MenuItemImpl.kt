package app.wetube.core.menu

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.ActionProvider
import android.view.ContextMenu.ContextMenuInfo
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewDebug.CapturedViewProperty
import android.widget.LinearLayout
import java.lang.reflect.Constructor
import java.lang.reflect.Method

class MenuItemImpl internal constructor(private val mMenu: MenuBuilder, private val mGroup: Int, private val mId: Int, private val mCategoryOrder: Int, val ordering: Int,
                                        private var mTitle: CharSequence?,
    showAsAction: Int
) : MenuItem {
    private var mTitleCondensed: CharSequence? = null
    private var mIntent: Intent? = null
    private var mShortcutNumericChar = 0.toChar()
    private var mShortcutNumericModifiers = KeyEvent.META_CTRL_ON
    private var mShortcutAlphabeticChar = 0.toChar()
    private var mShortcutAlphabeticModifiers = KeyEvent.META_CTRL_ON

    /** The icon's drawable which is only created as needed  */
    private var mIconDrawable: Drawable? = null

    /**
     * The icon's resource ID which is used to get the Drawable when it is
     * needed (if the Drawable isn't already obtained--only one of the two is
     * needed).
     */
    private var mIconResId: Int = NO_ICON

    private var mIconTintList: ColorStateList? = null
    private var mIconTintMode: PorterDuff.Mode? = null
    private var mHasIconTint = false
    private var mHasIconTintMode = false
    private var mNeedToApplyIconTint = false

    /** If this item should launch a sub menu, this is the sub menu to launch  */
    var callback: Runnable? = null
        private set
    private var mClickListener: MenuItem.OnMenuItemClickListener? = null

    private var mFlags: Int = ENABLED
    private var mShowAsAction = MenuItem.SHOW_AS_ACTION_NEVER

    private var mActionView: View? = null
    private var mActionProvider: ActionProvider? = null
    private var mOnActionExpandListener: MenuItem.OnActionExpandListener? = null
    private var mIsActionViewExpanded = false

    /**
     * Current use case is for context menu: Extra information linked to the
     * View that added this item to the context menu.
     */
    private var mMenuInfo: ContextMenuInfo? = null

    private var mContentDescription: CharSequence? = null
    private var mTooltipText: CharSequence? = null

    var internalOne: MenuItem? = null
    private set


    init {
        mShowAsAction = showAsAction
        val incl: Class<*> = Int::class.java
        val d = internalClass?.declaredConstructors?.map { it?.apply{ it.isAccessible = true } }?.map { it?.parameterTypes?.map { it.name }}
        try {
            val c: Constructor<*> = internalClass!!.getDeclaredConstructor(
                MenuBuilder.internalClass,
                incl,
                incl,
                incl,
                incl,
                CharSequence::class.java,
                incl
            )
            c.isAccessible = true
            internalOne = c.newInstance(
                mMenu.internalOne,
                mGroup,
                mId,
                mCategoryOrder,
                ordering,
                mTitle,
                showAsAction
            ) as MenuItem
        } catch (e: Exception) {
            throw Error("Failed to born the menuitem\nAlva:\n${d?.joinToString(separator  = "\n")}",e)
        }
    }

    /**
     * Invokes the item by calling various listeners or callbacks.
     *
     * @return true if the invocation was handled, false otherwise
     */
    fun invoke(): Boolean {
        if (mClickListener != null &&
            mClickListener!!.onMenuItemClick(this)
        ) {
            return true
        }

        if (mMenu.dispatchMenuItemSelected(mMenu, this)) {
            return true
        }

        if (this.callback != null) {
            callback!!.run()
            return true
        }

        if (mIntent != null) {
            try {
                mMenu.context.startActivity(mIntent)
                return true
            } catch (e: ActivityNotFoundException) {
                Log.e(TAG, "Can't find activity to handle intent; ignoring", e)
            }
        }

        return mActionProvider != null && mActionProvider!!.onPerformDefaultAction()
    }

    override fun isEnabled(): Boolean {
        return (mFlags and ENABLED) != 0
    }

    override fun setEnabled(enabled: Boolean): MenuItem {
        if (enabled) {
            mFlags = mFlags or ENABLED
        } else {
            mFlags = mFlags and ENABLED.inv()
        }

        mMenu.onItemsChanged(false)

        return this
    }

    override fun getGroupId(): Int {
        return mGroup
    }

    @CapturedViewProperty
    override fun getItemId(): Int {
        return mId
    }

    override fun getOrder(): Int {
        return mCategoryOrder
    }

    override fun getIntent(): Intent? {
        return mIntent
    }

    override fun setIntent(intent: Intent?): MenuItem {
        mIntent = intent
        return this
    }

    fun setCallback(callback: Runnable?): MenuItem {
        this.callback = callback
        return this
    }

    override fun getAlphabeticShortcut(): Char {
        return mShortcutAlphabeticChar
    }

    override fun getAlphabeticModifiers(): Int {
        return mShortcutAlphabeticModifiers
    }

    override fun setAlphabeticShortcut(alphaChar: Char): MenuItem {
        if (mShortcutAlphabeticChar == alphaChar) return this

        mShortcutAlphabeticChar = alphaChar.lowercaseChar()

        mMenu.onItemsChanged(false)

        return this
    }

    override fun setAlphabeticShortcut(alphaChar: Char, alphaModifiers: Int): MenuItem {
        if (mShortcutAlphabeticChar == alphaChar &&
            mShortcutAlphabeticModifiers == alphaModifiers
        ) {
            return this
        }

        mShortcutAlphabeticChar = alphaChar.lowercaseChar()
        mShortcutAlphabeticModifiers = KeyEvent.normalizeMetaState(alphaModifiers)

        mMenu.onItemsChanged(false)

        return this
    }

    override fun getNumericShortcut(): Char {
        return mShortcutNumericChar
    }

    override fun getNumericModifiers(): Int {
        return mShortcutNumericModifiers
    }

    override fun setNumericShortcut(numericChar: Char): MenuItem {
        if (mShortcutNumericChar == numericChar) return this

        mShortcutNumericChar = numericChar

        mMenu.onItemsChanged(false)

        return this
    }

    override fun setNumericShortcut(numericChar: Char, numericModifiers: Int): MenuItem {
        if (mShortcutNumericChar == numericChar && mShortcutNumericModifiers == numericModifiers) {
            return this
        }

        mShortcutNumericChar = numericChar
        mShortcutNumericModifiers = KeyEvent.normalizeMetaState(numericModifiers)

        mMenu.onItemsChanged(false)

        return this
    }

    override fun setShortcut(numericChar: Char, alphaChar: Char): MenuItem {
        mShortcutNumericChar = numericChar
        mShortcutAlphabeticChar = alphaChar.lowercaseChar()

        mMenu.onItemsChanged(false)

        return this
    }

    override fun setShortcut(
        numericChar: Char, alphaChar: Char, numericModifiers: Int,
        alphaModifiers: Int
    ): MenuItem {
        mShortcutNumericChar = numericChar
        mShortcutNumericModifiers = KeyEvent.normalizeMetaState(numericModifiers)
        mShortcutAlphabeticChar = alphaChar.lowercaseChar()
        mShortcutAlphabeticModifiers = KeyEvent.normalizeMetaState(alphaModifiers)

        mMenu.onItemsChanged(false)

        return this
    }

    val shortcut: Char
        /**
         * @return The active shortcut (based on QWERTY-mode of the menu).
         */
        get() = (if (mMenu.isQwertyMode()) mShortcutAlphabeticChar else mShortcutNumericChar)

    val shortcutLabel: String
        /**
         * @return The label to show for the shortcut. This includes the chording
         * key (for example 'Menu+a'). Also, any non-human readable
         * characters should be human readable (for example 'Menu+enter').
         */
        get() {
            val shortcut = this.shortcut
            if (shortcut.code == 0) {
                return ""
            }

            val res = mMenu.context.getResources()

            val sb = StringBuilder()
            if (ViewConfiguration.get(mMenu.context).hasPermanentMenuKey()) {
                // Only prepend "Menu+" if there is a hardware menu key.
                sb.append("menu+")
            }

            val modifiers =
                if (mMenu.isQwertyMode()) mShortcutAlphabeticModifiers else mShortcutNumericModifiers
            appendModifier(
                sb,
                modifiers,
                KeyEvent.META_META_ON,
                "[WINDOWS BUTTON]"
            )
            appendModifier(
                sb,
                modifiers,
                KeyEvent.META_CTRL_ON,
                "[CTRL]"
            )
            appendModifier(
                sb,
                modifiers,
                KeyEvent.META_ALT_ON,
                "[ALT]"
            )
            appendModifier(
                sb,
                modifiers,
                KeyEvent.META_SHIFT_ON,
                "[SHIFT]"
            )
            appendModifier(
                sb,
                modifiers,
                KeyEvent.META_SYM_ON,
                "[SYM]"
            )
            appendModifier(
                sb,
                modifiers,
                KeyEvent.META_FUNCTION_ON,
                "[FN]"
            )

            when (shortcut) {
                '\n' -> sb.append("[<- ENTER]")
                '\b' -> sb.append("[DEL]")
                ' ' -> sb.append("[SPACE]")
                else -> sb.append(shortcut)
            }

            return sb.toString()
        }

    /**
     * @return Whether this menu item should be showing shortcuts (depends on
     * whether the menu should show shortcuts and whether this item has
     * a shortcut defined)
     */
    fun shouldShowShortcut(): Boolean {
        // Show shortcuts if the menu is supposed to show shortcuts AND this item has a shortcut
        return mMenu.isShortcutsVisible && (this.shortcut.code != 0)
    }


    override fun hasSubMenu(): Boolean {
        var m: Method? = null
        var result: Boolean
        try {
            m = internalClass!!.getDeclaredMethod("hasSubMenu")
            m.setAccessible(true)
            result = (m.invoke(internalOne) as kotlin.Boolean?)!!
        } catch (e: Exception) {
            result = false
        }
        return result
    }

    override fun getSubMenu(): SubMenu? {
        return null
    }


    @CapturedViewProperty
    override fun getTitle(): CharSequence? {
        return mTitle
    }


    override fun setTitle(title: CharSequence?): MenuItem {
        mTitle = title

        mMenu.onItemsChanged(false)


        return this
    }

    override fun setTitle(title: Int): MenuItem {
        return setTitle(mMenu.context.getString(title))
    }

    override fun getTitleCondensed(): CharSequence? {
        return if (mTitleCondensed != null) mTitleCondensed else mTitle
    }

    override fun setTitleCondensed(title: CharSequence?): MenuItem {
        var title = title
        mTitleCondensed = title

        // Could use getTitle() in the loop below, but just cache what it would do here
        if (title == null) {
            title = mTitle
        }

        mMenu.onItemsChanged(false)

        return this
    }

    override fun getIcon(): Drawable? {
        if (mIconDrawable != null) {
            return applyIconTintIfNecessary(mIconDrawable)
        }

        if (mIconResId != NO_ICON) {
            val icon = mMenu.context.getDrawable(mIconResId)
            mIconResId = NO_ICON
            mIconDrawable = icon
            return applyIconTintIfNecessary(icon)
        }

        return null
    }

    override fun setIcon(icon: Drawable?): MenuItem {
        mIconResId = NO_ICON
        mIconDrawable = icon
        mNeedToApplyIconTint = true
        mMenu.onItemsChanged(false)

        return this
    }

    override fun setIcon(iconResId: Int): MenuItem {
        mIconDrawable = null
        mIconResId = iconResId
        mNeedToApplyIconTint = true

        // If we have a view, we need to push the Drawable to them
        mMenu.onItemsChanged(false)

        return this
    }

    override fun setIconTintList(iconTintList: ColorStateList?): MenuItem {
        mIconTintList = iconTintList
        mHasIconTint = true
        mNeedToApplyIconTint = true

        mMenu.onItemsChanged(false)

        return this
    }

    override fun getIconTintList(): ColorStateList? {
        return mIconTintList
    }

    override fun setIconTintMode(iconTintMode: PorterDuff.Mode?): MenuItem {
        mIconTintMode = iconTintMode
        mHasIconTintMode = true
        mNeedToApplyIconTint = true

        mMenu.onItemsChanged(false)

        return this
    }

    override fun getIconTintMode(): PorterDuff.Mode? {
        return mIconTintMode
    }

    private fun applyIconTintIfNecessary(icon: Drawable?): Drawable? {
        var icon = icon
        if (icon != null && mNeedToApplyIconTint && (mHasIconTint || mHasIconTintMode)) {
            icon = icon.mutate()

            if (mHasIconTint) {
                icon.setTintList(mIconTintList)
            }

            if (mHasIconTintMode) {
                icon.setTintMode(mIconTintMode)
            }

            mNeedToApplyIconTint = false
        }

        return icon
    }

    override fun isCheckable(): Boolean {
        return (mFlags and CHECKABLE) == CHECKABLE
    }

    override fun setCheckable(checkable: Boolean): MenuItem {
        val oldFlags = mFlags
        mFlags = (mFlags and CHECKABLE.inv()) or (if (checkable) CHECKABLE else 0)
        if (oldFlags != mFlags) {
            mMenu.onItemsChanged(false)
        }

        return this
    }


    var isExclusiveCheckable: Boolean
        get() = (mFlags and EXCLUSIVE) != 0
        set(exclusive) {
            mFlags =
                (mFlags and EXCLUSIVE.inv()) or (if (exclusive) EXCLUSIVE else 0)
        }

    override fun isChecked(): Boolean {
        return (mFlags and CHECKED) == CHECKED
    }

    override fun setChecked(checked: Boolean): MenuItem {
        if ((mFlags and EXCLUSIVE) != 0) {
            // Call the method on the Menu since it knows about the others in this
            // exclusive checkable group
            mMenu.setExclusiveItemChecked(this)
        } else {
            setCheckedInt(checked)
        }

        return this
    }

    fun setCheckedInt(checked: Boolean) {
        val oldFlags = mFlags
        mFlags = (mFlags and CHECKED.inv()) or (if (checked) CHECKED else 0)
        if (oldFlags != mFlags) {
            mMenu.onItemsChanged(false)
        }
    }

    override fun isVisible(): Boolean {
        if (mActionProvider != null && mActionProvider!!.overridesItemVisibility()) {
            return (mFlags and HIDDEN) == 0 && mActionProvider!!.isVisible()
        }
        return (mFlags and HIDDEN) == 0
    }

    /**
     * Changes the visibility of the item. This method DOES NOT notify the
     * parent menu of a change in this item, so this should only be called from
     * methods that will eventually trigger this change.  If unsure, use [.setVisible]
     * instead.
     *
     * @param shown Whether to show (true) or hide (false).
     * @return Whether the item's shown state was changed
     */
    fun setVisibleInt(shown: Boolean): Boolean {
        val oldFlags = mFlags
        mFlags = (mFlags and HIDDEN.inv()) or (if (shown) 0 else HIDDEN)
        return oldFlags != mFlags
    }

    override fun setVisible(shown: Boolean): MenuItem {
        // Try to set the shown state to the given state. If the shown state was changed
        // (i.e. the previous state isn't the same as given state), notify the parent menu that
        // the shown state has changed for this item
        if (setVisibleInt(shown)) mMenu.onItemVisibleChanged(this)

        return this
    }

    override fun setOnMenuItemClickListener(clickListener: MenuItem.OnMenuItemClickListener?): MenuItem {
        mClickListener = clickListener
        return this
    }

    override fun toString(): String {
        return (if (mTitle != null) mTitle.toString() else null)!!
    }


    fun setMenuInfo(menuInfo: ContextMenuInfo?) {
        mMenuInfo = menuInfo
    }

    override fun getMenuInfo(): ContextMenuInfo? {
        return mMenuInfo
    }

    fun actionFormatChanged() {
    }

    /**
     * @return Whether the menu should show icons for menu items.
     */
    fun shouldShowIcon(): Boolean {
        return mMenu.optionalIconsVisible
    }


    var isActionButton: Boolean
        get() = (mFlags and IS_ACTION) == IS_ACTION
        set(isActionButton) {
            if (isActionButton) {
                mFlags = mFlags or IS_ACTION
            } else {
                mFlags = mFlags and IS_ACTION.inv()
            }
        }


    fun requestsActionButton(): Boolean {
        return (mShowAsAction and MenuItem.SHOW_AS_ACTION_IF_ROOM) == MenuItem.SHOW_AS_ACTION_IF_ROOM
    }


    fun requiresActionButton(): Boolean {
        return (mShowAsAction and MenuItem.SHOW_AS_ACTION_ALWAYS) == MenuItem.SHOW_AS_ACTION_ALWAYS
    }


    fun requiresOverflow(): Boolean {
        return !requiresActionButton() && !requestsActionButton()
    }

    fun showsTextAsAction(): Boolean {
        return (mShowAsAction and MenuItem.SHOW_AS_ACTION_WITH_TEXT) == MenuItem.SHOW_AS_ACTION_WITH_TEXT
    }

    override fun setShowAsAction(actionEnum: Int) {
        when (actionEnum and SHOW_AS_ACTION_MASK) {
            MenuItem.SHOW_AS_ACTION_ALWAYS, MenuItem.SHOW_AS_ACTION_IF_ROOM, MenuItem.SHOW_AS_ACTION_NEVER -> {}
            else ->                 // Mutually exclusive options selected!
                throw IllegalArgumentException(
                    "SHOW_AS_ACTION_ALWAYS, SHOW_AS_ACTION_IF_ROOM,"
                            + " and SHOW_AS_ACTION_NEVER are mutually exclusive."
                )
        }
        mShowAsAction = actionEnum
        mMenu.onItemActionRequestChanged(this)
    }

    override fun setActionView(view: View?): MenuItem {
        mActionView = view
        mActionProvider = null
        if (view != null && view.getId() == View.NO_ID && mId > 0) {
            view.setId(mId)
        }
        mMenu.onItemActionRequestChanged(this)
        return this
    }

    override fun setActionView(resId: Int): MenuItem {
        val context = mMenu.context
        val inflater = LayoutInflater.from(context)
        setActionView(inflater.inflate(resId, LinearLayout(context), false))
        return this
    }

    override fun getActionView(): View? {
        if (mActionView != null) {
            return mActionView
        } else if (mActionProvider != null) {
            mActionView = mActionProvider!!.onCreateActionView(this)
            return mActionView
        } else {
            return null
        }
    }

    override fun getActionProvider(): ActionProvider? {
        return mActionProvider
    }

    override fun setActionProvider(actionProvider: ActionProvider?): MenuItem {
        var m: Method? = null
        try {
            m = internalClass!!.getDeclaredMethod("setActionProvider", ActionProvider::class.java)
            m.setAccessible(true)
            m.invoke(internalOne, actionProvider)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        return this
    }

    override fun setShowAsActionFlags(actionEnum: Int): MenuItem {
        setShowAsAction(actionEnum)
        return this
    }

    override fun expandActionView(): Boolean {
        if (!hasCollapsibleActionView()) {
            return false
        }

        if (mOnActionExpandListener == null ||
            mOnActionExpandListener!!.onMenuItemActionExpand(this)
        ) {
            return mMenu.expandItemActionView(this)
        }

        return false
    }

    override fun collapseActionView(): Boolean {
        if ((mShowAsAction and MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW) == 0) {
            return false
        }
        if (mActionView == null) {
            // We're already collapsed if we have no action view.
            return true
        }

        if (mOnActionExpandListener == null ||
            mOnActionExpandListener!!.onMenuItemActionCollapse(this)
        ) {
            return mMenu.collapseItemActionView(this)
        }

        return false
    }

    override fun setOnActionExpandListener(listener: MenuItem.OnActionExpandListener?): MenuItem {
        mOnActionExpandListener = listener
        return this
    }

    fun hasCollapsibleActionView(): Boolean {
        if ((mShowAsAction and MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW) != 0) {
            if (mActionView == null && mActionProvider != null) {
                mActionView = mActionProvider!!.onCreateActionView(this)
            }
            return mActionView != null
        }
        return false
    }


    fun setActionViewExpanded(isExpanded: Boolean) {
        mIsActionViewExpanded = isExpanded
        mMenu.onItemsChanged(false)
    }

    override fun isActionViewExpanded(): Boolean {
        return mIsActionViewExpanded
    }

    override fun setContentDescription(contentDescription: CharSequence?): MenuItem {
        mContentDescription = contentDescription

        mMenu.onItemsChanged(false)

        return this
    }

    override fun getContentDescription(): CharSequence? {
        return mContentDescription
    }

    override fun setTooltipText(tooltipText: CharSequence?): MenuItem {
        mTooltipText = tooltipText

        mMenu.onItemsChanged(false)

        return this
    }

    override fun getTooltipText(): CharSequence? {
        return mTooltipText
    }

    companion object {
        private const val TAG = "MenuItemImpl"

        private val SHOW_AS_ACTION_MASK = MenuItem.SHOW_AS_ACTION_NEVER or
                MenuItem.SHOW_AS_ACTION_IF_ROOM or
                MenuItem.SHOW_AS_ACTION_ALWAYS

        private const val CHECKABLE = 0x00000001
        private const val CHECKED = 0x00000002
        private const val EXCLUSIVE = 0x00000004
        private const val HIDDEN = 0x00000008
        private const val ENABLED = 0x00000010
        private const val IS_ACTION = 0x00000020

        /** Used for the icon resource ID if this item does not have an icon  */
        const val NO_ICON: Int = 0

        /**
         * Instantiates this menu item.
         *
         * @param mMenu
         * @param mGroup Item ordering grouping control. The item will be added after
         * all other items whose order is <= this number, and before any
         * that are larger than it. This can also be used to define
         * groups of items for batch state changes. Normally use 0.
         * @param mId Unique item ID. Use 0 if you do not need a unique ID.
         * @param mCategoryOrder The ordering for this item.
         * @param mTitle The text to display for the item.
         */
        val internalClass: Class<*>? get(){
            try {
                return Class.forName("com.android.internal.view.menu.MenuItemImpl")
            } catch (e: ClassNotFoundException) {
                throw RuntimeException(e)
            }
        }


        private fun appendModifier(sb: StringBuilder, mask: Int, modifier: Int, label: String?) {
            if ((mask and modifier) == modifier) {
                sb.append(label)
            }
        }
    }
}

