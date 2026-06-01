package com.midterm.english_flashcard_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MatchingGameActivity extends AppCompatActivity {

    LinearLayout layoutImagesRow1, layoutImagesRow2, layoutWordsRow1, layoutWordsRow2;
    TextView tvTimer, tvScore, tvMatched;
    ImageButton btnBack;
    ImageView imgFeedback;

    FirebaseHelper firebaseHelper;
    List<String[]> allWords = new ArrayList<>();
    List<String[]> gameWords = new ArrayList<>();
    
    MediaPlayer correctSound, incorrectSound;
    TextToSpeech tts;
    
    // Game state
    View selectedImage = null;
    View selectedWord = null;
    String selectedImageWord = "";
    String selectedWordText = "";
    
    int score = 0;
    int matched = 0;
    int totalPairs = 4; // Số cặp mỗi vòng (4 cặp = 2x2 grid)
    
    // Tổng kết cộng dồn
    int totalScore = 0;
    int totalMatched = 0;
    int totalPairsAccumulated = 0; // Tổng số cặp cộng dồn
    int totalTimeSpent = 0;
    
    CountDownTimer timer;
    long timeLeft = 60000; // 60 giây
    int roundStartTime = 60; // Thời gian bắt đầu vòng
    
    private static final int REQUEST_CODE_RESULT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching_game);

        layoutImagesRow1 = findViewById(R.id.layoutImagesRow1);
        layoutImagesRow2 = findViewById(R.id.layoutImagesRow2);
        layoutWordsRow1 = findViewById(R.id.layoutWordsRow1);
        layoutWordsRow2 = findViewById(R.id.layoutWordsRow2);
        tvTimer = findViewById(R.id.tvTimer);
        tvScore = findViewById(R.id.tvScore);
        tvMatched = findViewById(R.id.tvMatched);
        btnBack = findViewById(R.id.btnBack);
        imgFeedback = findViewById(R.id.imgFeedback);

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

        // Load dữ liệu
        loadAllWords();

        // Nút Back
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadAllWords() {
        String[] categories = {"Animals", "Colors", "Numbers", "Objects"};
        
        for (String category : categories) {
            firebaseHelper.getWordsByTopic(category, new FirebaseHelper.OnWordsLoadedListener() {
                @Override
                public void onLoaded(List<String[]> wordList) {
                    allWords.addAll(wordList);
                    
                    // Chỉ bắt đầu game 1 lần khi đã load đủ dữ liệu
                    if (allWords.size() >= totalPairs && totalPairsAccumulated == 0) {
                        // Lần đầu tiên khởi động game
                        totalPairsAccumulated = totalPairs;
                        startNewGame();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(MatchingGameActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void startNewGame() {
        // Reset game state cho vòng mới
        score = 0;
        matched = 0;
        selectedImage = null;
        selectedWord = null;
        timeLeft = 60000;
        roundStartTime = 60;
        
        updateScore();
        
        // Chọn random từ
        selectRandomWords();
        
        // Tạo UI
        createGameUI();
        
        // Bắt đầu timer
        startTimer();
    }

    private void selectRandomWords() {
        gameWords.clear();
        
        if (allWords.size() < totalPairs) {
            Toast.makeText(this, "Không đủ dữ liệu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Random chọn từ
        List<String[]> tempList = new ArrayList<>(allWords);
        Collections.shuffle(tempList);
        
        for (int i = 0; i < totalPairs && i < tempList.size(); i++) {
            gameWords.add(tempList.get(i));
        }
    }

    private void createGameUI() {
        layoutImagesRow1.removeAllViews();
        layoutImagesRow2.removeAllViews();
        layoutWordsRow1.removeAllViews();
        layoutWordsRow2.removeAllViews();
        
        // Tạo list từ để shuffle
        List<String> words = new ArrayList<>();
        for (String[] word : gameWords) {
            words.add(word[0]); // word
        }
        Collections.shuffle(words);
        
        // Tạo hình ảnh (4 ảnh: 2 ở row1, 2 ở row2)
        for (int i = 0; i < gameWords.size(); i++) {
            String[] wordData = gameWords.get(i);
            createImageView(wordData, i);
        }
        
        // Tạo từ vựng (4 từ: 2 ở row1, 2 ở row2)
        for (int i = 0; i < words.size(); i++) {
            createWordView(words.get(i), i);
        }
    }

    private void createImageView(String[] wordData, int index) {
        try {
            // Kiểm tra wordData
            if (wordData == null || wordData.length < 3) {
                return;
            }
            
            // Container
            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setGravity(android.view.Gravity.CENTER);
            
            // LinearLayout params
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
            );
            params.setMargins(8, 8, 8, 8);
            container.setLayoutParams(params);
            
            container.setBackgroundResource(R.drawable.bg_input);
            container.setPadding(16, 16, 16, 16);
            container.setTag(wordData[0]); // Lưu word vào tag
            
            // ImageView
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            imageView.setLayoutParams(imgParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            
            // Load image từ assets
            String imageName = wordData[2];
            if (imageName != null && !imageName.isEmpty()) {
                loadImageFromAssets(imageName, imageView);
            }
            
            container.addView(imageView);
            
            // Click listener
            container.setOnClickListener(v -> onImageClick(container));
            
            // Thêm vào row tương ứng
            if (index < 2) {
                layoutImagesRow1.addView(container);
            } else {
                layoutImagesRow2.addView(container);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createWordView(String word, int index) {
        try {
            TextView textView = new TextView(this);
            
            // LinearLayout params
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
            );
            params.setMargins(8, 8, 8, 8);
            textView.setLayoutParams(params);
            
            textView.setText(word);
            textView.setTextSize(18);
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundResource(R.drawable.bg_input);
            textView.setPadding(24, 24, 24, 24);
            textView.setGravity(android.view.Gravity.CENTER);
            textView.setTag(word);
            
            // Click listener
            textView.setOnClickListener(v -> onWordClick(textView));
            
            // Thêm vào row tương ứng
            if (index < 2) {
                layoutWordsRow1.addView(textView);
            } else {
                layoutWordsRow2.addView(textView);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo từ vựng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onImageClick(View view) {
        try {
            // Nếu đã chọn hình khác, bỏ chọn
            if (selectedImage != null && selectedImage != view) {
                selectedImage.setBackgroundResource(R.drawable.bg_input);
            }
            
            // Chọn hình này
            selectedImage = view;
            selectedImageWord = (String) view.getTag();
            
            if (selectedImageWord == null) {
                Toast.makeText(this, "Lỗi: Không tìm thấy từ", Toast.LENGTH_SHORT).show();
                return;
            }
            
            view.setBackgroundColor(Color.parseColor("#FFE0B2")); // Màu cam nhạt
            
            // Nếu đã chọn cả hình và từ, kiểm tra
            if (selectedWord != null) {
                checkMatch();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi chọn hình: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onWordClick(View view) {
        try {
            // Nếu đã chọn từ khác, bỏ chọn
            if (selectedWord != null && selectedWord != view) {
                selectedWord.setBackgroundResource(R.drawable.bg_input);
            }
            
            // Chọn từ này
            selectedWord = view;
            selectedWordText = (String) view.getTag();
            
            if (selectedWordText == null) {
                Toast.makeText(this, "Lỗi: Không tìm thấy từ", Toast.LENGTH_SHORT).show();
                return;
            }
            
            view.setBackgroundColor(Color.parseColor("#FFE0B2")); // Màu cam nhạt
            
            // Nếu đã chọn cả hình và từ, kiểm tra
            if (selectedImage != null) {
                checkMatch();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi chọn từ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkMatch() {
        if (selectedImageWord.equals(selectedWordText)) {
            // Đúng!
            onCorrectMatch();
        } else {
            // Sai!
            onIncorrectMatch();
        }
    }

    private void onCorrectMatch() {
        // Hiển thị feedback
        showFeedback(true);
        
        // Phát âm thanh
        if (correctSound != null) {
            correctSound.start();
        }
        
        // Đọc từ vựng sau khi âm thanh correct kết thúc (delay ~1 giây)
        if (tts != null && selectedImageWord != null && !selectedImageWord.isEmpty()) {
            new Handler().postDelayed(() -> {
                tts.speak(selectedImageWord, TextToSpeech.QUEUE_FLUSH, null, null);
            }, 1000);
        }
        
        // Tăng điểm: +10 điểm cho mỗi cặp đúng
        score += 10;
        matched++;
        
        // Cập nhật tổng điểm ngay lập tức
        totalScore += 10;
        totalMatched++;
        
        updateScore();
        
        // Lưu reference để tránh null
        final View imageToHide = selectedImage;
        final View wordToHide = selectedWord;
        
        // Animation biến mất
        if (imageToHide != null) {
            imageToHide.animate()
                    .alpha(0f)
                    .scaleX(0.5f)
                    .scaleY(0.5f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        if (imageToHide != null) {
                            imageToHide.setVisibility(View.INVISIBLE);
                        }
                    })
                    .start();
        }
        
        if (wordToHide != null) {
            wordToHide.animate()
                    .alpha(0f)
                    .scaleX(0.5f)
                    .scaleY(0.5f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        if (wordToHide != null) {
                            wordToHide.setVisibility(View.INVISIBLE);
                        }
                    })
                    .start();
        }
        
        // Reset selection
        selectedImage = null;
        selectedWord = null;
        
        // Kiểm tra thắng
        if (matched == totalPairs) {
            onRoundComplete(false);
        }
    }

    private void onIncorrectMatch() {
        // Hiển thị feedback
        showFeedback(false);
        
        // Phát âm thanh
        if (incorrectSound != null) {
            incorrectSound.start();
        }
        
        // Trừ điểm: -5 điểm cho mỗi lần chọn sai
        score -= 5;
        
        // Cập nhật tổng điểm ngay lập tức
        totalScore -= 5;
        
        updateScore();
        
        // Lưu reference để tránh null
        final View imageToShake = selectedImage;
        final View wordToShake = selectedWord;
        
        // Animation rung cho image
        if (imageToShake != null) {
            imageToShake.animate()
                    .translationX(-10f)
                    .setDuration(50)
                    .withEndAction(() -> {
                        if (imageToShake != null) {
                            imageToShake.animate().translationX(10f).setDuration(50)
                                    .withEndAction(() -> {
                                        if (imageToShake != null) {
                                            imageToShake.animate().translationX(0f).setDuration(50).start();
                                            imageToShake.setBackgroundResource(R.drawable.bg_input);
                                        }
                                    }).start();
                        }
                    }).start();
        }
        
        // Animation rung cho word
        if (wordToShake != null) {
            wordToShake.animate()
                    .translationX(-10f)
                    .setDuration(50)
                    .withEndAction(() -> {
                        if (wordToShake != null) {
                            wordToShake.animate().translationX(10f).setDuration(50)
                                    .withEndAction(() -> {
                                        if (wordToShake != null) {
                                            wordToShake.animate().translationX(0f).setDuration(50).start();
                                            wordToShake.setBackgroundResource(R.drawable.bg_input);
                                        }
                                    }).start();
                        }
                    }).start();
        }
        
        // Reset selection
        selectedImage = null;
        selectedWord = null;
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        
        timer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                int seconds = (int) (millisUntilFinished / 1000);
                tvTimer.setText("⏱ " + seconds + "s");
                
                // Đổi màu khi sắp hết giờ
                if (seconds <= 10) {
                    tvTimer.setTextColor(Color.RED);
                } else {
                    tvTimer.setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onFinish() {
                onRoundComplete(true);
            }
        }.start();
    }

    private void onRoundComplete(boolean isTimeout) {
        // Dừng timer
        if (timer != null) {
            timer.cancel();
        }
        
        // Tính thời gian đã chơi trong vòng này
        int roundTime = roundStartTime - (int)(timeLeft / 1000);
        
        // Cộng dồn thời gian (totalScore và totalMatched đã được cộng dồn realtime)
        totalTimeSpent += roundTime;
        
        // Chuyển sang màn hình kết quả
        Intent intent = new Intent(this, MatchingResultActivity.class);
        intent.putExtra("totalScore", totalScore);
        intent.putExtra("totalMatched", totalMatched);
        intent.putExtra("totalTimeSpent", totalTimeSpent);
        intent.putExtra("roundScore", score);
        intent.putExtra("roundMatched", matched);
        intent.putExtra("roundTime", roundTime);
        intent.putExtra("isTimeout", isTimeout);
        startActivityForResult(intent, REQUEST_CODE_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_RESULT && resultCode == RESULT_OK && data != null) {
            String action = data.getStringExtra("action");
            
            if ("continue".equals(action)) {
                // Chơi tiếp - cộng thêm 4 cặp vào tổng và bắt đầu vòng mới
                totalPairsAccumulated += totalPairs;
                startNewGame();
            } else if ("restart".equals(action)) {
                // Chơi lại - Reset tất cả về 0
                totalScore = 0;
                totalMatched = 0;
                totalPairsAccumulated = totalPairs; // Reset về 4
                totalTimeSpent = 0;
                startNewGame();
            } else if ("finish".equals(action)) {
                // Kết thúc - đóng activity
                finish();
            }
        }
    }

    private void updateScore() {
        // Hiển thị tổng điểm và tổng matched (cộng dồn)
        tvScore.setText("Score: " + totalScore);
        tvMatched.setText("Matched: " + totalMatched + "/" + totalPairsAccumulated);
    }

    private void showFeedback(boolean isCorrect) {
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
                    // Sau 800ms, fade out
                    new android.os.Handler().postDelayed(() -> {
                        imgFeedback.animate()
                                .alpha(0f)
                                .scaleX(0.8f)
                                .scaleY(0.8f)
                                .setDuration(300)
                                .withEndAction(() -> imgFeedback.setVisibility(View.GONE))
                                .start();
                    }, 800);
                })
                .start();
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
        if (timer != null) {
            timer.cancel();
        }
        if (correctSound != null) {
            correctSound.release();
        }
        if (incorrectSound != null) {
            incorrectSound.release();
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
