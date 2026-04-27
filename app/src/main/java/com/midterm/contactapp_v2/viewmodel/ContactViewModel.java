package com.midterm.contactapp_v2.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.midterm.contactapp_v2.data.Contact;
import com.midterm.contactapp_v2.data.ContactRepository;
import java.util.List;

public class ContactViewModel extends AndroidViewModel {

    private final ContactRepository repository;
    private final MediatorLiveData<List<Contact>> contacts = new MediatorLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private LiveData<List<Contact>> currentSource;

    public ContactViewModel(Application application) {
        super(application);
        repository = new ContactRepository(application);

        searchQuery.observeForever(query -> {
            if (currentSource != null) {
                contacts.removeSource(currentSource);
            }

            if (query == null || query.trim().isEmpty()) {
                currentSource = repository.getAllContacts();
            } else {
                currentSource = repository.searchContacts(query);
            }

            contacts.addSource(currentSource, contacts::setValue);
        });
    }

    public LiveData<List<Contact>> getContacts() {
        return contacts;
    }

    public LiveData<Contact> getContactById(long id) {
        return repository.getContactById(id);
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void insertContact(Contact contact) {
        repository.insert(contact);
    }

    public void updateContact(Contact contact) {
        repository.update(contact);
    }

    public void deleteContact(Contact contact) {
        repository.delete(contact);
    }
}