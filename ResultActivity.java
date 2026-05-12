package com.midterm.english_flashcard_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
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
}