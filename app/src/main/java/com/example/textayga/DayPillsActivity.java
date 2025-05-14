package com.example.textayga;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class DayPillsActivity extends AppCompatActivity {
    private LinearLayout pillsLayout;
    private TextView dateTitle;
    private SharedPreferences prefs;
    private Gson gson = new Gson();
    private String storageDate; // Храним дату в формате "dd.MM.yyyy"

    Button btnCalandar;
    Button btnHomepage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Добавьте этот флаг, чтобы предотвратить наслоение активностей
        if (getIntent() != null && !getIntent().hasExtra("from_notification")) {
            getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        setContentView(R.layout.activity_day_pills);

        pillsLayout = findViewById(R.id.pillsLayout);
        dateTitle = findViewById(R.id.dateTitle);
        btnCalandar = findViewById(R.id.buttonCalandar);
        btnHomepage = findViewById(R.id.buttonHomepage);
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToMainMenu();
            }
        });

        // Получаем даты из интента
        String displayDate = getIntent().getStringExtra("selectedDate");
        storageDate = getIntent().getStringExtra("storageDate");

        dateTitle.setText(displayDate);
        loadDayPills();

        // Кнопки навигации
        btnCalandar.setOnClickListener(v -> {
            startActivity(new Intent(this, Calandar.class));
            finish();
        });


        btnHomepage.setOnClickListener(v -> {
            startActivity(new Intent(this, MainMenu.class));
            finish();
        });
    }

    private void navigateToMainMenu() {
        Intent intent = new Intent(this, MainMenu.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void loadDayPills() {
        pillsLayout.removeAllViews();
        Map<String, ?> allEntries = prefs.getAll();

        try {
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getKey().startsWith("pill_")) {
                    try {
                        Object value = entry.getValue();
                        String pillJson = (value instanceof Integer) ? String.valueOf(value) : (String) value;
                        MainMenu.Pill pill = gson.fromJson(pillJson, MainMenu.Pill.class);

                        if (pill.date == null) {
                            Log.e("DayPills", "Pill date is null for key: " + entry.getKey());
                            continue;
                        }

                        String pillDateStr = pill.date.split(" ")[0];
                        if (pillDateStr.equals(storageDate)) {
                            addPillView(pill);
                        }
                    } catch (Exception e) {
                        Log.e("DayPills", "Error loading pill with key: " + entry.getKey(), e);
                    }
                }
            }

            if (pillsLayout.getChildCount() == 0) {
                TextView emptyView = new TextView(this);
                emptyView.setText("Нет лекарств на этот день");
                emptyView.setTextSize(16);
                emptyView.setTextColor(Color.parseColor("#2F281F"));
                emptyView.setGravity(Gravity.CENTER);
                pillsLayout.addView(emptyView);
            }
        } catch (Exception e) {
            Log.e("DayPills", "Error loading pills", e);
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
        }
    }

    private void addPillView(MainMenu.Pill pill) {
        LinearLayout pillItem = new LinearLayout(this);
        pillItem.setOrientation(LinearLayout.VERTICAL);
        pillItem.setPadding(32, 24, 32, 24);
        pillItem.setBackgroundResource(R.drawable.pill_item_background);

        // Основной контейнер для содержимого
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Контейнер для текстовой информации
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        // Название лекарства
        TextView nameView = new TextView(this);
        nameView.setText(pill.name);
        nameView.setTextSize(18);
        nameView.setTextColor(Color.parseColor("#2F281F"));
        nameView.setTypeface(null, Typeface.BOLD);
        textLayout.addView(nameView);

        // Дата и время
        TextView dateView = new TextView(this);
        try {
            SimpleDateFormat srcFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            Date date = srcFormat.parse(pill.date);
            dateView.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(date));
        } catch (ParseException e) {
            dateView.setText(pill.date.split(" ")[0]);
        }
        dateView.setTextSize(14);
        dateView.setTextColor(Color.parseColor("#2F281F"));
        dateView.setPadding(0, 8, 0, 0);
        textLayout.addView(dateView);

        // Количество таблеток
        TextView countView = new TextView(this);
        countView.setText(PillUtils.getPillCountString(pill.count));
        countView.setTextSize(14);
        countView.setTextColor(Color.parseColor("#2F281F"));
        countView.setPadding(0, 8, 0, 0);
        textLayout.addView(countView);

        // Описание
        if (!pill.description.isEmpty()) {
            TextView descView = new TextView(this);
            descView.setText(pill.description);
            descView.setTextSize(14);
            descView.setTextColor(Color.parseColor("#2F281F"));
            descView.setPadding(0, 8, 0, 16);
            textLayout.addView(descView);
        }

        // Добавляем текстовый контейнер в основной
        contentLayout.addView(textLayout);

        // Добавляем иконку статуса (если есть)
        addStatusIcon(contentLayout, pill);

        // Добавляем основной контейнер в элемент
        pillItem.addView(contentLayout);

        pillsLayout.addView(pillItem);
    }

    private void addStatusIcon(LinearLayout parentLayout, MainMenu.Pill pill) {
        // Проверяем статус таблетки
        String statusKey = "pill_status_" + pill.name + "_" + pill.date;
        String status = prefs.getString(statusKey, null);

        // Если статус не установлен, проверяем, не устарела ли таблетка
        if (status == null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                Date pillDate = sdf.parse(pill.date);
                Date now = new Date();

                // Если время приема таблетки прошло, отмечаем как пропущенную
                if (pillDate.before(now)) {
                    status = "missed";
                    prefs.edit().putString(statusKey, status).apply();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Если статус есть, добавляем соответствующую иконку
        if (status != null) {
            ImageView statusIcon = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(24),
                    dpToPx(24));
            params.gravity = Gravity.CENTER_VERTICAL;
            statusIcon.setLayoutParams(params);

            if (status.equals("taken")) {
                statusIcon.setImageResource(R.drawable.ic_check);
            } else if (status.equals("missed")) {
                statusIcon.setImageResource(R.drawable.ic_cross);
            }

            parentLayout.addView(statusIcon);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}