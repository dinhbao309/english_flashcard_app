package com.midterm.contactapp_v2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.midterm.contactapp_v2.R;
import com.midterm.contactapp_v2.data.Contact;
import com.midterm.contactapp_v2.viewmodel.ContactViewModel;
import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactDetailActivity extends AppCompatActivity {

    private ContactViewModel viewModel;
    private CircleImageView ivAvatar;
    private TextView tvName, tvPhone, tvEmail;
    private Button buttonEdit, buttonDelete;
    private Toolbar toolbar;
    private long contactId;
    private Contact currentContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        contactId = getIntent().getLongExtra("contact_id", -1);
        if (contactId == -1) {
            Toast.makeText(this, "Error loading contact", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupClickListeners();
        loadContactData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupClickListeners() {
        buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ContactDetailActivity.this, AddEditContactActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("contact_id", contactId);
            startActivity(intent);
        });

        buttonDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void loadContactData() {
        viewModel.getContactById(contactId).observe(this, contact -> {
            if (contact != null) {
                currentContact = contact;
                tvName.setText(contact.getName());
                tvPhone.setText(contact.getPhone());
                tvEmail.setText(contact.getEmail());
                toolbar.setTitle(contact.getName());

                if (contact.getAvatarPath() != null && !contact.getAvatarPath().isEmpty()) {
                    Glide.with(this)
                            .load(new File(contact.getAvatarPath()))
                            .placeholder(R.drawable.ic_default_avatar)
                            .into(ivAvatar);
                }
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete " + currentContact.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteContact())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteContact() {
        viewModel.deleteContact(currentContact);
        Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (contactId != -1) {
            loadContactData();
        }
    }
}