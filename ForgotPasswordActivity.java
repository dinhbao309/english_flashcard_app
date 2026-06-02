package com.midterm.english_flashcard_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText etPhone;
    Button btnConfirm;
    TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etPhone     = findViewById(R.id.etPhone);
        btnConfirm  = findViewById(R.id.btnConfirm);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnConfirm.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();

            if (TextUtils.isEmpty(phone)) {
                etPhone.setError("Vui lòng nhập số điện thoại");
                return;
            }
            if (phone.length() != 10) {
                etPhone.setError("Số điện thoại phải đủ 10 số");
                return;
            }

            // Chuyển sang màn hình nhập OTP
            Toast.makeText(this, "Đã gửi mã OTP!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, VerifyOtpActivity.class);
            intent.putExtra("phone", phone);
            startActivity(intent);
        });

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
