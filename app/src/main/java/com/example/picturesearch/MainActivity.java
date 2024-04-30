package com.example.picturesearch;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.picturesearch.database.DatabaseManager;
import com.example.picturesearch.database.ImageItem;
import com.example.picturesearch.database.ImageItemDao;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private boolean isLoading = false;
    private static final int LAST_PAGE = 25;
    private int currentPage; // Текущая страница данных
    private String query;
    private ImageItemDao imageItemDao;
    private ImageSearchHelper imageSearchHelper;
    private EditText editTextQuery;
    private Button buttonSearch;
    private ImageListAdapter adapter;
    private ImageListAdapter favoriteAdapter;
    private List<ImageItem> imageItems;
    private Executor executor;
    private RecyclerView recyclerView;
    private RecyclerView favoriteRecyclerView;
    private RelativeLayout headerLayout;

    private final Map<Integer, Runnable> navigationActions = new HashMap<Integer, Runnable>() {{
        put(R.id.action_search, () -> switchToSearchFragment());
        put(R.id.action_favorites, () -> switchToFavoritesFragment());
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        DatabaseManager.initialize(getApplicationContext());
        imageItemDao = DatabaseManager.getImageItemDao();
        imageSearchHelper = new ImageSearchHelper();

        editTextQuery = findViewById(R.id.searchEditText);
        buttonSearch = findViewById(R.id.searchButton);
        headerLayout = findViewById(R.id.headerLayout);
        imageItems = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ImageListAdapter(this, imageItems);
        recyclerView.setAdapter(adapter);

        favoriteRecyclerView = findViewById(R.id.favorite_recyclerView);
        favoriteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoriteAdapter = new ImageListAdapter(MainActivity.this, new ArrayList<>());
        favoriteRecyclerView.setAdapter(favoriteAdapter);

        executor = Executors.newCachedThreadPool();

        recyclerView.setVisibility(View.VISIBLE);
        favoriteRecyclerView.setVisibility(View.GONE);

        Button buttonAuthor = findViewById(R.id.button_author);
        Button buttonTask = findViewById(R.id.taskButton);

        buttonAuthor.setOnClickListener(v -> {
            // Создать намерение для перехода к новой активности
            Intent intent = new Intent(MainActivity.this, AuthorActivity.class);

            // Запустить новую активность
            startActivity(intent);
        });

        buttonTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TaskInfoActivity.class);
            startActivity(intent);
        });

        buttonSearch.setOnClickListener(v -> {
            query = editTextQuery.getText().toString();
            if (!query.isEmpty()) {
                findViewById(R.id.headerLayout).setVisibility(View.GONE);
                currentPage = 1;
                imageItems.clear();
                executeCustomSearchTask(query, currentPage, adapter);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert layoutManager != null;
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && (currentPage <= LAST_PAGE)) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                        loadMoreItems();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновить данные в активности
        adapter.checkFavoriteStatus();
        favoriteAdapter.checkFavoriteStatus();
    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        int id = item.getItemId();
        Runnable action = navigationActions.get(id);
        if (action != null) {
            action.run();
            return true;
        }
        return false;
    };

    @SuppressLint("NotifyDataSetChanged")
    private void switchToSearchFragment() {
        recyclerView.setVisibility(View.VISIBLE);
        favoriteRecyclerView.setVisibility(View.GONE);
        headerLayout.setVisibility(View.GONE);

        editTextQuery.setVisibility(View.VISIBLE);
        buttonSearch.setVisibility(View.VISIBLE);

        adapter.setImageItems(imageItems);
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void switchToFavoritesFragment() {
        recyclerView.setVisibility(View.GONE);
        favoriteRecyclerView.setVisibility(View.VISIBLE);
        editTextQuery.setVisibility(View.GONE);
        buttonSearch.setVisibility(View.GONE);
        headerLayout.setVisibility(View.VISIBLE);


        executor.execute(() -> {
            // Выполняем получение данных в фоновом потоке
            List<ImageItem> favoriteImages = imageItemDao.getAll();

            // Обновляем UI в основном потоке
            runOnUiThread(() -> {
                favoriteAdapter.setImageItems(favoriteImages);
                favoriteAdapter.notifyDataSetChanged();
            });
        });
    }

    private void loadMoreItems() {
        isLoading = true;
        int nextPage = ++currentPage;
        executeCustomSearchTask(query, nextPage, adapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void executeCustomSearchTask(String query, int pageNumber, ImageListAdapter adapter) {
        executor.execute(() -> {
            List<ImageItem> newImageItems = imageSearchHelper.searchImages(query, pageNumber);
            // Обновляем интерфейс в основном потоке
            runOnUiThread(() -> {
                imageItems.addAll(newImageItems);
                adapter.notifyDataSetChanged();
                isLoading = false;
            });
        });
    }
}

