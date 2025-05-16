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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class WorkRequests extends AppCompatActivity {

    private WorkRequestAdapter adapter;
    private List<ProvRequest> requestList;
    private FirebaseFirestore db;
    RecyclerView recyclerViewWorkRequests;

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

        recyclerViewWorkRequests = (RecyclerView) findViewById(R.id.recyclerViewWorkRequests);
        recyclerViewWorkRequests.setLayoutManager(new LinearLayoutManager(this));

        requestList = new ArrayList<>();
        adapter = new WorkRequestAdapter(requestList);
        recyclerViewWorkRequests.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchRequests();
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
                            //String date = documentSnapshot.getTimestamp("createdAt").toString();
                            Timestamp timestamp = documentSnapshot.getTimestamp("createdAt");
                            Date date = timestamp != null ? timestamp.toDate() : null;
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a z", Locale.getDefault());
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Optional: to match Firestore Console format
                            String formattedDate = date != null ? sdf.format(date) : "N/A";

                            requestList.add(new ProvRequest(id,name, formattedDate));
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
 /*               .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(WorkRequests.this, "successful",
                                Toast.LENGTH_SHORT).show();
                        //requestList.clear(); // clear old data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String date = document.getString("createdAt");

                            Toast.makeText(WorkRequests.this, "wow",
                                    Toast.LENGTH_SHORT).show();

                            //requestList.add(new ProvRequest(name, date));
                        }
                        //adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(WorkRequests.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });

  */
    }

}