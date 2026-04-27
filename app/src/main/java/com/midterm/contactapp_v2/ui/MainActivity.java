package com.midterm.contactapp_v2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.midterm.contactapp_v2.R;
import com.midterm.contactapp_v2.data.Contact;
import com.midterm.contactapp_v2.viewmodel.ContactViewModel;

public class MainActivity extends AppCompatActivity {

    private ContactViewModel viewModel;
    private ContactAdapter adapter;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
            return;
        }

        try {
            viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerViewContacts);
        fabAddContact = findViewById(R.id.fabAddContact);

        setupRecyclerView();
        setupFloatingActionButton();
        observeContacts();
    }

    private void setupRecyclerView() {
        try {
            adapter = new ContactAdapter(contact -> {
                Intent intent = new Intent(MainActivity.this, ContactDetailActivity.class);
                intent.putExtra("contact_id", contact.getId());
                startActivity(intent);
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            recyclerView.setHasFixedSize(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupFloatingActionButton() {
        fabAddContact.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, AddEditContactActivity.class);
                intent.putExtra("mode", "add");
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar.make(v, "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void observeContacts() {
        try {
            viewModel.getContacts().observe(this, contacts -> {
                if (contacts != null) {
                    adapter.updateContacts(contacts);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    viewModel.setSearchQuery(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    viewModel.setSearchQuery(newText);
                    return true;
                }
            });
        }

        return true;
    }
}