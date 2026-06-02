package com.midterm.english_flashcard_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText etNewPassword, etConfirmPassword;
    Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etNewPassword     = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnReset          = findViewById(R.id.btnReset);

        btnReset.setOnClickListener(v -> {
            String newPass = etNewPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(newPass)) {
                etNewPassword.setError("Vui lòng nhập mật khẩu mới");
                return;
            }
            if (newPass.length() < 6) {
                etNewPassword.setError("Mật khẩu phải ít nhất 6 ký tự");
                return;
            }
            if (!newPass.equals(confirm)) {
                etConfirmPassword.setError("Mật khẩu không khớp");
                return;
            }

            // TODO: cập nhật mật khẩu vào database
            Toast.makeText(this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity(); // đóng hết các màn hình trước đó
        });
    }
}
