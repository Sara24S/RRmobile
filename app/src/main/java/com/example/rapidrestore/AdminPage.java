package com.example.rapidrestore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminPage extends AppCompatActivity {

    private Button buttonCheckReviews, buttonCheckWorkRequests;
    private String adminId;

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
        buttonCheckReviews = findViewById(R.id.button_check_reviews);
        buttonCheckWorkRequests = findViewById(R.id.button_check_work_requests);
       // adminId = getIntent().getStringExtra("adminId");

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


    }
}