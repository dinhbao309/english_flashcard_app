package com.midterm.english_flashcard_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    EditText etFullName, etUsername, etEmail, etPhone, etPassword, etConfirmPassword;
    Button btnRegister;
    TextView tvGoToLogin;
    ImageButton btnTogglePassword, btnToggleConfirmPassword;
    boolean isPasswordVisible = false;
    boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName        = findViewById(R.id.etFullName);
        etUsername        = findViewById(R.id.etUsername);
        etEmail           = findViewById(R.id.etEmail);
        etPhone           = findViewById(R.id.etPhone);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister       = findViewById(R.id.btnRegister);
        tvGoToLogin       = findViewById(R.id.tvGoToLogin);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);

        // Toggle hiển thị mật khẩu
        btnTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                isPasswordVisible = false;
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                isPasswordVisible = true;
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        // Toggle hiển thị xác nhận mật khẩu
        btnToggleConfirmPassword.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                isConfirmPasswordVisible = false;
            } else {
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                isConfirmPasswordVisible = true;
            }
            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
        });

        btnRegister.setOnClickListener(v -> {
            String fullName  = etFullName.getText().toString().trim();
            String username  = etUsername.getText().toString().trim();
            String email     = etEmail.getText().toString().trim();
            String phone     = etPhone.getText().toString().trim();
            String password  = etPassword.getText().toString().trim();
            String confirm   = etConfirmPassword.getText().toString().trim();

            // Kiểm tra các trường bắt buộc
            if (TextUtils.isEmpty(fullName)) {
                etFullName.setError("Vui lòng nhập họ và tên");
                return;
            }
            if (TextUtils.isEmpty(username)) {
                etUsername.setError("Vui lòng nhập tên đăng nhập");
                return;
            }
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Vui lòng nhập email");
                return;
            }
            if (TextUtils.isEmpty(phone)) {
                etPhone.setError("Vui lòng nhập số điện thoại");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Vui lòng nhập mật khẩu");
                return;
            }
            if (password.length() < 6) {
                etPassword.setError("Mật khẩu phải ít nhất 6 ký tự");
                return;
            }
            if (!password.equals(confirm)) {
                etConfirmPassword.setError("Mật khẩu không khớp");
                return;
            }

            // Lưu tài khoản vào Firebase Firestore
            FirebaseHelper firebaseHelper = new FirebaseHelper();
            firebaseHelper.registerUser(fullName, username, email, phone, password, new FirebaseHelper.OnRegisterListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                }
            });
        });

        // Quay lại đăng nhập
        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}