package com.example.rapidrestore;

import android.os.Bundle;
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

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Rate_Review extends AppCompatActivity {


    private RatingBar ratingBar;
    private EditText etReview;
    private Button btnSubmitReview;
    private String reviewId;
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
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(Rate_Review.this, "Failed to submit review.", Toast.LENGTH_SHORT).show()
                        );
            }
        });
    }

}