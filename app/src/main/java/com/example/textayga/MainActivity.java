package com.example.textayga;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Инициализация пользовательских настроек
        AppPreferences prefs = new AppPreferences(this);

        // Проверка, является ли запуск первым — если да, открываем экран без цели
        if (prefs.isFirstRun()) {
            startActivity(new Intent(this, NoGoalMenu.class));
        } else {
            // Иначе переходим на главный экран приложения
            startActivity(new Intent(this, MainMenu.class));
        }

        // Завершаем текущую активность, чтобы предотвратить возврат назад
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Проверка поддержки точных будильников (AlarmManager) на Android 12 и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                // Если разрешения нет — запрашиваем у пользователя возможность планировать точные будильники
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent); // Приводит пользователя в настройки для выдачи разрешения
            }
        }

        // Проверка и запрос разрешения на уведомления на Android 13+ (Tiramisu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                // Запрашиваем разрешение у пользователя
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }
}
