package com.example.picturesearch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.picturesearch.detailfragment.ImageDetailActivity;
import com.example.picturesearch.database.ImageItem;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.List;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder> {

    private final Context context;
    private List<ImageItem> imageItems;
    private ImageItem chosenItem;
    private ImageButton current_favoriteButton;
    private final FavoriteImageManager favoriteImageManager;

    public ImageListAdapter(Context context, List<ImageItem> imageItems) {
        this.context = context;
        this.imageItems = imageItems;
        this.favoriteImageManager = new FavoriteImageManager();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setImageItems(List<ImageItem> imageItems) {
        this.imageItems = imageItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return imageItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageItem imageItem = imageItems.get(position);
        String itemUrl = imageItem.getImageUrl();

        Picasso.get()
                .load(imageItem.getImageUrl())
                .resize(1000, 1000)
                .centerInside()
                .placeholder(R.drawable.download)
                .into(holder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Картинка успешно загружена
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onError(Exception e) {
                        imageItems.remove(imageItem);
                        notifyDataSetChanged();
                    }
                });

        holder.imageView.setOnClickListener(v -> {
            chosenItem = imageItems.get(position);
            current_favoriteButton = holder.favoriteButton;

            Intent intent = new Intent(context, ImageDetailActivity.class);
            intent.putExtra("imageItems", (Serializable) imageItems);
            intent.putExtra("position", position);
            context.startActivity(intent);
        });

        favoriteImageManager.checkFavorites(itemUrl, holder.favoriteButton);

        holder.favoriteButton.setOnClickListener(v -> favoriteImageManager.toggleFavoriteStatus(imageItem, holder.favoriteButton));
    }

    public void checkFavoriteStatus() {
        if (chosenItem != null) {
            String chosenItemUrl = chosenItem.getImageUrl();
            favoriteImageManager.checkFavorites(chosenItemUrl, current_favoriteButton);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton favoriteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }
}
