package com.example.picturesearch;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.MessageFormat;

public class ImageDetailFragment extends Fragment {

    ImageButton favoriteButton;
    ImageButton downloadButton;

    private ImageItemDao imageItemDao;

    // Пустой конструктор обязателен
    public ImageDetailFragment() {
        // Required empty public constructor
    }

    // Метод для создания нового экземпляра фрагмента с передачей данных
    public static ImageDetailFragment newInstance(ImageItem imageItem) {
        ImageDetailFragment fragment = new ImageDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("imageItem", (Serializable) imageItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        imageItemDao = DatabaseManager.getImageItemDao();
        return inflater.inflate(R.layout.fragment_image_detail, container, false);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Находим представления
        ImageView imageView = view.findViewById(R.id.imageView);
        TextView titleTextView = view.findViewById(R.id.titleTextView);
        TextView sourceTextView = view.findViewById(R.id.sourceTextView);
        TextView sizeTextView = view.findViewById(R.id.sizeTextView);
        TextView byte_sizeTextView = view.findViewById(R.id.byte_sizeTextView);

        favoriteButton = view.findViewById(R.id.favoriteButton);
        downloadButton = view.findViewById(R.id.downloadButton);

        // Получаем переданные данные
        Bundle args = getArguments();
        if (args != null) {
            ImageItem imageItem = (ImageItem) args.getSerializable("imageItem");
            if (imageItem != null) {
                Picasso.get().load(imageItem.getImageUrl()).into(imageView);
                titleTextView.setText(imageItem.getTitle());
                sourceTextView.setText(imageItem.getSourceUrl());
                sizeTextView.setText(MessageFormat.format("Resolution: {0}x{1}", imageItem.getWidth(), imageItem.getHeight()));
                double kByteSize = imageItem.getByteSize() / 1024.0;
                byte_sizeTextView.setText(String.format("Size: %.2f KB", kByteSize));

                new CheckFavoritesTask(favoriteButton, imageItem.getImageUrl()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                favoriteButton.setOnClickListener(v -> toggleFavoriteStatus(imageItem, favoriteButton));

                // Обработчик события для кнопки скачивания
                downloadButton.setOnClickListener(v -> downloadImage(imageItem));


            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class CheckFavoritesTask extends AsyncTask<Void, Void, Boolean> {
        private final ImageButton favoriteButton;
        private final String imageUrl;

        public CheckFavoritesTask(ImageButton favoriteButton, String imageUrl) {
            this.favoriteButton = favoriteButton;
            this.imageUrl = imageUrl;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return imageItemDao.existsByImageUrl(imageUrl);
        }

        @Override
        protected void onPostExecute(Boolean isFavorite) {
            // Установка иконки кнопки избранного в соответствии с результатом проверки
            favoriteButton.setImageResource(isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        }
    }


    @SuppressLint("StaticFieldLeak")
    private void toggleFavoriteStatus(ImageItem currentItem, ImageButton favoriteButton) {
        // Выполним операции с базой данных в фоновом потоке
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                String imageUrl = currentItem.getImageUrl();
                if (imageItemDao.existsByImageUrl(imageUrl)) {
                    imageItemDao.deleteByImageUrl(imageUrl);
                    return false; // Картинка была удалена из избранного
                } else {
                    ImageItem imageItem = new ImageItem(imageUrl,
                            currentItem.getTitle(),
                            currentItem.getSourceUrl(),
                            currentItem.getHeight(),
                            currentItem.getWidth(),
                            currentItem.getByteSize());
                    imageItemDao.insert(imageItem);
                    return true; // Картинка была добавлена в избранное
                }
            }

            @Override
            protected void onPostExecute(Boolean isAddedToFavorite) {
                // Обновляем UI на основе результата операции с базой данных
                if (isAddedToFavorite) {
                    favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
                    Toast.makeText(requireContext(), "Image added to Favorites", Toast.LENGTH_SHORT).show();
                } else {
                    favoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
                    Toast.makeText(requireContext(), "Image removed from Favorites", Toast.LENGTH_SHORT).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void downloadImage(ImageItem imageItem) {
        String imageUrl = imageItem.getImageUrl();
        String title = imageItem.getTitle();
        String fileName = title + ".jpg";

        ContentResolver resolver = requireContext().getContentResolver(); // Получение контекста активити
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Picasso.get().load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(android.graphics.Bitmap bitmap, Picasso.LoadedFrom from) {
                try {
                    assert imageUri != null;
                    OutputStream outputStream = resolver.openOutputStream(imageUri);
                    assert outputStream != null;
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                    Log.d("ImageSave", "Image saved successfully to: " + imageUri);
                    Toast.makeText(getContext(), "Image saved to Pictures folder", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e("ImageSave", "Failed to save image: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Toast.makeText(getContext(), "Failed to download image", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // Not needed for download
            }
        });
    }
}
