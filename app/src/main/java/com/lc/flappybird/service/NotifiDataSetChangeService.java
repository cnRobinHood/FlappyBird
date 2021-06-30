package com.lc.flappybird.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;

import com.lc.flappybird.R;
import com.lc.flappybird.widget.RankingListAppWidgetProvider;

public class NotifiDataSetChangeService extends Service {
    private ContentObserver mRankingListObserver;
    public static final String PROVIDER_URI = "content://com.lc.flappybird.provider.RankListProvider/rankinglist";

    @Override
    public void onCreate() {
        super.onCreate();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel;
            String channelId = "flappybird";
            channel = new NotificationChannel(channelId, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(getApplicationContext(), channelId).build();
            startForeground(1, notification);
            Handler handler = new Handler();
            handler.postDelayed(() -> stopForeground(true), 1000);
        }

        mRankingListObserver = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) {
                Context context = NotifiDataSetChangeService.this;
                AppWidgetManager manager = AppWidgetManager.getInstance(context);
                ComponentName provider = new ComponentName(context, RankingListAppWidgetProvider.class);
                int[] ids = manager.getAppWidgetIds(provider);
                manager.notifyAppWidgetViewDataChanged(ids, R.id.lv_rankinglist);
            }

        };
        getContentResolver().registerContentObserver(Uri.parse(PROVIDER_URI),
                true, mRankingListObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mRankingListObserver);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
