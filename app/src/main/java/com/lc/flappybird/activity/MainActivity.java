package com.lc.flappybird.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.lc.flappybird.R;
import com.lc.flappybird.util.ActivityCollector;

import static android.view.animation.Animation.RELATIVE_TO_SELF;


public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Handler handler = new Handler();
    public static final int ANIMATION_TIME = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        ActivityCollector.addActivity(this);
        imageView = findViewById(R.id.img_doge);
        setAnimation();

        //设置定时器1.5s之后跳转到主界面
        handler.postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, StartingActivity.class);
            startActivity(intent);
        }, ANIMATION_TIME);
    }

    //启用一个1.5s的旋转动画
    private void setAnimation() {
        RotateAnimation animation = new RotateAnimation
                (0, 360, RELATIVE_TO_SELF, (float) 0.50, RELATIVE_TO_SELF, (float) 0.50);
        animation.setDuration(ANIMATION_TIME);
        imageView.setAnimation(animation);
        animation.start();
    }

    //重写onBackPressed，让用户在这个阶段无法返回
    @Override
    public void onBackPressed() {

    }


}
