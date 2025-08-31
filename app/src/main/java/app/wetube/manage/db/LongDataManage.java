package app.wetube.manage.db;

import android.database.sqlite.SQLiteOpenHelper;
import android.os.Binder;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class LongDataManage<DB extends CCon, Item> extends Binder implements CCon<DB, Item> {
    @Override
    public void insert(@NonNull Item o) {
        reused.insert(o);
    }

    @Override
    public void doing(@NonNull doing f) {
        reused.doing(f);
    }

    @Override
    public void removeThis(@NonNull Item o) {
        reused.removeThis(o);
    }

    @NonNull
    @Override
    public ArrayList<Item> rayOut() {
        return reused.rayOut();
    }

    public LongDataManage(@NotNull DB re){
        reused = re;
    }

    @NotNull
    public final DB reused;



    public SQLiteOpenHelper getRealManaged(){
        return (SQLiteOpenHelper)reused;
    }

}
