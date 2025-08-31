package app.wetube.window.actionBar

import android.app.ActionBar
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.SpinnerAdapter
import app.wetube.core.ANDROID_INTERNAL

sealed class WithActionBar : ActionBar() {
    protected open val className:Class<*>? = null
    val instance: ActionBar? get() {
        return try {
            className?.newInstance()
        }catch (e: Exception){
            null
        } as ActionBar
    }

    val unNull get() = requireNotNull(instance){
        "Must implement"
    }

    override fun setTitle(title: CharSequence?) {
        instance?.setTitle(title)
    }

    override fun setTitle(resId: Int) {
        TODO("Not yet implemented")
    }

    override fun setCustomView(view: View?) {
        TODO("Not yet implemented")
    }

    override fun setCustomView(
        view: View?,
        layoutParams: LayoutParams?,
    ) {
        TODO("Not yet implemented")
    }

    override fun setCustomView(resId: Int) {
        TODO("Not yet implemented")
    }

    override fun setIcon(resId: Int) {
        TODO("Not yet implemented")
    }

    override fun setIcon(icon: Drawable?) {
        TODO("Not yet implemented")
    }

    override fun setLogo(resId: Int) {
        TODO("Not yet implemented")
    }

    override fun setLogo(logo: Drawable?) {
        TODO("Not yet implemented")
    }

    override fun setListNavigationCallbacks(
        adapter: SpinnerAdapter?,
        callback: OnNavigationListener?,
    ) {
        TODO("Not yet implemented")
    }

    override fun setSelectedNavigationItem(position: Int) {
        TODO("Not yet implemented")
    }

    override fun getSelectedNavigationIndex(): Int {
        TODO("Not yet implemented")
    }

    override fun getNavigationItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun setSubtitle(subtitle: CharSequence?) {
        TODO("Not yet implemented")
    }

    override fun setSubtitle(resId: Int) {
        TODO("Not yet implemented")
    }

    override fun setDisplayOptions(options: Int) {
        TODO("Not yet implemented")
    }

    override fun setDisplayOptions(options: Int, mask: Int) {
        TODO("Not yet implemented")
    }

    override fun setDisplayUseLogoEnabled(useLogo: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setDisplayShowHomeEnabled(showHome: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setDisplayHomeAsUpEnabled(showHomeAsUp: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setDisplayShowTitleEnabled(showTitle: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setDisplayShowCustomEnabled(showCustom: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setBackgroundDrawable(d: Drawable?) {
        TODO("Not yet implemented")
    }

    override fun setStackedBackgroundDrawable(d: Drawable?) {
        super.setStackedBackgroundDrawable(d)
    }

    override fun setSplitBackgroundDrawable(d: Drawable?) {
        super.setSplitBackgroundDrawable(d)
    }

    override fun getCustomView(): View? {
        TODO("Not yet implemented")
    }

    override fun getTitle(): CharSequence? {
        TODO("Not yet implemented")
    }

    override fun getSubtitle(): CharSequence? {
        TODO("Not yet implemented")
    }

    override fun getNavigationMode(): Int {
        TODO("Not yet implemented")
    }

    override fun setNavigationMode(mode: Int) {
        TODO("Not yet implemented")
    }

    override fun getDisplayOptions(): Int {
        TODO("Not yet implemented")
    }

    override fun newTab(): Tab? {
        TODO("Not yet implemented")
    }

    override fun addTab(tab: Tab?) {
        TODO("Not yet implemented")
    }

    override fun addTab(tab: Tab?, setSelected: Boolean) {
        TODO("Not yet implemented")
    }

    override fun addTab(tab: Tab?, position: Int) {
        TODO("Not yet implemented")
    }

    override fun addTab(
        tab: Tab?,
        position: Int,
        setSelected: Boolean,
    ) {
        TODO("Not yet implemented")
    }

    override fun removeTab(tab: Tab?) {
        TODO("Not yet implemented")
    }

    override fun removeTabAt(position: Int) {
        TODO("Not yet implemented")
    }

    override fun removeAllTabs() {
        TODO("Not yet implemented")
    }

    override fun selectTab(tab: Tab?) {
        TODO("Not yet implemented")
    }

    override fun getSelectedTab(): Tab? {
        TODO("Not yet implemented")
    }

    override fun getTabAt(index: Int): Tab? {
        TODO("Not yet implemented")
    }

    override fun getTabCount(): Int {
        TODO("Not yet implemented")
    }

    override fun getHeight(): Int {
        TODO("Not yet implemented")
    }

    override fun show() {
        TODO("Not yet implemented")
    }

    override fun hide() {
        TODO("Not yet implemented")
    }

    override fun isShowing(): Boolean {
        TODO("Not yet implemented")
    }

    override fun addOnMenuVisibilityListener(listener: OnMenuVisibilityListener?) {
        TODO("Not yet implemented")
    }

    override fun removeOnMenuVisibilityListener(listener: OnMenuVisibilityListener?) {
        TODO("Not yet implemented")
    }

    override fun setHomeButtonEnabled(enabled: Boolean) {
        super.setHomeButtonEnabled(enabled)
    }

    override fun getThemedContext(): Context? {
        return super.getThemedContext()
    }

    override fun setHomeAsUpIndicator(indicator: Drawable?) {
        super.setHomeAsUpIndicator(indicator)
    }

    override fun setHomeAsUpIndicator(resId: Int) {
        super.setHomeAsUpIndicator(resId)
    }

    override fun setHomeActionContentDescription(description: CharSequence?) {
        super.setHomeActionContentDescription(description)
    }

    override fun setHomeActionContentDescription(resId: Int) {
        super.setHomeActionContentDescription(resId)
    }

    override fun setHideOnContentScrollEnabled(hideOnContentScroll: Boolean) {
        super.setHideOnContentScrollEnabled(hideOnContentScroll)
    }

    override fun isHideOnContentScrollEnabled(): Boolean {
        return super.isHideOnContentScrollEnabled()
    }

    override fun getHideOffset(): Int {
        return super.getHideOffset()
    }

    override fun setHideOffset(offset: Int) {
        super.setHideOffset(offset)
    }

    override fun setElevation(elevation: Float) {
        super.setElevation(elevation)
    }

    override fun getElevation(): Float {
        return super.getElevation()
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return super.toString()
    }

    class WindowDecor(view: View): WithActionBar() {
        override val className: Class<*>?
            get() = Class.forName(ANDROID_INTERNAL+".app.WindowDecorActionBar")
    }
}