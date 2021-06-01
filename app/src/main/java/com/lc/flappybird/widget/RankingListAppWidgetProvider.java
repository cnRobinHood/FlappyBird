package com.lc.flappybird.widget;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.lc.flappybird.R;
import com.lc.flappybird.activity.StartingActivity;
import com.lc.flappybird.service.RankingListWidgetService;

import java.util.List;

public class RankingListAppWidgetProvider extends AppWidgetProvider {
    public static final String PACKET_PATH = "com.lc.flappybird";
    public static final String NOTIFIDATASETSERVICE_CLASSNAME = "com.lc.flappybird.service.NotifiDataSetChangeService";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, RankingListWidgetService.class);
            RemoteViews rv = new RemoteViews(context.getPackageName(),
                    R.layout.rankiinglist_widget);
            rv.setRemoteAdapter(appWidgetId, R.id.lv_rankinglist, intent);
            rv.setEmptyView(R.id.lv_rankinglist, R.id.tv_list_empty);
            Intent viewIntent = new Intent(context, StartingActivity.class);
            PendingIntent pi = PendingIntent.getActivity(context, 0, viewIntent, 0);
            rv.setOnClickPendingIntent(R.id.liner_widget, pi);
            rv.setPendingIntentTemplate(R.id.lv_rankinglist, pi);
            manager.updateAppWidget(appWidgetId, rv);
        }

    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Intent intent = new Intent();
        if (!isServiceExisted(context, NOTIFIDATASETSERVICE_CLASSNAME)) {
            intent.setComponent(new ComponentName(PACKET_PATH, NOTIFIDATASETSERVICE_CLASSNAME));
            try {
                context.startService(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(PACKET_PATH, NOTIFIDATASETSERVICE_CLASSNAME));
        context.stopService(intent);
    }

    public static boolean isServiceExisted(Context context, String className) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;
            if (serviceName.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }
}

