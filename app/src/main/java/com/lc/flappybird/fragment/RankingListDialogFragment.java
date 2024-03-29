package com.lc.flappybird.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lc.flappybird.R;
import com.lc.flappybird.adapter.RankingListAdapter;
import com.lc.flappybird.domain.UserData;

import java.util.ArrayList;
import java.util.List;

//APP内显示的排行榜界面，用DialogFragment实现
public class RankingListDialogFragment extends DialogFragment {
    private static final String TAG = "RankingListDialogFragme";
    RecyclerView mRankingListRecyclerView;
    ImageButton mDismissImageButton;
    RankingListAdapter mRankingListAdapter;
    private List<UserData> mUserDataList;
    public static final String PROVIDER_URI = "content://com.lc.flappybird.provider.RankListProvider/rankinglist";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ranking_list, null);
        mUserDataList = new ArrayList<>();
        getRankingList();
        mRankingListRecyclerView = view.findViewById(R.id.rv_rank_list);
        mRankingListAdapter = new RankingListAdapter(mUserDataList, getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRankingListRecyclerView.setLayoutManager(linearLayoutManager);
        mRankingListRecyclerView.setAdapter(mRankingListAdapter);
        mDismissImageButton = view.findViewById(R.id.bt_dissmiss);
        mDismissImageButton.setOnClickListener(v -> dismiss());
        return view;
    }

    private void getRankingList() {
        Uri rankingListUri = Uri.parse(PROVIDER_URI);
        Cursor rankingListCursor = getActivity().getContentResolver().query(rankingListUri, new String[]{"username", "score", "time"}, null, null, null);
        if (rankingListCursor != null) {
            while (rankingListCursor.moveToNext()) {
                mUserDataList.add(new UserData(rankingListCursor.getString(rankingListCursor.getColumnIndex("username")),
                        Integer.valueOf(rankingListCursor.getInt(rankingListCursor.getColumnIndex("score"))).toString(),
                        Integer.valueOf(rankingListCursor.getInt(rankingListCursor.getColumnIndex("time"))).toString()));
            }
            rankingListCursor.close();

            mUserDataList.sort((o1, o2) -> {
                if (0 == Integer.parseInt(o2.getScore()) - Integer.parseInt(o1.getScore())) {
                    return Integer.parseInt(o1.getTime()) - Integer.parseInt(o2.getTime());
                } else {
                    return Integer.parseInt(o2.getScore()) - Integer.parseInt(o1.getScore());
                }
            });
        }
    }
}
