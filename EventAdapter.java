package com.example.dailyscheduler;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;

import java.util.List;

public class EventAdapter extends ArrayAdapter<Event> {

    private final Context context;
    private final List<Event> events;
    private final SharedPreferences sharedPreferences;

    public EventAdapter(Context context, List<Event> events, SharedPreferences sharedPreferences) {
        super(context, 0, events);
        this.context = context;
        this.events = events;
        this.sharedPreferences = sharedPreferences;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Event event = events.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        }

        TextView title = convertView.findViewById(R.id.eventTitle);
        TextView time = convertView.findViewById(R.id.eventTime);
        ImageButton deleteBtn = convertView.findViewById(R.id.deleteBtn);

        title.setText(event.getTitle());
        time.setText(event.getTime() + " - " + event.getDate());

        deleteBtn.setOnClickListener(v -> {
            String key = event.getDate() + "_" + event.getTime();
            sharedPreferences.edit().remove(key).apply();
            events.remove(position);
            notifyDataSetChanged();
            Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show();
        });

        return convertView;
    }
}