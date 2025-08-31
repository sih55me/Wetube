package app.wetube.page.dialog;

import static app.wetube.core.SetTextColorKt.setTextColor;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;

import org.jetbrains.annotations.NotNull;

import app.wetube.core.InfoDialog;

public class ResetDialog extends InfoDialog {

    private final ActivityManager man;
    public ResetDialog(@NotNull Context context) {
        super(context);
        man = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Reset the app data?");
        setMessage("If you reset the app data, your saved preference, saved videos, and history will be deleted.");
        setButtonsText("Reset", getContext().getString(android.R.string.cancel));
        setListener(new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == DialogInterface.BUTTON_POSITIVE){
                    man.clearApplicationUserData();
                }
            }
        });

    }
}
