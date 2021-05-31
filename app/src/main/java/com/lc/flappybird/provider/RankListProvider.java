package com.lc.flappybird.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.lc.flappybird.util.RankListDBHelper;


public class RankListProvider extends ContentProvider {
    private static final String TAG = "RankListProvider";
    private Context mContext;
    RankListDBHelper mDbHelper = null;
    SQLiteDatabase db = null;
    public static final String AUTOHORITY = "com.lc.flappybird.provider.RankListProvider";
    public static final String TABLE_NAME = "rankinglist";

    private static final UriMatcher mMatcher;

    static {
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mMatcher.addURI(AUTOHORITY, TABLE_NAME, 0);
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        mDbHelper = new RankListDBHelper(getContext());
        db = mDbHelper.getWritableDatabase();
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        db.insert(RankListDBHelper.USER_TABLE_NAME, null, values);
        mContext.getContentResolver().notifyChange(uri, null);
        Log.d(TAG, "insert: ");
        return uri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        return db.query(RankListDBHelper.USER_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder, null);
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete: ");
        db.delete(RankListDBHelper.USER_TABLE_NAME, null, null);
        mContext.getContentResolver().notifyChange(uri, null);
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

}