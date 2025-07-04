package com.example.dailyscheduler;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class DailyPlannerActivity extends AppCompatActivity {

    private CalendarView plannerCalendarView;
    private EditText morningEditText, afternoonEditText, eveningEditText, nightEditText;
    private Button saveAllSectionsButton;
    private SharedPreferences sharedPreferences;
    private String selectedDate = "";

    private TimePicker morningTimePicker, afternoonTimePicker, eveningTimePicker, nightTimePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_planner);

        // Permission for notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // Bind UI components
        plannerCalendarView = findViewById(R.id.plannerCalendarView);
        morningEditText = findViewById(R.id.morningEditText);
        afternoonEditText = findViewById(R.id.afternoonEditText);
        eveningEditText = findViewById(R.id.eveningEditText);
        nightEditText = findViewById(R.id.nightEditText);
        morningTimePicker = findViewById(R.id.morningTimePicker);
        afternoonTimePicker = findViewById(R.id.afternoonTimePicker);
        eveningTimePicker = findViewById(R.id.eveningTimePicker);
        nightTimePicker = findViewById(R.id.nightTimePicker);
        saveAllSectionsButton = findViewById(R.id.saveAllSectionsButton);

        // Force 24-hour format programmatically
        morningTimePicker.setIs24HourView(true);
        afternoonTimePicker.setIs24HourView(true);
        eveningTimePicker.setIs24HourView(true);
        nightTimePicker.setIs24HourView(true);

        sharedPreferences = getSharedPreferences("DailyPlans", MODE_PRIVATE);
        selectedDate = getDateString(plannerCalendarView.getDate());
        loadAllSections(selectedDate);

        plannerCalendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "-" + (month + 1) + "-" + year;
            loadAllSections(selectedDate);
        });

        saveAllSectionsButton.setOnClickListener(v -> {
            saveSection("morning", morningEditText.getText().toString());
            saveSection("afternoon", afternoonEditText.getText().toString());
            saveSection("evening", eveningEditText.getText().toString());
            saveSection("night", nightEditText.getText().toString());

            Toast.makeText(this, "Plan saved!", Toast.LENGTH_SHORT).show();

            // Schedule notifications 5 minutes before
            scheduleReminder("Morning Plan", morningEditText.getText().toString(),
                    morningTimePicker.getHour(), morningTimePicker.getMinute());

            scheduleReminder("Afternoon Plan", afternoonEditText.getText().toString(),
                    afternoonTimePicker.getHour(), afternoonTimePicker.getMinute());

            scheduleReminder("Evening Plan", eveningEditText.getText().toString(),
                    eveningTimePicker.getHour(), eveningTimePicker.getMinute());

            scheduleReminder("Night Plan", nightEditText.getText().toString(),
                    nightTimePicker.getHour(), nightTimePicker.getMinute());
        });
    }

    private void saveSection(String section, String text) {
        sharedPreferences.edit().putString(selectedDate + "_" + section, text).apply();
    }

    private void loadAllSections(String date) {
        morningEditText.setText(sharedPreferences.getString(date + "_morning", ""));
        afternoonEditText.setText(sharedPreferences.getString(date + "_afternoon", ""));
        eveningEditText.setText(sharedPreferences.getString(date + "_evening", ""));
        nightEditText.setText(sharedPreferences.getString(date + "_night", ""));
    }

    private String getDateString(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        return day + "-" + month + "-" + year;
    }

    private void scheduleReminder(String section, String message, int hour, int minute) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("title", section + ": " + message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                section.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, Math.max(minute - 5, 0)); // Ensure non-negative
        calendar.set(Calendar.SECOND, 0);

        // If time has already passed today, schedule for next day
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}