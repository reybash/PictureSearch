package com.example.picturesearch.pager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.picturesearch.detailfragment.ImageDetailFragment;
import com.example.picturesearch.database.ImageItem;

import java.util.List;

public class ImagePagerAdapter extends FragmentStateAdapter {

    private final List<ImageItem> imageItems;

    public ImagePagerAdapter(@NonNull FragmentActivity fragmentActivity, List<ImageItem> imageItems) {
        super(fragmentActivity);
        this.imageItems = imageItems;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Создаем новый экземпляр ImageDetailFragment с передачей объекта ImageItem
        return ImageDetailFragment.newInstance(imageItems.get(position));
    }

    @Override
    public int getItemCount() {
        return imageItems.size();
    }
}
