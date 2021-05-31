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

    // UriMatcher类使用:在ContentProvider 中注册URI
    private static final UriMatcher mMatcher;

    static {
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // 初始化
        mMatcher.addURI(AUTOHORITY, "rankinglist", 0);
    }

    /**
     * 初始化ContentProvider
     */
    @Override
    public boolean onCreate() {
        mContext = getContext();
        // 在ContentProvider创建时对数据库进行初始化
        // 运行在主线程，故不能做耗时操作,此处仅作展示
        mDbHelper = new RankListDBHelper(getContext());
        db = mDbHelper.getWritableDatabase();
        return true;
    }

    /**
     * 添加数据
     */

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // 向该表添加数据
        db.insert(RankListDBHelper.USER_TABLE_NAME, null, values);
        // 当该URI的ContentProvider数据发生变化时，通知外界（即访问该ContentProvider数据的访问者）
        mContext.getContentResolver().notifyChange(uri, null);
        Log.d(TAG, "insert: ");
        return uri;
    }

    /**
     * 查询数据
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        return db.query(RankListDBHelper.USER_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder, null);
    }

    /**
     * 更新数据
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // 由于不展示,此处不作展开
        return 0;
    }

    /**
     * 删除数据
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete: ");
        db.delete(RankListDBHelper.USER_TABLE_NAME, null, null);
        mContext.getContentResolver().notifyChange(uri, null);
        return 0;
    }

    @Override
    public String getType(Uri uri) {

        // 由于不展示,此处不作展开
        return null;
    }

}