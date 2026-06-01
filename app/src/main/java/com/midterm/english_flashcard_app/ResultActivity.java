package com.midterm.english_flashcard_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    TextView tvTopicName, tvTotalCards;
    Button btnRelearn, btnOtherTopic, btnHome;
    String topic;
    int total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvTopicName  = findViewById(R.id.tvTopicName);
        tvTotalCards = findViewById(R.id.tvTotalCards);
        btnRelearn   = findViewById(R.id.btnRelearn);
        btnOtherTopic= findViewById(R.id.btnOtherTopic);
        btnHome      = findViewById(R.id.btnHome);

        // Nhận dữ liệu từ FlashCardActivity
        topic = getIntent().getStringExtra("topic");
        total = getIntent().getIntExtra("total", 0);

        tvTopicName.setText(topic);
        tvTotalCards.setText(String.valueOf(total));

        // Cập nhật tiến độ học tập
        updateProgress();

        // Học lại chủ đề này
        btnRelearn.setOnClickListener(v -> {
            Intent intent = new Intent(this, FlashCardActivity.class);
            intent.putExtra("topic", topic);
            startActivity(intent);
            finish();
        });

        // Chọn chủ đề khác
        btnOtherTopic.setOnClickListener(v -> {
            startActivity(new Intent(this, TopicActivity.class));
            finish();
        });

        // Về trang chủ
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            // Xóa hết stack, về Home luôn
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void updateProgress() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "");
        
        if (username.isEmpty()) {
            return;
        }

        // Lấy tiến độ hiện tại
        FirebaseHelper firebaseHelper = new FirebaseHelper();
        firebaseHelper.getUserProgress(username, new FirebaseHelper.OnProgressLoadedListener() {
            @Override
            public void onLoaded(int learnedCards, int totalCards) {
                // Tăng số thẻ đã học thêm số thẻ vừa học
                int newLearnedCards = learnedCards + total;
                
                // Không vượt quá tổng số thẻ
                if (newLearnedCards > totalCards) {
                    newLearnedCards = totalCards;
                }

                // Cập nhật lên Firebase
                firebaseHelper.updateUserProgress(username, newLearnedCards, new FirebaseHelper.OnUpdateProgressListener() {
                    @Override
                    public void onSuccess() {
                        // Cập nhật thành công
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ResultActivity.this, "Lỗi cập nhật tiến độ", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                // Không làm gì nếu lỗi
            }
        });
    }
}