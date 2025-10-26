package app.wetube.page.dialog;

import static app.wetube.core.SetTextColorKt.setTextColor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;

import app.wetube.SupaContainer;

public class RestartDialog extends AlertDialog {
    public RestartDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Restart the app?");
        String hexColor = String.format("#%06X", (0xFFFFFF & t()));
        setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(android.R.string.cancel), (dialog, which) -> {});
        setButton(DialogInterface.BUTTON_POSITIVE, setTextColor("Restart", hexColor), (dialog, which) -> SupaContainer.Companion.restart());
        super.onCreate(savedInstanceState);
    }



    int t(){
        TypedArray array = getContext().obtainStyledAttributes(null, new int[]{android.R.attr.colorActivatedHighlight});
        try {
            return array.getColor(0, 0);
        } finally {
            array.recycle();
        }
    }
}
