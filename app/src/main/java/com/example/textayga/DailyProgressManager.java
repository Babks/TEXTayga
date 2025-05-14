package com.example.textayga;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;

// Класс отвечает за ежедневную проверку и сброс статистики при смене дня
public class DailyProgressManager {
    private static final String LAST_RESET_DATE_KEY = "last_reset_date"; // Ключ даты последнего сброса
    private static final String PREFERENCES_NAME = "daily_progress_prefs"; // Название файла настроек сброса

    // Проверяет дату последнего сброса и обнуляет статистику, если наступил новый день
    public static void checkAndResetProgress(Context context) {
        SharedPreferences progressPrefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences mainPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE); // Основное хранилище статистики

        // Получаем текущую дату в виде числа: YYYYMMDD
        Calendar cal = Calendar.getInstance();
        int currentDate = cal.get(Calendar.YEAR) * 10000 +
                (cal.get(Calendar.MONTH) + 1) * 100 +
                cal.get(Calendar.DAY_OF_MONTH);

        int lastResetDate = progressPrefs.getInt(LAST_RESET_DATE_KEY, -1); // Последняя зафиксированная дата сброса

        // Если дата сброса не совпадает с сегодняшней, сбрасываем счётчики
        if (lastResetDate != currentDate) {
            SharedPreferences.Editor editor = mainPrefs.edit();

            // Сбрасываем только новые значения статистики
            editor.putLong("taken_pills_new", 0);
            editor.putLong("missed_pills_new", 0);
            editor.apply();

            // Обновляем дату последнего сброса
            progressPrefs.edit()
                    .putInt(LAST_RESET_DATE_KEY, currentDate)
                    .apply();
        }
    }
}
