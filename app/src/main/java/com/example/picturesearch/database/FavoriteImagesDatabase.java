package com.example.picturesearch.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ImageItem.class}, version = 2)
public abstract class FavoriteImagesDatabase extends RoomDatabase {
    public abstract ImageItemDao imageItemDao();
}


