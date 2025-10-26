package app.wetube.page.dialog;

import static app.wetube.core.SetTextColorKt.setTextColor;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;

import org.jetbrains.annotations.NotNull;

import app.wetube.R;
import app.wetube.SupaContainer;
import app.wetube.core.InfoDialog;

public class ResetDialog extends AlertDialog {

    private final ActivityManager man;


    private final DialogInterface.OnClickListener click =  new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which == DialogInterface.BUTTON_POSITIVE){
                man.clearApplicationUserData();
            }
        }
    };
    public ResetDialog(@NotNull Context context) {
        super(context);
        man = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Reset the app data?");
        setMessage("If you reset the app data, your saved preference, saved videos, and history will be deleted.");
        String hexColor = String.format("#%06X", (0xFFFFFF & t()));
        setButton(setTextColor("Reset", hexColor), click);
        setButton2(getContext().getString(android.R.string.cancel), click);

        super.onCreate(savedInstanceState);
    }




    int t(){
        TypedArray array;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            array = getContext().obtainStyledAttributes(null, new int[]{android.R.attr.colorError});
        }else {
            array = getContext().obtainStyledAttributes(null, new int[]{android.R.attr.colorActivatedHighlight});
        }
        try {
            return array.getColor(0, 0);
        } finally {
            array.recycle();
        }
    }

}
