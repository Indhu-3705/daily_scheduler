package com.example.dailyscheduler;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class DailyNoteActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private EditText noteEditText;
    private Button saveNoteButton;
    private String selectedDate;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_note);

        calendarView = findViewById(R.id.calendarView);
        noteEditText = findViewById(R.id.noteEditText);
        saveNoteButton = findViewById(R.id.saveNoteButton);
        sharedPreferences = getSharedPreferences("DailyData", MODE_PRIVATE);

        selectedDate = getDateString(calendarView.getDate());
        loadNoteForDate(selectedDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "-" + (month + 1) + "-" + year;
            loadNoteForDate(selectedDate);
        });

        saveNoteButton.setOnClickListener(v -> {
            String note = noteEditText.getText().toString();
            sharedPreferences.edit().putString(selectedDate, note).apply();
            Toast.makeText(this, "Note saved for " + selectedDate, Toast.LENGTH_SHORT).show();
        });

        noteEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && noteEditText.getText().toString().equals("Nothing planned for today. Tap to edit.")) {
                noteEditText.setText("");
            }
        });
    }

    private void loadNoteForDate(String date) {
        String note = sharedPreferences.getString(date, "");
        noteEditText.setText(note.isEmpty() ? "Nothing planned for today. Tap to edit." : note);
    }

    private String getDateString(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.DAY_OF_MONTH) + "-" +
                (calendar.get(Calendar.MONTH) + 1) + "-" +
                calendar.get(Calendar.YEAR);
    }
}