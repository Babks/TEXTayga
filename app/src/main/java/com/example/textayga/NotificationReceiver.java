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
    public static final String CHANNEL_ID = "pill_reminder_channel";
    public static final String NOTIFICATION_CHANNEL_ID = "pill_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Создаем канал уведомлений (для Android 8.0+)
        createNotificationChannel(context);

        // Получаем данные о лекарстве
        String pillName = intent.getStringExtra("pill_name");
        String pillTime = intent.getStringExtra("pill_time");
        String pillCount = intent.getStringExtra("pill_count");

        // Intent для открытия приложения при нажатии на уведомление
        Intent appIntent = new Intent(context, MainMenu.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, appIntent, PendingIntent.FLAG_IMMUTABLE);

        // Строим уведомление
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Примите лекарство")
                .setContentText(pillName + " - " + pillCount + " в " + pillTime)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        // Показываем уведомление
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) System.currentTimeMillis(), notification);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Напоминания о лекарствах",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Канал для напоминаний о приеме лекарств");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}