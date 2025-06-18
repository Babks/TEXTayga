package com.example.textayga;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// Активность для отображения детальной информации о лекарстве
public class PillDetailsActivity extends AppCompatActivity {

    // Элементы интерфейса
    private ImageView progressBarImage;  // Индикатор прогресса
    private TextView pillDescriptionLabel;  // Заголовок описания
    private TextView progressText;  // Текст прогресса
    private TextView pillName;  // Название лекарства
    private TextView pillDosage;  // Количество приемов
    private TextView pillFrequency;  // Периодичность приема
    private TextView pillPortions;  // Дозировка
    private TextView pillDescription;  // Описание лекарства

    private SharedPreferences prefs;  // Хранилище данных

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_details);

        // Инициализация всех View элементов
        Button btnHomepage = findViewById(R.id.buttonHomepage);
        Button btnCalandar = findViewById(R.id.buttonCalandar);
        pillDescriptionLabel = findViewById(R.id.pillDescriptionLabel);
        progressBarImage = findViewById(R.id.progressBarImage);
        progressText = findViewById(R.id.progressText);
        pillName = findViewById(R.id.pillName);
        pillDosage = findViewById(R.id.pillDosage);
        pillFrequency = findViewById(R.id.pillFrequency);
        pillPortions = findViewById(R.id.pillPortions);
        pillDescription = findViewById(R.id.pillDescription);

        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Обработчики нажатий кнопок
        btnHomepage.setOnClickListener(v -> {
            startActivity(new Intent(this, MainMenu.class));
            finish();
        });

        btnCalandar.setOnClickListener(v -> {
            startActivity(new Intent(this, Calandar.class));
            finish();
        });

        // Получение данных о лекарстве из Intent
        MainMenu.Pill pill = (MainMenu.Pill) getIntent().getSerializableExtra("pill");
        if (pill != null) {
            displayPillDetails(pill);
        }
    }

    // Отображение детальной информации о лекарстве
    // @param pill - объект лекарства для отображения
    private void displayPillDetails(MainMenu.Pill pill) {
        // Основная информация
        pillName.setText(pill.name);
        pillDosage.setText("Всего приемов: " + getTotalDosages(pill));
        pillPortions.setText("Дозировка: " + PillUtils.getPillCountString(pill.count));

        // Периодичность приема
        String frequency = getFrequency(pill);
        pillFrequency.setText("Периодичность: " + frequency);

        // Расчет и отображение прогресса
        int progress = calculateProgress(pill);
        updateProgressBar(progress);
        progressText.setText("Прогресс курса: " + progress + "%");

        // Дополнительная информация
        if (!pill.description.isEmpty()) {
            pillDescription.setText(pill.description);
            pillDescription.setVisibility(View.VISIBLE);
            pillDescriptionLabel.setVisibility(View.VISIBLE);
        } else {
            pillDescription.setVisibility(View.GONE);
            pillDescriptionLabel.setVisibility(View.GONE);
        }
    }

    // Обновление индикатора прогресса
    // @param percent - процент выполнения (0-100)
    private void updateProgressBar(int percent) {
        int level = Math.min(10, percent / 10);
        String drawableName = "progress_bar" + level;
        int resId = getResources().getIdentifier(drawableName, "drawable", getPackageName());

        if (resId != 0) {
            progressBarImage.setImageResource(resId);
        } else {
            Log.e("ProgressBar", "Image not found: " + drawableName);
            progressBarImage.setImageResource(R.drawable.progress_bar0);
        }
    }

    // Получение информации о периодичности приема
    // @param pill - объект лекарства
    // @return строка с днями недели или "разовое"
    private String getFrequency(MainMenu.Pill pill) {
        String daysKey = "pill_days_" + pill.name;
        String daysString = prefs.getString(daysKey, "");

        if (!daysString.isEmpty()) {
            return formatDaysOfWeek(daysString);
        }

        Set<Integer> weekDays = getPillWeekDays(pill);

        if (weekDays.isEmpty()) {
            return "разовое";
        }

        return formatDaysOfWeek(weekDays);
    }

    // Получение дней недели для приема лекарства
    // @param pill - объект лекарства
    // @return множество дней недели (Calendar.DAY_OF_WEEK)
    private Set<Integer> getPillWeekDays(MainMenu.Pill pill) {
        Set<Integer> weekDays = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        Map<String, ?> allEntries = prefs.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("pill_") && !key.startsWith("pill_status_") && !key.startsWith("pill_days_")) {
                try {
                    String pillJson = entry.getValue().toString();
                    if (pillJson.contains("\"name\":\"" + pill.name + "\"")) {
                        String[] parts = key.split("_");
                        if (parts.length >= 2) {
                            Date date = sdf.parse(parts[1]);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            weekDays.add(cal.get(Calendar.DAY_OF_WEEK));
                        }
                    }
                } catch (Exception e) {
                    Log.e("Frequency", "Error parsing pill data", e);
                }
            }
        }
        return weekDays;
    }

    // Форматирование дней недели из строки
    // @param daysString - строка с кодами дней (например "1,2,3")
    // @return отформатированная строка (например "Пн, Вт, Ср")
    private String formatDaysOfWeek(String daysString) {
        String[] dayCodes = daysString.split(",");
        StringBuilder daysBuilder = new StringBuilder();

        for (String code : dayCodes) {
            switch (code.trim()) {
                case "1": daysBuilder.append("Пн, "); break;
                case "2": daysBuilder.append("Вт, "); break;
                case "3": daysBuilder.append("Ср, "); break;
                case "4": daysBuilder.append("Чт, "); break;
                case "5": daysBuilder.append("Пт, "); break;
                case "6": daysBuilder.append("Сб, "); break;
                case "7": daysBuilder.append("Вс, "); break;
            }
        }

        if (daysBuilder.length() > 0) {
            daysBuilder.delete(daysBuilder.length()-2, daysBuilder.length());
            return daysBuilder.toString();
        }
        return "разовое";
    }

    // Форматирование дней недели из множества
    // @param weekDays - множество дней недели
    // @return отформатированная строка (например "Пн, Ср, Пт")
    private String formatDaysOfWeek(Set<Integer> weekDays) {
        StringBuilder freq = new StringBuilder();

        if (weekDays.contains(Calendar.MONDAY)) freq.append("Пн, ");
        if (weekDays.contains(Calendar.TUESDAY)) freq.append("Вт, ");
        if (weekDays.contains(Calendar.WEDNESDAY)) freq.append("Ср, ");
        if (weekDays.contains(Calendar.THURSDAY)) freq.append("Чт, ");
        if (weekDays.contains(Calendar.FRIDAY)) freq.append("Пт, ");
        if (weekDays.contains(Calendar.SATURDAY)) freq.append("Сб, ");
        if (weekDays.contains(Calendar.SUNDAY)) freq.append("Вс, ");

        if (freq.length() > 0) {
            freq.delete(freq.length()-2, freq.length());
            return freq.toString();
        }
        return "разовое";
    }

    // Расчет прогресса выполнения курса лечения
    // @param pill - объект лекарства
    // @return процент выполнения (0-100)
    private int calculateProgress(MainMenu.Pill pill) {
        int taken = 0;
        int totalPlanned = 0;
        int totalCompletedOrPassed = 0;
        String currentDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());

        Map<String, ?> allEntries = prefs.getAll();

        // Считаем все запланированные приёмы
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("pill_") && !key.startsWith("pill_status_") &&
                    !key.startsWith("pill_days_") && entry.getValue().toString().contains(pill.name)) {
                totalPlanned++;
            }
        }

        // Считаем завершённые или пропущенные приёмы
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("pill_status_") && key.contains(pill.name)) {
                try {
                    String[] parts = key.split("_");
                    if (parts.length >= 4) {
                        String pillDate = parts[3]; // Дата в формате dd.MM.yyyy

                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                        Date date = sdf.parse(pillDate);
                        Date today = sdf.parse(currentDate);

                        // Если дата приёма уже прошла
                        if (!date.after(today)) {
                            totalCompletedOrPassed++;
                            if ("taken".equals(entry.getValue())) {
                                taken++;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("ProgressCalc", "Error parsing date: " + key, e);
                }
            }
        }

        if (totalPlanned == 0) return 0;

        // Для разовых приёмов
        if (totalPlanned == 1 && totalCompletedOrPassed == 1) {
            return taken == 1 ? 100 : 0;
        }

        // Для курсов с несколькими приёмами
        double progress = (double) taken / totalPlanned * 100;
        int roundedProgress = (int) Math.round(progress);
        return Math.min(100, roundedProgress);
    }

    // Получение общего количества запланированных приемов
    // @param pill - объект лекарства
    // @return количество запланированных приемов
    private int getTotalDosages(MainMenu.Pill pill) {
        int count = 0;
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith("pill_") && entry.getValue().toString().contains(pill.name)) {
                count++;
            }
        }
        return count;
    }
}