package com.example.rapidrestore;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ProviderRequestsActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private RepairRequestAdapter adapter;
    private List<RepairRequest> requestList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String providerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_provider_requests);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        providerId = getIntent().getStringExtra("providerId");

        recyclerView = findViewById(R.id.recyclerViewRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RepairRequestAdapter(this, requestList);
        recyclerView.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {
        db.collection("repairRequests")
                .whereEqualTo("providerId", providerId)
              //  .orderBy("timestamp", Query.Direction.DESCENDING) //check later
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        requestList.clear();
                        Toast.makeText(ProviderRequestsActivity.this, "successful",
                                Toast.LENGTH_SHORT).show();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            String id = documentSnapshot.getId();
                            String name = documentSnapshot.getString("name");
                            String state = documentSnapshot.getString("state");
                            Date date = documentSnapshot.getDate("timestamp");
                            String description = documentSnapshot.getString("description");
                            List<String> images = (List<String>) documentSnapshot.get("images");
                            requestList.add(new RepairRequest(id, name, state,date, description, images));
                        }
                        adapter.notifyDataSetChanged();
                    }
                    else{
                        Toast.makeText(ProviderRequestsActivity.this, task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }
}