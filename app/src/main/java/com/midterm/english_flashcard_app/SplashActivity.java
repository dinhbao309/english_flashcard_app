package com.midterm.english_flashcard_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final long SESSION_TIMEOUT = 10 * 60 * 1000; // 10 phút

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Lottie tự chạy animation, không cần code thêm
        LottieAnimationView lottieAnim = findViewById(R.id.lottieAnim);

        // Fade-in cho 2 dòng text
        TextView tvAppName = findViewById(R.id.tvAppName);
        TextView tvSlogan = findViewById(R.id.tvSlogan);

        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1000);
        tvAppName.startAnimation(fadeIn);
        tvSlogan.startAnimation(fadeIn);

        // Sau 3 giây kiểm tra đăng nhập
        new Handler().postDelayed(() -> {
            checkLoginStatus();
        }, SPLASH_DELAY);
    }

    private void checkLoginStatus() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        long lastActivity = prefs.getLong("lastActivity", 0);
        long currentTime = System.currentTimeMillis();

        // Kiểm tra session timeout
        if (isLoggedIn && (currentTime - lastActivity) < SESSION_TIMEOUT) {
            // Còn session, chuyển thẳng vào MainActivity
            String username = prefs.getString("username", "");
            String fullName = prefs.getString("fullName", "");
            
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("fullName", fullName);
            startActivity(intent);
        } else {
            // Hết session hoặc chưa đăng nhập, chuyển sang LoginActivity
            if (isLoggedIn) {
                // Xóa session cũ
                prefs.edit().clear().apply();
            }
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
        finish();
    }
}