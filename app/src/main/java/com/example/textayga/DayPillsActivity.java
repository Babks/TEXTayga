package com.example.textayga;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import android.widget.Toast;

// Отображает список таблеток на выбранный день, загружая данные из SharedPreferences
public class DayPillsActivity extends AppCompatActivity {
    private LinearLayout pillsLayout;
    private TextView dateTitle;
    private SharedPreferences prefs;
    private Gson gson = new Gson();
    private String storageDate; // Дата для фильтрации таблеток в формате "dd.MM.yyyy"

    Button btnCalandar;
    Button btnHomepage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Защита от наложения активностей при повторном запуске
        if (getIntent() != null && !getIntent().hasExtra("from_notification")) {
            getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        setContentView(R.layout.activity_day_pills);

        // Инициализация UI-элементов
        pillsLayout = findViewById(R.id.pillsLayout);
        dateTitle = findViewById(R.id.dateTitle);
        btnCalandar = findViewById(R.id.buttonCalandar);
        btnHomepage = findViewById(R.id.buttonHomepage);
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Обработка аппаратной кнопки "назад"
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToMainMenu();
            }
        });

        // Получаем выбранную пользователем дату
        String displayDate = getIntent().getStringExtra("selectedDate");
        storageDate = getIntent().getStringExtra("storageDate");

        dateTitle.setText(displayDate);
        loadDayPills(); // Загружаем таблетки за выбранную дату

        // Переход к календарю
        btnCalandar.setOnClickListener(v -> {
            startActivity(new Intent(this, Calandar.class));
            finish();
        });

        // Переход на главный экран
        btnHomepage.setOnClickListener(v -> {
            startActivity(new Intent(this, MainMenu.class));
            finish();
        });
    }

    // Возврат в главное меню
    private void navigateToMainMenu() {
        Intent intent = new Intent(this, MainMenu.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Загружает и отображает таблетки за выбранную дату
    private void loadDayPills() {
        pillsLayout.removeAllViews();
        Map<String, ?> allEntries = prefs.getAll();

        try {
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getKey().startsWith("pill_")) {
                    try {
                        Object value = entry.getValue();
                        // В SharedPreferences может храниться как JSON-строка, так и случайно сохранённое число
                        String pillJson = (value instanceof Integer) ? String.valueOf(value) : (String) value;
                        MainMenu.Pill pill = gson.fromJson(pillJson, MainMenu.Pill.class);

                        if (pill.date == null) {
                            Log.e("DayPills", "Pill date is null for key: " + entry.getKey());
                            continue;
                        }

                        // Сравниваем только дату без времени
                        String pillDateStr = pill.date.split(" ")[0];
                        if (pillDateStr.equals(storageDate)) {
                            addPillView(pill);
                        }
                    } catch (Exception e) {
                        Log.e("DayPills", "Error loading pill with key: " + entry.getKey(), e);
                    }
                }
            }

            // Если не найдено ни одной таблетки
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

    // Добавляет представление отдельной таблетки в список
    private void addPillView(MainMenu.Pill pill) {
        LinearLayout pillItem = new LinearLayout(this);
        pillItem.setOrientation(LinearLayout.VERTICAL);
        pillItem.setPadding(32, 24, 32, 24);
        pillItem.setBackgroundResource(R.drawable.pill_item_background);

        // Основной горизонтальный контейнер
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Контейнер с текстом (название, дата, описание)
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        // Название таблетки
        TextView nameView = new TextView(this);
        nameView.setText(pill.name);
        nameView.setTextSize(18);
        nameView.setTextColor(Color.parseColor("#2F281F"));
        nameView.setTypeface(null, Typeface.BOLD);
        textLayout.addView(nameView);

        // Дата и время приёма
        TextView dateView = new TextView(this);
        try {
            SimpleDateFormat srcFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            Date date = srcFormat.parse(pill.date);
            dateView.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(date));
        } catch (ParseException e) {
            dateView.setText(pill.date.split(" ")[0]); // fallback
        }
        dateView.setTextSize(14);
        dateView.setTextColor(Color.parseColor("#2F281F"));
        dateView.setPadding(0, 8, 0, 0);
        textLayout.addView(dateView);

        // Количество
        TextView countView = new TextView(this);
        countView.setText(PillUtils.getPillCountString(pill.count));
        countView.setTextSize(14);
        countView.setTextColor(Color.parseColor("#2F281F"));
        countView.setPadding(0, 8, 0, 0);
        textLayout.addView(countView);

        // Описание (если есть)
        if (!pill.description.isEmpty()) {
            TextView descView = new TextView(this);
            descView.setText(pill.description);
            descView.setTextSize(14);
            descView.setTextColor(Color.parseColor("#2F281F"));
            descView.setPadding(0, 8, 0, 16);
            textLayout.addView(descView);
        }

        contentLayout.addView(textLayout);

        // Добавляем иконку статуса: принята / пропущена
        addStatusIcon(contentLayout, pill);

        // Добавляем собранный элемент в список
        pillItem.addView(contentLayout);
        pillsLayout.addView(pillItem);
    }

    // Добавляет иконку статуса таблетки (принята / пропущена)
    private void addStatusIcon(LinearLayout parentLayout, MainMenu.Pill pill) {
        String statusKey = "pill_status_" + pill.name + "_" + pill.date;
        String status = prefs.getString(statusKey, null);

        // Если статус не задан, проверяем — не прошло ли уже время приёма
        if (status == null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                Date pillDate = sdf.parse(pill.date);
                Date now = new Date();

                if (pillDate.before(now)) {
                    status = "missed";
                    prefs.edit().putString(statusKey, status).apply();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (status != null) {
            ImageView statusIcon = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(24),
                    dpToPx(24));
            params.gravity = Gravity.CENTER_VERTICAL;
            statusIcon.setLayoutParams(params);

            // Устанавливаем иконку
            if (status.equals("taken")) {
                statusIcon.setImageResource(R.drawable.ic_check);
            } else if (status.equals("missed")) {
                statusIcon.setImageResource(R.drawable.ic_cross);
            }

            parentLayout.addView(statusIcon);
        }
    }

    // Преобразование dp в пиксели
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
