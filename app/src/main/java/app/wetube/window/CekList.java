package app.wetube.window;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewDebug;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public class CekList extends Paper{
    @NotNull
    public static final String KEY = "ceklistr";
    @NotNull
    public final ListView listView;
    public CekList(@NotNull Context context) {
        super(context);
        listView = new ListView(context);
    }

    public ListAdapter getAdapter() {
        return listView.getAdapter();
    }

    public void setAdapter(ListAdapter adapter) {
        listView.setAdapter(adapter);
    }

    public void setOnItemClickListener(@Nullable AdapterView.OnItemClickListener listener) {
        listView.setOnItemClickListener(listener);
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        listView.setOnItemLongClickListener(listener);
    }

    @ViewDebug.CapturedViewProperty
    public int getCount() {
        return listView.getCount();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(listView);
    }
}
