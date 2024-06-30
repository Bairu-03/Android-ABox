package com.example.abox;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.Nullable;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    private Context context;

    public MySQLiteOpenHelper(@Nullable Context context,
                              @Nullable String name,
                              @Nullable SQLiteDatabase.CursorFactory factory,
                              int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table users (" +
                "id integer primary key autoincrement, " +
                "username text, " +
                "password text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        String sql = "drop table if exists users";
        db.execSQL(sql);
        onCreate(db);
    }
}
