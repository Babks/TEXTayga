package com.example.textayga;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "pill_reminder_channel"; // Идентификатор канала уведомлений
    public static final String NOTIFICATION_CHANNEL_ID = "pill_reminder_channel"; // Идентификатор канала уведомлений

    @Override
    public void onReceive(Context context, Intent intent) {
        // Создаем канал уведомлений (для Android 8.0+)
        createNotificationChannel(context);

        // Получаем данные о лекарстве из интента
        String pillName = intent.getStringExtra("pill_name"); // Название лекарства
        String pillTime = intent.getStringExtra("pill_time"); // Время приема
        String pillCount = intent.getStringExtra("pill_count"); // Количество таблеток

        // Intent для открытия приложения при нажатии на уведомление
        Intent appIntent = new Intent(context, MainMenu.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Очищаем стек активности
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, appIntent, PendingIntent.FLAG_IMMUTABLE); // Создаем PendingIntent

        // Строим уведомление
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Иконка уведомления
                .setContentTitle("Примите лекарство") // Заголовок уведомления
                .setContentText(pillName + " - " + pillCount + " в " + pillTime) // Текст уведомления с информацией о лекарстве
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Высокий приоритет уведомления
                .setContentIntent(pendingIntent) // PendingIntent для перехода в приложение
                .setAutoCancel(true) // Уведомление закроется при нажатии на него
                .build(); // Строим уведомление

        // Показываем уведомление
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) System.currentTimeMillis(), notification); // Показ уведомления с уникальным ID
    }

    // Метод для создания канала уведомлений (необходим для Android 8.0 и выше)
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, // Идентификатор канала
                    "Напоминания о лекарствах", // Название канала
                    NotificationManager.IMPORTANCE_HIGH); // Важность канала
            channel.setDescription("Канал для напоминаний о приеме лекарств"); // Описание канала

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel); // Создаем канал уведомлений
        }
    }
}
