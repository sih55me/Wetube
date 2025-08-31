package app.wetube.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import app.wetube.SupaContainer;

public abstract class ApplyJava<T>{
    @Nullable
    public T result;

    public boolean isNull = true;

    public ApplyJava(@Nullable T target){
        result = apply(target);
        isNull = Objects.isNull(result);
    }
    @NonNull
    final public T getNonNullEdition(){
        if(result == null){
            throw new NullPointerException();
        }
        return result;
    }

    abstract public T apply(T t);


}
