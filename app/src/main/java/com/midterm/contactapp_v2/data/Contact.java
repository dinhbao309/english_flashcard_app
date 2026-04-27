package com.midterm.contactapp_v2.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "contacts")
public class Contact {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private String phone;
    private String email;
    private String avatarPath;

    public Contact() {
    }

    public Contact(String name, String phone, String email, String avatarPath) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.avatarPath = avatarPath;
    }

    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAvatarPath() { return avatarPath; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
}