package com.example.rapidrestore;

import android.content.Intent;
import android.net.Uri;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProviderProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_provider_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button btnAddWork = findViewById(R.id.btnAddWork);

        btnAddWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open gallery to pick images
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);

            }

        });

        RatingBar ratingBar = findViewById(R.id.ratingBar);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser) {
                    Toast.makeText(ProviderProfile.this, "You rated: " + rating + " stars", Toast.LENGTH_SHORT).show();

                    // Here you can save the rating to database if you want
                }
            }
        });
        EditText etReview = findViewById(R.id.etReview);
        Button btnSubmitReview = findViewById(R.id.btnSubmitReview);
        RecyclerView reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);

        ArrayList<String> reviewsList = new ArrayList<>();
        ReviewsAdapter adapter = new ReviewsAdapter(reviewsList);

        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(adapter);

        btnSubmitReview.setOnClickListener(v -> {
            String review = etReview.getText().toString().trim();
            if (!review.isEmpty()) {
                reviewsList.add(review); // Add new review to list
                adapter.notifyItemInserted(reviewsList.size() - 1); // Update RecyclerView
                etReview.setText(""); // Clear the field
                Toast.makeText(this, "Review Submitted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please write something first.", Toast.LENGTH_SHORT).show();
            }
        });
    }@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();

            // You can now display this image in an ImageView, or upload it to your database
            Toast.makeText(this, "Image Selected!", Toast.LENGTH_SHORT).show();
        }
    }
}