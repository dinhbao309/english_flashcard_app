package com.midterm.contactapp_v2.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.midterm.contactapp_v2.R;
import com.midterm.contactapp_v2.data.Contact;
import com.midterm.contactapp_v2.viewmodel.ContactViewModel;
import java.io.File;
import de.hdodenhof.circleimageview.CircleImageView;

public class AddEditContactActivity extends AppCompatActivity {

    private ContactViewModel viewModel;
    private CircleImageView ivAvatar;
    private TextInputLayout tilName, tilPhone, tilEmail;
    private EditText editTextName, editTextPhone, editTextEmail;
    private Button buttonSave, buttonCancel;
    private Toolbar toolbar;
    private FloatingActionButton fabCamera;

    private boolean isEditMode = false;
    private long contactId = -1;
    private String currentAvatarPath = null;
    private Uri cameraImageUri = null;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bắt lỗi crash khi inflate layout
        try {
            setContentView(R.layout.activity_add_edit_contact);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Layout error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "ViewModel error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupClickListeners();
        checkIntentData();
    }

    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            ivAvatar = findViewById(R.id.ivAvatar);
            fabCamera = findViewById(R.id.fabCamera);
            tilName = findViewById(R.id.tilName);
            tilPhone = findViewById(R.id.tilPhone);
            tilEmail = findViewById(R.id.tilEmail);
            editTextName = findViewById(R.id.editTextName);
            editTextPhone = findViewById(R.id.editTextPhone);
            editTextEmail = findViewById(R.id.editTextEmail);
            buttonSave = findViewById(R.id.buttonSave);
            buttonCancel = findViewById(R.id.buttonCancel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupToolbar() {
        try {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkIntentData() {
        try {
            String mode = getIntent().getStringExtra("mode");
            if ("edit".equals(mode)) {
                isEditMode = true;
                contactId = getIntent().getLongExtra("contact_id", -1);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Edit Contact");
                }
                buttonSave.setText("Update");
                loadContactData();
            } else {
                isEditMode = false;
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Add New Contact");
                }
                buttonSave.setText("Save");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadContactData() {
        try {
            viewModel.getContactById(contactId).observe(this, contact -> {
                if (contact != null) {
                    editTextName.setText(contact.getName());
                    editTextPhone.setText(contact.getPhone());
                    editTextEmail.setText(contact.getEmail());
                    currentAvatarPath = contact.getAvatarPath();

                    if (currentAvatarPath != null && !currentAvatarPath.isEmpty()) {
                        File avatarFile = new File(currentAvatarPath);
                        if (avatarFile.exists()) {
                            Glide.with(this)
                                    .load(avatarFile)
                                    .placeholder(R.drawable.ic_default_avatar)
                                    .error(R.drawable.ic_default_avatar)
                                    .into(ivAvatar);
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupClickListeners() {
        buttonCancel.setOnClickListener(v -> finish());
        buttonSave.setOnClickListener(v -> saveContact());
        fabCamera.setOnClickListener(v -> checkAndRequestPermissions());
    }

    private void checkAndRequestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CODE);
                } else {
                    openCamera();
                }
            } else {
                openCamera();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot open camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to take photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        try {
            File photoFile = new File(getCacheDir(), "avatar_" + System.currentTimeMillis() + ".jpg");
            cameraImageUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", photoFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && cameraImageUri != null) {
                currentAvatarPath = cameraImageUri.getPath();
                Glide.with(this)
                        .load(new File(currentAvatarPath))
                        .into(ivAvatar);
                Toast.makeText(this, "Photo saved", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveContact() {
        try {
            String name = editTextName.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();

            if (name.isEmpty()) {
                tilName.setError("Name is required");
                return;
            } else {
                tilName.setError(null);
            }

            if (phone.isEmpty()) {
                tilPhone.setError("Phone number is required");
                return;
            } else {
                tilPhone.setError(null);
            }

            if (email.isEmpty()) {
                tilEmail.setError("Email is required");
                return;
            } else {
                tilEmail.setError(null);
            }

            Contact contact;
            if (isEditMode) {
                contact = new Contact(name, phone, email, currentAvatarPath);
                contact.setId(contactId);
                viewModel.updateContact(contact);
                Toast.makeText(this, "Contact updated!", Toast.LENGTH_SHORT).show();
            } else {
                contact = new Contact(name, phone, email, currentAvatarPath);
                viewModel.insertContact(contact);
                Toast.makeText(this, "Contact saved!", Toast.LENGTH_SHORT).show();
            }

            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}