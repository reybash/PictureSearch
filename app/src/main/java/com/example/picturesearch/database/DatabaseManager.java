package com.example.picturesearch.database;

import android.content.Context;

import androidx.room.Room;

public class DatabaseManager {

    private static FavoriteImagesDatabase favoriteImagesDatabase;

    public static void initialize(Context context) {
        if (favoriteImagesDatabase == null) {
            favoriteImagesDatabase = Room.databaseBuilder(context.getApplicationContext(),
                            FavoriteImagesDatabase.class, "like_images.db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
    }

    public static ImageItemDao getImageItemDao() {
        if (favoriteImagesDatabase == null) {
            throw new IllegalStateException("DatabaseManager.initialize() must be called before accessing the database.");
        }
        return favoriteImagesDatabase.imageItemDao();
    }
}
