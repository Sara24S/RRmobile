package com.example.rapidrestore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestDetailsActivity extends AppCompatActivity {

    TextView textViewDetails;
    LinearLayout imageContainer;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String requestId, providerId, homeownerId;
    Button btnCompleted;

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
        db.collection("repairRequests").document(requestId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String number = documentSnapshot.getString("phone");
                        String address = documentSnapshot.getString("address");
                        String issueLocation = documentSnapshot.getString("issueLocation");
                        String desc = documentSnapshot.getString("description");
                        String date = documentSnapshot.getString("date");
                        String time = documentSnapshot.getString("time");
                        providerId = documentSnapshot.getString("providerId");
                        homeownerId = documentSnapshot.getString("homeownerId");

                        textViewDetails.setText("Date and Time: " + date + " at " + time +
                                "\n\nName: " + name + "\n\nContact number: " + number +
                                "\n\nAddress: " + address + "\n\nIssue Location: " + issueLocation +
                                "\n\nDescription: " + desc + "\n\nIssue Images: ");
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