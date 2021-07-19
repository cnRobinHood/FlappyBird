package com.lc.flappybird.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.lc.flappybird.R;
import com.lc.flappybird.activity.GameActivity;
import com.lc.flappybird.domain.Pipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class GameView extends TextureView implements TextureView.SurfaceTextureListener {

    private float mMeasuredWidth;

    private float mMeasuredHeight;

    private Paint mPaint;

    private Bitmap mBitmap;

    // The colors
    private static final int colorPipe = Color.parseColor("#C75B39");
    private static final String TAG = "GameView";
    // The current score
    public int score = 0;

    public int getScore() {
        return score;
    }

    // For the bird
    public float positionX = 0.0f;
    public float positionY = 0.0f;
    private float velocityX = 0.0f;
    private float velocityY = 0.0f;
    private float accelerationX = 0.0f;
    private float accelerationY = 0.7f;

    // For the pipes
    public float iteratorInt = 0;
    private static final int interval = 200;
    private static final float gap = 450.0f;
    private static final float base = 100.0f;
    private float speed;
    private float pipeWidth = 100.0f;
    public List<Pipe> pipeList;
    private float pipeVelocity = 3.0f;

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet a) {
        super(context, a);
        init();
    }

    public GameView(Context context, AttributeSet a, int b) {
        super(context, a, b);
        init();
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        setOpaque(false);
        // For the bird
        mBitmap = getBitmapFromVectorDrawable(getContext(), R.drawable.ic_bird);
        mBitmap = Bitmap.createScaledBitmap(mBitmap, 100, 100, false);

        // For the pipes
        pipeList = new ArrayList<>();
        setKeepScreenOn(true);
    }

    private float getSpeed() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("name", MODE_PRIVATE);
        return (float) ((sharedPreferences.getInt("speed", 0) / 10.0 + 1));
    }

    /**
     * Updates the UI.
     */
    public void update() {
        mPaint.setStyle(Paint.Style.FILL);
        Canvas canvas = lockCanvas();
        if (null == canvas) {
            return;
        }
        // Clear the canvas
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // Draw the bird
        canvas.drawBitmap(mBitmap, positionX - 100.0f / 2.0f, positionY - 100.0f / 2.0f, null);
        // Draw the pipes
        mPaint.setColor(colorPipe);
        List<Integer> removeList = new ArrayList<>();
        int size = pipeList.size();
        for (int index = 0; index < size; index++) {
            Pipe pipe = pipeList.get(index);
            if (isPipeOut(pipe)) {
                removeList.add(index);
            } else {
                // Draw the upper part of the pipe
                canvas.drawRect(pipe.getPositionX() - pipeWidth / 2.0f,
                        0.0f,
                        pipe.getPositionX() + pipeWidth / 2.0f,
                        mMeasuredHeight - pipe.getHeight() - gap,
                        mPaint);

                // Draw the lower part of the pipe
                canvas.drawRect(pipe.getPositionX() - pipeWidth / 2.0f,
                        mMeasuredHeight - pipe.getHeight(),
                        pipe.getPositionX() + pipeWidth / 2.0f,
                        mMeasuredHeight,
                        mPaint);
            }
        }
        removeItemsFromPipeList(removeList);

        unlockCanvasAndPost(canvas);

        // Update the data for the bird
        positionX += velocityX;
        positionY += velocityY;
        velocityX += accelerationX;
        // Only accelerate velocityY when it is not too large
        if (velocityY <= 10.0F) {
            velocityY += accelerationY;
        }

        // Update the data for the pipes
        for (Pipe pipe : pipeList) {
            pipe.setPositionX(pipe.getPositionX() - pipeVelocity);
        }
        if (iteratorInt >= interval) {
            Log.d(TAG, "update: addpipes");
            addPipe();
            iteratorInt = 0;
        } else {
            iteratorInt += speed;
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Get the measured size of the view
        mMeasuredWidth = getMeasuredWidth();
        mMeasuredHeight = getMeasuredHeight();
        speed = getSpeed();
        Log.d(TAG, "onSizeChanged: " + speed);
        pipeVelocity *= speed;
        // Set the initial position
        if (!((GameActivity) getContext()).isResumeGame) {
            setPosition(mMeasuredWidth / 2.0f, mMeasuredHeight / 2.0f);
        }
        if (((GameActivity) getContext()).isResumeGame) {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("name", MODE_PRIVATE);
            score = sharedPreferences.getInt("score", 0);

            iteratorInt = sharedPreferences.getFloat("iteratorInt", 0);
        }


        // Add the initial pipe
        if (!((GameActivity) getContext()).isResumeGame) {
            addPipe();
        }
    }

    public void jump() {
        velocityY = -13.0f;
    }

    public void setPosition(float positionX, float positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Returns true if the bird is still alive, false otherwise.
     *
     * @return True if the bird is still alive, false otherwise.
     */
    public boolean isAlive() {
        // Check if the bird hits the pipes
        for (Pipe pipe : pipeList) {
            if ((pipe.getPositionX() >= mMeasuredWidth / 2.0f - pipeWidth / 2.0f - 100.0f / 2.0f) &&
                    (pipe.getPositionX() <= mMeasuredWidth / 2.0f + pipeWidth / 2.0f + 100.0f / 2.0f)) {
                if ((positionY <= mMeasuredHeight - pipe.getHeight() - gap + 50.0f / 2.0f) ||
                        (positionY >= mMeasuredHeight - pipe.getHeight() - 50.0f / 2.0f)) {
                    return false;
                } else {
                    if (pipe.getPositionX() - pipeVelocity <
                            mMeasuredWidth / 2.0f - pipeWidth / 2.0f - 100.0f / 2.0f) {
                        score++;
                        // Update the score in MainActivity
                        Context context = getContext();
                        if (context instanceof GameActivity) {
                            ((GameActivity) context).updateScore(score);
                            ((GameActivity) context).playScoreMusic();
                        }
                    }
                }
            }
        }

        // Check if the bird goes beyond the border
        if ((positionY < 0.0f + 100.0f / 2.0f) || (positionY > mMeasuredHeight - 100.0f / 2.0f)) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the pipe is out of the screen, false otherwise.
     *
     * @param pipe The pipe to be judged.
     * @return True if the pipe is out of the screen, false otherwise.
     */
    private boolean isPipeOut(Pipe pipe) {
        return (pipe.getPositionX() + pipeWidth / 2.0f) < 0.0f;
    }

    /**
     * Removes all the items at the indices specified by removeList.
     *
     * @param removeList The list of indices.
     */
    private void removeItemsFromPipeList(List<Integer> removeList) {
        List<Pipe> newList = new ArrayList<>();
        int size = pipeList.size();
        for (int index = 0; index < size; index++) {
            if (!removeList.remove(Integer.valueOf(index))) {
                newList.add(pipeList.get(index));
            }
        }

        pipeList = newList;
    }

    /**
     * Resets all the data of the over game.
     */
    public void resetData() {
        // For the bird
        positionX = 0.0f;
        positionY = 0.0f;
        velocityX = 0.0f;
        velocityY = 0.0f;
        accelerationX = 0.0f;
        accelerationY = 0.7f;

        // For the pipes
        iteratorInt = 0;
        pipeList = new ArrayList<>();

        score = 0;

        // Set the initial position
        setPosition(mMeasuredWidth / 2.0f, mMeasuredHeight / 2.0f);

        // Add the initial pipe
        if (!((GameActivity) getContext()).isResumeGame) {
            addPipe();
        }
    }

    /**
     * Adds a pipe into the list of pipes.
     */
    private void addPipe() {
        pipeList.add(new Pipe(mMeasuredWidth + pipeWidth / 2.0f,
                base + (mMeasuredHeight - 2 * base - gap) * new Random().nextFloat()));
    }

}
