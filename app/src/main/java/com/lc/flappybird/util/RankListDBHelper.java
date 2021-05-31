package com.lc.flappybird.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RankListDBHelper extends SQLiteOpenHelper {

    // 数据库名
    private static final String DATABASE_NAME = "rankinglist.db";

    // 表名
    public static final String USER_TABLE_NAME = "rankinglist";

    private static final int DATABASE_VERSION = 1;
    //数据库版本号

    public RankListDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + USER_TABLE_NAME + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + "username TEXT,score INTEGER ,time INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)   {

    }
}
