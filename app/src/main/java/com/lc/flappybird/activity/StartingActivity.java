package com.lc.flappybird.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.lc.flappybird.R;
import com.lc.flappybird.fragment.RankingListDialogFragment;
import com.lc.flappybird.fragment.SettingsDialogFragment;
import com.lc.flappybird.util.ActivityCollector;

import java.util.List;

public class StartingActivity extends AppCompatActivity {
    private static final String TAG = "StartingActivity";
    private Button mResumeGameButton;
    public static final String PACKET_PATH = "com.lc.flappybird";
    public static final String NOTIFIDATASETSERVICE_CLASSNAME = "com.lc.flappybird.service.NotifiDataSetChangeService";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting);
        ActivityCollector.addActivity(this);
        mResumeGameButton = findViewById(R.id.bt_resume_game);
        SharedPreferences sharedPreferences = getSharedPreferences("name", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("fresh", false)) {
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
        if (!isServiceExisted(this, NOTIFIDATASETSERVICE_CLASSNAME)) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(PACKET_PATH, NOTIFIDATASETSERVICE_CLASSNAME));
            Log.d(TAG, "onEnabled: ");
            this.startService(intent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForegroundService(intent);
            } else {
                this.startService(new Intent(intent));
            }
        }
    }

    public void goToGameActivity(View view) {
        Log.d(TAG, "goToGameActivity: ");
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("Mode", "Touch");
        startActivity(intent);
    }

    public void showSettingsDialog(View view) {
        SettingsDialogFragment settingsDialogFragment = new SettingsDialogFragment();
        settingsDialogFragment.show(getSupportFragmentManager(), "dialog");
    }

    public void showRankingListDialog(View view) {
        RankingListDialogFragment rankingListDialogFragment = new RankingListDialogFragment();
        rankingListDialogFragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    protected void onResume() {
        SharedPreferences sharedPreferences = getSharedPreferences("name", MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("fresh", false)) {
            mResumeGameButton.setVisibility(View.INVISIBLE);
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityCollector.removeAll();
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
