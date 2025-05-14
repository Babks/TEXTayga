package com.example.textayga;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NoGoalMenu extends AppCompatActivity {

    Button buttonHeal; // Кнопка для перехода на экран добавления таблетки
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001; // Код запроса на разрешение для уведомлений

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Отключаем темную тему для этого активити
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Устанавливаем layout для текущего активити
        setContentView(R.layout.no_goal_menu);

        // Проверка на наличие разрешения для отправки уведомлений на устройствах с Android 13 и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Если разрешение на уведомления ещё не получено, запрашиваем его
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION); // Запрашиваем разрешение
            }
        }

        // Инициализация кнопки и установка обработчика клика
        buttonHeal = findViewById(R.id.buttonHeal);
        buttonHeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Переход на экран добавления таблетки
                Intent intentAddPill = new Intent(NoGoalMenu.this, Add_Pill.class);
                startActivity(intentAddPill);
                finish(); // Закрываем текущую активность
            }
        });
    }
}
