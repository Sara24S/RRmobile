package com.example.rapidrestore;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CareersPage extends AppCompatActivity {

    private ProviderAdapter adapter, adapter1;
    List<Provider> providerList;

    //the recyclerview
    RecyclerView recyclerView;

    Button buttonFilter;
    FirebaseAuth mAuth;
    TextView myRepairRequests, tvResetFilter;
    private FirebaseFirestore db;
    private List<Provider> filteredProviders;

    Spinner spinnerRegion, spinnerProfession, spinnerPrice, spinnerRating;

    String homeownerId;
    private ImageView filter1, filter2;

    LinearLayout llFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_careers_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinnerRegion = findViewById(R.id.spinnerRegion);
        spinnerProfession = findViewById(R.id.spinnerProfession);
        spinnerPrice = findViewById(R.id.spinnerPrice);
        spinnerRating = findViewById(R.id.spinnerRating);
        myRepairRequests = findViewById(R.id.tvMyRepairRequests);
        tvResetFilter = findViewById(R.id.tvResetFilter);
        filter1 = findViewById(R.id.ivFilter);
        filter2 = findViewById(R.id.ivFilter2);
        llFilter = findViewById(R.id.filter);

        llFilter.setVisibility(View.GONE);

        homeownerId = getIntent().getStringExtra("homeownerId");

        NotificationHelper.createNotificationChannel(this);

        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();

        //getting the recyclerview from xml
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        providerList = new ArrayList<>();
        adapter = new ProviderAdapter(CareersPage.this, providerList, homeownerId);
        recyclerView.setAdapter(adapter);
        loadProducts();

        filter1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llFilter.setVisibility(View.VISIBLE);
                filter2.setVisibility(View.VISIBLE);
                filter1.setVisibility(View.GONE);
            }
        });

        filter2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter1.setVisibility(View.VISIBLE);
                llFilter.setVisibility(View.GONE);
                filter2.setVisibility(View.GONE);
            }
        });

        myRepairRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CareersPage.this, HomeownerRepairRequests.class);
                intent.putExtra("homeownerId", homeownerId);
                startActivity(intent);
            }
        });

        tvResetFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                providerList.clear();
                providerList.addAll(filteredProviders);
                adapter.notifyDataSetChanged();
                spinnerPrice.setSelection(0);
                spinnerProfession.setSelection(0);
                spinnerRating.setSelection(0);
                spinnerRegion.setSelection(0);
            }
        });


       // Toolbar toolbar = findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);
        filteredProviders = new ArrayList<>();  // ✅ ensure it's not null Sara123@gmail.com

        //adapter1 = new ProviderAdapter(CareersPage.this, filteredProviders, homeownerId);

        //recyclerView.setAdapter(adapter1);

        //fetchProviders();

        buttonFilter = findViewById(R.id.buttonFilter);
        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedRegion = spinnerRegion.getSelectedItem().toString();
                String selectedProfession = spinnerProfession.getSelectedItem().toString();
                String selectedPrice = spinnerPrice.getSelectedItem().toString();
                String selectedRatingStr = spinnerRating.getSelectedItem().toString();

                List<Provider> filteredList = new ArrayList<>();

                for (Provider p : filteredProviders) { // ✅ use original unfiltered list
                    boolean matchesRegion = selectedRegion.equals("Any") || p.getRegion().equalsIgnoreCase(selectedRegion);
                    boolean matchesProfession = selectedProfession.equals("Any") || p.getProfession().toLowerCase().contains(selectedProfession.toLowerCase());
                    boolean matchesPrice = selectedPrice.equals("Any") || matchesPriceRange(p.getPrice(), selectedPrice);

                    boolean matchesRating = true;
                    if (!selectedRatingStr.equals("Any")) {
                        try {
                            int selectedRating = Integer.parseInt(selectedRatingStr);
                            matchesRating = (int) Math.floor(p.getRating()) == selectedRating;
                        } catch (NumberFormatException e) {
                            matchesRating = true; // fail-safe
                        }
                    }

                    if (matchesRegion && matchesProfession && matchesPrice && matchesRating) {
                        filteredList.add(p);
                    }
                }

                providerList.clear();
                providerList.addAll(filteredList);
                adapter.notifyDataSetChanged();
            }
        });

        db.collection("Reviews")
                .whereEqualTo("homeownerId", homeownerId)
                .whereEqualTo("state", "pending")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String reviewId = dc.getDocument().getId();
                            Intent intent = new Intent(this, Rate_Review.class);
                            intent.putExtra("reviewId", reviewId);
                            NotificationHelper.showNotification(
                                    this,
                                    "Rate your provider!!",
                                    "Please leave a rating/review for your recent request.",
                                    intent
                            );
                        }
                    }
                });

    }
    private boolean matchesPriceRange(double price, String selectedRange) {
        switch (selectedRange) {
            case "Under $30": return price < 30;
            case "$30 - $50": return price >= 30 && price <= 50;
            case "Above $50": return price > 50;
            default: return true;
        }
    }
  /*  private void fetchProviders() {
        String spinnRegion = spinnerRegion.getSelectedItem().toString();
        // Example dummy data; replace this with Firestore fetching or other data source
        db.collection("providers")
                .whereEqualTo("region", spinnRegion)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        providerList.clear();
                        Toast.makeText(CareersPage.this, "successful",
                                Toast.LENGTH_SHORT).show();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            String id = documentSnapshot.getId();
                            String name = documentSnapshot.getString("name");
                            String profession ="";
                            ArrayList<String> professions = (ArrayList<String>) documentSnapshot.get("profession");
                            if (professions != null) {
                                for (String prof : professions) {
                                    profession+=prof+", ";
                                }
                            }
                            Double rating = documentSnapshot.getDouble("rating");
                            String region = documentSnapshot.getString("region");
                            Double costperhour = documentSnapshot.getDouble("costPerHour");
                            String profileimage = documentSnapshot.getString("profile image");//temp
                            providerList.add(new Provider(id,name, profession,1,1, region,profileimage));

                        }
                        recyclerView.setAdapter(adapter1);
                        filteredProviders.clear();
                        filteredProviders.addAll(providerList);
                        adapter1.notifyDataSetChanged();
                    }
                    else{
                        Toast.makeText(CareersPage.this, "not successful",
                                Toast.LENGTH_SHORT).show();
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }

   */

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Optional: Notify user that permission is required
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("Search by name...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String lowerQuery = query.toLowerCase();

                Toast.makeText(CareersPage.this, lowerQuery,Toast.LENGTH_SHORT).show();

                //Toast.makeText(CareersPage.this, filteredProviders.size(),Toast.LENGTH_SHORT).show();

                filterByName(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //String lowerQuery = newText.toLowerCase();
                //Toast.makeText(CareersPage.this, lowerQuery,Toast.LENGTH_SHORT).show();
                //filterByName(newText);
                return true;
            }
        });

        return true;
    }
    private void filterByName(String query) {
        try {
            filteredProviders.clear();
            for (Provider p : providerList) {
                if (p.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredProviders.add(p);
                }
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("FilterError", "Error while filtering: " + e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void loadProducts(){

        db.collection("providers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        providerList.clear();
                        Toast.makeText(CareersPage.this, "successful",
                                Toast.LENGTH_SHORT).show();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            String id = documentSnapshot.getId();
                            String name = documentSnapshot.getString("name");
                            String profession ="";
                            ArrayList<String> professions = (ArrayList<String>) documentSnapshot.get("profession");
                            if (professions != null) {
                                for (String prof : professions) {
                                    profession+=prof+", ";
                                }
                            }
                            Double rating = documentSnapshot.getDouble("rating");
                            String region = documentSnapshot.getString("region");
                            Double costperhour = documentSnapshot.getDouble("costPerHour");
                            String profileimage = documentSnapshot.getString("profilePicture");//temp
                            providerList.add(new Provider(id,name, profession,rating,costperhour, region,profileimage));
                            filteredProviders.add(new Provider(id,name, profession,rating,costperhour, region,profileimage));

                        }
                        adapter.notifyDataSetChanged();
                    }
                    else{
                        Toast.makeText(CareersPage.this, "not successful",
                                Toast.LENGTH_SHORT).show();
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }


}