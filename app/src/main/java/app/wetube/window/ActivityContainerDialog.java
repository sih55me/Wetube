package app.wetube.window;

import android.app.Activity;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import app.wetube.core.SetheKt;
import app.wetube.core.ViewKt;

public class ActivityContainerDialog extends Dialog {
    @NotNull
    final Manaj ma;

    @NotNull
    public static Manaj m(@NotNull LocalActivityManager lam, @NotNull Intent i, @Nullable String name){
        final Manaj f = new Manaj(lam, i);
        if(name != null) {
            f.name = name;
        }
        return f;
    }


    /**
     * if true, the {@code Activity} will finish when the dialog dismiss
     */
    public boolean finishOnDismiss = false;


    @Nullable
    public Activity getSelected() {
        return ma.getSelected();
    }

    public ActivityContainerDialog(@NotNull Context context, @NotNull Manaj man, boolean fullscreen) {
        super(context, fullscreen ? SetheKt.getThemeId(context) : SetheKt.getThemeResDialog(context));
        ma = man;
    }


    @Override
    public void show() {
        super.show();

        final Window con;
        if (ma.getSelected() == null) {
            con = ma.start();
        } else {
            con = ma.getSelected().getWindow();
        }
        final View tent = con.getDecorView();
        if(tent.getParent() instanceof ViewGroup){
            ((ViewGroup) tent.getParent()).removeView(tent);
        }
        con.getAttributes().width = Objects.requireNonNull(getWindow()).getAttributes().width;
        con.getAttributes().height = Objects.requireNonNull(getWindow()).getAttributes().height;
        setContentView(tent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }



    @Override
    protected void onStop() {
        if(finishOnDismiss){
            finish();
        }
        super.onStop();
    }


    /**
     * Finish the {@code Activity}
     */
    public void finish(){
        ma.mdi.destroyActivity(ma.name, true);
    }



    @NonNull
    @Override
    public Bundle onSaveInstanceState() {
        Bundle b = super.onSaveInstanceState();
        b.putBundle("sel", ma.mdi.saveInstanceState());
        return b;
    }



    public static class Manaj extends Binder{
        @NotNull
        public final LocalActivityManager mdi;

        @NotNull
        public String name = "";

        @NotNull
        public final Intent intent;

        public Manaj(@NotNull LocalActivityManager md, @NotNull Intent in){
            this(md, in , Objects.requireNonNull(in.getComponent()).getClassName());
        }

        public Manaj(@NotNull LocalActivityManager md, @NotNull Intent in, @NotNull String t){
            mdi = md;
            intent= in;
            name = t;
        }

        @Nullable
        public Activity getSelected() {
            return mdi.getActivity(name);
        }

        @NotNull
        public Window start(){
            return Objects.requireNonNull(mdi.startActivity(name, intent));
        }

        public void stop(){
            mdi.destroyActivity(name, true);
        }
    }
}
