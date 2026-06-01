package com.midterm.english_flashcard_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView tvUsername, tvProgressPercent, tvProgressDetail;
    LinearLayout tvLogout;
    ProgressBar progressBar;
    LinearLayout cardAnimals, cardColors, cardNumbers, cardObjects;
    Button btnAllTopics, btnMiniGame, btnMatchingGame;
    
    boolean isLogoutVisible = false;

    // Dữ liệu giả lập tiến độ
    int totalCards = 20;
    int learnedCards = 0;

    // Session management
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULLNAME = "fullName";
    private static final String KEY_LAST_ACTIVITY = "lastActivity";
    private static final long SESSION_TIMEOUT = 10 * 60 * 1000; // 10 phút
    
    private Handler sessionHandler = new Handler();
    private Runnable sessionRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvUsername        = findViewById(R.id.tvUsername);
        tvLogout          = findViewById(R.id.tvLogout);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvProgressDetail  = findViewById(R.id.tvProgressDetail);
        progressBar       = findViewById(R.id.progressBar);
        cardAnimals       = findViewById(R.id.cardAnimals);
        cardColors        = findViewById(R.id.cardColors);
        cardNumbers       = findViewById(R.id.cardNumbers);
        cardObjects       = findViewById(R.id.cardObjects);
        btnAllTopics      = findViewById(R.id.btnAllTopics);
        btnMiniGame       = findViewById(R.id.btnMiniGame);
        btnMatchingGame   = findViewById(R.id.btnMatchingGame);

        // Lấy thông tin từ Intent hoặc SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String fullName = getIntent().getStringExtra("fullName");
        
        if (fullName == null || fullName.isEmpty()) {
            fullName = prefs.getString(KEY_FULLNAME, "");
        }

        // Hiển thị tên người dùng
        displayUserName(fullName);

        // Click vào tên để hiện/ẩn nút đăng xuất
        tvUsername.setOnClickListener(v -> toggleLogoutButton());

        // Click vào "Đăng xuất"
        tvLogout.setOnClickListener(v -> logout());

        // Tải tiến độ từ Firebase
        loadProgressFromFirebase();

        // Click vào từng chủ đề nổi bật ở Home
        cardAnimals.setOnClickListener(v -> goToFlashCard("Animals"));
        cardColors.setOnClickListener(v -> goToFlashCard("Colors"));
        cardNumbers.setOnClickListener(v -> goToFlashCard("Numbers"));
        cardObjects.setOnClickListener(v -> goToFlashCard("Objects"));

        // Xem tất cả chủ đề → chuyển sang TopicActivity
        btnAllTopics.setOnClickListener(v ->
                startActivity(new Intent(this, TopicActivity.class))
        );

        // Mini Game
        btnMiniGame.setOnClickListener(v ->
                startActivity(new Intent(this, QuizActivity.class))
        );

        // Matching Game
        btnMatchingGame.setOnClickListener(v ->
                startActivity(new Intent(this, MatchingGameActivity.class))
        );

        // Bắt đầu đếm session timeout
        startSessionTimeout();
    }

    private void displayUserName(String fullName) {
        if (fullName != null && !fullName.isEmpty()) {
            // Lấy 2 từ cuối của họ tên
            String[] nameParts = fullName.trim().split("\\s+");
            String displayName;
            
            if (nameParts.length >= 2) {
                // Lấy 2 từ cuối
                displayName = nameParts[nameParts.length - 2] + " " + nameParts[nameParts.length - 1];
            } else {
                // Nếu chỉ có 1 từ thì lấy từ đó
                displayName = fullName;
            }
            
            tvUsername.setText(displayName + " ơi!");
        } else {
            tvUsername.setText("Bạn nhỏ ơi!");
        }
    }

    private void loadProgressFromFirebase() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String username = prefs.getString(KEY_USERNAME, "");
        
        if (username.isEmpty()) {
            updateProgress();
            return;
        }

        // Lấy tiến độ từ Firebase
        FirebaseHelper firebaseHelper = new FirebaseHelper();
        firebaseHelper.getUserProgress(username, new FirebaseHelper.OnProgressLoadedListener() {
            @Override
            public void onLoaded(int learned, int total) {
                learnedCards = learned;
                totalCards = total;
                updateProgress();
            }

            @Override
            public void onError(String error) {
                // Nếu lỗi, dùng giá trị mặc định
                updateProgress();
            }
        });
    }

    private void toggleLogoutButton() {
        if (isLogoutVisible) {
            // Ẩn nút đăng xuất với animation slide up
            tvLogout.animate()
                    .translationY(-20)
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> tvLogout.setVisibility(View.GONE))
                    .start();
            isLogoutVisible = false;
        } else {
            // Hiện nút đăng xuất với animation slide down
            tvLogout.setVisibility(View.VISIBLE);
            tvLogout.setAlpha(0f);
            tvLogout.setTranslationY(-20);
            tvLogout.animate()
                    .translationY(0)
                    .alpha(1f)
                    .setDuration(300)
                    .start();
            isLogoutVisible = true;
        }
    }

    private void logout() {
        // Xóa thông tin đăng nhập
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Hủy session timeout
        if (sessionRunnable != null) {
            sessionHandler.removeCallbacks(sessionRunnable);
        }

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startSessionTimeout() {
        sessionRunnable = () -> {
            // Tự động đăng xuất sau 10 phút không hoạt động
            logout();
        };
        sessionHandler.postDelayed(sessionRunnable, SESSION_TIMEOUT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset session timeout khi user quay lại
        if (sessionRunnable != null) {
            sessionHandler.removeCallbacks(sessionRunnable);
        }
        startSessionTimeout();
        
        // Cập nhật lại tiến độ khi quay lại màn hình
        loadProgressFromFirebase();
        
        // Cập nhật thời gian hoạt động cuối
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis()).apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Lưu thời gian hoạt động cuối
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis()).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy session timeout khi activity bị destroy
        if (sessionRunnable != null) {
            sessionHandler.removeCallbacks(sessionRunnable);
        }
    }

    private void updateProgress() {
        int percent = totalCards == 0 ? 0 : (learnedCards * 100 / totalCards);
        progressBar.setProgress(percent);
        tvProgressPercent.setText(percent + "%");
        tvProgressDetail.setText(learnedCards + " / " + totalCards + " thẻ đã học");
    }

    private void goToFlashCard(String topic) {
        Intent intent = new Intent(this, FlashCardActivity.class);
        intent.putExtra("topic", topic);
        startActivity(intent);
    }
}