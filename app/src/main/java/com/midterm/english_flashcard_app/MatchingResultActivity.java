package com.midterm.english_flashcard_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MatchingResultActivity extends AppCompatActivity {

    TextView tvResultTitle, tvTotalScore, tvTotalMatched, tvTotalTime, tvRoundDetail;
    Button btnContinue, btnFinish;

    int totalScore, totalMatched, totalTimeSpent;
    int roundScore, roundMatched, roundTime;
    boolean isTimeout;
    
    MediaPlayer incorrectSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching_result);

        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvTotalScore = findViewById(R.id.tvTotalScore);
        tvTotalMatched = findViewById(R.id.tvTotalMatched);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvRoundDetail = findViewById(R.id.tvRoundDetail);
        btnContinue = findViewById(R.id.btnContinue);
        btnFinish = findViewById(R.id.btnFinish);

        // Nhận dữ liệu từ Intent
        totalScore = getIntent().getIntExtra("totalScore", 0);
        totalMatched = getIntent().getIntExtra("totalMatched", 0);
        totalTimeSpent = getIntent().getIntExtra("totalTimeSpent", 0);
        roundScore = getIntent().getIntExtra("roundScore", 0);
        roundMatched = getIntent().getIntExtra("roundMatched", 0);
        roundTime = getIntent().getIntExtra("roundTime", 0);
        isTimeout = getIntent().getBooleanExtra("isTimeout", false);

        // Khởi tạo âm thanh
        incorrectSound = MediaPlayer.create(this, R.raw.incorrect_sound);

        // Nếu hết giờ, phát âm thanh incorrect
        if (isTimeout && incorrectSound != null) {
            incorrectSound.start();
        }

        // Hiển thị kết quả
        displayResults();

        // Nút Chơi tiếp / Chơi lại
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent();
            if (isTimeout) {
                // Nếu hết giờ -> Chơi lại -> Reset tất cả
                intent.putExtra("action", "restart");
            } else {
                // Nếu hoàn thành -> Chơi tiếp -> Cộng dồn
                intent.putExtra("action", "continue");
            }
            setResult(RESULT_OK, intent);
            finish();
        });

        // Nút Kết thúc
        btnFinish.setOnClickListener(v -> {
            // Cập nhật tiến độ lên Firebase
            updateProgress();
            
            Intent intent = new Intent();
            intent.putExtra("action", "finish");
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private void displayResults() {
        // Thay đổi title và text nút nếu hết giờ
        if (isTimeout) {
            tvResultTitle.setText("⏰ Hết giờ!");
            btnContinue.setText("🔄 Chơi lại");
            btnContinue.setBackgroundColor(android.graphics.Color.parseColor("#888888"));
        } else {
            tvResultTitle.setText("🎉 Hoàn thành!");
            btnContinue.setText("▶ Chơi tiếp");
            btnContinue.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"));
        }

        // Hiển thị tổng kết
        tvTotalScore.setText(String.valueOf(totalScore));
        tvTotalMatched.setText(String.valueOf(totalMatched));
        tvTotalTime.setText(formatTime(totalTimeSpent));

        // Hiển thị chi tiết vòng này
        String detail = roundMatched + " từ • " + formatTime(roundTime) + " • ";
        if (roundScore >= 0) {
            detail += "+" + roundScore + " điểm";
        } else {
            detail += roundScore + " điểm";
        }
        tvRoundDetail.setText(detail);
    }

    private String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else {
            int minutes = seconds / 60;
            int secs = seconds % 60;
            return minutes + "m " + secs + "s";
        }
    }

    private void updateProgress() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "");
        
        if (username.isEmpty()) {
            return;
        }

        FirebaseHelper firebaseHelper = new FirebaseHelper();
        firebaseHelper.getUserProgress(username, new FirebaseHelper.OnProgressLoadedListener() {
            @Override
            public void onLoaded(int learnedCards, int totalCards) {
                int newLearnedCards = learnedCards + totalMatched;
                if (newLearnedCards > totalCards) {
                    newLearnedCards = totalCards;
                }

                firebaseHelper.updateUserProgress(username, newLearnedCards, new FirebaseHelper.OnUpdateProgressListener() {
                    @Override
                    public void onSuccess() {
                        // Success
                    }

                    @Override
                    public void onError(String error) {
                        // Error
                    }
                });
            }

            @Override
            public void onError(String error) {
                // Error
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Chặn nút back, bắt buộc chọn Chơi tiếp hoặc Kết thúc
        // Không làm gì
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release media player
        if (incorrectSound != null) {
            incorrectSound.release();
        }
    }
}
