package com.example.picturesearch;

import android.widget.ImageButton;
import android.widget.Toast;

import com.example.picturesearch.R;
import com.example.picturesearch.database.DatabaseManager;
import com.example.picturesearch.database.ImageItem;
import com.example.picturesearch.database.ImageItemDao;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FavoriteImageManager {

    private final ImageItemDao imageItemDao;
    private final Executor executor;

    public FavoriteImageManager() {
        imageItemDao = DatabaseManager.getImageItemDao();
        executor = Executors.newCachedThreadPool();
    }

    public void checkFavorites(String imageUrl, ImageButton favoriteButton) {
        executor.execute(() -> {
            boolean isFavorite = imageItemDao.existsByImageUrl(imageUrl);
            favoriteButton.post(() -> favoriteButton.setImageResource(isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off));
        });
    }

    public void toggleFavoriteStatus(ImageItem imageItem, ImageButton favoriteButton) {
        executor.execute(() -> {
            String imageUrl = imageItem.getImageUrl();
            boolean existsInFavorites = imageItemDao.existsByImageUrl(imageUrl);

            if (existsInFavorites) {
                imageItemDao.deleteByImageUrl(imageUrl);
            } else {
                imageItemDao.insert(imageItem);
            }

            boolean isAddedToFavorite = !existsInFavorites;
            int messageResId = isAddedToFavorite ? R.string.image_added_to_favorites : R.string.image_removed_from_favorites;
            int iconResId = isAddedToFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off;

            favoriteButton.post(() -> {
                favoriteButton.setImageResource(iconResId);
                Toast.makeText(favoriteButton.getContext(), messageResId, Toast.LENGTH_SHORT).show();
            });
        });
    }
}