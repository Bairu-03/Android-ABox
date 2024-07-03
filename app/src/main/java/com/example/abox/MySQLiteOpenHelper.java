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
        // 用户账号信息表
        String sql1 = "create table users (" +
                "id integer primary key autoincrement, " +
                "username text, " +
                "password text)";
        db.execSQL(sql1);

        // 温湿度表
        String sql2 = "create table THdata (" +
                "id integer primary key autoincrement, " +
                "time text, " +
                "temperature text, " +
                "humidity text)";
        db.execSQL(sql2);

        // 人体活动和光感数据表
        String sql3 = "create table HLdata (" +
                "id integer primary key autoincrement, " +
                "time text, " +
                "human text, " +
                "light text)";
        db.execSQL(sql3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        String sql1 = "drop table if exists users";
        db.execSQL(sql1);
        String sql2 = "drop table if exists THdata";
        db.execSQL(sql2);
        String sql3 = "drop table if exists HLdata";
        db.execSQL(sql3);
        onCreate(db);
    }
}
