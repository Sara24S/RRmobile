package com.example.rapidrestore;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    private Button btnSubmitFeedback;
    private EditText etFeedback;
    private String userId, userType;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feedback);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etFeedback = findViewById(R.id.etFeedback);
        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);

        db = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("userId");
        db.collection("users")
                .document(userId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                userType = documentSnapshot.getString("role");
            }
        });


        btnSubmitFeedback.setOnClickListener(v -> {
            String feedbackText = etFeedback.getText().toString().trim();
            if (feedbackText.isEmpty()) {
                Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Map<String, Object> feedback = new HashMap<>();
            feedback.put("userId", userId);
            feedback.put("userType", userType);
            feedback.put("comment", feedbackText);
            feedback.put("timestamp", FieldValue.serverTimestamp());

            FirebaseFirestore.getInstance()
                    .collection("feedback")
                    .add(feedback)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Feedback submitted", Toast.LENGTH_SHORT).show();
                        etFeedback.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}