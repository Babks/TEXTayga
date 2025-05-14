package com.example.textayga;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager2.widget.ViewPager2;
import java.util.Calendar;
import java.util.Locale;

public class Calandar extends AppCompatActivity {

    Button btnHomepage; // Кнопка возврата на главный экран

    private ViewPager2 monthPager; // Компонент для перелистывания месяцев
    private TextView monthYearText; // Текст для отображения текущего месяца и года
    private MonthPagerAdapter adapter; // Адаптер для отображения месяцев
    private Calendar currentDate = Calendar.getInstance(); // Текущая дата для расчёта месяцев

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Отключаем тёмную тему
        setContentView(R.layout.calandar); // Устанавливаем макет активности

        monthPager = findViewById(R.id.monthPager); // Получаем ссылку на ViewPager
        monthYearText = findViewById(R.id.monthYearText); // Получаем ссылку на текст месяца/года
        btnHomepage = findViewById(R.id.buttonHomepage); // Получаем ссылку на кнопку "Домой"

        btnHomepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Переход на главный экран при нажатии на кнопку
                Intent intentHomepage = new Intent(Calandar.this, MainMenu.class);
                startActivity(intentHomepage);
                finish(); // Закрываем текущую активность
            }
        });

        adapter = new MonthPagerAdapter(this); // Инициализация адаптера для месяцев
        monthPager.setAdapter(adapter); // Привязываем адаптер к ViewPager
        monthPager.setCurrentItem(500, false); // Устанавливаем стартовую позицию (середина диапазона)

        monthPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // Обновляем заголовок при смене месяца
                updateMonthTitle(position);
            }
        });

        updateMonthTitle(500); // Отображаем текущий месяц при запуске
    }

    private void updateMonthTitle(int position) {
        // Обновляет заголовок с названием месяца и годом на основе позиции ViewPager
        Calendar calendar = (Calendar) currentDate.clone();
        calendar.add(Calendar.MONTH, position - 500); // Смещаем текущий месяц

        String monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, new Locale("ru")); // Название месяца
        String year = String.valueOf(calendar.get(Calendar.YEAR)); // Год
        monthYearText.setText(monthName.substring(0, 1).toUpperCase() + monthName.substring(1) + " " + year); // Устанавливаем заголовок с заглавной буквы
    }
}
