package com.midterm.english_flashcard_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {

    ImageView imgQuestion, imgFeedback;
    TextView tvScore, tvAnswer;
    Button btnOption1, btnOption2, btnOption3, btnOption4;
    Button btnContinue, btnFinish;
    LinearLayout layoutOptions, layoutResult, layoutPreviousQuestions;
    ImageButton btnBack;

    FirebaseHelper firebaseHelper;
    List<String[]> allWords = new ArrayList<>();
    String[] currentWord;
    String correctAnswer;
    int score = 0;
    int totalQuestions = 0;

    MediaPlayer correctSound, incorrectSound;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        imgQuestion = findViewById(R.id.imgQuestion);
        imgFeedback = findViewById(R.id.imgFeedback);
        tvScore = findViewById(R.id.tvScore);
        tvAnswer = findViewById(R.id.tvAnswer);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        btnContinue = findViewById(R.id.btnContinue);
        btnFinish = findViewById(R.id.btnFinish);
        layoutOptions = findViewById(R.id.layoutOptions);
        layoutResult = findViewById(R.id.layoutResult);
        layoutPreviousQuestions = findViewById(R.id.layoutPreviousQuestions);
        btnBack = findViewById(R.id.btnBack);

        // Khởi tạo âm thanh
        correctSound = MediaPlayer.create(this, R.raw.correct_sound);
        incorrectSound = MediaPlayer.create(this, R.raw.incorrect_sound);

        // Khởi tạo Text-to-Speech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.ENGLISH);
            }
        });

        firebaseHelper = new FirebaseHelper();

        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Load tất cả từ vựng từ Firebase
        loadAllWords();

        // Click listeners cho các nút đáp án
        btnOption1.setOnClickListener(v -> checkAnswer(btnOption1.getText().toString()));
        btnOption2.setOnClickListener(v -> checkAnswer(btnOption2.getText().toString()));
        btnOption3.setOnClickListener(v -> checkAnswer(btnOption3.getText().toString()));
        btnOption4.setOnClickListener(v -> checkAnswer(btnOption4.getText().toString()));

        // Click listeners cho nút tiếp tục/kết thúc
        btnContinue.setOnClickListener(v -> continueQuiz());
        btnFinish.setOnClickListener(v -> finishQuiz());
    }

    private void loadAllWords() {
        // Load từ tất cả các category
        String[] categories = {"Animals", "Colors", "Numbers", "Objects"};
        
        for (String category : categories) {
            firebaseHelper.getWordsByTopic(category, new FirebaseHelper.OnWordsLoadedListener() {
                @Override
                public void onLoaded(List<String[]> wordList) {
                    allWords.addAll(wordList);
                    
                    // Khi đã load đủ, bắt đầu quiz
                    if (allWords.size() >= 4) {
                        generateQuestion();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(QuizActivity.this, "Lỗi tải dữ liệu: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void generateQuestion() {
        if (allWords.isEmpty() || allWords.size() < 4) {
            Toast.makeText(this, "Không đủ dữ liệu để tạo câu hỏi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Random 1 từ làm câu hỏi
        Random random = new Random();
        currentWord = allWords.get(random.nextInt(allWords.size()));
        correctAnswer = currentWord[0]; // word

        // Hiển thị hình ảnh từ assets
        String imageName = currentWord[2]; // image (ví dụ: "pink.png")
        if (imageName != null && !imageName.isEmpty()) {
            loadImageFromAssets(imageName, imgQuestion);
        }

        // Tạo 4 đáp án (1 đúng + 3 sai)
        List<String> options = new ArrayList<>();
        options.add(correctAnswer);

        // Thêm 3 đáp án sai
        while (options.size() < 4) {
            String randomWord = allWords.get(random.nextInt(allWords.size()))[0];
            if (!options.contains(randomWord)) {
                options.add(randomWord);
            }
        }

        // Shuffle để đáp án đúng không luôn ở vị trí đầu
        Collections.shuffle(options);

        // Hiển thị các đáp án
        btnOption1.setText(options.get(0));
        btnOption2.setText(options.get(1));
        btnOption3.setText(options.get(2));
        btnOption4.setText(options.get(3));

        // Reset UI
        layoutOptions.setVisibility(View.VISIBLE);
        layoutResult.setVisibility(View.GONE);
        imgFeedback.setVisibility(View.GONE);
        tvAnswer.setVisibility(View.GONE);
        
        // Enable tất cả các nút
        btnOption1.setEnabled(true);
        btnOption2.setEnabled(true);
        btnOption3.setEnabled(true);
        btnOption4.setEnabled(true);
    }

    private void checkAnswer(String selectedAnswer) {
        totalQuestions++;
        
        // Disable tất cả các nút để không click nhiều lần
        btnOption1.setEnabled(false);
        btnOption2.setEnabled(false);
        btnOption3.setEnabled(false);
        btnOption4.setEnabled(false);

        if (selectedAnswer.equals(correctAnswer)) {
            // Đúng
            score++;
            showFeedback("CORRECT ✓", true);
        } else {
            // Sai
            showFeedback("INCORRECT ✗", false);
        }

        // Cập nhật điểm
        tvScore.setText("Score: " + score + " / " + totalQuestions);
    }

    private void showFeedback(String message, boolean isCorrect) {
        // Hiển thị ảnh feedback ở giữa màn hình
        int imageResource = isCorrect ? R.drawable.correct : R.drawable.incorrect;
        imgFeedback.setImageResource(imageResource);
        imgFeedback.setVisibility(View.VISIBLE);
        imgFeedback.setAlpha(0f);
        imgFeedback.setScaleX(0.5f);
        imgFeedback.setScaleY(0.5f);

        // Animation: Scale + Fade in
        imgFeedback.animate()
                .alpha(1f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(300)
                .withEndAction(() -> {
                    // Sau 1 giây, fade out
                    new Handler().postDelayed(() -> {
                        imgFeedback.animate()
                                .alpha(0f)
                                .scaleX(0.8f)
                                .scaleY(0.8f)
                                .setDuration(500)
                                .withEndAction(() -> {
                                    imgFeedback.setVisibility(View.GONE);
                                    showResultOptions();
                                })
                                .start();
                    }, 1000);
                })
                .start();

        // Phát âm thanh (nếu có)
        if (isCorrect && correctSound != null) {
            correctSound.start();
        } else if (!isCorrect && incorrectSound != null) {
            incorrectSound.start();
        }

        // Làm mờ câu hỏi hiện tại và đưa vào background
        addToPreviousQuestions();
    }

    private void addToPreviousQuestions() {
        // Tạo view cho câu hỏi cũ (làm mờ)
        View oldQuestion = getLayoutInflater().inflate(R.layout.item_previous_question, layoutPreviousQuestions, false);
        ImageView imgOld = oldQuestion.findViewById(R.id.imgOldQuestion);
        
        // Copy hình ảnh
        imgOld.setImageDrawable(imgQuestion.getDrawable());
        
        // Làm mờ
        oldQuestion.setAlpha(0.3f);
        
        // Thêm vào layout
        layoutPreviousQuestions.addView(oldQuestion, 0);
    }

    private void showResultOptions() {
        // Ẩn các nút đáp án
        layoutOptions.setVisibility(View.GONE);
        
        // Hiện tên tiếng Anh
        tvAnswer.setText(correctAnswer);
        tvAnswer.setVisibility(View.VISIBLE);
        
        // Đọc từ vựng bằng Text-to-Speech
        if (tts != null && correctAnswer != null && !correctAnswer.isEmpty()) {
            // Delay 500ms để âm thanh feedback kết thúc trước
            new Handler().postDelayed(() -> {
                tts.speak(correctAnswer, TextToSpeech.QUEUE_FLUSH, null, null);
            }, 500);
        }
        
        // Hiện nút tiếp tục/kết thúc
        layoutResult.setVisibility(View.VISIBLE);
    }

    private void continueQuiz() {
        // Tiếp tục câu hỏi mới
        generateQuestion();
    }

    private void finishQuiz() {
        // Cập nhật tiến độ
        updateProgress();
        
        // Hiển thị kết quả cuối cùng
        Toast.makeText(this, "Kết quả: " + score + "/" + totalQuestions, Toast.LENGTH_LONG).show();
        finish();
    }

    private void updateProgress() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "");
        
        if (username.isEmpty()) {
            return;
        }

        // Cập nhật tiến độ tương tự như FlashCard
        firebaseHelper.getUserProgress(username, new FirebaseHelper.OnProgressLoadedListener() {
            @Override
            public void onLoaded(int learnedCards, int totalCards) {
                int newLearnedCards = learnedCards + score;
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

    private void loadImageFromAssets(String imageName, ImageView imageView) {
        try {
            // Xử lý tên file: nếu có .png thì đổi thành .jpg
            if (imageName.endsWith(".png")) {
                imageName = imageName.replace(".png", ".jpg");
            }
            
            // Load ảnh từ assets/images/
            String path = "file:///android_asset/images/" + imageName;
            Glide.with(this)
                    .load(path)
                    .placeholder(R.drawable.bg_input)
                    .error(R.drawable.bg_input)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi load ảnh: " + imageName, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release media players
        if (correctSound != null) {
            correctSound.release();
        }
        if (incorrectSound != null) {
            incorrectSound.release();
        }
        // Release Text-to-Speech
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
