package com.example.rapidrestore;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProviderAvailability extends AppCompatActivity {


    private CalendarView calendarView;
    private Button addTimeButton, saveButton;
    private RecyclerView timeListRecyclerView;
    private List<String> selectedTimes = new ArrayList<>();
    private String selectedDate; // yyyy-MM-dd
    private FirebaseFirestore db ;
    private String providerId;// get from intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_provider_availability);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = FirebaseFirestore.getInstance();
        providerId = getIntent().getStringExtra("providerId");

        calendarView = findViewById(R.id.calendarView);
        addTimeButton = findViewById(R.id.addTimeButton);
        saveButton = findViewById(R.id.saveButton);
        timeListRecyclerView = findViewById(R.id.timeListRecyclerView);

        TimeListAdapter adapter = new TimeListAdapter(selectedTimes);
        timeListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        timeListRecyclerView.setAdapter(adapter);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = sdf.format(new Date());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            selectedDate = sdf.format(cal.getTime());
            selectedTimes.clear();
            adapter.notifyDataSetChanged();
        });

        addTimeButton.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                if (!selectedTimes.contains(time)) {
                    selectedTimes.add(time);
                    adapter.notifyItemInserted(selectedTimes.size() - 1);
                }
            }, hour, minute, true).show();
        });

        saveButton.setOnClickListener(v -> {
            if (selectedDate != null && !selectedTimes.isEmpty()) {
                Map<String, Object> availability = new HashMap<>();
                availability.put("times", selectedTimes);

                db.collection("providerAvailability")
                        .document(providerId)
                        .collection("dates")
                        .document(selectedDate)
                        .set(availability)
                        .addOnSuccessListener(unused ->
                                Toast.makeText(this, "Availability saved", Toast.LENGTH_SHORT).show());
            }
        });
    }
}