package com.example.dailyscheduler;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import java.util.*;

public class UpcomingEventsActivity extends AppCompatActivity {

    private ListView eventsListView;
    private TextView noEventsText;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_events);

        eventsListView = findViewById(R.id.eventsListView);
        noEventsText = findViewById(R.id.noEventsText); // Add this in your layout
        sharedPreferences = getSharedPreferences("DailyData", MODE_PRIVATE);
        gson = new Gson();

        Map<String, ?> allEntries = sharedPreferences.getAll();
        List<Event> upcomingEvents = new ArrayList<>();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (key.contains("_")) {  // Events are saved as date_time keys
                String json = entry.getValue().toString();
                Event event = gson.fromJson(json, Event.class);

                if (event != null && isFutureEvent(event.getDate())) {
                    upcomingEvents.add(event);
                }
            }
        }

        if (upcomingEvents.isEmpty()) {
            noEventsText.setText("No upcoming events.");
            noEventsText.setVisibility(TextView.VISIBLE);
        } else {
            Collections.sort(upcomingEvents, Comparator.comparing(Event::getDate)); // Optional sorting
            EventAdapter adapter = new EventAdapter(this, upcomingEvents, sharedPreferences);
            eventsListView.setAdapter(adapter);
        }
    }

    private boolean isFutureEvent(String dateStr) {
        try {
            String[] parts = dateStr.split("-");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int year = Integer.parseInt(parts[2]);

            Calendar eventDate = Calendar.getInstance();
            eventDate.set(Calendar.DAY_OF_MONTH, day);
            eventDate.set(Calendar.MONTH, month);
            eventDate.set(Calendar.YEAR, year);
            eventDate.set(Calendar.HOUR_OF_DAY, 0);
            eventDate.set(Calendar.MINUTE, 0);
            eventDate.set(Calendar.SECOND, 0);

            return eventDate.getTimeInMillis() >= Calendar.getInstance().getTimeInMillis();
        } catch (Exception e) {
            return false;
        }
    }
}