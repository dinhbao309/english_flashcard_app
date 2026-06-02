package com.midterm.english_flashcard_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;


public class TopicActivity extends AppCompatActivity {

    ImageButton btnBack;
    LinearLayout cardAnimals, cardColors, cardNumbers, cardObjects, cardFruits, cardFamily;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        btnBack     = findViewById(R.id.btnBack);
        cardAnimals = findViewById(R.id.cardAnimals);
        cardColors  = findViewById(R.id.cardColors);
        cardNumbers = findViewById(R.id.cardNumbers);
        cardObjects = findViewById(R.id.cardObjects);
        cardFruits  = findViewById(R.id.cardFruits);
        cardFamily  = findViewById(R.id.cardFamily);

        btnBack.setOnClickListener(v -> finish());

        cardAnimals.setOnClickListener(v -> goToFlashCard("Động vật"));
        cardColors.setOnClickListener(v -> goToFlashCard("Màu sắc"));
        cardNumbers.setOnClickListener(v -> goToFlashCard("Số đếm"));
        cardObjects.setOnClickListener(v -> goToFlashCard("Đồ vật"));
        cardFruits.setOnClickListener(v -> goToFlashCard("Trái cây"));
        cardFamily.setOnClickListener(v -> goToFlashCard("Gia đình"));
    }

    
    private void goToFlashCard(String topic) {
        Intent intent = new Intent(this, FlashCardActivity.class);
        intent.putExtra("topic", topic);
        startActivity(intent);
    }
}
