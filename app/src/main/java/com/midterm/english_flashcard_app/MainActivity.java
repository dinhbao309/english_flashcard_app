package com.midterm.english_flashcard_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView tvUsername, tvProgressPercent, tvProgressDetail;
    ProgressBar progressBar;
    LinearLayout cardAnimals, cardColors, cardNumbers, cardObjects;
    Button btnAllTopics;

    // Dữ liệu giả lập tiến độ
    int totalCards = 20;
    int learnedCards = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvUsername        = findViewById(R.id.tvUsername);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvProgressDetail  = findViewById(R.id.tvProgressDetail);
        progressBar       = findViewById(R.id.progressBar);
        cardAnimals       = findViewById(R.id.cardAnimals);
        cardColors        = findViewById(R.id.cardColors);
        cardNumbers       = findViewById(R.id.cardNumbers);
        cardObjects       = findViewById(R.id.cardObjects);
        btnAllTopics      = findViewById(R.id.btnAllTopics);

        // Hiển thị tên người dùng (tạm thời cứng, sau này lấy từ DB)
        tvUsername.setText("Bạn nhỏ ơi!");

        // Hiển thị tiến độ
        updateProgress();

        // Click vào từng chủ đề
        cardAnimals.setOnClickListener(v -> goToFlashCard("Động vật"));
        cardColors.setOnClickListener(v -> goToFlashCard("Màu sắc"));
        cardNumbers.setOnClickListener(v -> goToFlashCard("Số đếm"));
        cardObjects.setOnClickListener(v -> goToFlashCard("Đồ vật"));

        // Xem tất cả chủ đề
        btnAllTopics.setOnClickListener(v -> {
            // TODO: chuyển sang màn hình chọn chủ đề
            // startActivity(new Intent(this, TopicActivity.class));
        });
    }

    private void updateProgress() {
        int percent = totalCards == 0 ? 0 : (learnedCards * 100 / totalCards);
        progressBar.setProgress(percent);
        tvProgressPercent.setText(percent + "%");
        tvProgressDetail.setText(learnedCards + " / " + totalCards + " thẻ đã học");
    }

    private void goToFlashCard(String topic) {
        // Intent intent = new Intent(this, FlashCardActivity.class);
        // intent.putExtra("topic", topic);
        // startActivity(intent);
    }
}