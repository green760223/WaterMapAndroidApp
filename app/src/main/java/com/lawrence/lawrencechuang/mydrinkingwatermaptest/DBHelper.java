package com.lawrence.lawrencechuang.mydrinkingwatermaptest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by LawrenceChuang on 16/3/27.
 */
public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // CREATE TABLE location ( _id INTEGER PRIMARY KEY,
        // longitude DOUBLE,
        // latitude DOUBLE)
        db.execSQL("CREATE TABLE location_table ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "longitude REAL, " +
                "latitude REAL, " +
                "zoom REAL )");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
