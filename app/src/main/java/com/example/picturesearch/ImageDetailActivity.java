package com.example.picturesearch;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ImageDetailActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);


        // Получаем переданные данные
        Intent intent = getIntent();
        if (intent != null) {
            // Извлекаем объект ImageItem из Intent
            ImageItem imageItem = (ImageItem) intent.getSerializableExtra("imageItem");

            if (imageItem != null) {
                // Создаем новый экземпляр ImageDetailFragment с передачей объекта ImageItem
                ImageDetailFragment fragment = ImageDetailFragment.newInstance(imageItem);

                // Заменяем текущий фрагмент на ImageDetailFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();

            }
        }
    }
}
