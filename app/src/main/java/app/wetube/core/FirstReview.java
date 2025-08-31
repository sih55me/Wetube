package app.wetube.core;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;

public final class FirstReview {
    @NotNull
    private final Context mCon;
    @NotNull
    private final SharedPreferences mPref;

    public FirstReview(@NotNull Context context) {
        mCon = context;
        mPref = mCon.getSharedPreferences("first_review", 0);
    }


    public boolean isSearchFunctionReviewed() {
        return mPref.getBoolean("search_function", false);
    }

    public void setSearchFunctionReviewed(boolean review) {
        mPref.edit().putBoolean("search_function", review).apply();
    }

    public boolean isStorageFunctionReviewed() {
        return mPref.getBoolean("storage_function", false);
    }

    public void setStorageFunctionReviewed(boolean review) {
        mPref.edit().putBoolean("storage_function", review).apply();
    }


    public boolean isCuSeOptionMenu() {
        return mPref.getBoolean("csop", false);
    }

    public void setCuSeOptionMenu(boolean review) {
        mPref.edit().putBoolean("csop", review).apply();
    }

}
