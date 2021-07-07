package com.lc.flappybird.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.lc.flappybird.R;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateFlappyBirdDialogFragment extends DialogFragment {
    private TextView mCurrentVersionTextView;
    private TextView mRemoteTextView;
    private Button mCheckUpdateButton;
    private Button mUpdateButton;
    private ProgressBar mCheckUpdatePB;
    private String latestVersion;
    private final int ERROR = -1;
    private final int SUCCESS = 0;
    Handler mHander = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    if (!latestVersion.equals(getCurrentVersion())) {
                        mUpdateButton.setBackgroundResource(R.drawable.border_line_white);
                        mUpdateButton.setClickable(true);
                        Toast.makeText(getActivity(), id2String(R.string.update_found), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), id2String(R.string.update_not_found), Toast.LENGTH_SHORT).show();
                    }
                    mRemoteTextView.setText(latestVersion);
                    mCheckUpdatePB.setVisibility(View.GONE);
                    break;
                case ERROR:
                    Toast.makeText(getActivity(), id2String(R.string.network_error), Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };

    //用于将字符串ID转换成字符串。
    private String id2String(int id) {
        return getResources().getString(id);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.version_info, null);
        initView(view);
        mCurrentVersionTextView.setText(getCurrentVersion());
        mCheckUpdateButton.setOnClickListener(v -> {
            mCheckUpdatePB.setVisibility(View.VISIBLE);
            getLatestVersionFromServer();
        });
        mUpdateButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse("https://www.baidu.com");
            intent.setData(content_url);
            startActivity(intent);
        });
        return view;
    }

    private void initView(View view) {
        mCurrentVersionTextView = view.findViewById(R.id.tv_current_version);
        mCheckUpdateButton = view.findViewById(R.id.bt_check_update);
        mCheckUpdatePB = view.findViewById(R.id.pb_check_update);
        mUpdateButton = view.findViewById(R.id.bt_update);
        mRemoteTextView = view.findViewById(R.id.tv_remote_version);
    }

    private String getCurrentVersion() {
        String versionName = "";
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionName
            versionName = getActivity().getPackageManager().
                    getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;


    }

    private void getLatestVersionFromServer() {
        String url = "https://www.baidu.com";
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHander.sendEmptyMessage(ERROR);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                latestVersion = response.body().string();
                mHander.sendEmptyMessage(SUCCESS);
            }
        });

    }
}
