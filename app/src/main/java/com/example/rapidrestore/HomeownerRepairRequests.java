package com.example.rapidrestore;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeownerRepairRequests extends AppCompatActivity {


    private RecyclerView recyclerView;
    private HomeownerRepairRequestAdapter adapter;
    private List<HomeownerRepairRequest> requestList = new ArrayList<>();
    private FirebaseFirestore db;
    private String homeownerId;
    Spinner spinnerFilterStatus;
    List<HomeownerRepairRequest> fullList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homeowner_repair_requests);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerViewRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HomeownerRepairRequestAdapter(requestList);
        recyclerView.setAdapter(adapter);
        homeownerId = getIntent().getStringExtra("homeownerId");

        db = FirebaseFirestore.getInstance();
        spinnerFilterStatus = findViewById(R.id.spinnerFilterStatus);
        String[] statusOptions = {"all", "pending", "completed"};

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterStatus.setAdapter(spinnerAdapter);

        spinnerFilterStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterRequests(spinnerFilterStatus.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                HomeownerRepairRequest request = requestList.get(position);

                if (direction == ItemTouchHelper.LEFT) {
                    if (!"pending".equalsIgnoreCase(request.getStatus())) {
                        // Not deletable
                        Toast.makeText(HomeownerRepairRequests.this, "Only pending requests can be deleted.", Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(position); // Revert swipe
                        return;
                    }
                    // Show confirmation dialog before delete
                    new AlertDialog.Builder(HomeownerRepairRequests.this)
                            .setTitle("Confirm Deletion")
                            .setMessage("Are you sure you want to delete this request?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                db.collection("repairRequests").document(request.getId())
                                        //.delete()//to delete from firestore too
                                        .update("state", "deleted")
                                        .addOnSuccessListener(aVoid -> {
                                            requestList.remove(position);
                                            adapter.notifyItemRemoved(position);
                                            Toast.makeText(HomeownerRepairRequests.this, "Request deleted", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                adapter.notifyItemChanged(position); // Revert swipe
                            })
                            .setCancelable(false)
                            .show();
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
        fetchRequests();
    }
    private void fetchRequests() {
        db.collection("repairRequests")
                .whereEqualTo("homeownerId", homeownerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    requestList.clear();
                    fullList.clear();//forFilter

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String providerId = doc.getString("providerId");
                        String status = doc.getString("state");
                        String location = doc.getString("issueLocation");
                        String Day = doc.getString("date");
                        String time = doc.getString("time");
                        String dateTime = Day + " at " + time;
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        Date date = timestamp != null ? timestamp.toDate() : null;
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a z", Locale.getDefault());
                        //sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Optional: to match Firestore Console format
                        String formattedDate = date != null ? sdf.format(date) : "N/A";
                        if(!status.equals("deleted")){
                            fetchProviderNameAndAdd(doc.getId(), providerId, dateTime, status, location);
                        }

                    }
                });
    }
    private void fetchProviderNameAndAdd(String id, String providerId, String date, String status, String location) {
        db.collection("providers").document(providerId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    String providerName = snapshot.getString("name");
                    requestList.add(new HomeownerRepairRequest(id, providerId, providerName, date, status, location));
                    fullList.add(new HomeownerRepairRequest(id, providerId, providerName, date, status, location)); // preserve original
                    adapter.notifyDataSetChanged();
                });
    }

    private void filterRequests(String selectedStatus) {
        List<HomeownerRepairRequest> filtered = new ArrayList<>();

        for (HomeownerRepairRequest request : fullList) {
            if (selectedStatus.equals("all") || request.getStatus().equalsIgnoreCase(selectedStatus)) {
                filtered.add(request);
            }
        }

        requestList.clear();
        requestList.addAll(filtered);
        adapter.notifyDataSetChanged();
    }
}