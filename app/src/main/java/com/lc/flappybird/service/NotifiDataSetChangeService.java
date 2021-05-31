package com.lc.flappybird.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.lc.flappybird.R;
import com.lc.flappybird.widget.RankingListAppWidgetProvider;

public class NotifiDataSetChangeService extends Service {
    private ContentObserver mRankingListObserver;
    private static final String TAG = "NotifiDataSetChangeServ";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        mRankingListObserver = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) {
                Log.d(TAG, "onChange: ");
                Context context = NotifiDataSetChangeService.this;
                AppWidgetManager manager = AppWidgetManager.getInstance(context);
                ComponentName provider = new ComponentName(context, RankingListAppWidgetProvider.class);
                //获得，所有利用AppWidgetProvider作为广播接收器的那些widget的id
                int[] ids = manager.getAppWidgetIds(provider);
                //这样当有数据发生变化时，这种变化会反映到所有桌面上的widget的ListView列表中
                manager.notifyAppWidgetViewDataChanged(ids, R.id.lv_rankinglist);
            }

        };
        getContentResolver().registerContentObserver(Uri.parse("content://com.lc.flappybird.provider.RankListProvider/rankinglist"),
                true, mRankingListObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        getContentResolver().unregisterContentObserver(mRankingListObserver);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
