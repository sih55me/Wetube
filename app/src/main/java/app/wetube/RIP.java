package app.wetube;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import app.wetube.core.SetheKt;
import app.wetube.widget.EmptyViewContainer;
import kotlin.Pair;

public class RIP extends Activity {



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SetheKt.setupTheme(this);
        ScrollView s = new ScrollView(this);
        TextView t = (new TextView(this));
        setTitle("Wetube crash");
        setContentView(s);
        t.setSelected(true);
        t.setTextIsSelectable(true);
        s.addView(t);
        t.setId(R.id.text);
        t.setTextSize(25F);
        t.setText(getIntent().getStringExtra("msg"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Go home").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                finish();
                startActivity(new Intent(RIP.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
