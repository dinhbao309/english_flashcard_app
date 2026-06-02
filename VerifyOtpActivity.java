package com.midterm.english_flashcard_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VerifyOtpActivity extends AppCompatActivity {

    private static final String FAKE_OTP = "123456"; // mã giả lập

    EditText etOtp;
    Button btnVerify;
    TextView tvPhoneHint, tvResend, tvGoBack;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        etOtp       = findViewById(R.id.etOtp);
        btnVerify   = findViewById(R.id.btnVerify);
        tvPhoneHint = findViewById(R.id.tvPhoneHint);
        tvResend    = findViewById(R.id.tvResend);
        tvGoBack    = findViewById(R.id.tvGoBack);

        // Nhận số điện thoại từ ForgotPasswordActivity
        phone = getIntent().getStringExtra("phone");
        tvPhoneHint.setText("Mã đã được gửi đến số: " + phone);

        // Xác nhận OTP
        btnVerify.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();

            if (TextUtils.isEmpty(otp)) {
                etOtp.setError("Vui lòng nhập mã OTP");
                return;
            }
            if (otp.length() != 6) {
                etOtp.setError("Mã OTP phải đủ 6 số");
                return;
            }
            if (!otp.equals(FAKE_OTP)) {
                etOtp.setError("Mã OTP không đúng");
                return;
            }

            // OTP đúng → chuyển sang đặt lại mật khẩu
            Intent intent = new Intent(this, ResetPasswordActivity.class);
            intent.putExtra("phone", phone);
            startActivity(intent);
            finish();
        });

        // Gửi lại mã
        tvResend.setOnClickListener(v ->
                Toast.makeText(this, "Đã gửi lại mã: " + FAKE_OTP, Toast.LENGTH_LONG).show()
        );

        // Quay lại
        tvGoBack.setOnClickListener(v -> finish());
    }
}
