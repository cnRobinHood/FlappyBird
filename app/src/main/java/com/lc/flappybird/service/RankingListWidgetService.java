package com.lc.flappybird.service;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.lc.flappybird.R;
import com.lc.flappybird.domain.UserData;

import java.util.ArrayList;
import java.util.List;

public class RankingListWidgetService extends RemoteViewsService {
    private static final String TAG = "RankingListWidgetServic";
    public static final String PROVIDER_URI = "content://com.lc.flappybird.provider.RankListProvider/rankinglist";

    //实现RemoteViewService时要实现的抽象方法，获得一个RemoteViewsFactory实例
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RankingListRemoteViewFactory(this, intent);
    }

    /**
     * 内部类，实现RemoteViewsFatory接口
     * 这个RemoteViewsFactory就相当于BaseAdatper
     */
    private class RankingListRemoteViewFactory implements RemoteViewsFactory {
        private final Context mContext;
        private List<UserData> mUserDataList = new ArrayList<>();

        public RankingListRemoteViewFactory(Context context, Intent intent) {
            mContext = context;
        }

        @Override
        public void onCreate() {
            //just for test
            mUserDataList = new ArrayList<>();
            Log.d(TAG, "onCreate: ");
            getRankingList();
        }

        private void getRankingList() {
            Uri rankingListUri = Uri.parse(PROVIDER_URI);
            Cursor rankingListCursor = mContext.getContentResolver().query(rankingListUri, new String[]{"username", "score", "time"}, null, null, null);
            if (rankingListCursor != null) {
                while (rankingListCursor.moveToNext()) {
                    mUserDataList.add(new UserData(rankingListCursor.getString(rankingListCursor.getColumnIndex("username")),
                            Integer.valueOf(rankingListCursor.getInt(rankingListCursor.getColumnIndex("score"))).toString(),
                            Integer.valueOf(rankingListCursor.getInt(rankingListCursor.getColumnIndex("time"))).toString()));
                }
                rankingListCursor.close();
            }
            mUserDataList.sort((o1, o2) -> {
                if (0 == Integer.parseInt(o2.getScore()) - Integer.parseInt(o1.getScore())) {
                    return Integer.parseInt(o1.getTime()) - Integer.parseInt(o2.getTime());
                } else {
                    return Integer.parseInt(o2.getScore()) - Integer.parseInt(o1.getScore());
                }
            });
        }

        @Override
        public void onDataSetChanged() {
            mUserDataList.clear();
            getRankingList();
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return mUserDataList.size() + 1;
        }

        /**
         * 与BaseAdapter中的getView方法作用是一样的
         */
        @Override
        public RemoteViews getViewAt(int position) {
            if (position > mUserDataList.size()) {
                return null;
            }

            RemoteViews rv = new RemoteViews(getPackageName(), R.layout.rankinglist_recycler_item);
            if (0 == position) {
                rv.setTextViewText(R.id.tv_user_name, id2String(R.string.user_name));
                rv.setTextViewText(R.id.tv_score, id2String(R.string.score));
                rv.setTextViewText(R.id.tv_game_time, id2String(R.string.time));
            } else {
                UserData userData = mUserDataList.get(position - 1);
                rv.setTextViewText(R.id.tv_user_name, userData.getUserName());
                rv.setTextViewText(R.id.tv_score, userData.getScore());
                rv.setTextViewText(R.id.tv_game_time, userData.getTime());
            }
            Intent intent = new Intent();
            intent.putExtra("position", position);
            rv.setOnClickFillInIntent(R.id.liner_item, intent);
            return rv;
        }

        /**
         * 在getView方法执行获得View的过程中，该方法的返回值会作为等待加载画面一直显示
         * 当getView方法返回时，等待加载画面会自动消失
         */
        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        /**
         * 该方法与BaseAdapter中的getViewTypeCount的意思是一样的
         */
        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        private String id2String(int id) {
            return getResources().getString(id);
        }
    }

}
