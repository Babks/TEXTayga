package com.example.textayga;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    private static final String PREF_NAME = "AppPrefs"; // Название файла настроек SharedPreferences
    private static final String KEY_FIRST_RUN = "first_run"; // Ключ для определения первого запуска приложения

    private final SharedPreferences prefs; // Объект для доступа к сохранённым настройкам

    public AppPreferences(Context context) {
        // Инициализация SharedPreferences с приватным режимом доступа
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isFirstRun() {
        // Проверяет, является ли текущий запуск первым (по умолчанию — true)
        return prefs.getBoolean(KEY_FIRST_RUN, true);
    }

    public void setFirstRunCompleted() {
        // Устанавливает флаг, что первый запуск уже был выполнен
        prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply();
    }
}
