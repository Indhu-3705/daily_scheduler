package com.example.dailyscheduler;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private EditText dailyNoteEditText, eventTitle;
    private Button saveNoteButton, saveEventBtn, openPlannerBtn, openUpcomingEventsBtn, logoutBtn;
    private TimePicker timePicker;
    private String selectedDate = "";
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private CardView dashboardCard;
    private Animation clickAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        calendarView = findViewById(R.id.calendarView);
        dailyNoteEditText = findViewById(R.id.dailyNoteEditText);
        saveNoteButton = findViewById(R.id.saveNoteButton);
        eventTitle = findViewById(R.id.eventTitle);
        saveEventBtn = findViewById(R.id.saveEventButton);
        timePicker = findViewById(R.id.timePicker);
        openPlannerBtn = findViewById(R.id.openPlannerBtn);
        openUpcomingEventsBtn = findViewById(R.id.openUpcomingEventsBtn);
        logoutBtn = findViewById(R.id.logoutButton);
        dashboardCard = findViewById(R.id.dashboardCard);

        timePicker.setIs24HourView(true);
        gson = new Gson();
        sharedPreferences = getSharedPreferences("DailyData", MODE_PRIVATE);

        // Load animations
        clickAnim = AnimationUtils.loadAnimation(this, R.anim.button_click);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        dashboardCard.startAnimation(slideUp);

        // Notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // Check exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Enable 'Exact Alarms' in App Settings", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }

        // Default selected date
        selectedDate = getDateString(calendarView.getDate());
        loadNoteForDate(selectedDate);

        // Daily reminders
        DailyReminderHelper.setDailyReminder(this, "Drink Water", 10, 0, 101);
        DailyReminderHelper.setDailyReminder(this, "Exercise", 6, 30, 102);
        DailyReminderHelper.setDailyReminder(this, "Go to Sleep", 22, 30, 103);

        // Clear hint text on focus
        dailyNoteEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && dailyNoteEditText.getText().toString().equals("Nothing planned for today. Tap to edit.")) {
                dailyNoteEditText.setText("");
            }
        });

        // Calendar date change listener
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "-" + (month + 1) + "-" + year;
            loadNoteForDate(selectedDate);
        });

        // Save daily note
        saveNoteButton.setOnClickListener(v -> {
            v.startAnimation(clickAnim);
            String note = dailyNoteEditText.getText().toString();
            sharedPreferences.edit().putString(selectedDate, note).apply();
            Toast.makeText(MainActivity.this, "Note saved!", Toast.LENGTH_SHORT).show();
        });

        // Save scheduled event
        saveEventBtn.setOnClickListener(v -> {
            v.startAnimation(clickAnim);
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String time = String.format("%02d:%02d", hour, minute);
            String title = eventTitle.getText().toString();

            Event newEvent = new Event(title, time, selectedDate);
            String eventJson = gson.toJson(newEvent);
            sharedPreferences.edit().putString(selectedDate + "_" + time, eventJson).apply();
            Toast.makeText(this, "Event scheduled successfully!!", Toast.LENGTH_SHORT).show();

            // Schedule Notification (exact alarm)
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                NotificationHelper.scheduleNotification(this, title, hour, minute);
            } else {
                Toast.makeText(this, "Exact alarm permission not granted!", Toast.LENGTH_SHORT).show();
            }
        });

        // Open Planner
        openPlannerBtn.setOnClickListener(v -> {
            v.startAnimation(clickAnim);
            startActivity(new Intent(this, DailyPlannerActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // View Upcoming Events
        openUpcomingEventsBtn.setOnClickListener(v -> {
            v.startAnimation(clickAnim);
            startActivity(new Intent(this, UpcomingEventsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Logout
        logoutBtn.setOnClickListener(v -> {
            v.startAnimation(clickAnim);
            FirebaseAuth.getInstance().signOut();
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("isLoggedIn", false).apply();

            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });
    }

    private void loadNoteForDate(String date) {
        String note = sharedPreferences.getString(date, "");
        dailyNoteEditText.setText(note.isEmpty() ? "Nothing planned for today. Tap to edit." : note);
    }

    private String getDateString(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        return day + "-" + month + "-" + year;
    }
}