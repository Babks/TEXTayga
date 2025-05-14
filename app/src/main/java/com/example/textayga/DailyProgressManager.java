package com.example.textayga;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;

public class DailyProgressManager {
    private static final String LAST_RESET_DATE_KEY = "last_reset_date";
    private static final String PREFERENCES_NAME = "daily_progress_prefs";

    public static void checkAndResetProgress(Context context) {
        SharedPreferences progressPrefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences mainPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Получаем текущую дату как число (YYYYMMDD)
        Calendar cal = Calendar.getInstance();
        int currentDate = cal.get(Calendar.YEAR) * 10000 +
                (cal.get(Calendar.MONTH) + 1) * 100 +
                cal.get(Calendar.DAY_OF_MONTH);

        // Безопасное чтение предыдущей даты
        int lastResetDate = progressPrefs.getInt(LAST_RESET_DATE_KEY, -1);

        if (lastResetDate != currentDate) {
            SharedPreferences.Editor editor = mainPrefs.edit();

            // Сбрасываем только статистику
            editor.putLong("taken_pills_new", 0);
            editor.putLong("missed_pills_new", 0);
            editor.apply();

            // Сохраняем новую дату сброса как число
            progressPrefs.edit()
                    .putInt(LAST_RESET_DATE_KEY, currentDate)
                    .apply();
        }
    }
}