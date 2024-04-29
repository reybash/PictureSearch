package com.example.picturesearch;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.io.OutputStream;

public class ImageDownloader {

    private final Context context;

    public ImageDownloader(Context context) {
        this.context = context;
    }

    public void downloadImage(String imageUrl, String title) {
        String fileName = title + ".jpg";

        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Picasso.get().load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                try {
                    if (imageUri != null) {
                        OutputStream outputStream = resolver.openOutputStream(imageUri);
                        if (outputStream != null) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            outputStream.close();
                            Log.d("ImageSave", "Image saved successfully to: " + imageUri);
                            Toast.makeText(context, "Image saved to Pictures folder", Toast.LENGTH_SHORT).show();
                        }
                    }
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
