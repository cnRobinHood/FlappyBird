package com.lc.flappybird.fragment;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.lc.flappybird.R;

import static android.content.Context.MODE_PRIVATE;

//设置界面使用的DialogFragment
public class SettingsDialogFragment extends DialogFragment {
    private EditText mUserNameEditText;
    private SeekBar mSpeedBar;
    private SeekBar mVolumeBar;
    private ProgressBar mprogressBar;
    private static final int STOP_PROGRESS_BAR = 1;
    public static final String PROVIDER_URI = "content://com.lc.flappybird.provider.RankListProvider/rankinglist";
    Handler handler = new Handler(){
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case STOP_PROGRESS_BAR:
                    mprogressBar.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), id2String(R.string.clear_data_toast), Toast.LENGTH_SHORT).show();
                default:

            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.setting, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("name", MODE_PRIVATE);
        String username = sharedPreferences.getString("userName", "temp");
        int volume = sharedPreferences.getInt("volume", 5);
        int speed = sharedPreferences.getInt("speed", 3);
        mUserNameEditText = view.findViewById(R.id.et_username);
        Button button = view.findViewById(R.id.bt_clear_rankdata);
        mprogressBar = view.findViewById(R.id.pb_clear_data);
        mSpeedBar = view.findViewById(R.id.sb_speed);
        mVolumeBar = view.findViewById(R.id.sb_volume);
        mSpeedBar.setProgress(speed);
        mVolumeBar.setProgress(volume);
        button.setOnClickListener(v -> deleteRankingListDB());
        mUserNameEditText.setText(username);
    }

    //重写onDismiss方法，增加保存用户设置的内容
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("name", MODE_PRIVATE).edit();
        if ("".equals(mUserNameEditText.getText().toString())) {
            Toast.makeText(getActivity(), id2String(R.string.empty_user_toast), Toast.LENGTH_SHORT).show();
            editor.putString("userName", "temp");
        } else {
            editor.putString("userName", mUserNameEditText.getText().toString());
        }
        editor.putInt("volume", mVolumeBar.getProgress());
        editor.putInt("speed", mSpeedBar.getProgress());
        editor.apply();

    }

    //用于将字符串ID转换成字符串。
    private String id2String(int id) {
        return getResources().getString(id);
    }

    private void deleteRankingListDB() {
        ContentResolver contentProvider = getActivity().getContentResolver();
        mprogressBar.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> handler.sendEmptyMessage(STOP_PROGRESS_BAR), 1000);
        // 设置URI
        Uri uri = Uri.parse(PROVIDER_URI);
        contentProvider.delete(uri, null, null);
    }
}
