package app.wetube.page.dialog;

import static app.wetube.core.SetTextColorKt.setTextColor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;

import app.wetube.SupaContainer;

public class RestartDialog extends AlertDialog {
    public RestartDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Restart the app?");
        setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(android.R.string.cancel), new Message());
        setButton(DialogInterface.BUTTON_POSITIVE, setTextColor("Restart", "#EF0D0D"), (dialog, which) -> SupaContainer.Companion.restart());
    }
}
