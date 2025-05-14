package com.example.textayga;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.DatePicker;
import android.widget.TimePicker;
import java.util.concurrent.TimeUnit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;

public class Add_Pill extends AppCompatActivity {
    private EditText namePill, datePill, countPill, descPill, editTextDaysCount;
    private Calendar selectedDateTime = Calendar.getInstance();
    private SharedPreferences prefs;
    private Gson gson = new Gson();
    private CheckBox checkBoxRange;
    private LinearLayout rangeLayout;
    private CheckBox[] dayCheckboxes;
    private static final String ACTION_REQUEST_SCHEDULE_EXACT_ALARM =
            "android.settings.REQUEST_SCHEDULE_EXACT_ALARM";

    Button btnCalandar;
    Button btnHomepage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.add_pill);

        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Инициализация полей ввода
        namePill = findViewById(R.id.EditTextPillName);
        datePill = findViewById(R.id.EditTextPillDate);
        countPill = findViewById(R.id.EditTextPillCount);
        descPill = findViewById(R.id.EditTextPillInf);
        btnCalandar = findViewById(R.id.buttonCalandar);
        btnHomepage = findViewById(R.id.buttonHomepage);

        // Инициализация элементов для диапазона
        checkBoxRange = findViewById(R.id.checkBoxRange);
        rangeLayout = findViewById(R.id.rangeLayout);
        editTextDaysCount = findViewById(R.id.editTextDaysCount);

        dayCheckboxes = new CheckBox[]{
                findViewById(R.id.monday),
                findViewById(R.id.tuesday),
                findViewById(R.id.wednesday),
                findViewById(R.id.thursday),
                findViewById(R.id.friday),
                findViewById(R.id.saturday),
                findViewById(R.id.sunday)
        };

        checkBoxRange.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rangeLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        datePill.setFocusable(false);
        datePill.setOnClickListener(v -> showDateTimePicker());

        // Обработчик кнопки добавления
        Button btnAddPill = findViewById(R.id.buttonAddPill);
        btnAddPill.setOnClickListener(v -> {
            if (!checkAllFieldsFilled()) {
                Toast.makeText(Add_Pill.this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }
            savePill();
        });

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

    private boolean checkAllFieldsFilled() {
        boolean basicFieldsFilled = !namePill.getText().toString().trim().isEmpty()
                && !datePill.getText().toString().trim().isEmpty()
                && !countPill.getText().toString().trim().isEmpty()
                && !descPill.getText().toString().trim().isEmpty();

        if (checkBoxRange.isChecked()) {
            boolean atLeastOneDaySelected = false;
            for (CheckBox cb : dayCheckboxes) {
                if (cb.isChecked()) {
                    atLeastOneDaySelected = true;
                    break;
                }
            }
            return basicFieldsFilled && atLeastOneDaySelected &&
                    !editTextDaysCount.getText().toString().trim().isEmpty();
        }
        return basicFieldsFilled;
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(year, month, dayOfMonth);
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (view1, hourOfDay, minute) -> {
                                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedDateTime.set(Calendar.MINUTE, minute);
                                updateDateText();
                            },
                            selectedDateTime.get(Calendar.HOUR_OF_DAY),
                            selectedDateTime.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        datePill.setText(sdf.format(selectedDateTime.getTime()));
    }

    private void savePill() {
        if (checkBoxRange.isChecked()) {
            savePillRange();
        } else {
            saveSinglePill();
        }
    }

    private void saveSinglePill() {
        if (!validateDate()) return;

        try {
            String countStr = countPill.getText().toString().replaceAll("[^0-9]", "");
            long count = Math.min(Long.parseLong(countStr), 1000);

            SimpleDateFormat storageFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            String formattedDate = storageFormat.format(selectedDateTime.getTime());

            MainMenu.Pill newPill = new MainMenu.Pill(
                    namePill.getText().toString().trim(),
                    formattedDate,
                    String.valueOf(count),
                    descPill.getText().toString().trim()
            );

            savePillToPrefs(newPill, selectedDateTime.getTimeInMillis());
            scheduleNotification(newPill, selectedDateTime.getTimeInMillis()); // Добавьте эту строку

            new AppPreferences(this).setFirstRunCompleted();

            Toast.makeText(this, "Лекарство добавлено", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainMenu.class));
            finish();
        } catch (Exception e) {
            Log.e("SavePill", "Error saving pill", e);
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePillRange() {
        if (!validateDate()) return;

        // Проверка количества дней
        int daysCount;
        try {
            daysCount = Integer.parseInt(editTextDaysCount.getText().toString());
            if (daysCount < 1) {
                Toast.makeText(this, "Количество дней должно быть положительным", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректное количество дней", Toast.LENGTH_SHORT).show();
            return;
        }

        // Получаем выбранные дни недели
        List<Integer> selectedDays = new ArrayList<>();
        if (dayCheckboxes[0].isChecked()) selectedDays.add(Calendar.MONDAY);
        if (dayCheckboxes[1].isChecked()) selectedDays.add(Calendar.TUESDAY);
        if (dayCheckboxes[2].isChecked()) selectedDays.add(Calendar.WEDNESDAY);
        if (dayCheckboxes[3].isChecked()) selectedDays.add(Calendar.THURSDAY);
        if (dayCheckboxes[4].isChecked()) selectedDays.add(Calendar.FRIDAY);
        if (dayCheckboxes[5].isChecked()) selectedDays.add(Calendar.SATURDAY);
        if (dayCheckboxes[6].isChecked()) selectedDays.add(Calendar.SUNDAY);

        if (selectedDays.isEmpty()) {
            Toast.makeText(this, "Выберите хотя бы один день недели", Toast.LENGTH_SHORT).show();
            return;
        }

        // Получаем выбранное время
        int hour = selectedDateTime.get(Calendar.HOUR_OF_DAY);
        int minute = selectedDateTime.get(Calendar.MINUTE);
        int savedPills = 0;

        // 1. Сначала проверяем сегодняшний день
        Calendar today = (Calendar) selectedDateTime.clone();
        today.set(Calendar.HOUR_OF_DAY, hour);
        today.set(Calendar.MINUTE, minute);
        today.set(Calendar.SECOND, 0);

        // Проверяем, выбран ли сегодняшний день в чекбоксах
        if (selectedDays.contains(today.get(Calendar.DAY_OF_WEEK))) {
            savePillForDate(today);
            savedPills++;

            // Если нужно только одно напоминание, завершаем
            if (savedPills >= daysCount) {
                Toast.makeText(this, "Добавлено " + savedPills + " напоминаний", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainMenu.class));
                finish();
                return;
            }
        }

        // 2. Затем добавляем остальные напоминания
        Calendar currentDate = (Calendar) today.clone();
        int daysAdded = 0;
        int maxAttempts = 1000; // Защита от бесконечного цикла

        while (savedPills < daysCount && daysAdded < maxAttempts) {
            currentDate.add(Calendar.DAY_OF_YEAR, 1);
            daysAdded++;

            // Проверяем, подходит ли текущий день
            if (selectedDays.contains(currentDate.get(Calendar.DAY_OF_WEEK))) {
                savePillForDate(currentDate);
                savedPills++;
            }
        }

        Toast.makeText(this, "Добавлено " + savedPills + " напоминаний", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainMenu.class));
        finish();
    }

    private boolean validateDate() {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        Calendar selectedDate = (Calendar) selectedDateTime.clone();
        selectedDate.set(Calendar.SECOND, 0);
        selectedDate.set(Calendar.MILLISECOND, 0);

        if (selectedDate.before(now)) {
            Toast.makeText(this, "Нельзя добавлять лекарства на прошедшую дату", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void savePillForDate(Calendar date) {
        try {
            String countStr = countPill.getText().toString().replaceAll("[^0-9]", "");
            long count = Math.min(Long.parseLong(countStr), 1000);

            SimpleDateFormat storageFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            String formattedDate = storageFormat.format(date.getTime());

            MainMenu.Pill newPill = new MainMenu.Pill(
                    namePill.getText().toString().trim(),
                    formattedDate,
                    String.valueOf(count),
                    descPill.getText().toString().trim()
            );

            savePillToPrefs(newPill, date.getTimeInMillis());
            scheduleNotification(newPill, date.getTimeInMillis()); // Добавьте эту строку

            new AppPreferences(this).setFirstRunCompleted();

        } catch (Exception e) {
            Log.e("SavePillRange", "Error saving pill for date", e);
        }
    }

    private void savePillToPrefs(MainMenu.Pill pill, long timestamp) {
        String pillKey = "pill_" + timestamp;
        String pillJson = gson.toJson(pill);
        prefs.edit().putString(pillKey, pillJson).apply();
    }

    private void scheduleNotification(MainMenu.Pill pill, long triggerTime) {
        // Проверяем, что время уведомления в будущем
        long delay = triggerTime - System.currentTimeMillis();
        if (delay <= 0) {
            Log.e("Notification", "Notification time is in the past");
            return;
        }

        // Создаем данные для Worker
        Data inputData = new Data.Builder()
                .putString("pill_name", pill.name)
                .putString("pill_time", pill.date.split(" ")[1])
                .putString("pill_count", pill.count)
                .build();

        // Создаем WorkRequest
        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(PillReminderWorker.class)
                .setInputData(inputData)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        // Запускаем WorkManager
        WorkManager.getInstance(this).enqueue(notificationWork);
    }
}