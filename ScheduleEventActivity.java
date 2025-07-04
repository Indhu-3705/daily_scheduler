package com.example.dailyscheduler;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;

import java.util.Calendar;

public class ScheduleEventActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private EditText eventTitle;
    private TextView selectedTimeText;
    private Button pickTimeBtn, saveEventBtn;
    private String selectedDate = "";
    private int selectedHour = -1, selectedMinute = -1;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_event);

        calendarView = findViewById(R.id.eventCalendarView);
        eventTitle = findViewById(R.id.eventTitle);
        selectedTimeText = findViewById(R.id.selectedTimeText);
        pickTimeBtn = findViewById(R.id.pickTimeBtn);
        saveEventBtn = findViewById(R.id.saveEventBtn);

        sharedPreferences = getSharedPreferences("DailyData", MODE_PRIVATE);
        gson = new Gson();

        selectedDate = getDateString(calendarView.getDate());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "-" + (month + 1) + "-" + year;
        });

        pickTimeBtn.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    ScheduleEventActivity.this,
                    (view, hourOfDay, minute1) -> {
                        selectedHour = hourOfDay;
                        selectedMinute = minute1;
                        selectedTimeText.setText(String.format("Selected Time: %02d:%02d", selectedHour, selectedMinute));
                    },
                    hour, minute, true);
            timePickerDialog.show();
        });

        saveEventBtn.setOnClickListener(v -> {
            if (eventTitle.getText().toString().isEmpty() || selectedHour == -1) {
                Toast.makeText(this, "Enter title and pick time", Toast.LENGTH_SHORT).show();
                return;
            }

            String time = String.format("%02d:%02d", selectedHour, selectedMinute);
            Event newEvent = new Event(eventTitle.getText().toString(), time, selectedDate);
            String eventJson = gson.toJson(newEvent);

            sharedPreferences.edit().putString(selectedDate + "_" + time, eventJson).apply();
            Toast.makeText(this, "Event scheduled successfully!!", Toast.LENGTH_SHORT).show();

            NotificationHelper.scheduleNotification(this, newEvent.getTitle(), selectedHour, selectedMinute);
            finish();
        });
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