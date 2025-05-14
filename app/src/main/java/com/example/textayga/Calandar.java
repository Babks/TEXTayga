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

    Button btnHomepage;

    private ViewPager2 monthPager;
    private TextView monthYearText;
    private MonthPagerAdapter adapter;
    private Calendar currentDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.calandar);

        monthPager = findViewById(R.id.monthPager);
        monthYearText = findViewById(R.id.monthYearText);
        btnHomepage = findViewById(R.id.buttonHomepage);

        btnHomepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentHomepage = new Intent(Calandar.this, MainMenu.class);
                startActivity(intentHomepage);
                finish();
            }
        });

        // Настройка адаптера
        adapter = new MonthPagerAdapter(this);
        monthPager.setAdapter(adapter);
        monthPager.setCurrentItem(500, false); // Старт с текущего месяца

        // Обновление заголовка при перелистывании
        monthPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateMonthTitle(position);
            }
        });

        updateMonthTitle(500); // Инициализация заголовка
    }

    private void updateMonthTitle(int position) {
        Calendar calendar = (Calendar) currentDate.clone();
        calendar.add(Calendar.MONTH, position - 500);

        String monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, new Locale("ru"));
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        monthYearText.setText(monthName.substring(0, 1).toUpperCase() + monthName.substring(1) + " " + year);
    }
}