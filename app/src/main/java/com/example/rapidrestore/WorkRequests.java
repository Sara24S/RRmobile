package com.example.rapidrestore;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkRequests extends AppCompatActivity {

    private WorkRequestAdapter adapter;
    private List<ProvRequest> requestList, filteredRequests;
    private FirebaseFirestore db;
    RecyclerView recyclerViewWorkRequests;
    Spinner spinnerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_work_requests);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        db = FirebaseFirestore.getInstance();

        recyclerViewWorkRequests = (RecyclerView) findViewById(R.id.recyclerViewWorkRequests);
        recyclerViewWorkRequests.setLayoutManager(new LinearLayoutManager(this));
        spinnerState = findViewById(R.id.spinnerState);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) { filterRequests(); }
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerState.setOnItemSelectedListener(filterListener);


        requestList = new ArrayList<>();
        adapter = new WorkRequestAdapter(requestList);
        recyclerViewWorkRequests.setAdapter(adapter);
        filteredRequests = new ArrayList<>();

        fetchRequests();
    }

    public void filterRequests(){
        String selectedState = spinnerState.getSelectedItem().toString();

        List<ProvRequest> filteredList = new ArrayList<>();
        for (ProvRequest p : filteredRequests) { // ✅ use original unfiltered list
            boolean matchesState = selectedState.equals("all") || p.getState().equalsIgnoreCase(selectedState);

            if (matchesState) {
                filteredList.add(p);
            }
        }
        requestList.clear();
        requestList.addAll(filteredList);
        adapter.notifyDataSetChanged();

    }

    private void fetchRequests() {
        db.collection("workRequests")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        requestList.clear();
                        Toast.makeText(WorkRequests.this, "successful",
                                Toast.LENGTH_SHORT).show();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            String id = documentSnapshot.getId();
                            String name = documentSnapshot.getString("name");
                            String state = documentSnapshot.getString("status");
                            Timestamp timestamp = documentSnapshot.getTimestamp("createdAt");
                            Date date = timestamp != null ? timestamp.toDate() : null;
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
                            //sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Optional: to match Firestore Console format
                            String formattedDate = date != null ? sdf.format(date) : "N/A";

                            requestList.add(new ProvRequest(id,name, formattedDate, state));
                            filteredRequests.add(new ProvRequest(id,name, formattedDate, state));
                            //Toast.makeText(WorkRequests.this, formattedDate,Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();
                    }
                    else{
                        Toast.makeText(WorkRequests.this, "not successful",
                                Toast.LENGTH_SHORT).show();
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }

}