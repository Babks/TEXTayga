package com.example.textayga;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import java.util.concurrent.TimeUnit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;

public class Add_Pill extends AppCompatActivity {
    // UI элементы
    private EditText namePill, datePill, countPill, descPill, editTextDaysCount;
    private Calendar selectedDateTime = Calendar.getInstance(); // Текущая дата и время по умолчанию
    private SharedPreferences prefs; // Хранилище для данных приложения
    private Gson gson = new Gson(); // Для работы с JSON
    private CheckBox checkBoxRange; // Чекбокс "Повторять"
    private LinearLayout rangeLayout; // Контейнер для настроек повтора
    private CheckBox[] dayCheckboxes; // Чекбоксы дней недели

    // Кнопки навигации
    Button btnCalandar;
    Button btnHomepage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Отключение ночной темы
        setContentView(R.layout.add_pill);

        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Инициализация всех полей ввода
        namePill = findViewById(R.id.EditTextPillName);
        datePill = findViewById(R.id.EditTextPillDate);
        countPill = findViewById(R.id.EditTextPillCount);
        descPill = findViewById(R.id.EditTextPillInf);
        btnCalandar = findViewById(R.id.buttonCalandar);
        btnHomepage = findViewById(R.id.buttonHomepage);

        // Настройка элементов для повторяющихся напоминаний
        checkBoxRange = findViewById(R.id.checkBoxRange);
        rangeLayout = findViewById(R.id.rangeLayout);
        editTextDaysCount = findViewById(R.id.editTextDaysCount);

        // Инициализация чекбоксов дней недели
        dayCheckboxes = new CheckBox[]{
                findViewById(R.id.monday),    // Понедельник
                findViewById(R.id.tuesday),   // Вторник
                findViewById(R.id.wednesday),  // Среда
                findViewById(R.id.thursday),   // Четверг
                findViewById(R.id.friday),     // Пятница
                findViewById(R.id.saturday),  // Суббота
                findViewById(R.id.sunday)      // Воскресенье
        };

        // Обработчик изменения состояния чекбокса "Повторять"
        checkBoxRange.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rangeLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Настройка поля даты (только для чтения)
        datePill.setFocusable(false);
        datePill.setOnClickListener(v -> showDateTimePicker());

        // Обработчик кнопки добавления лекарства
        Button btnAddPill = findViewById(R.id.buttonAddPill);
        btnAddPill.setOnClickListener(v -> {
            if (!checkAllFieldsFilled()) {
                Toast.makeText(Add_Pill.this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }
            savePill();
        });

        // Обработчики кнопок навигации
        btnCalandar.setOnClickListener(v -> {
            startActivity(new Intent(this, Calandar.class));
            finish();
        });

        btnHomepage.setOnClickListener(v -> {
            startActivity(new Intent(this, MainMenu.class));
            finish();
        });
    }

    // Проверка заполнения всех обязательных полей
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

    // Показ диалога выбора даты и времени
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

    // Обновление текста в поле даты
    private void updateDateText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        datePill.setText(sdf.format(selectedDateTime.getTime()));
    }

    // Сохранение лекарства (выбор типа сохранения)
    private void savePill() {
        if (checkBoxRange.isChecked()) {
            savePillRange();
        } else {
            saveSinglePill();
        }
    }

    // Сохранение одиночного напоминания
    private void saveSinglePill() {
        if (!validateDate()) return;

        try {
            // Обработка количества (только цифры, максимум 1000)
            String countStr = countPill.getText().toString().replaceAll("[^0-9]", "");
            long count = Math.min(Long.parseLong(countStr), 1000);

            // Форматирование даты
            SimpleDateFormat storageFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            String formattedDate = storageFormat.format(selectedDateTime.getTime());

            // Создание объекта Pill
            MainMenu.Pill newPill = new MainMenu.Pill(
                    namePill.getText().toString().trim(),
                    formattedDate,
                    String.valueOf(count),
                    descPill.getText().toString().trim()
            );

            // Сохранение и планирование уведомления
            savePillToPrefs(newPill, selectedDateTime.getTimeInMillis());
            scheduleNotification(newPill, selectedDateTime.getTimeInMillis());

            new AppPreferences(this).setFirstRunCompleted();

            Toast.makeText(this, "Лекарство добавлено", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainMenu.class));
            finish();
        } catch (Exception e) {
            Log.e("SavePill", "Error saving pill", e);
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
        }
    }

    // Сохранение повторяющегося напоминания
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

        // Получение выбранных дней недели
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

        // Настройка времени напоминания
        int hour = selectedDateTime.get(Calendar.HOUR_OF_DAY);
        int minute = selectedDateTime.get(Calendar.MINUTE);
        int savedPills = 0;

        // Проверка сегодняшнего дня
        Calendar today = (Calendar) selectedDateTime.clone();
        today.set(Calendar.HOUR_OF_DAY, hour);
        today.set(Calendar.MINUTE, minute);
        today.set(Calendar.SECOND, 0);

        // Если сегодня выбранный день недели
        if (selectedDays.contains(today.get(Calendar.DAY_OF_WEEK))) {
            savePillForDate(today);
            savedPills++;

            if (savedPills >= daysCount) {
                Toast.makeText(this, "Добавлено " + savedPills + " напоминаний", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainMenu.class));
                finish();
                return;
            }
        }

        // Добавление напоминаний на следующие дни
        Calendar currentDate = (Calendar) today.clone();
        int daysAdded = 0;
        int maxAttempts = 1000; // Лимит итераций

        while (savedPills < daysCount && daysAdded < maxAttempts) {
            currentDate.add(Calendar.DAY_OF_YEAR, 1);
            daysAdded++;

            if (selectedDays.contains(currentDate.get(Calendar.DAY_OF_WEEK))) {
                savePillForDate(currentDate);
                savedPills++;
            }
        }

        Toast.makeText(this, "Добавлено " + savedPills + " напоминаний", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainMenu.class));
        finish();
    }

    // Проверка что дата в будущем
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

    // Сохранение напоминания для конкретной даты
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
            scheduleNotification(newPill, date.getTimeInMillis());

            new AppPreferences(this).setFirstRunCompleted();

        } catch (Exception e) {
            Log.e("SavePillRange", "Error saving pill for date", e);
        }
    }

    // Сохранение в SharedPreferences
    private void savePillToPrefs(MainMenu.Pill pill, long timestamp) {
        String pillKey = "pill_" + timestamp;
        String pillJson = gson.toJson(pill);
        prefs.edit().putString(pillKey, pillJson).apply();
    }

    // Планирование уведомления через WorkManager
    private void scheduleNotification(MainMenu.Pill pill, long triggerTime) {
        long delay = triggerTime - System.currentTimeMillis();
        if (delay <= 0) {
            Log.e("Notification", "Notification time is in the past");
            return;
        }

        Data inputData = new Data.Builder()
                .putString("pill_name", pill.name)
                .putString("pill_time", pill.date.split(" ")[1])
                .putString("pill_count", pill.count)
                .build();

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(PillReminderWorker.class)
                .setInputData(inputData)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(this).enqueue(notificationWork);
    }
}