package com.midterm.contactapp_v2.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactRepository {

    private final ContactDao contactDao;
    private final ExecutorService executorService;

    public ContactRepository(Context context) {
        contactDao = ContactDatabase.getInstance(context).contactDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Contact>> getAllContacts() {
        return contactDao.getAllContacts();
    }

    public LiveData<List<Contact>> searchContacts(String query) {
        return contactDao.searchContacts("%" + query + "%");
    }

    public LiveData<Contact> getContactById(long id) {
        return contactDao.getContactById(id);
    }

    public void insert(Contact contact) {
        executorService.execute(() -> contactDao.insert(contact));
    }

    public void update(Contact contact) {
        executorService.execute(() -> contactDao.update(contact));
    }

    public void delete(Contact contact) {
        executorService.execute(() -> contactDao.delete(contact));
    }
}