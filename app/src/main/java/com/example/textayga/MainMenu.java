package com.example.textayga;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.gson.Gson;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainMenu extends AppCompatActivity {
    private LinearLayout linearLayoutPills; // Контейнер для отображения списка таблеток
    private SharedPreferences prefs; // Хранилище пользовательских данных
    private Gson gson = new Gson(); // Библиотека для работы с JSON
    private TextView textViewProgress; // Поле для отображения прогресса пользователя
    private Button btnCalandar; // Кнопка перехода к календарю
    private Button btnAddPill; // Кнопка добавления новой таблетки

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Отключаем тёмную тему
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.main_menu);

        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE); // Инициализация настроек

        cleanOldMarks(); // Очистка устаревших отметок о приёме таблеток

        try {
            // Проверяем, нужно ли сбрасывать прогресс нового дня
            DailyProgressManager.checkAndResetProgress(this);
        } catch (Exception e) {
            Log.e("MainMenu", "Error resetting progress", e);
        }

        initViews(); // Инициализация интерфейса
        migratePreferencesToLong(); // Миграция старых значений прогресса к Long
        updateProgress(); // Обновление отображения прогресса
        loadAllPills(); // Загрузка списка актуальных таблеток
        setupButtonListeners(); // Установка слушателей на кнопки

        TextView textViewSpisok = findViewById(R.id.textViewSpisok);
        textViewSpisok.setOnClickListener(v -> {
            startActivity(new Intent(MainMenu.this, AllPillsActivity.class));
        });
    }

    private void initViews() {
        // Получаем ссылки на элементы интерфейса
        linearLayoutPills = findViewById(R.id.linearLayoutPills);
        textViewProgress = findViewById(R.id.textViewProgress);
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        btnCalandar = findViewById(R.id.buttonCalandar);
        btnAddPill = findViewById(R.id.buttonAddPill);
    }

    private void setupButtonListeners() {
        // Обработка кнопки добавления таблетки
        btnAddPill.setOnClickListener(v -> {
            startActivity(new Intent(MainMenu.this, Add_Pill.class));
            finish();
        });

        // Обработка кнопки перехода к календарю
        btnCalandar.setOnClickListener(v -> {
            startActivity(new Intent(this, Calandar.class));
            finish();
        });
    }

    private void loadAllPills() {
        try {
            linearLayoutPills.removeAllViews(); // Очищаем контейнер
            Map<String, ?> allEntries = prefs.getAll(); // Получаем все записи
            List<Pill> pills = new ArrayList<>();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String currentDate = dateFormat.format(new Date()); // Текущая дата

            // Перебираем все сохранённые таблетки
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getKey().startsWith("pill_")) {
                    try {
                        // Десериализуем таблетку
                        Object value = entry.getValue();
                        String pillJson = (value instanceof Integer) ? String.valueOf(value) : (String) value;
                        Pill pill = gson.fromJson(pillJson, Pill.class);

                        // Проверка даты таблетки
                        if (pill.date == null) {
                            Log.e("PillLoad", "Pill date is null for key: " + entry.getKey());
                            continue;
                        }

                        // Если таблетка на сегодня и ещё не принята
                        String pillDate = pill.date.split(" ")[0];
                        boolean isMarked = prefs.getBoolean("marked_" + pill.name + "_" + pill.date, false);

                        if (pillDate.equals(currentDate) && !isMarked) {
                            pills.add(pill);
                        }
                    } catch (Exception e) {
                        Log.e("PillLoad", "Error loading pill with key: " + entry.getKey(), e);
                    }
                }
            }

            // Сортировка по дате (по убыванию)
            Collections.sort(pills, (p1, p2) -> p2.date.compareTo(p1.date));

            // Добавляем таблетки в интерфейс
            for (Pill pill : pills) {
                addPillView(pill);
            }

            // Если таблеток нет
            if (pills.isEmpty()) {
                TextView emptyView = new TextView(this);
                emptyView.setText("На сегодня нет запланированных лекарств");
                emptyView.setTextSize(16);
                emptyView.setTextColor(Color.parseColor("#2F281F"));
                emptyView.setGravity(Gravity.CENTER);
                linearLayoutPills.addView(emptyView);
            }
        } catch (Exception e) {
            Log.e("MainMenu", "Error in loadAllPills", e);
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
        }
    }

    private void addPillView(Pill pill) {
        // Создаём контейнер для таблетки
        LinearLayout pillContainer = new LinearLayout(this);
        pillContainer.setOrientation(LinearLayout.VERTICAL);
        pillContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        pillContainer.setPadding(20, 20, 20, 20);
        pillContainer.setBackgroundResource(R.drawable.rectangle_border_image2);

        // Название таблетки
        TextView nameView = new TextView(this);
        nameView.setText(pill.name);
        nameView.setTextSize(18);
        nameView.setTypeface(null, Typeface.BOLD);
        nameView.setTextColor(Color.parseColor("#2F281F"));
        pillContainer.addView(nameView);

        // Дата и время
        TextView dateView = new TextView(this);
        dateView.setText(pill.date);
        dateView.setTextSize(14);
        dateView.setTextColor(Color.parseColor("#382912"));
        dateView.setPadding(0, 8, 0, 0);
        pillContainer.addView(dateView);

        // Количество таблеток
        TextView countView = new TextView(this);
        countView.setText(PillUtils.getPillCountString(pill.count));
        countView.setTextSize(14);
        countView.setTextColor(Color.parseColor("#382912"));
        countView.setPadding(0, 8, 0, 0);
        pillContainer.addView(countView);

        // Описание, если есть
        if (!pill.description.isEmpty()) {
            TextView descView = new TextView(this);
            descView.setText(pill.description);
            descView.setTextSize(14);
            descView.setTextColor(Color.parseColor("#382912"));
            descView.setPadding(0, 8, 0, 0);
            pillContainer.addView(descView);
        }

        // Добавляем обработчик нажатия на контейнер
        pillContainer.setOnClickListener(v -> {
            Intent intent = new Intent(this, PillDetailsActivity.class);
            intent.putExtra("pill", pill);
            startActivity(intent);
        });

        // Добавляем кнопки принятия/отклонения
        addActionButtons(pillContainer, pill);
        linearLayoutPills.addView(pillContainer);
    }

    private void addActionButtons(LinearLayout container, Pill pill) {
        // Кнопки действий (принял/пропустил)
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonsLayout.setGravity(Gravity.END);

        Button btnCheck = createButton(R.drawable.green_circle_checkbox, v -> handlePillAction(pill, true));
        Button btnCross = createButton(R.drawable.red_circle_cross, v -> handlePillAction(pill, false));

        buttonsLayout.addView(btnCheck);
        buttonsLayout.addView(btnCross);
        container.addView(buttonsLayout);
    }

    private Button createButton(int backgroundRes, View.OnClickListener listener) {
        // Универсальный метод создания кнопки
        Button button = new Button(this);
        button.setBackgroundResource(backgroundRes);
        button.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(29), dpToPx(28)));
        button.setOnClickListener(listener);
        return button;
    }

    private void handlePillAction(Pill pill, boolean isTaken) {
        try {
            long count = parsePillCount(pill.count); // Получаем числовое значение из строки с количеством таблеток
            updatePillStats(count, isTaken); // Обновляем статистику в зависимости от действия
            removePillFromPrefs(pill); // Отмечаем таблетку как просмотренную

            // Сохраняем статус приёма таблетки (принята / пропущена)
            String statusKey = "pill_status_" + pill.name + "_" + pill.date;
            prefs.edit().putString(statusKey, isTaken ? "taken" : "missed").apply();

            refreshUI(); // Обновляем интерфейс, чтобы убрать таблетку из списка и пересчитать прогресс
        } catch (Exception e) {
            Log.e("PillAction", "Error handling pill", e);
        }
    }


    private long parsePillCount(String countStr) {
        // Пытаемся извлечь числовое значение из строки, ограничиваем значение максимумом в 1000
        try {
            return Math.min(1000, Long.parseLong(countStr.replaceAll("[^0-9]", "")));
        } catch (NumberFormatException e) {
            return 1; // Если не удалось распарсить — по умолчанию 1 таблетка
        }
    }


    private void updatePillStats(long count, boolean isTaken) {
        // Обновляем соответствующий счётчик
        SharedPreferences.Editor editor = prefs.edit();
        String key = isTaken ? "taken_pills_new" : "missed_pills_new";
        long current = prefs.getLong(key, 0);
        editor.putLong(key, current + count);
        editor.apply();
    }

    private void refreshUI() {
        // Перерисовываем интерфейс на главном потоке: обновляем список таблеток и прогресс
        runOnUiThread(() -> {
            loadAllPills();
            updateProgress();
        });
    }


    private void updateProgress() {
        try {
            // Получаем количество принятых и пропущенных таблеток из SharedPreferences
            long taken = prefs.getLong("taken_pills_new", 0); // Количество принятых таблеток
            long missed = prefs.getLong("missed_pills_new", 0); // Количество пропущенных таблеток
            long total = taken + missed; // Общее количество таблеток (принятых + пропущенных)

            if (total > 0) {
                // Рассчитываем процент принятых таблеток
                int takenPercent = (int)((taken * 100) / total);
                // Обновляем интерфейс с новым прогрессом
                updateProgressViews(taken, missed, takenPercent);
            } else {
                // Если нет таблеток, отображаем нулевой прогресс
                updateProgressViews(0, 0, 0);
            }
        } catch (Exception e) {
            // Логируем ошибку и отображаем сообщение об ошибке на экране
            Log.e("ProgressUpdate", "Error updating progress", e);
            textViewProgress.setText("Ошибка расчета прогресса"); // Текст ошибки
            // Отображаем иконку прогресса для ошибки
            textViewProgress.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.progress_bar0, 0, 0, 0);
        }
    }


    private void updateProgressViews(long taken, long missed, int takenPercent) {
        // Отображаем текст и иконку прогресса в зависимости от того, есть ли таблетки
        if (taken + missed == 0) {
            // Если таблетки не принимались, показываем сообщение о том, что ничего не было принято
            textViewProgress.setText("Сегодня таблеток не принималось");
            // Устанавливаем иконку прогресса как "нулевую"
            textViewProgress.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.progress_bar0, 0, 0, 0);
        } else {
            // Формируем строку с процентами принятых и пропущенных таблеток
            String progressText = String.format(Locale.getDefault(),
                    "Таблеток принято: %d%%\nПропущено: %d%%", takenPercent, 100 - takenPercent);
            textViewProgress.setText(progressText); // Отображаем строку с прогрессом

            // Получаем ресурс для иконки прогресса в зависимости от процента принятия таблеток
            int drawableRes = getProgressDrawable(takenPercent);
            // Устанавливаем иконку прогресса в текстовое поле
            textViewProgress.setCompoundDrawablesWithIntrinsicBounds(
                    drawableRes, 0, 0, 0);
        }
    }


    private int getProgressDrawable(int percent) {
        // Вычисляем уровень прогресса, который будет использоваться для выбора иконки
        // Уровень прогресса зависит от процента, но ограничен диапазоном от 0 до 10
        int level = Math.min(10, Math.max(0, percent / 10));

        // Формируем имя ресурса для иконки прогресса, например: progress_bar5
        String drawableName = "progress_bar" + (level == 0 ? "0" : level);

        // Получаем идентификатор ресурса с помощью имени drawable
        return getResources().getIdentifier(drawableName, "drawable", getPackageName());
    }


    private void removePillFromPrefs(Pill pill) {
        // Отмечаем таблетку как просмотренную (принятую или пропущенную)
        SharedPreferences.Editor editor = prefs.edit();

        // Ключ для отметки таблетки, состоит из имени таблетки и даты приёма
        // Строка "marked_" используется для идентификации просмотренных таблеток
        editor.putBoolean("marked_" + pill.name + "_" + pill.date, true);

        // Применяем изменения в SharedPreferences
        editor.apply();
    }


    private void cleanOldMarks() {
        // Удаляем устаревшие отметки о принятых/пропущенных таблетках
        if (prefs == null) return; // Если prefs не инициализирован, выходим из метода

        // Создаём редактор для редактирования SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();

        // Формируем текущую дату в формате "дд.мм.гггг"
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        // Перебираем все записи в SharedPreferences
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            // Проверяем, что ключ начинается с "marked_" (это значит, что это отметка о таблетке)
            if (entry.getKey().startsWith("marked_")) {
                // Разделяем ключ по символу "_", чтобы получить части ключа
                String[] parts = entry.getKey().split("_");

                // Если длина массива частей больше или равна 4 и дата не совпадает с текущей
                if (parts.length >= 4 && !parts[3].equals(currentDate)) {
                    // Удаляем устаревшую запись из SharedPreferences
                    editor.remove(entry.getKey());
                }
            }
        }

        // Применяем изменения в SharedPreferences
        editor.apply();
    }


    private void migratePreferencesToLong() {
        // Миграция с int на long для счётчиков прогресса
        try {
            if (prefs.contains("taken_pills") && !prefs.contains("taken_pills_new")) {
                SharedPreferences.Editor editor = prefs.edit();
                int taken = prefs.getInt("taken_pills", 0);
                int missed = prefs.getInt("missed_pills", 0);
                editor.putLong("taken_pills_new", taken);
                editor.putLong("missed_pills_new", missed);
                editor.remove("taken_pills");
                editor.remove("missed_pills");
                editor.apply();
            }
        } catch (Exception e) {
            Log.e("MainMenu", "Migration error", e);
        }
    }

    private int dpToPx(int dp) {
        // Конвертация dp в пиксели
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    public static class Pill implements Serializable {
        // Название таблетки
        String name;

        // Дата приёма таблетки
        String date;

        // Количество таблеток (в виде строки)
        String count;

        // Описание таблетки (например, инструкция или дополнительные примечания)
        String description;

        // Конструктор класса Pill, инициализирует поля с заданными значениями
        public Pill(String name, String date, String count, String description) {
            this.name = name;             // Инициализация названия таблетки
            this.date = date;             // Инициализация даты приёма
            this.count = count;           // Инициализация количества таблеток
            this.description = description; // Инициализация описания таблетки
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // При возврате на экран проверяем прогресс нового дня и обновляем отображение
        DailyProgressManager.checkAndResetProgress(this);
        updateProgress();
    }

}
