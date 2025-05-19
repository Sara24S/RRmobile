package com.example.rapidrestore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CareersPage extends AppCompatActivity {

    private ProviderAdapter adapter;
    private List<Provider> providerList;

    private RecyclerView recyclerView;
    private Button buttonFilter;
    private TextView myRepairRequests;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<Provider> filteredProviders;

    private Spinner spinnerRegion, spinnerProfession, spinnerPrice, spinnerRating;

    private String homeownerId;

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

        homeownerId = getIntent().getStringExtra("homeownerId");
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        providerList = new ArrayList<>();
        filteredProviders = new ArrayList<>();

        adapter = new ProviderAdapter(CareersPage.this, providerList, homeownerId);
        recyclerView.setAdapter(adapter);

        loadProducts();

        myRepairRequests.setOnClickListener(view -> {
            Intent intent = new Intent(CareersPage.this, HomeownerRepairRequests.class);
            intent.putExtra("homeownerId", homeownerId);
            startActivity(intent);
        });

        buttonFilter = findViewById(R.id.buttonFilter);
        buttonFilter.setOnClickListener(view -> {
            String selectedRegion = spinnerRegion.getSelectedItem().toString();
            String selectedProfession = spinnerProfession.getSelectedItem().toString();
            String selectedPrice = spinnerPrice.getSelectedItem().toString();
            String selectedRatingStr = spinnerRating.getSelectedItem().toString();

            List<Provider> filteredList = new ArrayList<>();

            for (Provider p : filteredProviders) {
                boolean matchesRegion = selectedRegion.equals("Any") || p.getRegion().equalsIgnoreCase(selectedRegion);
                boolean matchesProfession = selectedProfession.equals("Any") || p.getProfession().toLowerCase().contains(selectedProfession.toLowerCase());
                boolean matchesPrice = selectedPrice.equals("Any") || matchesPriceRange(p.getPrice(), selectedPrice);

                boolean matchesRating = true;
                if (!selectedRatingStr.equals("Any")) {
                    try {
                        int selectedRating = Integer.parseInt(selectedRatingStr);
                        matchesRating = (int) Math.floor(p.getRating()) == selectedRating;
                    } catch (NumberFormatException e) {
                        matchesRating = true;
                    }
                }

                if (matchesRegion && matchesProfession && matchesPrice && matchesRating) {
                    filteredList.add(p);
                }
            }

            providerList.clear();
            providerList.addAll(filteredList);
            adapter.notifyDataSetChanged();
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

    private void loadProducts() {
        db.collection("providers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        providerList.clear();
                        filteredProviders.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String id = doc.getId();
                            String name = doc.getString("name");
                            String region = doc.getString("region");
                            String profileImage = doc.getString("profilePicture");

                            ArrayList<String> professions = (ArrayList<String>) doc.get("profession");
                            String profession = "";
                            if (professions != null) {
                                for (String prof : professions) profession += prof + ", ";
                            }

                            Double rating = doc.getDouble("rating");
                            Double cost = doc.getDouble("costPerHour");

                            Provider p = new Provider(id, name, profession, rating, cost, region, profileImage);
                            providerList.add(p);
                            filteredProviders.add(p);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load providers", Toast.LENGTH_SHORT).show();
                    }
                });
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
                filterByName(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
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
            Toast.makeText(this, "Error filtering: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
