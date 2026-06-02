package com.midterm.english_flashcard_app;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlashCardActivity extends AppCompatActivity {

    TextView tvTopicName, tvCardCount, tvEmoji, tvEnglish;
    TextView tvEmojiBack, tvVietnamese, tvEnglishSmall;
    LinearLayout cardFront, cardBack;
    ProgressBar progressBar;
    Button btnPrev, btnNext, btnSpeak;
    ImageButton btnBack;

    TextToSpeech tts;
    boolean isFlipped = false;
    int currentIndex = 0;
    List<String[]> wordList = new ArrayList<>();
    String topic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        // Ánh xạ view
        tvTopicName    = findViewById(R.id.tvTopicName);
        tvCardCount    = findViewById(R.id.tvCardCount);
        tvEmoji        = findViewById(R.id.tvEmoji);
        tvEnglish      = findViewById(R.id.tvEnglish);
        tvEmojiBack    = findViewById(R.id.tvEmojiBack);
        tvVietnamese   = findViewById(R.id.tvVietnamese);
        tvEnglishSmall = findViewById(R.id.tvEnglishSmall);
        cardFront      = findViewById(R.id.cardFront);
        cardBack       = findViewById(R.id.cardBack);
        progressBar    = findViewById(R.id.progressBar);
        btnPrev        = findViewById(R.id.btnPrev);
        btnNext        = findViewById(R.id.btnNext);
        btnSpeak       = findViewById(R.id.btnSpeak);
        btnBack        = findViewById(R.id.btnBack);

        // Nhận tên chủ đề từ Intent
        topic = getIntent().getStringExtra("topic");
        tvTopicName.setText(topic);
        tvCardCount.setText("Đang tải...");

        // Khởi tạo Text-to-Speech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS)
                tts.setLanguage(Locale.ENGLISH);
        });

        // Load dữ liệu từ Firebase console
        FirebaseHelper firebaseHelper = new FirebaseHelper();
        firebaseHelper.getWordsByTopic(topic, new FirebaseHelper.OnWordsLoadedListener() {
            @Override
            public void onLoaded(List<String[]> words) {
                if (words.isEmpty()) {
                    tvCardCount.setText("Không có dữ liệu!");
                    return;
                }
                wordList = words;
                currentIndex = 0;
                showCard(currentIndex);
            }

            @Override
            public void onError(String error) {
                tvCardCount.setText("Lỗi tải dữ liệu!");
                Toast.makeText(FlashCardActivity.this,
                        "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // Lật thẻ khi nhấn vào
        cardFront.setOnClickListener(v -> flipCard());
        cardBack.setOnClickListener(v -> flipCard());

        // Nút phát âm
        btnSpeak.setOnClickListener(v -> {
            if (!wordList.isEmpty()) {
                String word = wordList.get(currentIndex)[1];
                tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        // Nút Previous
        btnPrev.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                isFlipped = false;
                showCard(currentIndex);
            } else {
                Toast.makeText(this, "Đây là thẻ đầu tiên!", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút Next
        btnNext.setOnClickListener(v -> {
            if (currentIndex < wordList.size() - 1) {
                currentIndex++;
                isFlipped = false;
                showCard(currentIndex);
            } else {
                // Học xong → chuyển sang màn hình kết quả
                Intent intent = new Intent(FlashCardActivity.this, ResultActivity.class);
                intent.putExtra("topic", topic);
                intent.putExtra("total", wordList.size());
                startActivity(intent);
                finish();
            }
        });

        // Nút Back
        btnBack.setOnClickListener(v -> finish());
    }

    private void showCard(int index) {
        String[] word = wordList.get(index);

        // Mặt trước
        tvEmoji.setText(word[0]);
        tvEnglish.setText(word[1]);

        // Mặt sau
        tvEmojiBack.setText(word[0]);
        tvVietnamese.setText(word[2]);
        tvEnglishSmall.setText(word[1]);

        // Reset về mặt trước
        cardFront.setVisibility(View.VISIBLE);
        cardBack.setVisibility(View.GONE);
        isFlipped = false;

        // Cập nhật số thẻ và thanh tiến độ
        tvCardCount.setText((index + 1) + " / " + wordList.size());
        progressBar.setProgress((index + 1) * 100 / wordList.size());

        // Đổi text nút Next ở thẻ cuối
        if (index == wordList.size() - 1) {
            btnNext.setText("Hoàn thành ✓");
        } else {
            btnNext.setText("Tiếp ▶");
        }
    }

    private void flipCard() {
        if (!isFlipped) {
            cardFront.setVisibility(View.GONE);
            cardBack.setVisibility(View.VISIBLE);
            isFlipped = true;
        } else {
            cardFront.setVisibility(View.VISIBLE);
            cardBack.setVisibility(View.GONE);
            isFlipped = false;
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
