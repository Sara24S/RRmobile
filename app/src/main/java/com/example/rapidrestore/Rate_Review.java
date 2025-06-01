package com.example.rapidrestore;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Rate_Review extends AppCompatActivity {


    private RatingBar ratingBar;
    private EditText etReview;
    private Button btnSubmitReview;
    private String reviewId, providerId;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rate_review);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        reviewId = getIntent().getStringExtra("reviewId");

        db.collection("Reviews")
                .document(reviewId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        providerId = documentSnapshot.getString("providerId");
                    }
                });

        ratingBar = findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser) {
                    Toast.makeText(Rate_Review.this, "You rated: " + rating + " stars", Toast.LENGTH_SHORT).show();

                    // Here you can save the rating to database if you want
                }
            }
        });
        etReview = findViewById(R.id.etReview);

        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        btnSubmitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = ratingBar.getRating();
                String review = etReview.getText().toString().trim();

                updateRating();
                // add to firestore //remember to update the rating of the provider
                Map<String, Object> newReview = new HashMap<>();
                newReview.put("rating", rating);
                newReview.put("review", review);
                newReview.put("state", "complete");
                db.collection("Reviews")
                        .document(reviewId)
                        .update(newReview)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(Rate_Review.this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show();
                            updateRating();
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(Rate_Review.this, "Failed to submit review.", Toast.LENGTH_SHORT).show()
                        );
            }
        });
    }

    public Void updateRating(){
        db.collection("Reviews")
                .whereEqualTo("providerId", providerId)
                .whereEqualTo("state", "complete") // Only include valid reviews
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double total = 0;
                    int count = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Long rating = doc.getLong("rating");
                        // Toast.makeText(ProviderProfile.this, rating.toString(), Toast.LENGTH_LONG).show();
                        if (rating != null) {
                            total += rating;
                            count++;
                        }
                    }
                    if (count > 0) {
                        double average = total / count;
                        average = Math.floor(average * 10) / 10.0;

                        // Update provider's rating field
                        db.collection("providers")
                                .document(providerId)
                                .update("rating", average);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RatingUpdate", "Failed to calculate average rating", e);
                });

        return null;
    }

}