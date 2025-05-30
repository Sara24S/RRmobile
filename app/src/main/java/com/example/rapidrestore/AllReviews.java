package com.example.rapidrestore;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AllReviews extends AppCompatActivity {

    private RecyclerView recyclerFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_reviews);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerFeedback = findViewById(R.id.recyclerFeedback);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Feedback> feedbackList = new ArrayList<>();
        FeedbackAdapter adapter = new FeedbackAdapter(feedbackList);

        recyclerFeedback.setLayoutManager(new LinearLayoutManager(this));
        recyclerFeedback.setAdapter(adapter);

        db.collection("feedback")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    feedbackList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Feedback feedback = doc.toObject(Feedback.class);
                        feedbackList.add(feedback);
                    }
                    adapter.notifyDataSetChanged();
                });

    }
}