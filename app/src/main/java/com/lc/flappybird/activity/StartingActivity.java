package com.lc.flappybird.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.lc.flappybird.R;
import com.lc.flappybird.fragment.RankingListDialogFragment;
import com.lc.flappybird.fragment.SettingsDialogFragment;

import java.util.List;
import java.util.Timer;

public class StartingActivity extends AppCompatActivity {
    private static final String TAG = "StartingActivity";
    public static final int SHINE = 1;
    private boolean isStartButtonVisible = true;
    private Button mStartGameButton;
    private ImageButton mSettingButton;
    private Button mResumeGameButton;
    Timer mShinetimer;
    private RankingListDialogFragment mRankingListDialogFragment;
    private SettingsDialogFragment mSettingsDialogFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting);
        mSettingButton = findViewById(R.id.bt_setting);
        mStartGameButton = findViewById(R.id.bt_start_game);
        mResumeGameButton = findViewById(R.id.bt_resume_game);
        SharedPreferences sharedPreferences = getSharedPreferences("name",  MODE_MULTI_PROCESS);
        if (sharedPreferences.getBoolean("fresh", false)) {
            Log.d(TAG, "onCreate: " + sharedPreferences.getBoolean("fresh", false));
            mResumeGameButton.setVisibility(View.VISIBLE);
        }
        mResumeGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartingActivity.this, GameActivity.class);
            intent.putExtra("resume", "true");
            intent.putExtra("Mode", "Touch");
            startActivity(intent);
        });
        // Hide the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (!isServiceExisted(this, "com.lc.flappybird.NotifiDataSetChangeService")) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.lc.flappybird", "com.lc.flappybird.NotifiDataSetChangeService"));
            Log.d(TAG, "onEnabled: ");
            this.startService(intent);
        }
    }

    public void goToGameActivity(View view) {
        Log.d(TAG, "goToGameActivity: ");
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("Mode", "Touch");
        startActivity(intent);
    }

    public void showSettingsDialog(View view) {
        mSettingsDialogFragment = new SettingsDialogFragment();
        mSettingsDialogFragment.show(getSupportFragmentManager(), "dialog");
    }

    public void showRankingListDialog(View view) {
        mRankingListDialogFragment = new RankingListDialogFragment();
        mRankingListDialogFragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (mShinetimer != null) {
            mShinetimer.cancel();
            mShinetimer.purge();
        }
    }

    @Override
    protected void onResume() {
        SharedPreferences sharedPreferences = getSharedPreferences("name",  MODE_MULTI_PROCESS);
        if (!sharedPreferences.getBoolean("fresh", false)) {
            mResumeGameButton.setVisibility(View.INVISIBLE);
        }
        super.onResume();

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
