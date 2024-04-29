package com.example.picturesearch.detailfragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.picturesearch.database.ImageItem;
import com.example.picturesearch.pager.ImagePagerAdapter;
import com.example.picturesearch.R;

import java.util.List;

public class ImageDetailActivity extends AppCompatActivity {

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        // Получаем переданные данные
        Intent intent = getIntent();
        if (intent != null) {
            List<ImageItem> imageItems = (List<ImageItem>) getIntent().getSerializableExtra("imageItems");
            int position = getIntent().getIntExtra("position", 0);
            if (imageItems != null && !imageItems.isEmpty()) {
                ViewPager2 viewPager = findViewById(R.id.viewPager);
                ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(this, imageItems); // передаем позицию в адаптер
                viewPager.setAdapter(pagerAdapter);
                viewPager.setCurrentItem(position, false);
            }
        }
    }
}
