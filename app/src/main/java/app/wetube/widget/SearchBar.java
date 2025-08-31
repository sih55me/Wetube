package app.wetube.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import app.wetube.core.ApplyJava;
import app.wetube.databinding.SearchBarBinding;

public class SearchBar {
    private final SearchBarBinding bin;

    public onSubmit onSubmitListener = q -> {};

    public SearchBar(@NonNull Context context) {
        this(LayoutInflater.from(context));
    }

    public SearchBar(LayoutInflater inflater) {
        bin = SearchBarBinding.inflate(inflater);
        new ApplyJava<EditText>(bin.search){
            @Override
            public EditText apply(EditText it) {
                it.setBackground (null);
                it.setSingleLine(true);
                it.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
                it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18F);
                it.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        onSubmitListener.onSubmit(v.getText());
                        return true;
                    }
                });
                it.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        bin.clear.setVisibility((!s.toString().isEmpty()) ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                return it;
            }
        };
        bin.clear.setOnClickListener(v -> setText(null));

    }



    public void setBackButton(View.OnClickListener click){
        bin.back.setOnClickListener(click);
    }





    public void setText(CharSequence text) {
        bin.search.setText(text);
    }

    public CharSequence getText() {
        return bin.search.getText();
    }

    public View getView(){
        return bin.getRoot();
    }

    public interface onSubmit{
        public void onSubmit(CharSequence q);
    }

}
