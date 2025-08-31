package app.wetube.core;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SpinnerAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public final class SampledActionBar  extends ActionBar {


    private View cus;

    private Drawable icon;


    private LayoutInflater l;

    private Bundle man = new Bundle();



    @Override
    public void setCustomView(View view) {
        setCustomView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setCustomView(View view, LayoutParams layoutParams) {
        cus = view;
    }

    @Override
    public void setCustomView(int resId) {
        View v = l.inflate(resId, null, false);
        setCustomView(v);
    }

    @Override
    public void setIcon(int resId) {
        putId("iconres", resId);
    }

    public void putId(String name, int id){
        man.putInt(name, id);
    }

    @Override
    public void setIcon(Drawable icon) {
        this.icon = icon;
    }



    @Override
    public void setLogo(int resId) {

    }

    @Override
    public void setLogo(Drawable logo) {

    }

    @Override
    public void setListNavigationCallbacks(SpinnerAdapter adapter, OnNavigationListener callback) {

    }

    @Override
    public void setSelectedNavigationItem(int position) {

    }

    @Override
    public int getSelectedNavigationIndex() {
        return 0;
    }

    @Override
    public int getNavigationItemCount() {
        return 0;
    }

    @Override
    public void setTitle(CharSequence title) {
        man.putCharSequence("title", title);
    }

    @Override
    public void setTitle(int resId) {
        putId("titleres", resId);
    }

    @Override
    public void setSubtitle(CharSequence subtitle) {
        man.putCharSequence("stitle", subtitle);
    }

    @Override
    public void setSubtitle(int resId) {
        putId("stitleres", resId);
    }

    @Override
    public void setDisplayOptions(int options) {
        man.putInt("disop", options);
    }

    @Override
    public void setDisplayOptions(int options, int mask) {
        setDisplayOptions(options);
    }

    @Override
    public void setDisplayUseLogoEnabled(boolean useLogo) {

    }

    @Override
    public void setDisplayShowHomeEnabled(boolean showHome) {
        man.putBoolean("showhome", showHome);
    }

    @Override
    public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        man.putBoolean("showhomeup", showHomeAsUp);
    }

    @Override
    public void setDisplayShowTitleEnabled(boolean showTitle) {
        man.putBoolean("showtitle", showTitle);
    }

    @Override
    public void setDisplayShowCustomEnabled(boolean showCustom) {
        man.putBoolean("showcustom", showCustom);
    }

    @Override
    public void setBackgroundDrawable(@Nullable Drawable d) {

    }

    @Override
    public View getCustomView() {
        return cus;
    }

    @Override
    public CharSequence getTitle() {
        return man.getCharSequence("title");
    }

    @Override
    public CharSequence getSubtitle() {
        return man.getCharSequence("stitle");
    }

    @Override
    public int getNavigationMode() {
        return ActionBar.NAVIGATION_MODE_STANDARD;
    }

    @Override
    public void setNavigationMode(int mode) {

    }

    @Override
    public int getDisplayOptions() {
        return man.getInt("disop");
    }

    @Override
    public Tab newTab() {
        return null;
    }

    @Override
    public void addTab(Tab tab) {

    }

    @Override
    public void addTab(Tab tab, boolean setSelected) {

    }

    @Override
    public void addTab(Tab tab, int position) {

    }

    @Override
    public void addTab(Tab tab, int position, boolean setSelected) {

    }

    @Override
    public void removeTab(Tab tab) {

    }

    @Override
    public void removeTabAt(int position) {

    }

    @Override
    public void removeAllTabs() {

    }

    @Override
    public void selectTab(Tab tab) {

    }

    @Override
    public Tab getSelectedTab() {
        return null;
    }

    @Override
    public Tab getTabAt(int index) {
        return null;
    }

    @Override
    public int getTabCount() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void show() {
        man.putBoolean("show", true);
    }

    @Override
    public void hide() {
        man.putBoolean("show", false);
    }

    @Override
    public boolean isShowing() {
        return man.getBoolean("show", true);
    }

    @Override
    public void addOnMenuVisibilityListener(OnMenuVisibilityListener listener) {

    }

    @Override
    public void removeOnMenuVisibilityListener(OnMenuVisibilityListener listener) {

    }

    public void copyTo(ActionBar target){
        target.setDisplayShowTitleEnabled(man.getBoolean("showtitle"));
        target.setDisplayShowCustomEnabled(man.getBoolean("showcustom"));
        target.setTitle(getTitle());
        target.setSubtitle(getSubtitle());
        target.setDisplayOptions(getDisplayOptions());
        target.setCustomView(getCustomView());
        target.setIcon(icon);
        TryoutKt.tryOn(() -> {
            if (man.getBoolean("show")) {
                target.show();
            } else {
                target.hide();
            }
            return Unit.INSTANCE;
        });

        if (man.getBoolean("showhome")) {
            target.setDisplayShowHomeEnabled(true);
        }
        if (man.getBoolean("showhomeup")) {
            target.setDisplayHomeAsUpEnabled(true);
        }

    }

}
