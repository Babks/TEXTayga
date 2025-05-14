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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.example.textayga.PillUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainMenu extends AppCompatActivity {
    private LinearLayout linearLayoutPills;
    private SharedPreferences prefs;
    private Gson gson = new Gson();
    private TextView textViewProgress;
    private Button btnCalandar;
    private Button btnAddPill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.main_menu);

        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        cleanOldMarks();

        // Проверяем и сбрасываем прогресс при необходимости
        try {
            DailyProgressManager.checkAndResetProgress(this);
        } catch (Exception e) {
            Log.e("MainMenu", "Error resetting progress", e);
        }

        initViews();
        migratePreferencesToLong();
        updateProgress();
        loadAllPills();
        setupButtonListeners();
    }

    private void initViews() {
        linearLayoutPills = findViewById(R.id.linearLayoutPills);
        textViewProgress = findViewById(R.id.textViewProgress);
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        btnCalandar = findViewById(R.id.buttonCalandar);
        btnAddPill = findViewById(R.id.buttonAddPill);
    }

    private void setupButtonListeners() {
        btnAddPill.setOnClickListener(v -> {
            startActivity(new Intent(MainMenu.this, Add_Pill.class));
            finish();
        });

        btnCalandar.setOnClickListener(v -> {
            startActivity(new Intent(this, Calandar.class));
            finish();
        });
    }

    private void loadAllPills() {
        try {
            linearLayoutPills.removeAllViews();
            Map<String, ?> allEntries = prefs.getAll();
            List<Pill> pills = new ArrayList<>();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());

            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getKey().startsWith("pill_")) {
                    try {
                        Object value = entry.getValue();
                        String pillJson = (value instanceof Integer) ? String.valueOf(value) : (String) value;
                        Pill pill = gson.fromJson(pillJson, Pill.class);

                        if (pill.date == null) {
                            Log.e("PillLoad", "Pill date is null for key: " + entry.getKey());
                            continue;
                        }

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

            Collections.sort(pills, (p1, p2) -> p2.date.compareTo(p1.date));

            for (Pill pill : pills) {
                addPillView(pill);
            }

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
        LinearLayout pillContainer = new LinearLayout(this);
        pillContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        pillContainer.setLayoutParams(params);
        pillContainer.setBackgroundResource(R.drawable.rectangle_border_image2);
        pillContainer.setPadding(20, 20, 20, 20);

        // Название лекарства
        TextView nameView = new TextView(this);
        nameView.setText(pill.name);
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        nameView.setTextColor(Color.parseColor("#2F281F"));
        nameView.setTypeface(null, Typeface.BOLD);
        pillContainer.addView(nameView);

        // Дата и время
        TextView dateView = new TextView(this);
        dateView.setText(pill.date);
        dateView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        dateView.setTextColor(Color.parseColor("#382912"));
        dateView.setPadding(0, 8, 0, 0);
        pillContainer.addView(dateView);

        // Количество таблеток
        TextView countView = new TextView(this);
        countView.setText(PillUtils.getPillCountString(pill.count));
        countView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        countView.setTextColor(Color.parseColor("#382912"));
        countView.setPadding(0, 8, 0, 0);
        pillContainer.addView(countView);

        // Описание (если есть)
        if (!pill.description.isEmpty()) {
            TextView descView = new TextView(this);
            descView.setText(pill.description);
            descView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            descView.setTextColor(Color.parseColor("#382912"));
            descView.setPadding(0, 8, 0, 0);
            pillContainer.addView(descView);
        }

        // Кнопки принятия/отклонения
        addActionButtons(pillContainer, pill);
        linearLayoutPills.addView(pillContainer);
    }

    private void addActionButtons(LinearLayout container, Pill pill) {
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
        Button button = new Button(this);
        button.setBackgroundResource(backgroundRes);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(29), dpToPx(28));
        params.setMargins(dpToPx(8), 0, 0, 0);
        button.setLayoutParams(params);
        button.setOnClickListener(listener);
        return button;
    }

    private void handlePillAction(Pill pill, boolean isTaken) {
        try {
            long count = parsePillCount(pill.count);
            updatePillStats(count, isTaken);
            removePillFromPrefs(pill);

            // Сохраняем статус таблетки
            String statusKey = "pill_status_" + pill.name + "_" + pill.date;
            prefs.edit()
                    .putString(statusKey, isTaken ? "taken" : "missed")
                    .apply();

            refreshUI();
        } catch (Exception e) {
            Log.e("PillAction", "Error handling pill", e);
        }
    }

    private long parsePillCount(String countStr) {
        try {
            return Math.min(1000, Long.parseLong(countStr.replaceAll("[^0-9]", "")));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void updatePillStats(long count, boolean isTaken) {
        SharedPreferences.Editor editor = prefs.edit();
        String key = isTaken ? "taken_pills_new" : "missed_pills_new";
        long current = prefs.getLong(key, 0);
        editor.putLong(key, current + count);
        editor.apply();
    }

    private void refreshUI() {
        runOnUiThread(() -> {
            loadAllPills();
            updateProgress();
        });
    }

    private void updateProgress() {
        try {
            long taken = prefs.getLong("taken_pills_new", 0);
            long missed = prefs.getLong("missed_pills_new", 0);
            long total = taken + missed;

            if (total > 0) {
                int takenPercent = (int)((taken * 100) / total);
                updateProgressViews(taken, missed, takenPercent);
            } else {
                updateProgressViews(0, 0, 0); // Нулевой прогресс
            }
        } catch (Exception e) {
            Log.e("ProgressUpdate", "Error updating progress", e);
            textViewProgress.setText("Ошибка расчета прогресса");
            textViewProgress.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.progress_bar0, 0, 0, 0);
        }
    }

    private void updateProgressViews(long taken, long missed, int takenPercent) {
        if (taken + missed == 0) {
            // Если таблеток не было сегодня
            textViewProgress.setText("Сегодня таблеток не принималось");
            textViewProgress.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.progress_bar0, 0, 0, 0);
        } else {
            // Если таблетки были - показываем проценты
            String progressText = String.format(Locale.getDefault(),
                    "Таблеток принято: %d%%\nПропущено: %d%%",
                    takenPercent, 100 - takenPercent);
            textViewProgress.setText(progressText);

            // Обновляем картинку прогресса
            int drawableRes = getProgressDrawable(takenPercent);
            textViewProgress.setCompoundDrawablesWithIntrinsicBounds(
                    drawableRes, 0, 0, 0);
        }
    }

    private int getProgressDrawable(int percent) {
        int level = Math.min(10, Math.max(0, percent / 10));
        String drawableName = "progress_bar" + (level == 0 ? "0" : level);
        return getResources().getIdentifier(
                drawableName, "drawable", getPackageName());
    }

    private void removePillFromPrefs(Pill pill) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("marked_" + pill.name + "_" + pill.date, true);
        editor.apply();
    }

    private void cleanOldMarks() {
        if (prefs == null) return;

        SharedPreferences.Editor editor = prefs.edit();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            if (entry.getKey().startsWith("marked_")) {
                String[] parts = entry.getKey().split("_");
                if (parts.length >= 4 && !parts[3].equals(currentDate)) {
                    editor.remove(entry.getKey());
                }
            }
        }
        editor.apply();
    }

    private void migratePreferencesToLong() {
        try {
            if (prefs.contains("taken_pills") && !prefs.contains("taken_pills_new")) {
                SharedPreferences.Editor editor = prefs.edit();

                // Безопасное чтение старых значений
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
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    public static class Pill implements Serializable {
        String name;
        String date;
        String count;
        String description;

        public Pill(String name, String date, String count, String description) {
            this.name = name;
            this.date = date;
            this.count = count;
            this.description = description;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Проверяем снова при возвращении в приложение
        DailyProgressManager.checkAndResetProgress(this);
        updateProgress();
    }
}