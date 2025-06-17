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
import androidx.cardview.widget.CardView;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PillDetailsActivity extends AppCompatActivity {

    private ImageView progressBarImage;
    private TextView pillDescriptionLabel;
    private TextView progressText, pillName, pillDosage, pillFrequency, pillPortions, pillDescription;
    private SharedPreferences prefs;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_details);

        // Инициализация всех View
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

        btnHomepage.setOnClickListener(v -> {
            startActivity(new Intent(this, MainMenu.class));
            finish();
        });

        btnCalandar.setOnClickListener(v -> {
            startActivity(new Intent(this, Calandar.class));
            finish();
        });

        // Получение данных о лекарстве
        MainMenu.Pill pill = (MainMenu.Pill) getIntent().getSerializableExtra("pill");
        if (pill != null) {
            displayPillDetails(pill);
        }
    }

    private void displayPillDetails(MainMenu.Pill pill) {
        // Основная информация
        pillName.setText(pill.name);
        pillDosage.setText("Всего приемов: " + getTotalDosages(pill));
        pillPortions.setText("Дозировка: " + PillUtils.getPillCountString(pill.count));

        // Периодичность
        String frequency = getFrequency(pill);
        pillFrequency.setText("Периодичность: " + frequency);

        // Прогресс
        int progress = calculateProgress(pill);
        updateProgressBar(progress);
        progressText.setText("Прогресс курса: " + progress + "%");

        // Дополнительная информация
        if (!pill.description.isEmpty()) {
            pillDescription.setText(pill.description); // Только текст без префикса
            pillDescription.setVisibility(View.VISIBLE);
            pillDescriptionLabel.setVisibility(View.VISIBLE);
        } else {
            pillDescription.setVisibility(View.GONE);
            pillDescriptionLabel.setVisibility(View.GONE);
        }
    }

    // Остальные методы остаются без изменений
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

    private int calculateProgress(MainMenu.Pill pill) {
        int taken = 0;
        int total = 0;
        Map<String, ?> allEntries = prefs.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();

            if (key.startsWith("pill_status_") && key.contains(pill.name)) {
                total++;
                if ("taken".equals(entry.getValue())) {
                    taken++;
                }
            }

            if (key.startsWith("pill_") && !key.startsWith("pill_status_") && !key.startsWith("pill_days_")) {
                String pillJson = entry.getValue().toString();
                if (pillJson.contains("\"name\":\"" + pill.name + "\"")) {
                    total++;
                }
            }
        }

        if (total == 0) return 0;
        return Math.min(100, (taken * 100) / total);
    }

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