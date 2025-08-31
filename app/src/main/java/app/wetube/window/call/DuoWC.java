package app.wetube.window.call;

import android.os.Build;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;

import java.util.List;

public class DuoWC extends WindowCallbackWrapper{

    private final Window.Callback sec;
    public DuoWC(@NonNull Window.Callback f, @NonNull Window.Callback s) {
        super(f);
        sec = s;
    }

    public Window.Callback getSec() {
        return sec;
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return sec.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }
    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return sec.dispatchKeyShortcutEvent(event) || super.dispatchKeyShortcutEvent(event);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return sec.dispatchTouchEvent(event) || super.dispatchTouchEvent(event);
    }
    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        return sec.dispatchTrackballEvent(event) | super.dispatchTrackballEvent(event);
    }
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return sec.dispatchGenericMotionEvent(event);
    }
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return sec.dispatchPopulateAccessibilityEvent(event);
    }
    @Override
    public View onCreatePanelView(int featureId) {
        return sec.onCreatePanelView(featureId);
    }
    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return sec.onCreatePanelMenu(featureId, menu)||super.onCreatePanelMenu(featureId,menu);
    }
    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        return sec.onPreparePanel(featureId, view, menu) || super.onPreparePanel(featureId, view, menu);
    }
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return sec.onMenuOpened(featureId, menu) || super.onMenuOpened(featureId, menu);
    }
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return sec.onMenuItemSelected(featureId, item)|| super.onMenuItemSelected(featureId, item);
    }
    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
        super.onWindowAttributesChanged(attrs);
        sec.onWindowAttributesChanged(attrs);
    }
    @Override
    public void onContentChanged() {
        sec.onContentChanged();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        sec.onWindowFocusChanged(hasFocus);
    }
    @Override
    public void onAttachedToWindow() {
        sec.onAttachedToWindow();
    }
    @Override
    public void onDetachedFromWindow() {
        sec.onDetachedFromWindow();
    }
    @Override
    public void onPanelClosed(int featureId, @NonNull Menu menu) {
        sec.onPanelClosed(featureId, menu);
    }
    @Override
    public boolean onSearchRequested(SearchEvent searchEvent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return sec.onSearchRequested(searchEvent);
        }else return false;
    }
    @Override
    public boolean onSearchRequested() {
        return sec.onSearchRequested();
    }
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return sec.onWindowStartingActionMode(callback);
    }
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        ActionMode oldi = onWindowStartingActionMode(callback);
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return sec.onWindowStartingActionMode(callback, type);
            }else{
                return oldi;
            }
        }catch (Throwable e){
            return oldi;
        }
    }
    @Override
    public void onActionModeStarted(ActionMode mode) {
        sec.onActionModeStarted(mode);
        super.onActionModeStarted(mode);
    }
    @Override
    public void onActionModeFinished(ActionMode mode) {
        sec.onActionModeFinished(mode);
        super.onActionModeFinished(mode);
    }
    @Override
    public void onProvideKeyboardShortcuts(
            List<KeyboardShortcutGroup> data, Menu menu, int deviceId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sec.onProvideKeyboardShortcuts(data, menu, deviceId);
        }
    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sec.onPointerCaptureChanged(hasCapture);
        }
    }
}
