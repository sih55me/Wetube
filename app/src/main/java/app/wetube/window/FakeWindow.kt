package app.wetube.window

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.InputQueue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.view.Window

class FakeWindow(val content: ViewGroup): Window(content.context){

    override fun takeSurface(callback: SurfaceHolder.Callback2?) {

    }

    override fun takeInputQueue(callback: InputQueue.Callback?) {

    }

    override fun isFloating(): Boolean = false

    override fun setContentView(layoutResID: Int) {
    }



    override fun setContentView(view: View?) {
        content?.addView(view)
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        content?.addView(view, params)
    }

    override fun addContentView(view: View?, params: ViewGroup.LayoutParams?) {
        setContentView(view, params)
    }

    override fun getCurrentFocus(): View? {
        return content?.findFocus()
    }

    override fun getLayoutInflater(): LayoutInflater = LayoutInflater.from(context)

    override fun setTitle(title: CharSequence?) {

    }

    override fun setTitleColor(textColor: Int) {

    }

    override fun openPanel(featureId: Int, event: KeyEvent?) {

    }

    override fun closePanel(featureId: Int) {

    }

    override fun togglePanel(featureId: Int, event: KeyEvent?) {

    }

    override fun invalidatePanelMenu(featureId: Int) {

    }

    override fun performPanelShortcut(
        featureId: Int,
        keyCode: Int,
        event: KeyEvent?,
        flags: Int,
    ): Boolean {
        return true
    }

    override fun performPanelIdentifierAction(featureId: Int, id: Int, flags: Int): Boolean {
        return true
    }

    override fun closeAllPanels() {

    }

    override fun performContextMenuIdentifierAction(id: Int, flags: Int): Boolean {
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        content?.dispatchConfigurationChanged(newConfig)
    }

    override fun setBackgroundDrawable(drawable: Drawable?) {

    }

    override fun setFeatureDrawableResource(featureId: Int, resId: Int) {

    }

    override fun setFeatureDrawableUri(featureId: Int, uri: Uri?) {

    }

    override fun setFeatureDrawable(featureId: Int, drawable: Drawable?) {

    }

    override fun setFeatureDrawableAlpha(featureId: Int, alpha: Int) {

    }

    override fun setFeatureInt(featureId: Int, value: Int) {

    }

    override fun takeKeyEvents(get: Boolean) {

    }

    override fun superDispatchKeyEvent(event: KeyEvent?): Boolean {
        return true
    }

    override fun superDispatchKeyShortcutEvent(event: KeyEvent?): Boolean {
        return true
    }

    override fun superDispatchTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    override fun superDispatchTrackballEvent(event: MotionEvent?): Boolean {
        return true
    }

    override fun superDispatchGenericMotionEvent(event: MotionEvent?): Boolean {
        return true
    }

    override fun getDecorView(): View {
        return content
    }

    override fun peekDecorView(): View {
        return decorView
    }

    override fun saveHierarchyState(): Bundle {
        return Bundle()
    }

    override fun restoreHierarchyState(savedInstanceState: Bundle?) {

    }

    override fun onActive() {

    }

    override fun setChildDrawable(featureId: Int, drawable: Drawable?) {

    }

    override fun setChildInt(featureId: Int, value: Int) {

    }

    override fun isShortcutKey(keyCode: Int, event: KeyEvent?): Boolean = false

    override fun setVolumeControlStream(streamType: Int) {

    }

    override fun getVolumeControlStream(): Int = 0

    override fun getStatusBarColor(): Int = 0

    override fun setStatusBarColor(color: Int) {

    }

    override fun getNavigationBarColor(): Int {
        return 0
    }

    override fun setNavigationBarColor(color: Int) {

    }

    override fun setDecorCaptionShade(decorCaptionShade: Int) {

    }

    override fun setResizingCaptionDrawable(drawable: Drawable?) {

    }
}