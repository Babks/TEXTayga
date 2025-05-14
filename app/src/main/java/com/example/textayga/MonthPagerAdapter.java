package com.example.textayga;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MonthPagerAdapter extends RecyclerView.Adapter<MonthPagerAdapter.MonthViewHolder> {

    private final Context context;
    private final Calendar baseDate = Calendar.getInstance();

    public MonthPagerAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_month, parent, false);
        // Убедитесь, что GridView заполняет весь родительский контейнер
        GridView gridView = view.findViewById(R.id.calendarGrid);
        gridView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return new MonthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
        Calendar monthCalendar = (Calendar) baseDate.clone();
        monthCalendar.add(Calendar.MONTH, position - 500);

        CalendarDayAdapter dayAdapter = new CalendarDayAdapter(context, monthCalendar);
        holder.gridView.setAdapter(dayAdapter);
        holder.dayAdapter = dayAdapter; // Сохраняем ссылку на адаптер
    }

    @Override
    public int getItemCount() {
        return 1000; // Большое число для "бесконечного" перелистывания
    }

    static class MonthViewHolder extends RecyclerView.ViewHolder {
        GridView gridView;
        CalendarDayAdapter dayAdapter;

        MonthViewHolder(@NonNull View itemView) {
            super(itemView);
            gridView = itemView.findViewById(R.id.calendarGrid);
        }

        public void setSelectedDay(int day) {
            if (dayAdapter != null) {
                dayAdapter.setSelectedDay(day);
            }
        }
    }

    private static class CalendarDayAdapter extends BaseAdapter {
        private Context context;
        private Calendar calendar;
        private String[] days;
        private int selectedDay = -1;
        private final Calendar today = Calendar.getInstance();
        private int lastSelectedDay = -1; // Для отслеживания предыдущего выбора

        // Цвета и ресурсы
        private final int selectedColor = Color.parseColor("#E7BBA2");
        private final int defaultTextColor = Color.BLACK;
        private final int selectedTextColor = Color.WHITE;

        public CalendarDayAdapter(Context context, Calendar calendar) {
            this.context = context;
            this.calendar = calendar;
            this.days = generateDaysArray(calendar);

            // Выделяем сегодняшний день по умолчанию, если это текущий месяц
            if (isSameMonth(calendar, today)) {
                selectedDay = today.get(Calendar.DAY_OF_MONTH);
            }
        }

        private boolean isSameMonth(Calendar cal1, Calendar cal2) {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        }

        private String[] generateDaysArray(Calendar cal) {
            Calendar temp = (Calendar) cal.clone();
            temp.set(Calendar.DAY_OF_MONTH, 1);

            int firstDayOfWeek = temp.get(Calendar.DAY_OF_WEEK);
            int daysInMonth = temp.getActualMaximum(Calendar.DAY_OF_MONTH);

            String[] daysArray = new String[42]; // 6 недель максимум

            // Заполняем пустые дни перед первым числом
            int startDay = firstDayOfWeek - 2; // Для Пн=0, Вт=1 и т.д.
            if (startDay < 0) startDay += 7;

            for (int i = 0; i < startDay; i++) {
                daysArray[i] = "";
            }

            // Заполняем дни месяца
            for (int i = 1; i <= daysInMonth; i++) {
                daysArray[startDay + i - 1] = String.valueOf(i);
            }

            return daysArray;
        }

        @Override
        public int getCount() {
            return days.length;
        }

        @Override
        public Object getItem(int position) {
            return days[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView dayView;
            if (convertView == null) {
                dayView = new TextView(context);
                dayView.setLayoutParams(new GridView.LayoutParams(
                        dpToPx(40), dpToPx(40)));
                dayView.setGravity(Gravity.CENTER);
                dayView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

                dayView.setOnClickListener(v -> {
                    String dayStr = ((TextView)v).getText().toString();
                    if (!dayStr.isEmpty()) {
                        int day = Integer.parseInt(dayStr);
                        Calendar selectedDate = (Calendar) calendar.clone();
                        selectedDate.set(Calendar.DAY_OF_MONTH, day);

                        // Формат для отображения
                        SimpleDateFormat displayFormat = new SimpleDateFormat("d MMMM, yyyy", new Locale("ru"));
                        String displayDateStr = displayFormat.format(selectedDate.getTime());

                        // Формат для хранения и сравнения
                        SimpleDateFormat storageFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                        String storageDateStr = storageFormat.format(selectedDate.getTime());

                        Intent intent = new Intent(context, DayPillsActivity.class);
                        intent.putExtra("selectedDate", displayDateStr);
                        intent.putExtra("storageDate", storageDateStr);

                        // Добавляем флаг для очистки стека активностей
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                        context.startActivity(intent);

                        // Закрываем текущую активность календаря
                        if (context instanceof Calandar) {
                            ((Calandar) context).finish();
                        }
                    }
                });
            } else {
                dayView = (TextView) convertView;
            }

            // Безопасная установка текста
            String dayText = days[position];
            dayView.setText(dayText != null ? dayText : "");

            // Настройка внешнего вида с проверкой на null
            if (dayText != null && !dayText.isEmpty()) {
                try {
                    int day = Integer.parseInt(dayText);

                    if (day == selectedDay) {
                        GradientDrawable bg = new GradientDrawable();
                        bg.setShape(GradientDrawable.OVAL);
                        bg.setColor(selectedColor);
                        dayView.setBackground(bg); // Исправлено: убрано createFromPath
                        dayView.setTextColor(selectedTextColor);
                    } else {
                        dayView.setBackgroundResource(0);
                        dayView.setTextColor(defaultTextColor);

                        if (isSameMonth(calendar, today) && day == today.get(Calendar.DAY_OF_MONTH)) {
                            dayView.setTextColor(Color.RED);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Обработка ошибки парсинга числа
                    dayView.setBackgroundResource(0);
                    dayView.setTextColor(defaultTextColor);
                }
            } else {
                dayView.setBackgroundResource(0);
                dayView.setTextColor(defaultTextColor);
            }

            return dayView;
        }

        public void setSelectedDay(int day) {
            lastSelectedDay = selectedDay;
            selectedDay = day;
            notifyDataSetChanged(); // Обновляем все элементы
        }

        private int dpToPx(int dp) {
            return (int) (dp * context.getResources().getDisplayMetrics().density);
        }
    }
}