// TaskInfoActivity.java

package com.example.picturesearch;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TaskInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_info);

        // Находим TextView
        TextView textView = findViewById(R.id.taskInfoTextView);

        // Устанавливаем текст
        textView.setText(getString(R.string.task_info_text));
    }
}
