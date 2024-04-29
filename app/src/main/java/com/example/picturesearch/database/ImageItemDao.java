package com.example.picturesearch.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ImageItemDao {
    @Query("SELECT * FROM image_items")
    List<ImageItem> getAll();

    @Insert
    void insert(ImageItem imageItem);

    @Query("DELETE FROM image_items WHERE imageUrl = :imageUrl")
    void deleteByImageUrl(String imageUrl);


    @Query("SELECT COUNT(*) FROM image_items WHERE imageUrl = :imageUrl")
    int countByImageUrl(String imageUrl);

    default boolean existsByImageUrl(String imageUrl) {
        return countByImageUrl(imageUrl) > 0;
    }
}
