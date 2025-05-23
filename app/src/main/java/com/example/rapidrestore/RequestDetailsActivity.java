package com.example.rapidrestore;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestDetailsActivity extends AppCompatActivity {

    TextView textViewDetails;
    LinearLayout imageContainer;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String requestId, providerId, homeownerId, date, time;
    Button btnCompleted;
    private Button btnSetReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        textViewDetails = findViewById(R.id.textViewDetails);
        imageContainer = findViewById(R.id.imageContainerDetails);
        btnCompleted = findViewById(R.id.btnCompleted);

        requestId = getIntent().getStringExtra("requestId");
        Boolean isDeleted = getIntent().getBooleanExtra("isDeleted", false);
        if (isDeleted){
            btnCompleted.setVisibility(View.GONE);
        }

        btnSetReminder = findViewById(R.id.btnSetReminder);

        btnSetReminder.setOnClickListener(v -> showTimePickerDialog());
        createNotificationChannel();

        fetchRequest();
    }

    private void fetchRequest(){
        db.collection("repairRequests").document(requestId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String number = documentSnapshot.getString("phone");
                        String address = documentSnapshot.getString("address");
                        String issueLocation = documentSnapshot.getString("issueLocation");
                        String desc = documentSnapshot.getString("description");
                        date = documentSnapshot.getString("date");
                        time = documentSnapshot.getString("time");
                        providerId = documentSnapshot.getString("providerId");
                        homeownerId = documentSnapshot.getString("homeownerId");

                        textViewDetails.setText("Date and Time: " + date + " at " + time +
                                "\n\nName: " + name + "\n\nContact number: " + number +
                                "\n\nAddress: " + address + "\n\nIssue Location: " + issueLocation +
                                "\n\nDescription: " + desc);
                        //fix images then remove comment
                        List<String> images = (List<String>) documentSnapshot.get("images");
                        if (images != null) {
                            for (String uri : images) {
                                ImageView imageView = new ImageView(this);
                                imageView.setLayoutParams(new LinearLayout.LayoutParams(800, 500));
                                imageView.setPadding(10, 10, 10, 10);
                                String imageUrl = ImageUtils.getImageUrl(uri);
                                Glide.with(this)
                                        .load(imageUrl)
                                        //.centerCrop()
                                        //.circleCrop()
                                        .into(imageView);
                                imageContainer.addView(imageView);
                            }
                        }


                    }
                });
    }

    private void showTimePickerDialog() {
        Calendar current = Calendar.getInstance();
        int hour = current.get(Calendar.HOUR_OF_DAY);
        int minute = current.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setReminder(hourOfDay, minute1);
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setReminder(int hour, int minute) {
        // Get appointment date from Firestore (or passed data)
       // String date = "2025-05-19"; // Example
       // String time = "13:00";      // Example in 24hr format

        LocalDate localDate = LocalDate.parse(date);
        LocalTime localTime = LocalTime.parse(time);
        LocalDateTime appointmentDateTime = LocalDateTime.of(localDate, localTime);

        // User-chosen reminder time
        Calendar reminderTime = Calendar.getInstance();
        reminderTime.set(Calendar.YEAR, appointmentDateTime.getYear());
        reminderTime.set(Calendar.MONTH, appointmentDateTime.getMonthValue() - 1);
        reminderTime.set(Calendar.DAY_OF_MONTH, appointmentDateTime.getDayOfMonth());
        reminderTime.set(Calendar.HOUR_OF_DAY, hour);
        reminderTime.set(Calendar.MINUTE, minute);
        reminderTime.set(Calendar.SECOND, 0);

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("message", "Your appointment is at " + time + " on " + date);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent1 = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent1);
                return; // wait until user grants permission
            }
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);
        }
        //AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);

        Toast.makeText(this, "Reminder set!", Toast.LENGTH_SHORT).show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Reminder Channel";
            String description = "Channel for appointment reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("reminder_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void RepairCompleted(View view) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("state", "completed");
        requestData.put("date completed", FieldValue.serverTimestamp());

        Map<String, Object> review = new HashMap<>();
        review.put("state", "pending");
        review.put("homeownerId", homeownerId);
        review.put("providerId", providerId);

        db.collection("repairRequests")
                .document(requestId)
                .update(requestData)
                .addOnSuccessListener(documentReference ->
                    Toast.makeText(this, "Repair is done!!", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        //send a notification to homeowner to rate/review this provider
        db.collection("Reviews")
                .document(requestId)
                .set(review)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Review request is added", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());

        Intent intent = new Intent(this, ProviderProfile.class);
        intent.putExtra("providerId",providerId);
        intent.putExtra("isOwner", true);
        startActivity(intent);
        finish();
    }
}