package app.wetube;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.wetube.core.SetheKt;
import app.wetube.core.ViewKt;
import app.wetube.page.NextPageKt;

public class RoleplayFragmentActivity extends Activity {


    public final static String  FRAG_NAME = "frag_name";
    public final static String  FRAG_BUND = "frag_bund";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SetheKt.setupTheme(this);
        super.onCreate(savedInstanceState);
        final FrameLayout f = new FrameLayout(this);
        f.setId(R.id.hal);
        setContentView(f);

        final String cl = getIntent().getStringExtra(FRAG_NAME);
        if(cl == null){
            throw new RuntimeException("Well well well, you not enter FRAG_NAME");
        }
        final Bundle bu = getIntent().getBundleExtra(FRAG_BUND);
        final Fragment result = Fragment.instantiate(this, cl, bu);
        NextPageKt.nextPage(this, null, result, cl, "", "",R.id.hal);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }
}
