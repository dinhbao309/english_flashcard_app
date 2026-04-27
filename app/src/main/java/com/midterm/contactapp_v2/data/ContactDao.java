package com.midterm.contactapp_v2.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface ContactDao {

    @Insert
    void insert(Contact contact);

    @Update
    void update(Contact contact);

    @Delete
    void delete(Contact contact);

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    LiveData<List<Contact>> getAllContacts();

    @Query("SELECT * FROM contacts WHERE name LIKE :searchQuery OR phone LIKE :searchQuery ORDER BY name ASC")
    LiveData<List<Contact>> searchContacts(String searchQuery);

    @Query("SELECT * FROM contacts WHERE id = :contactId")
    LiveData<Contact> getContactById(long contactId);
}