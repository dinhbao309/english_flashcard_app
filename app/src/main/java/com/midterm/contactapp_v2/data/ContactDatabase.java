package com.midterm.contactapp_v2.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Contact.class}, version = 1, exportSchema = false)
public abstract class ContactDatabase extends RoomDatabase {

    public abstract ContactDao contactDao();

    private static volatile ContactDatabase INSTANCE;

    public static ContactDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ContactDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            ContactDatabase.class,
                            "contact_database_v2"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}