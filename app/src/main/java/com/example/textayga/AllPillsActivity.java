package com.example.textayga;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AllPillsActivity extends AppCompatActivity {
    private LinearLayout pillsLayout;
    private SharedPreferences prefs;
    private Gson gson = new Gson();

    Button btnCalandar;
    Button btnHomepage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_pills);

        pillsLayout = findViewById(R.id.pillsLayout);
        btnCalandar = findViewById(R.id.buttonCalandar);
        btnHomepage = findViewById(R.id.buttonHomepage);
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToMainMenu();
            }
        });

        loadAllPills();

        btnCalandar.setOnClickListener(v -> {
            startActivity(new Intent(this, Calandar.class));
            finish();
        });

        btnHomepage.setOnClickListener(v -> {
            startActivity(new Intent(this, MainMenu.class));
            finish();
        });
    }

    private void navigateToMainMenu() {
        Intent intent = new Intent(this, MainMenu.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void loadAllPills() {
        pillsLayout.removeAllViews();
        Map<String, ?> allEntries = prefs.getAll();
        List<MainMenu.Pill> allPills = new ArrayList<>();

        try {
            // Собираем все лекарства
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getKey().startsWith("pill_")) {
                    try {
                        Object value = entry.getValue();
                        String pillJson = (value instanceof Integer) ? String.valueOf(value) : (String) value;
                        MainMenu.Pill pill = gson.fromJson(pillJson, MainMenu.Pill.class);

                        if (pill != null && pill.date != null) {
                            allPills.add(pill);
                        }
                    } catch (Exception e) {
                        Log.e("AllPills", "Error loading pill", e);
                    }
                }
            }

            // Сортируем по дате (новые сверху)
            Collections.sort(allPills, (p1, p2) -> p2.date.compareTo(p1.date));

            // Добавляем в layout
            for (MainMenu.Pill pill : allPills) {
                addPillView(pill);
            }

            if (allPills.isEmpty()) {
                showEmptyState();
            }
        } catch (Exception e) {
            Log.e("AllPills", "Error loading pills", e);
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmptyState() {
        TextView emptyView = new TextView(this);
        emptyView.setText("Нет добавленных лекарств");
        emptyView.setTextSize(16);
        emptyView.setTextColor(Color.parseColor("#2F281F"));
        emptyView.setGravity(Gravity.CENTER);
        pillsLayout.addView(emptyView);
    }

    private void addPillView(MainMenu.Pill pill) {
        LinearLayout pillItem = new LinearLayout(this);
        pillItem.setOrientation(LinearLayout.VERTICAL);
        pillItem.setPadding(32, 24, 32, 24);
        pillItem.setBackgroundResource(R.drawable.pill_item_background);

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView nameView = new TextView(this);
        nameView.setText(pill.name);
        nameView.setTextSize(18);
        nameView.setTextColor(Color.parseColor("#2F281F"));
        nameView.setTypeface(null, Typeface.BOLD);
        textLayout.addView(nameView);

        TextView dateView = new TextView(this);
        dateView.setText(pill.date);
        dateView.setTextSize(14);
        dateView.setTextColor(Color.parseColor("#2F281F"));
        dateView.setPadding(0, 8, 0, 0);
        textLayout.addView(dateView);

        TextView countView = new TextView(this);
        countView.setText(PillUtils.getPillCountString(pill.count));
        countView.setTextSize(14);
        countView.setTextColor(Color.parseColor("#2F281F"));
        countView.setPadding(0, 8, 0, 0);
        textLayout.addView(countView);

        if (!pill.description.isEmpty()) {
            TextView descView = new TextView(this);
            descView.setText(pill.description);
            descView.setTextSize(14);
            descView.setTextColor(Color.parseColor("#2F281F"));
            descView.setPadding(0, 8, 0, 16);
            textLayout.addView(descView);
        }

        contentLayout.addView(textLayout);
        addStatusIcon(contentLayout, pill);
        pillItem.addView(contentLayout);
        pillsLayout.addView(pillItem);

        pillItem.setOnClickListener(v -> {
            Intent intent = new Intent(this, PillDetailsActivity.class);
            intent.putExtra("pill", pill);
            startActivity(intent);
        });
    }

    private void addStatusIcon(LinearLayout parentLayout, MainMenu.Pill pill) {
        String statusKey = "pill_status_" + pill.name + "_" + pill.date;
        String status = prefs.getString(statusKey, null);

        if (status != null) {
            ImageView statusIcon = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(24),
                    dpToPx(24));
            params.gravity = Gravity.CENTER_VERTICAL;
            statusIcon.setLayoutParams(params);

            if (status.equals("taken")) {
                statusIcon.setImageResource(R.drawable.ic_check);
            } else if (status.equals("missed")) {
                statusIcon.setImageResource(R.drawable.ic_cross);
            }

            parentLayout.addView(statusIcon);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}