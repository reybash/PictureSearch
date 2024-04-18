package com.example.picturesearch;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

public class ImageListAdapter extends BaseAdapter {

    private final Context context;
    private List<ImageItem> imageItems;
    private ListView listView;
    ImageItemDao imageItemDao;


    static class ViewHolder {
        ImageButton favoriteButton;
        ImageView imageView;
        TextView titleTextView;
        ImageButton downloadButton;
    }

    public ImageListAdapter(Context context, List<ImageItem> imageItems, ImageItemDao imageItemDao) {
        this.context = context;
        this.imageItems = imageItems;
        this.imageItemDao = imageItemDao;
    }

    public void setImageItems(List<ImageItem> imageItems) {
        this.imageItems = imageItems;
        notifyDataSetChanged();
        // Восстановить позицию прокрутки
        if (listView != null) {
            int startPosition = 0;
            listView.smoothScrollToPosition(startPosition);
        }
    }

    public void setListView(ListView listView) {
        this.listView = listView;
    }

    @Override
    public int getCount() {
        return imageItems.size();
    }

    @Override
    public Object getItem(int position) {
        return imageItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"ViewHolder", "WrongViewCast", "StaticFieldLeak"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.titleTextView = convertView.findViewById(R.id.titleTextView);
            holder.downloadButton = convertView.findViewById(R.id.downloadButton);
            holder.favoriteButton = convertView.findViewById(R.id.favoriteButton);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Picasso.get().load(imageItems.get(position).getImageUrl()).resize(1000, 1000).centerInside().into(holder.imageView);
        holder.titleTextView.setText(imageItems.get(position).getTitle());
        new CheckFavoritesTask(holder.favoriteButton, imageItems.get(position).getImageUrl()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        ImageItem currentItem = imageItems.get(position);

        // Задайте обработчик нажатия на изображение
        holder.imageView.setOnClickListener(v -> {
            // Создайте новый экземпляр ImageDetailFragment и передайте ему информацию о текущем элементе
            Intent intent = new Intent(context, ImageDetailActivity.class);
            // Добавляем объект ImageItem в Intent
            intent.putExtra("imageItem", (Serializable) currentItem);
            // Запускаем новое Activity с помощью Intent
            context.startActivity(intent);
        });

        // При нажатии на заголовок открываем соответствующую гиперссылку в браузере
        holder.titleTextView.setOnClickListener(v -> {
            String url = currentItem.getSourceUrl();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        });

        // При нажатии на кнопку скачивания выполняем процесс скачивания изображения
        holder.downloadButton.setOnClickListener(v -> downloadImage(position));

        // Установите слушатель на кнопку избранного
        holder.favoriteButton.setOnClickListener(v -> {
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
                        holder.favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
                        Toast.makeText(context, "Image added to Favorites", Toast.LENGTH_SHORT).show();
                    } else {
                        holder.favoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
                        Toast.makeText(context, "Image removed from Favorites", Toast.LENGTH_SHORT).show();
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        });


        return convertView;
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


    // Метод для скачивания изображения
    private void downloadImage(int position) {
        String imageUrl = imageItems.get(position).getImageUrl();
        String title = imageItems.get(position).getTitle();
        String fileName = title + ".jpg";

        ContentResolver resolver = context.getContentResolver();
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
                    Toast.makeText(context, "Image saved to Pictures folder", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e("ImageSave", "Failed to save image: " + e.getMessage());
                    Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // Not needed for download
            }
        });
    }

}
