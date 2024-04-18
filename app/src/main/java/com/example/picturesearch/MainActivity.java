package com.example.picturesearch;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "AIzaSyAePgadUyv3tpyNbiUOY4Ql9tVhWKM_Q9w";
    private static final String CX = "14b75d7e1762c40c8";

    ImageItemDao imageItemDao;

    private EditText editTextQuery;
    private Button buttonSearch;
    private ImageListAdapter adapter;
    private List<ImageItem> imageItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        DatabaseManager.initialize(getApplicationContext());
        imageItemDao = DatabaseManager.getImageItemDao();

        editTextQuery = findViewById(R.id.searchEditText);
        buttonSearch = findViewById(R.id.searchButton);
        ListView listView = findViewById(R.id.imageView);
        imageItems = new ArrayList<>();

        adapter = new ImageListAdapter(this, new ArrayList<>(), imageItemDao);
        listView.setAdapter(adapter);
        adapter.setListView(listView);

        buttonSearch.setOnClickListener(v -> {
            String query = editTextQuery.getText().toString();
            if (!query.isEmpty()) {
                new CustomSearchTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
            }
        });

    }


    private final NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int id = item.getItemId();

                if (id == R.id.action_search) {
                    // Переключиться на поиск
                    switchToSearchFragment();
                    return true;
                } else if (id == R.id.action_favorites) {
                    // Переключиться на избранное
                    switchToFavoritesFragment();
                    return true;
                }

                return false;
            };

    private void switchToSearchFragment() {
        // Ваш код для переключения на фрагмент поиска
        editTextQuery.setVisibility(View.VISIBLE);
        buttonSearch.setVisibility(View.VISIBLE);

        adapter.setImageItems(imageItems);
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("StaticFieldLeak")
    private void switchToFavoritesFragment() {
        // Ваш код для переключения на фрагмент избранных
        editTextQuery.setVisibility(View.GONE);
        buttonSearch.setVisibility(View.GONE);

        new AsyncTask<Void, Void, List<ImageItem>>() {
            @Override
            protected List<ImageItem> doInBackground(Void... voids) {
                return imageItemDao.getAll();
            }

            @Override
            protected void onPostExecute(List<ImageItem> favoriteImages) {
                // Передайте избранные картинки в адаптер и обновите ListView (или другой вид)
                adapter.setImageItems(favoriteImages);
                adapter.notifyDataSetChanged();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class CustomSearchTask extends AsyncTask<String, Void, List<ImageItem>> {

        @Override
        protected List<ImageItem> doInBackground(String... strings) {
            String query = strings[0];
            List<ImageItem> newImageItems = new ArrayList<>();
            try {
                URL url = new URL("https://www.googleapis.com/customsearch/v1?key=" + API_KEY +
                        "&cx=" + CX + "&q=" + query + "&searchType=image");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray items = jsonResponse.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    String link = item.getString("link");
                    String title = item.optString("title", "");

                    JSONObject imageObject = item.getJSONObject("image");
                    String contextLink = imageObject.optString("contextLink", "");
                    int height = imageObject.optInt("height", 0);
                    int width = imageObject.optInt("width", 0);
                    int byteSize = imageObject.optInt("byteSize", 0);

                    ImageItem imageItem = new ImageItem(link, title, contextLink, height, width, byteSize);
                    newImageItems.add(imageItem);
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e("CustomSearchTask", "Error: " + e.getMessage());
                e.printStackTrace();
            }
            return newImageItems;
        }

        @Override
        protected void onPostExecute(List<ImageItem> newImageItems) {
            if (newImageItems != null && !newImageItems.isEmpty()) {
                imageItems.clear(); // Очистить текущий список перед добавлением новых элементов
                imageItems.addAll(newImageItems); // Добавить новые элементы в текущий список
                adapter.setImageItems(imageItems);
                adapter.notifyDataSetChanged();
            } else {
                Log.d("CustomSearchTask", "No images found");
            }
        }
    }
}
