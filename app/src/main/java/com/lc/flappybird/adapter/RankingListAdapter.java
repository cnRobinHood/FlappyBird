package com.lc.flappybird.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lc.flappybird.R;
import com.lc.flappybird.domain.UserData;

import java.util.List;

//app内显示的排行榜的recyclerview的adapter
public class RankingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<UserData> mUserDataList;
    private final Context mContext;

    public RankingListAdapter(List<UserData> mUserDataList, Context mContext) {
        this.mUserDataList = mUserDataList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.rankinglist_recycler_item, parent, false);
        return new RankListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //设置第一行显示title（时间，分数，用户名）
        if (0 == position) {
            ((RankListViewHolder) holder).mGameTimeTextView.setText(R.string.time);
            ((RankListViewHolder) holder).mScoreTextView.setText(R.string.score);
            ((RankListViewHolder) holder).mUserNameTextView.setText(R.string.user_name);
            return;
        }

        //由于第一行显示了title，数据列就从第二行开始显示
        position -= 1;
        ((RankListViewHolder) holder).mGameTimeTextView.setText(mUserDataList.get(position).getTime());
        ((RankListViewHolder) holder).mScoreTextView.setText(mUserDataList.get(position).getScore());
        ((RankListViewHolder) holder).mUserNameTextView.setText(mUserDataList.get(position).getUserName());
    }

    @Override
    public int getItemCount() {
        return mUserDataList.size() + 1;
    }

    private static class RankListViewHolder extends RecyclerView.ViewHolder {
        public TextView mUserNameTextView;
        public TextView mScoreTextView;
        public TextView mGameTimeTextView;

        public RankListViewHolder(@NonNull View itemView) {
            super(itemView);
            mUserNameTextView = itemView.findViewById(R.id.tv_user_name);
            mScoreTextView = itemView.findViewById(R.id.tv_score);
            mGameTimeTextView = itemView.findViewById(R.id.tv_game_time);
        }
    }
}
