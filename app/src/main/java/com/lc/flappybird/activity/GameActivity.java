package com.lc.flappybird.activity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.lc.flappybird.R;
import com.lc.flappybird.domain.Pipe;
import com.lc.flappybird.view.GameView;

import org.litepal.LitePal;

import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {
    private static final String TAG = "GameActivity";

    private GameView mGameView;
    private TextView mScoreTextView;
    private Chronometer mChronometer;
    private ImageButton mPauseButton;
    private boolean isFirstTouch;
    private boolean isGameOver;

    private boolean isSetNewTimerThreadEnabled;
    public boolean isResumeGame;
    private Thread setNewTimerThread;

    private MediaPlayer mMediaPlayer;

    private int gameMode;
    private boolean isPaused;
    private long lastLiveTime;
    private long pauseTime;
    private static final int TOUCH_MODE = 0x00;
    private Timer mTimer;

    TelephonyManager mTelephonyMgr;
    MyPhoneStateListener myPhoneStateListener;
    public static final String PROVIDER_URI = "content://com.lc.flappybird.provider.RankListProvider/rankinglist";

    //此handler用于
    // 1.处理定时器画面更新
    // 2.用户点击之后画面更新
    // 3.用户是否存活的判断和处理
    // 4.用户挂掉之后是否重新开始游戏的处理.

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case UPDATE: {
                    if (mGameView.isAlive()) {
                        isGameOver = false;
                        mGameView.update();
                    } else {
                        if (isGameOver) {
                            break;
                        } else {
                            isGameOver = true;
                        }
                        gameOverProcess();
                    }
                    break;
                }

                case RESET_SCORE: {
                    mScoreTextView.setText("0");
                    isFirstTouch = true;
                    break;
                }

                default: {
                    break;
                }
            }
        }
    };

    //用户gameover之后，停止定时器，显示用户分数，用时。并且把数据存入数据库。
    private void gameOverProcess() {
        mTimer.cancel();
        mTimer.purge();
        mChronometer.stop();
        long time = (SystemClock.elapsedRealtime() - mChronometer.getBase()) / 1000;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(GameActivity.this);
        alertDialog.setTitle(R.string.gameover);
        alertDialog.setMessage(id2String(R.string.score) + ": " + mGameView.getScore() +
                "\n" + id2String(R.string.time) + ": " + time + " " + id2String(R.string.second) + "\n" +
                id2String(R.string.restart_game));
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(R.string.yes, (dialog, which) -> GameActivity.this.restartGame());
        alertDialog.setNegativeButton(R.string.no, (dialog, which) -> {
            SharedPreferences.Editor editor = getSharedPreferences("name", MODE_PRIVATE).edit();
            editor.putBoolean("fresh", false);
            editor.apply();
            GameActivity.this.onBackPressed();
        });
        alertDialog.show();
        updateRankingListDB(getUserName(), mGameView.getScore(), (int) time);
    }

    //用于将字符串ID转换成字符串。
    private String id2String(int id) {
        return getResources().getString(id);
    }

    private void updateRankingListDB(String username, int score, int time) {
        // 设置URI
        Uri uri = Uri.parse(PROVIDER_URI);

        // 插入表中数据
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("score", score);
        values.put("time", time);

        // 获取ContentResolver
        ContentResolver resolver = getContentResolver();
        // 通过ContentResolver 根据URI 向ContentProvider中插入数据
        resolver.insert(uri, values);
    }

    // The what values of the messages
    private static final int UPDATE = 0x00;
    private static final int RESET_SCORE = 0x01;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_game);
        isPaused = false;
        isResumeGame = false;

        if (getIntent().getStringExtra("Mode").equals("Touch")) {
            gameMode = TOUCH_MODE;
        }

        //判断上一次游戏是否正常退出（关机，进程被杀死等）
        if ("true".equals(getIntent().getStringExtra("resume"))) {
            isResumeGame = true;
        }
        // Initialize the private views
        initViews();
        mTelephonyMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        myPhoneStateListener = new MyPhoneStateListener();
        mTelephonyMgr.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        // 初始化一个Mediaplayer用于背景音乐播放
        mMediaPlayer = MediaPlayer.create(this, R.raw.sound_score);
        mMediaPlayer.setLooping(false);

        isFirstTouch = true;

        //设置新的定时器
        isSetNewTimerThreadEnabled = true;
        setNewTimerThread = new Thread(() -> {
            if (isSetNewTimerThreadEnabled) {
                setNewTimer();
            }
        });
        if (gameMode == TOUCH_MODE) {
            // Jump listener
            mGameView.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mGameView.jump();
                    if (isFirstTouch) {
                        if (isResumeGame) {
                            mChronometer.setBase(SystemClock.elapsedRealtime() - lastLiveTime * 1000);
                        } else {
                            mChronometer.setBase(SystemClock.elapsedRealtime());
                        }
                        mChronometer.start();
                    }
                    isFirstTouch = false;
                }

                return true;
            });
        }
        setNewTimerThread.start();
    }


    private void initViews() {
        mGameView = findViewById(R.id.game_view);
        mScoreTextView = findViewById(R.id.text_view_score);
        mChronometer = findViewById(R.id.chronometer);
        mPauseButton = findViewById(R.id.ib_pause);

        //如果是恢复游戏，那么就用保存在sharedpreferences中的数据恢复现场
        if (isResumeGame) {
            mGameView.pipeList.clear();
            mGameView.pipeList.addAll(LitePal.findAll(Pipe.class));
            SharedPreferences sharedPreferences = getSharedPreferences("name", MODE_PRIVATE);
            mGameView.positionX = sharedPreferences.getFloat("positionX", 0.0f);
            mGameView.positionY = sharedPreferences.getFloat("positionY", 0.0f);
            mGameView.score = sharedPreferences.getInt("score", 0);
            lastLiveTime = sharedPreferences.getLong("time", 0);
            mScoreTextView.setText(String.valueOf(mGameView.getScore()));
        }

        //暂停按钮的处理逻辑
        mPauseButton.setOnClickListener(v -> {
            if (isPaused) {
                isPaused = false;
                mChronometer.setBase(SystemClock.elapsedRealtime() - pauseTime * 1000);
                mChronometer.start();
                mPauseButton.setImageResource(R.drawable.ic_pause);
                SharedPreferences.Editor editor = getSharedPreferences("name", MODE_PRIVATE).edit();
                editor.putBoolean("fresh", false);
                editor.apply();
            } else {
                onPauesProcess();
            }

        });
    }

    //设置定时器更新GameView
    private void setNewTimer() {
        if (!isSetNewTimerThreadEnabled) {
            return;
        }

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (!isPaused) {
                    //在定时器中用Handler发送消息，定时更新UI
                    GameActivity.this.handler.sendEmptyMessage(UPDATE);
                }
            }

        }, 0, 17);
    }

    //执行释放定时器，释放mediaplayer的操作
    @Override
    protected void onDestroy() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mTelephonyMgr.listen(myPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        isSetNewTimerThreadEnabled = false;

        super.onDestroy();
    }

    //暂停时会保存当前状态
    @Override
    protected void onPause() {
        isSetNewTimerThreadEnabled = false;
        if (!isPaused) {
            onPauesProcess();
        }
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    //更新界面显示的分数
    public void updateScore(int score) {
        mScoreTextView.setText(String.valueOf(score));
    }

    //播放背景音乐
    public void playScoreMusic() {
        if (gameMode == TOUCH_MODE) {
            mMediaPlayer.start();
            mMediaPlayer.setVolume(getVolume() / 10, getVolume() / 10);
        }
    }

    //获取用户设置的背景音乐音量
    private float getVolume() {
        SharedPreferences sharedPreferences = getSharedPreferences("name", MODE_PRIVATE);
        return (float) (sharedPreferences.getInt("volume", 5));
    }

    //重新启动游戏
    private void restartGame() {
        //重置当前游戏数据
        mGameView.resetData();

        // 重置游戏界面显示的分数等信息
        new Thread(() -> handler.sendEmptyMessage(RESET_SCORE)).start();

        if (gameMode == TOUCH_MODE) {
            isSetNewTimerThreadEnabled = true;
            setNewTimerThread = new Thread(() -> {

                if (isSetNewTimerThreadEnabled) {
                    setNewTimer();
                }
            });
            setNewTimerThread.start();
        }
    }

    @Override
    public void onBackPressed() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        isSetNewTimerThreadEnabled = false;
        super.onBackPressed();
    }

    private String getUserName() {
        SharedPreferences sharedPreferences = getSharedPreferences("name", MODE_PRIVATE);
        return sharedPreferences.getString("userName", "temp");
    }

    //对用户点击暂停，返回桌面，来电话等情况的保存现场的具体处理
    private void onPauesProcess() {
        isPaused = true;
        mChronometer.stop();
        mPauseButton.setImageResource(R.drawable.ic_start);
        LitePal.deleteAll(Pipe.class);
        LitePal.markAsDeleted(mGameView.pipeList);
        LitePal.saveAll(mGameView.pipeList);
        SharedPreferences.Editor editor = getSharedPreferences("name", MODE_PRIVATE).edit();
        editor.putFloat("positionX", mGameView.positionX);
        editor.putFloat("positionY", mGameView.positionY);
        editor.putFloat("iteratorInt", mGameView.iteratorInt);
        editor.putInt("score", mGameView.getScore());
        pauseTime = (SystemClock.elapsedRealtime() - mChronometer.getBase()) / 1000;
        editor.putLong("time", (SystemClock.elapsedRealtime() - mChronometer.getBase()) / 1000);
        if (mGameView.isAlive()) {
            editor.putBoolean("fresh", true);
        }
        editor.apply();
    }

    //对来电呼入做暂停处理，同时保存现场。以便在进程被GC之后可以恢复现场
    private class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            super.onCallStateChanged(state, phoneNumber);
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                onPauesProcess();
            }
        }
    }
}


