package app.wetube.window;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import app.wetube.MainActivity;
import app.wetube.core.ApplyJava;



public class Kertas  extends ContextThemeWrapper implements Window.Callback {
    public boolean showActionBar = true;
    @SuppressLint("PrivateApi")
    private final Class<?> wc;{
        try {
            wc = Class.forName("com.android.internal.policy.PhoneWindow");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Internal Window not found in ur phone", e);
        }

    }
    @NonNull
    private final LayoutInflater inflat;

    @Nullable
    private GetMenu meli;

    @NonNull
    private Window jendela;

    @NonNull
    final IsK token = new IsK();

    @Nullable
    MenuItem.OnMenuItemClickListener itemClickListener;


    public Kertas(Context context, int themeId,boolean showActionBar) {
        super(context, themeId);
        this.showActionBar = showActionBar;
        inflat = LayoutInflater.from(context);
        try {;
            Constructor<?> con =wc.getDeclaredConstructor(Context.class);
            con.setAccessible(true);
            jendela = new ApplyJava<Window>((Window) con.newInstance(this)){
                @Override
                public Window apply(Window window) {
                    window.setCallback(Kertas.this);
                    window.setWindowManager((WindowManager) context.getSystemService(Activity.WINDOW_SERVICE),token , getPackageName());
                    if(!showActionBar){
                        window.requestFeature(Window.FEATURE_NO_TITLE);
                    }
                    return window;
                }

            }.getNonNullEdition();

        } catch (Exception e) {
            jendela = new ApplyJava<Window>(new Jendela(this)){
                @Override
                public Window apply(Window window) {
                    window.setCallback(Kertas.this);
                    window.setWindowManager((WindowManager) context.getSystemService(Activity.WINDOW_SERVICE),token , getPackageName());
                    if(!showActionBar){
                        window.requestFeature(Window.FEATURE_NO_TITLE);
                    }
                    return window;
                }

            }.getNonNullEdition();
        }

    }

    public Kertas(Context context, int themeId){
        this(context, themeId,true);
    }

    public Kertas(Context context){
        this(context, 0,true);
    }

    public Kertas(Context context, boolean showActionBar){
        this(context, 0,showActionBar);
    }


    public void setMeli(@Nullable GetMenu meli) {
        this.meli = meli;
    }



