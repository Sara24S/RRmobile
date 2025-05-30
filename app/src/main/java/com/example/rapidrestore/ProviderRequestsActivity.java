package com.example.rapidrestore;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProviderRequestsActivity extends AppCompatActivity {


    RecyclerView recyclerView;
    private RepairRequestAdapter adapter;
    List<RepairRequest> requestList, filteredRequests;
    private FirebaseFirestore db;
    private String providerId;
    private Spinner spinnerState;
    private EditText etDate;
    private TextView tvSearch;

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
        db = FirebaseFirestore.getInstance();
        providerId = getIntent().getStringExtra("providerId");

        etDate = findViewById(R.id.etDate);
        tvSearch = findViewById(R.id.tvSearch);
        spinnerState = findViewById(R.id.spinnerState2);
        recyclerView = findViewById(R.id.recyclerViewRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestList = new ArrayList<>();
        adapter = new RepairRequestAdapter(this, requestList);
        recyclerView.setAdapter(adapter);

        filteredRequests = new ArrayList<>();

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) { filterRequests(); }
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerState.setOnItemSelectedListener(filterListener);

        loadRequests();

        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String selectedDate = "2025-05-19";
                String selectedDate = etDate.getText().toString();
                List<RepairRequest> filteredList = new ArrayList<>();
                for (RepairRequest p : filteredRequests) { // ✅ use original unfiltered list
                    if (p.getDate().equalsIgnoreCase(selectedDate)) {
                        filteredList.add(p);
                    }
                }
                requestList.clear();
                requestList.addAll(filteredList);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void filterRequests(){
        String selectedState = spinnerState.getSelectedItem().toString();

        List<RepairRequest> filteredList = new ArrayList<>();
        for (RepairRequest p : filteredRequests) { // ✅ use original unfiltered list
            boolean matchesState = selectedState.equals("all") || p.getState().equalsIgnoreCase(selectedState);

            if (matchesState) {
                filteredList.add(p);
            }
        }
        requestList.clear();
        requestList.addAll(filteredList);
        adapter.notifyDataSetChanged();

    }

    private void loadRequests() {
        db.collection("repairRequests")
                .whereEqualTo("providerId", providerId)
              //  .orderBy("timestamp", Query.Direction.DESCENDING) //check later
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        requestList.clear();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            String id = documentSnapshot.getId();
                            String name = documentSnapshot.getString("name");
                            String state = documentSnapshot.getString("state");
                            String day = documentSnapshot.getString("date");
                            String time = documentSnapshot.getString("time");
                            String dateTime = day + " at " + time;
                            Date date = documentSnapshot.getDate("timestamp");
                            String description = documentSnapshot.getString("description");
                            List<String> images = (List<String>) documentSnapshot.get("images");
                            requestList.add(new RepairRequest(id, name, state,date, description, images, dateTime, day));
                            filteredRequests.add(new RepairRequest(id, name, state,date, description, images, dateTime, day));
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