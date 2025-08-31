package app.wetube.manage.db;

import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import kotlin.jvm.functions.Function0;

public interface CCon<DB extends CCon, Item> {
    @NotNull
    public static String SENDER_DB = "sender_db";

    public void insert(@NotNull Item o);

    public void removeThis(@NotNull Item o);

    @NotNull
    public ArrayList<Item> rayOut();

    public void doing(@NotNull doing f);

    public static interface doing{
        void doing(CCon d);
    }



}
