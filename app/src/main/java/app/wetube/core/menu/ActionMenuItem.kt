package app.wetube.core.menu


import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.ActionProvider
import android.view.ContextMenu.ContextMenuInfo
import android.view.KeyEvent
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import app.wetube.core.tryOn


class ActionMenuItem @JvmOverloads constructor(
    private val mContext: Context,
    private val mGroup: Int = 0,
    private val mId: Int = 0,
    private val mOrdering: Int = 0,
    private var mTitle: CharSequence?,
) : MenuItem {
    private var mTitleCondensed: CharSequence? = null
    private var mIntent: Intent? = null
    private var mShortcutNumericChar = 0.toChar()
    private var mShortcutNumericModifiers = KeyEvent.META_CTRL_ON
    private var mShortcutAlphabeticChar = 0.toChar()
    private var mShortcutAlphabeticModifiers = KeyEvent.META_CTRL_ON

    private var mIconDrawable: Drawable? = null
    private var mIconResId = NO_ICON
    private var mIconTintList: ColorStateList? = null
    private var mIconTintMode: PorterDuff.Mode? = null
    private var mHasIconTint = false
    private var mHasIconTintMode = false

    var mClickListener: MenuItem.OnMenuItemClickListener? = null

    private var mContentDescription: CharSequence? = null
    private var mTooltipText: CharSequence? = null

    private var mFlags = ENABLED

    override fun getAlphabeticShortcut(): Char {
        return mShortcutAlphabeticChar
    }

    override fun getAlphabeticModifiers(): Int {
        return mShortcutAlphabeticModifiers
    }

    override fun getGroupId(): Int {
        return mGroup
    }

    override fun getIcon(): Drawable? {
        return mIconDrawable
    }

    override fun getIntent(): Intent? {
        return mIntent
    }

    override fun getItemId(): Int {
        return mId
    }

    override fun getMenuInfo(): ContextMenuInfo? {
        return null
    }

    override fun getNumericShortcut(): Char {
        return mShortcutNumericChar
    }

    override fun getNumericModifiers(): Int {
        return mShortcutNumericModifiers
    }

    override fun getOrder(): Int {
        return mOrdering
    }

    override fun getSubMenu(): SubMenu? {
        return null
    }

    override fun getTitle(): CharSequence? {
        return mTitle
    }

    override fun getTitleCondensed(): CharSequence? {
        return if (mTitleCondensed != null) mTitleCondensed else mTitle
    }

    override fun hasSubMenu(): Boolean {
        return false
    }

    override fun isCheckable(): Boolean {
        return (mFlags and CHECKABLE) != 0
    }

    override fun isChecked(): Boolean {
        return (mFlags and CHECKED) != 0
    }

    override fun isEnabled(): Boolean {
        return (mFlags and ENABLED) != 0
    }

    override fun isVisible(): Boolean {
        return (mFlags and HIDDEN) == 0
    }

    override fun setAlphabeticShortcut(alphaChar: Char): MenuItem {
        mShortcutAlphabeticChar = alphaChar.lowercaseChar()
        return this
    }

    override fun setAlphabeticShortcut(alphachar: Char, alphaModifiers: Int): MenuItem {
        mShortcutAlphabeticChar = alphachar.lowercaseChar()
        mShortcutAlphabeticModifiers = KeyEvent.normalizeMetaState(alphaModifiers)
        return this
    }

    override fun setCheckable(checkable: Boolean): MenuItem {
        mFlags = (mFlags and CHECKABLE.inv()) or (if (checkable) CHECKABLE else 0)
        return this
    }

    fun setExclusiveCheckable(exclusive: Boolean): ActionMenuItem {
        mFlags = (mFlags and EXCLUSIVE.inv()) or (if (exclusive) EXCLUSIVE else 0)
        return this
    }

    override fun setChecked(checked: Boolean): MenuItem {
        mFlags = (mFlags and CHECKED.inv()) or (if (checked) CHECKED else 0)
        return this
    }

    override fun setEnabled(enabled: Boolean): MenuItem {
        mFlags = (mFlags and ENABLED.inv()) or (if (enabled) ENABLED else 0)
        return this
    }

    override fun setIcon(icon: Drawable?): MenuItem {
        mIconDrawable = icon
        mIconResId = NO_ICON

        applyIconTint()
        return this
    }

    override fun setIcon(iconRes: Int): MenuItem {
        mIconResId = iconRes
        tryOn{
            mIconDrawable = mContext.getDrawable(iconRes)
        }

        applyIconTint()
        return this
    }

    override fun setIconTintList(iconTintList: ColorStateList?): MenuItem {
        mIconTintList = iconTintList
        mHasIconTint = true

        applyIconTint()

        return this
    }

    override fun getIconTintList(): ColorStateList? {
        return mIconTintList
    }

    override fun setIconTintMode(iconTintMode: PorterDuff.Mode?): MenuItem {
        mIconTintMode = iconTintMode
        mHasIconTintMode = true

        applyIconTint()

        return this
    }

    override fun getIconTintMode(): PorterDuff.Mode? {
        return mIconTintMode
    }

    private fun applyIconTint() {
        if (mIconDrawable != null && (mHasIconTint || mHasIconTintMode)) {
            mIconDrawable = mIconDrawable!!.mutate()

            if (mHasIconTint) {
                mIconDrawable!!.setTintList(mIconTintList)
            }

            if (mHasIconTintMode) {
                mIconDrawable!!.setTintMode(mIconTintMode)
            }
        }
    }

    override fun setIntent(intent: Intent?): MenuItem {
        mIntent = intent
        return this
    }

    override fun setNumericShortcut(numericChar: Char): MenuItem {
        mShortcutNumericChar = numericChar
        return this
    }

    override fun setNumericShortcut(numericChar: Char, numericModifiers: Int): MenuItem {
        mShortcutNumericChar = numericChar
        mShortcutNumericModifiers = KeyEvent.normalizeMetaState(numericModifiers)
        return this
    }

    override fun setOnMenuItemClickListener(menuItemClickListener: MenuItem.OnMenuItemClickListener?): MenuItem {
        mClickListener = menuItemClickListener
        return this
    }

    override fun setShortcut(numericChar: Char, alphaChar: Char): MenuItem {
        mShortcutNumericChar = numericChar
        mShortcutAlphabeticChar = alphaChar.lowercaseChar()
        return this
    }

    override fun setShortcut(
        numericChar: Char, alphaChar: Char, numericModifiers: Int,
        alphaModifiers: Int,
    ): MenuItem {
        mShortcutNumericChar = numericChar
        mShortcutNumericModifiers = KeyEvent.normalizeMetaState(numericModifiers)
        mShortcutAlphabeticChar = alphaChar.lowercaseChar()
        mShortcutAlphabeticModifiers = KeyEvent.normalizeMetaState(alphaModifiers)
        return this
    }

    override fun setTitle(title: CharSequence?): MenuItem {
        mTitle = title
        return this
    }

    override fun setTitle(title: Int): MenuItem {
        mTitle = mContext.getResources().getString(title)
        return this
    }

    override fun setTitleCondensed(title: CharSequence?): MenuItem {
        mTitleCondensed = title
        return this
    }

    override fun setVisible(visible: Boolean): MenuItem {
        mFlags = (mFlags and HIDDEN) or (if (visible) 0 else HIDDEN)
        return this
    }

    fun invoke(): Boolean {
        if (mClickListener != null && mClickListener!!.onMenuItemClick(this)) {
            return true
        }

        if (mIntent != null) {
            mContext.startActivity(mIntent)
            return true
        }

        return false
    }

    override fun setShowAsAction(show: Int) {
        // Do nothing. ActionMenuItems always show as action buttons.
    }

    override fun setActionView(actionView: View?): MenuItem {
        return this
    }

    override fun getActionView(): View? {
        return null
    }

    override fun setActionView(resId: Int): MenuItem {
        return this
    }

    override fun getActionProvider(): ActionProvider? {
        return null
    }

    override fun setActionProvider(actionProvider: ActionProvider?): MenuItem {
        return this
    }

    override fun setShowAsActionFlags(actionEnum: Int): MenuItem {
        setShowAsAction(actionEnum)
        return this
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
        // No need to save the listener; ActionMenuItem does not support collapsing items.
        return this
    }

    override fun setContentDescription(contentDescription: CharSequence?): MenuItem {
        mContentDescription = contentDescription
        return this
    }

    override fun getContentDescription(): CharSequence? {
        return mContentDescription
    }

    override fun setTooltipText(tooltipText: CharSequence?): MenuItem {
        mTooltipText = tooltipText
        return this
    }

    override fun getTooltipText(): CharSequence? {
        return mTooltipText
    }

    companion object {
        private const val NO_ICON = 0

        private const val CHECKABLE = 0x00000001
        private const val CHECKED = 0x00000002
        private const val EXCLUSIVE = 0x00000004
        private const val HIDDEN = 0x00000008
        private const val ENABLED = 0x00000010
    }
}
