package com.midterm.english_flashcard_app;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FirebaseHelper {

    private final FirebaseFirestore db;

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // Interface callback vì Firestore bất đồng bộ
    public interface OnWordsLoadedListener {
        void onLoaded(List<String[]> wordList);
        void onError(String error);
    }

    // Lấy từ theo chủ đề, trả về list đã random
    public void getWordsByTopic(String topic, OnWordsLoadedListener listener) {
        db.collection("words")
                .whereEqualTo("topic", topic)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String[]> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String emoji      = doc.getString("emoji");
                        String english    = doc.getString("english");
                        String vietnamese = doc.getString("vietnamese");
                        list.add(new String[]{emoji, english, vietnamese});
                    }
                    // Random thứ tự
                    Collections.shuffle(list);
                    listener.onLoaded(list);
                })
                .addOnFailureListener(e ->
                        listener.onError(e.getMessage())
                );
    }
}