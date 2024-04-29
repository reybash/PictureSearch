package com.example.picturesearch.detailfragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.picturesearch.ImageDownloader;
import com.example.picturesearch.R;
import com.example.picturesearch.FavoriteImageManager;
import com.example.picturesearch.database.ImageItem;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;

public class ImageDetailFragment extends Fragment {

    ImageButton favoriteButton;
    ImageButton downloadButton;
    FavoriteImageManager favoriteImageManager;
    ImageDownloader imageDownloader;

    // Пустой конструктор обязателен
    public ImageDetailFragment() {
    }

    // Метод для создания нового экземпляра фрагмента с передачей данных
    public static ImageDetailFragment newInstance(ImageItem imageItem) {
        ImageDetailFragment fragment = new ImageDetailFragment();

        Bundle args = new Bundle();
        args.putSerializable("imageItem", imageItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        favoriteImageManager = new FavoriteImageManager();
        imageDownloader = new ImageDownloader(requireContext());
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
                String imageUrl = imageItem.getImageUrl();
                String imageTitle = imageItem.getTitle();

                Picasso.get().load(imageUrl).into(imageView);
                titleTextView.setText(imageTitle);
                sourceTextView.setText(imageItem.getSourceUrl());
                sizeTextView.setText(MessageFormat.format("Resolution: {0}x{1}", imageItem.getWidth(), imageItem.getHeight()));
                double kByteSize = imageItem.getByteSize() / 1024.0;
                byte_sizeTextView.setText(String.format("Size: %.2f KB", kByteSize));

                favoriteImageManager.checkFavorites(imageUrl, favoriteButton);

                favoriteButton.setOnClickListener(v -> favoriteImageManager.toggleFavoriteStatus(imageItem, favoriteButton));
                downloadButton.setOnClickListener(v -> imageDownloader.downloadImage(imageUrl, imageTitle));
            }
        }
    }
}
