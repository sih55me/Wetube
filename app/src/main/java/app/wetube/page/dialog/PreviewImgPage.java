package app.wetube.page.dialog;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;

import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import app.wetube.R;
import app.wetube.core.SetheKt;
import app.wetube.core.Utils;
import app.wetube.core.ViewKt;
import app.wetube.widget.TouchImageView;
import app.wetube.window.Paper;
import kotlin.Unit;

public class PreviewImgPage extends Paper {
    @NotNull
    final TouchImageView img = new TouchImageView(getContext());

    public static int PREVIEW_IMAGE = 13699;
    public static String PREVIEW_CODE = String.valueOf(PREVIEW_IMAGE);

    public final boolean isDialog;



    public static class ToFragment extends DialogFragment{

        public Get valueToRun;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if(savedInstanceState != null){

                IBinder i = savedInstanceState.getBinder(PreviewImgPage.PREVIEW_CODE);
                if(i instanceof Get) {
                    valueToRun = (Get) i;
                }
            }
            try {
                PreviewImgPage p = new PreviewImgPage(getActivity(), valueToRun);
                return p;
            } catch (Exception e) {
                return super.onCreateDialog(savedInstanceState);
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putBinder(PreviewImgPage.PREVIEW_CODE, valueToRun);
            super.onSaveInstanceState(outState);
        }
    }

    @Nullable
    Get data = null;
    public PreviewImgPage(@NotNull Context context) {
        this(context, false);
    }

    public PreviewImgPage(@NotNull Context context,boolean asDialog) {
        super(context);
        isDialog = asDialog;
        img.setSwipeToDismissEnabled(true);
        img.setOnDismiss(  () ->{
            dismiss();
            return Unit.INSTANCE;
        });
        img.setDismissProgressListener(  (progress) ->{
            Window w = Objects.requireNonNull(getWindow());
            w.getAttributes().alpha = 1.0F - progress;
            w.getDecorView().setY(img.getCurrentTransY());
            w.getWindowManager().updateViewLayout(w.getDecorView(), w.getAttributes());
            return Unit.INSTANCE;
        });
        if(!asDialog){
            Objects.requireNonNull(getWindow()).setWindowAnimations(android.R.style.Animation_InputMethod);
        }
    }

    @Override
    public void setupActionBar(@NotNull ActionBar actionBar) {
        super.setupActionBar(actionBar);
        showBackButton();
        img.setActionBar(actionBar);
    }

    public PreviewImgPage(@NotNull Context context, @Nullable IBinder lic) throws IllegalAccessException {
        this(context,lic,false);
    }

    private static int con(Context con, boolean asDialog){
        if(asDialog){
            return 0;
        }
        return SetheKt.getThemeId(con);
    }



    public PreviewImgPage(@NotNull Context context, @Nullable IBinder lic, boolean asDialog) throws IllegalAccessException {
        this(context, asDialog);
        if(lic instanceof Get){
            data = (Get) lic;
        }else{
            throw new IllegalAccessException("Must Get, not other binder");
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        assert data != null;
        setPic(data.target);
        if(data.title.length() != 0){
            setTitle(data.title);
        }
        ViewKt.releaseParent(img);
        setContentView(img);
        super.onCreate(savedInstanceState);
    }




    final void setPic(@NotNull Object pic){
        if(pic instanceof Drawable){
            img.setImageDrawable((Drawable) pic);
            return;
        }
        if(pic instanceof Bitmap){
            img.setImageBitmap((Bitmap) pic);
            return;
        }
        if(pic instanceof Integer){
            if((Integer) pic != 0){
                try {
                    img.setImageResource((int) pic);
                } catch (Exception e) {
                    throw new RuntimeException("not res type", e);
                }
            }
            return;
        }
        throw new RuntimeException("Invalid image type");
    }

    @Override
    protected void onStart() {
        super.onStart();
        ViewParent p = img.getParent();
        if(!isDialog){
            if (p instanceof ViewGroup) {
                ((ViewGroup) p).getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                ((ViewGroup) p).getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
        }
    }

    public final static class Get extends Binder{
        @NotNull
        public Object target;
        @NotNull
        public CharSequence title = "";

        public Get(@NotNull Object target){
            this(target, "");
        }

        public Get(@NotNull Object target, @NotNull CharSequence title){
            this.target = target;
            this.title = title;
        }

    }



}
