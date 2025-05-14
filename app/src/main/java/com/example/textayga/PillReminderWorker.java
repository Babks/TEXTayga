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

    private static final String CHANNEL_ID = "pill_reminder_channel"; // Идентификатор канала уведомлений

    public PillReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params); // Конструктор Worker, инициализирует контекст и параметры
    }

    @NonNull
    @Override
    public Result doWork() {
        // Извлекаем данные о лекарстве, переданные в Worker
        String pillName = getInputData().getString("pill_name"); // Название лекарства
        String pillTime = getInputData().getString("pill_time"); // Время приема
        String pillCount = getInputData().getString("pill_count"); // Количество таблеток

        // Создаем канал уведомлений для Android 8.0 и выше
        createNotificationChannel();

        // Строим уведомление
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                NotificationReceiver.NOTIFICATION_CHANNEL_ID) // Указываем канал уведомлений
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Иконка уведомления
                .setContentTitle("Напоминание о приеме лекарства") // Заголовок уведомления
                .setContentText("Примите " + pillCount + " " + pillName + " в " + pillTime) // Текст уведомления
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Высокий приоритет уведомления
                .setAutoCancel(true); // Уведомление исчезает после нажатия на него

        // Создаем Intent для открытия главного меню при нажатии на уведомление
        Intent mainIntent = new Intent(getApplicationContext(), MainMenu.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Очищаем стек активности
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Используем флаги для обновления и защиты от изменений
        );
        builder.setContentIntent(pendingIntent); // Устанавливаем Intent в уведомление

        // Показываем уведомление через NotificationManager
        NotificationManager notificationManager = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build()); // Показываем уведомление с уникальным ID

        return Result.success(); // Успешное выполнение задачи
    }

    // Метод для создания канала уведомлений для Android 8.0 и выше
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Проверяем версию Android
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, // Идентификатор канала
                    "Напоминания о лекарствах", // Название канала
                    NotificationManager.IMPORTANCE_HIGH); // Устанавливаем высокую важность канала
            channel.setDescription("Канал для напоминаний о приеме лекарств"); // Описание канала

            // Создаем канал уведомлений
            NotificationManager manager = (NotificationManager)
                    getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel); // Создаем канал в системе
        }
    }
}
