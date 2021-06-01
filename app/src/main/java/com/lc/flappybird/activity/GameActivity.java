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
import android.util.Log;
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

    public static final String PROVIDER_URI = "content://com.lc.flappybird.provider.RankListProvider/rankinglist";
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
        // Get the mode of the game from the StartingActivity
        if (getIntent().getStringExtra("Mode").equals("Touch")) {
            gameMode = TOUCH_MODE;
        }
        if ("true".equals(getIntent().getStringExtra("resume"))) {
            isResumeGame = true;
        }
        // Initialize the private views
        initViews();
        TelephonyManager mTelephonyMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyMgr.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
        // Initialize the MediaPlayer
        mMediaPlayer = MediaPlayer.create(this, R.raw.sound_score);
        mMediaPlayer.setLooping(false);

        isFirstTouch = true;
        // Set the Timer
        isSetNewTimerThreadEnabled = true;
        setNewTimerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (isSetNewTimerThreadEnabled) {
                    setNewTimer();
                }
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
                            mChronometer.start();
                        } else {
                            mChronometer.setBase(SystemClock.elapsedRealtime());
                            mChronometer.start();
                        }
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
        if (isResumeGame) {
            mGameView.pipeList.clear();
            mGameView.pipeList.addAll(LitePal.findAll(Pipe.class));
            SharedPreferences sharedPreferences = getSharedPreferences("name", MODE_PRIVATE);
            mGameView.positionX = sharedPreferences.getFloat("positionX", 0.0f);
            mGameView.positionY = sharedPreferences.getFloat("positionY", 0.0f);
            mGameView.score = sharedPreferences.getInt("score", 0);
            lastLiveTime = sharedPreferences.getLong("time", 0);
            Log.d(TAG, "initViews: " + lastLiveTime);
            mScoreTextView.setText(String.valueOf(mGameView.getScore()));
        }

        mPauseButton.setOnClickListener(v -> {
            if (isPaused) {
                isPaused = false;
                Log.d(TAG, "initViews:pauesTime " + pauseTime);
                mChronometer.setBase(SystemClock.elapsedRealtime() - pauseTime * 1000);
                mChronometer.start();
                mPauseButton.setImageResource(R.drawable.ic_pause);
                SharedPreferences.Editor editor = getSharedPreferences("name", MODE_PRIVATE).edit();
                Log.d(TAG, "initViews: ");
                editor.putBoolean("fresh", false);
                editor.apply();
                SharedPreferences sharedPreferences = getSharedPreferences("name", MODE_PRIVATE);
                Log.d(TAG, "initViews: " + sharedPreferences.getBoolean("fresh", false));
            } else {
                onPauesProcess();
            }

        });
    }

    /**
     * Sets the Timer to update the UI of the GameView.
     */
    private void setNewTimer() {
        if (!isSetNewTimerThreadEnabled) {
            return;
        }

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (!isPaused) {
                    // Send the message to the handler to update the UI of the GameView
                    GameActivity.this.handler.sendEmptyMessage(UPDATE);
                    // For garbage collection
                    System.gc();
                }
            }

        }, 0, 17);
    }

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

        isSetNewTimerThreadEnabled = false;

        super.onDestroy();
    }

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

    /**
     * Updates the displayed score.
     *
     * @param score The new score.
     */
    public void updateScore(int score) {
        mScoreTextView.setText(String.valueOf(score));
    }

    /**
     * Plays the music for score.
     */
    public void playScoreMusic() {
        if (gameMode == TOUCH_MODE) {
            mMediaPlayer.start();
            mMediaPlayer.setVolume(getVolume() / 10, getVolume() / 10);
        }
    }

    private float getVolume() {
        SharedPreferences sharedPreferences = getSharedPreferences("name", MODE_PRIVATE);
        return (float) (sharedPreferences.getInt("volume", 5));
    }

    /**
     * Restarts the game.
     */
    private void restartGame() {
        // Reset all the data of the over game in the GameView
        mGameView.resetData();

        // Refresh the TextView for displaying the score
        new Thread(() -> handler.sendEmptyMessage(RESET_SCORE)).start();

        if (gameMode == TOUCH_MODE) {
            isSetNewTimerThreadEnabled = true;
            setNewTimerThread = new Thread(new Runnable() {

                @Override
                public void run() {

                    if (isSetNewTimerThreadEnabled) {
                        setNewTimer();
                    }
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