    public void setItemClickListener(@Nullable MenuItem.OnMenuItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setTitle(CharSequence title) {
        jendela.setTitle(title);
    }


    public void showText(CharSequence text){
        final TextView t = new TextView(this);
        t.setText(text);
        jendela.setContentView(t);
    }



    public void setContentView(@NonNull Object any) {
        if(any instanceof Integer){
            try{
                jendela.setContentView((Integer) any);
            } catch (Exception e) {
                showText(any.toString());
            }
            return;
        }
        if(any instanceof CharSequence){
            showText((CharSequence)any);
            return;
        }
        if(any instanceof View){
            jendela.setContentView((View) any);
            return;
        }
        Log.e("Kertas", "invalid content");
    }


    public View getDecorView(){
        return jendela.getDecorView();
    }

    @Nullable
    public Toolbar getToolBar(){
        return app.wetube.core.ViewKt.getTul(getWindow());
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return jendela.superDispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return jendela.superDispatchTouchEvent(event);
    }

    @NonNull
    public Window getWindow(){
        return jendela;
    }


    @NonNull
    public WindowManager getWM(){
        return jendela.getWindowManager();
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return true;
    }

    @Nullable
    @Override
    public View onCreatePanelView(int featureId) {
        return null;
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        assert meli != null;
        meli.getMenu(menu);
        return true;
    }

    @Override
    public boolean onPreparePanel(int featureId, @Nullable View view, @NonNull Menu menu) {
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, @NonNull Menu menu) {
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
        assert itemClickListener != null;
        return itemClickListener.onMenuItemClick(item);
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {

    }

    @Override
    public void onContentChanged() {

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

    }

    @Override
    public void onAttachedToWindow() {

    }

    @Override
    public void onDetachedFromWindow() {

    }

    @Override
    public void onPanelClosed(int featureId, @NonNull Menu menu) {

    }



    @Override
    public boolean onSearchRequested() {
        return false;
    }

    @Override
    public boolean onSearchRequested(SearchEvent searchEvent) {
        return false;
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return null;
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        return null;
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {

    }

    @Override
    public void onActionModeFinished(ActionMode mode) {

    }

    @NonNull
    public LayoutInflater getLayoutInflater(){
        return inflat;
    }

    public void doConfig(Configuration configuration){
        jendela.onConfigurationChanged(configuration);
    }

    /**
     * Retrieve the current window attributes associated with this panel.
     *
     * @return WindowManager.LayoutParams Either the existing window
     * attributes object, or a freshly created one if there is none.
     */
    public WindowManager.LayoutParams getAttributes() {
        return jendela.getAttributes();
    }

    public CharSequence getTitle() {
        return getAttributes().getTitle();
    }

    /**
     * Returns a {@code Method} object that reflects the specified
     * declared method of the class or interface represented by this
     * {@code Class} object. The {@code name} parameter is a
     * {@code String} that specifies the simple name of the desired
     * method, and the {@code parameterTypes} parameter is an array of
     * {@code Class} objects that identify the method's formal parameter
     * types, in declared order.  If more than one method with the same
     * parameter types is declared in a class, and one of these methods has a
     * return type that is more specific than any of the others, that method is
     * returned; otherwise one of the methods is chosen arbitrarily.  If the
     * name is "&lt;init&gt;"or "&lt;clinit&gt;" a {@code NoSuchMethodException}
     * is raised.
     *
     * <p> If this {@code Class} object represents an array type, then this
     * method does not find the {@code clone()} method.
     *
     * @param name           the name of the method
     * @param parameterTypes the parameter array
     * @return the {@code Method} object for the method of this class
     * matching the specified name and parameters
     * @throws NoSuchMethodException if a matching method is not found.
     * @throws NullPointerException  if {@code name} is {@code null}
     * @throws SecurityException     If a security manager, <i>s</i>, is present and any of the
     *                               following conditions is met:
     *
     *                               <ul>
     *
     *                               <li> the caller's class loader is not the same as the
     *                               class loader of this class and invocation of
     *                               {@link SecurityManager#checkPermission
     *                               s.checkPermission} method with
     *                               {@code RuntimePermission("accessDeclaredMembers")}
     *                               denies access to the declared method
     *
     *                               <li> the caller's class loader is not the same as or an
     *                               ancestor of the class loader for the current class and
     *                               invocation of {@link SecurityManager#checkPackageAccess
     *                               s.checkPackageAccess()} denies access to the package
     *                               of this class
     *
     *                               </ul>
     * @jls 8.2 Class Members
     * @jls 8.4 Method Declarations
     * @since 1.1
     */

    public Method getDeclaredMethod(@NonNull String name, @Nullable Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        final Method method = wc.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method;
    }

    @Override
    public Object getSystemService(String name) {
        if(LAYOUT_INFLATER_SERVICE.equals(name)){
            return inflat;
        }
        if(WINDOW_SERVICE.equals(name)){
            return jendela.getWindowManager();
        }
        return super.getSystemService(name);
    }

    @Override
    public String getSystemServiceName(Class<?> serviceClass) {
        if(LayoutInflater.class.equals(serviceClass)){
            return LAYOUT_INFLATER_SERVICE;
        }
        if(WindowManager.class.equals(serviceClass)) {
            return WINDOW_SERVICE;
        }
        return super.getSystemServiceName(serviceClass);
    }

    public Bundle save(){
        return jendela.saveHierarchyState();
    }
    public void restore(Bundle data){
        jendela.restoreHierarchyState(data);
    }

    /**
     * To help identify by {@code Binder}
     */
    final static class IsK extends Binder{

    }


    public interface GetMenu{
        public void getMenu(Menu menu);
    }
}
