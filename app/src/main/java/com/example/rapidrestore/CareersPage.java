package com.example.rapidrestore;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class CareersPage extends AppCompatActivity {

    private static final String URL_PRODUCTS = "http://192.168.242.1/RRmobile/Api.php";

    //a list to store all the products
    private ProviderAdapter adapter, adapter1;
    List<Provider> providerList;

    //the recyclerview
    RecyclerView recyclerView;

    Button buttonLogOut;
    FirebaseAuth mAuth;
    private FirebaseFirestore db;

    JSONArray data;
    private List<Provider> filteredProviders;

    String homeownerId;
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

        homeownerId = getIntent().getStringExtra("homeownerId");
        Toast.makeText(CareersPage.this, homeownerId, Toast.LENGTH_SHORT).show();

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

/*
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        filteredProviders = new ArrayList<>();  // ✅ ensure it's not null Sara123@gmail.com

        adapter1 = new ProviderAdapter(CareersPage.this, filteredProviders, homeownerId);
        recyclerView.setAdapter(adapter1);

        fetchProviders(); // You need to implement this based on your data source

 */

    }

    private void fetchProviders() {
        // Example dummy data; replace this with Firestore fetching or other data source
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
                            String profession = documentSnapshot.getString("profession");
                            Double rating = documentSnapshot.getDouble("rating");
                            String region = documentSnapshot.getString("region");
                            Double costperhour = documentSnapshot.getDouble("costPerHour");
                            String profileimage = documentSnapshot.getString("profile image");//temp
                            providerList.add(new Provider(id,name, profession,1,1, region,profileimage));

                        }
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
                            String profession = documentSnapshot.getString("profession");
                            Double rating = documentSnapshot.getDouble("rating");
                            String region = documentSnapshot.getString("region");
                            Double costperhour = documentSnapshot.getDouble("costPerHour");
                            String profileimage = documentSnapshot.getString("profile image");//temp
                            providerList.add(new Provider(id,name, profession,1,1, region,profileimage));

                        }
                        adapter.notifyDataSetChanged();
                       /* Intent intent = new Intent(this, RepairRequestForm.class);//temp
                        intent.putExtra("homeownerId", homeownerId);// or document ID
                        startActivity(intent);

                        */
                    }
                    else{
                        Toast.makeText(CareersPage.this, "not successful",
                                Toast.LENGTH_SHORT).show();
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });

        /*
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_PRODUCTS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);
                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {
                                //getting product object from json array
                                JSONObject product = array.getJSONObject(i);
                                //adding the product to product list
                                providerList.add(new Provider(
                                        product.getInt("id"),
                                        product.getString("name"),
                                        product.getString("profession"),
                                        product.getDouble("rating"),
                                        product.getDouble("costperhour"),
                                        product.getString("region"),
                                        product.getString("profileimage")
                                ));
                            }

                            //creating adapter object and setting it to recyclerview
                            ProviderAdapter adapter = new ProviderAdapter(CareersPage.this, providerList);
                            recyclerView.setAdapter(adapter);
                        } catch (JSONException e) {
                            Toast.makeText(CareersPage.this, e.toString(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        //adding our stringrequest to queue
        queue.add(stringRequest);

         */

    }


}