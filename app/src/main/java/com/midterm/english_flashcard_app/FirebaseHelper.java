package com.midterm.english_flashcard_app;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {

    private final FirebaseFirestore db;

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public interface OnWordsLoadedListener {
        void onLoaded(List<String[]> wordList);
        void onError(String error);
    }

    public interface OnRegisterListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnLoginListener {
        void onSuccess(String username, String fullName);
        void onError(String error);
    }

    public interface OnProgressLoadedListener {
        void onLoaded(int learnedCards, int totalCards);
        void onError(String error);
    }

    // Lấy từ theo category, trả về list đã random
    // Mỗi phần tử: {word, meaning, image, audio}
    public void getWordsByTopic(String category, OnWordsLoadedListener listener) {
        db.collection("vocabulary")
                .whereEqualTo("category", category)
                .orderBy("order")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String[]> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String word    = doc.getString("word");
                        String meaning = doc.getString("meaning");
                        String image   = doc.getString("image");
                        String audio   = doc.getString("audio");
                        list.add(new String[]{word, meaning, image, audio});
                    }
                    // Random thứ tự
                    Collections.shuffle(list);
                    listener.onLoaded(list);
                })
                .addOnFailureListener(e ->
                        listener.onError(e.getMessage())
                );
    }

    // Đăng ký tài khoản mới
    public void registerUser(String fullName, String username, String email, String phone, String password, OnRegisterListener listener) {
        // Kiểm tra username đã tồn tại chưa
        db.collection("taikhoan")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        listener.onError("Tên đăng nhập đã tồn tại!");
                        return;
                    }

                    // Kiểm tra phone đã tồn tại chưa
                    db.collection("taikhoan")
                            .whereEqualTo("phone", phone)
                            .get()
                            .addOnSuccessListener(phoneSnapshot -> {
                                if (!phoneSnapshot.isEmpty()) {
                                    listener.onError("Số điện thoại đã được đăng ký!");
                                    return;
                                }

                                // Tạo tài khoản mới
                                Map<String, Object> user = new HashMap<>();
                                user.put("fullName", fullName);
                                user.put("username", username);
                                user.put("email", email);
                                user.put("phone", phone);
                                user.put("password", password);
                                user.put("createdAt", System.currentTimeMillis());
                                user.put("learnedCards", 0);
                                user.put("totalCards", 20);

                                db.collection("taikhoan")
                                        .add(user)
                                        .addOnSuccessListener(documentReference -> listener.onSuccess())
                                        .addOnFailureListener(e -> listener.onError("Lỗi: " + e.getMessage()));
                            })
                            .addOnFailureListener(e -> listener.onError("Lỗi: " + e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onError("Lỗi: " + e.getMessage()));
    }

    // Đăng nhập
    public void loginUser(String username, String password, OnLoginListener listener) {
        db.collection("taikhoan")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        listener.onError("Tên đăng nhập hoặc mật khẩu không đúng!");
                    } else {
                        String fullName = querySnapshot.getDocuments().get(0).getString("fullName");
                        if (fullName == null || fullName.isEmpty()) {
                            fullName = username; // Fallback nếu không có fullName
                        }
                        listener.onSuccess(username, fullName);
                    }
                })
                .addOnFailureListener(e -> listener.onError("Lỗi: " + e.getMessage()));
    }

    // Lấy tiến độ học tập của user
    public void getUserProgress(String username, OnProgressLoadedListener listener) {
        db.collection("taikhoan")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Long learnedCards = querySnapshot.getDocuments().get(0).getLong("learnedCards");
                        Long totalCards = querySnapshot.getDocuments().get(0).getLong("totalCards");
                        
                        int learned = (learnedCards != null) ? learnedCards.intValue() : 0;
                        int total = (totalCards != null) ? totalCards.intValue() : 20;
                        
                        listener.onLoaded(learned, total);
                    } else {
                        listener.onError("Không tìm thấy user");
                    }
                })
                .addOnFailureListener(e -> listener.onError("Lỗi: " + e.getMessage()));
    }

    // Cập nhật tiến độ học tập
    public void updateUserProgress(String username, int learnedCards, OnUpdateProgressListener listener) {
        db.collection("taikhoan")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        db.collection("taikhoan")
                                .document(docId)
                                .update("learnedCards", learnedCards)
                                .addOnSuccessListener(aVoid -> listener.onSuccess())
                                .addOnFailureListener(e -> listener.onError("Lỗi: " + e.getMessage()));
                    } else {
                        listener.onError("Không tìm thấy user");
                    }
                })
                .addOnFailureListener(e -> listener.onError("Lỗi: " + e.getMessage()));
    }

    public interface OnUpdateProgressListener {
        void onSuccess();
        void onError(String error);
    }
}