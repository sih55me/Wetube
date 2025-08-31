package app.wetube.page.dialog;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageView;

import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.NotNull;

import app.wetube.window.Paper;

public class PreviewImgPage extends Paper {
    @NotNull
    final ImageView img = new ImageView(getContext());

    public static int PREVIEW_IMAGE = 13699;
    public static String PREVIEW_CODE = String.valueOf(PREVIEW_IMAGE);

    @Nullable
    Get data = null;
    public PreviewImgPage(@NotNull Context context) {
        super(context);
        setContentView(img);
    }



    public PreviewImgPage(@NotNull Context context, @Nullable IBinder lic) throws IllegalAccessException {
        this(context);
        if(lic instanceof Get){
            data = (Get) lic;
        }else{
            throw new IllegalAccessException("Must Get, not other binder");
        }
    }

    @Override
    public void setupActionBar(@NotNull ActionBar actionBar) {
        showBackButton();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        assert data != null;
        setPic(data.target);
        if(data.title.length() != 0){
            setTitle(data.title);
        }
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
