package com.example.textayga;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class PillReminderWorker extends Worker {

    private static final String CHANNEL_ID = "pill_reminder_channel";

    public PillReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String pillName = getInputData().getString("pill_name");
        String pillTime = getInputData().getString("pill_time");
        String pillCount = getInputData().getString("pill_count");

        // Создаем канал уведомлений (для Android 8.0+)
        createNotificationChannel();

        // Создаем уведомление (аналогично NotificationReceiver)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                NotificationReceiver.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Напоминание о приеме лекарства")
                .setContentText("Примите " + pillCount + " " + pillName + " в " + pillTime)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Intent для открытия MainMenu
        Intent mainIntent = new Intent(getApplicationContext(), MainMenu.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());

        return Result.success();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Напоминания о лекарствах",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Канал для напоминаний о приеме лекарств");

            NotificationManager manager = (NotificationManager)
                    getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }
}