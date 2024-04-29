package com.example.picturesearch.database;

import android.support.annotation.NonNull;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "image_items")
public class ImageItem implements Serializable {
    @PrimaryKey
    private @androidx.annotation.NonNull String imageUrl;
    private final String title;
    private final String sourceUrl;
    private final int height;
    private final int width;
    private final int byteSize;

    public ImageItem(@NonNull String imageUrl, String title, String sourceUrl, int height, int width, int byteSize) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.sourceUrl = sourceUrl;
        this.height = height;
        this.width = width;
        this.byteSize = byteSize;
    }

    @NonNull
    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getByteSize() {
        return byteSize;
    }

}
