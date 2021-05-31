package com.lc.flappybird.fragment;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.lc.flappybird.R;

import static android.content.Context.MODE_PRIVATE;

public class SettingsDialogFragment extends DialogFragment {
    private EditText mUserNameEditText;
    private SeekBar mSpeedBar;
    private SeekBar mVolumeBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting, null);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("name", MODE_PRIVATE);
        String username = sharedPreferences.getString("userName", "temp");
        int volume = sharedPreferences.getInt("volume", 5);
        int speed = sharedPreferences.getInt("speed", 3);
        mUserNameEditText = view.findViewById(R.id.et_username);
        Button button = view.findViewById(R.id.bt_clear_rankdata);
        mSpeedBar = view.findViewById(R.id.sb_speed);
        mVolumeBar = view.findViewById(R.id.sb_volume);
        mSpeedBar.setProgress(speed);
        mVolumeBar.setProgress(volume);
        button.setOnClickListener(v -> deleteRankingListDB());
        mUserNameEditText.setText(username);
        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("name", MODE_PRIVATE).edit();
        editor.putString("userName", mUserNameEditText.getText().toString());
        editor.putInt("volume", mVolumeBar.getProgress());
        editor.putInt("speed", mSpeedBar.getProgress());
        editor.apply();

    }

    private void deleteRankingListDB() {
        ContentResolver contentProvider = getActivity().getContentResolver();
        // 设置URI
        Uri uri = Uri.parse("content://com.lc.flappybird.provider.RankListProvider/rankinglist");
        contentProvider.delete(uri, null, null);

    }
}
