package com.example.rapidrestore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminPage extends AppCompatActivity {

    private Button buttonCheckReviews, buttonCheckWorkRequests;

    private TextView logOut;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        buttonCheckReviews = findViewById(R.id.button_check_reviews);
        buttonCheckWorkRequests = findViewById(R.id.button_check_work_requests);
        logOut = findViewById(R.id.tvlogOut);

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminPage.this, MainActivity.class));
                finish();
            }
        });

        buttonCheckReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminPage.this, AllReviews.class);
               // intent.putExtra("adminId", adminId);
                startActivity(intent);
            }
        });

        buttonCheckWorkRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminPage.this, WorkRequests.class);
                startActivity(intent);
            }
        });
       /* db.collection("workRequests")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String state = dc.getDocument().getString("status");
                            String requestId = dc.getDocument().getId();
                            if (state.equals("pending")){
                                boolean isNotified = dc.getDocument().getBoolean("isNotified");
                                if (!isNotified){
                                    db.collection("workRequests")
                                            .document(requestId)
                                            .update("isNotified", true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                }
                                            });
                                    Intent intent = new Intent(this, WorkRequestView.class);
                                    intent.putExtra("userId", requestId);
                                    NotificationHelper.showNotification(
                                            this,
                                            "New Request Added",
                                            "please review request to accept or reject provider",
                                            intent
                                    );
                                }
                            }
                        }

                    }
                });

        */

    }
}