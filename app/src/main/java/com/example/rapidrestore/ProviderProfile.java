package com.example.rapidrestore;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProviderProfile extends AppCompatActivity {


    //private static final int PICK_IMAGES_CODE = 1000;
    //ArrayList<Uri> imageUris = new ArrayList<>();
    private static final int PICK_IMAGE_REQUEST = 1;
    String providerId, homeownerId, imageTitle;
    Button btnEditProfile, btnRequests, btnSaveChanges, btnAddRequest, btnSetAvailability;
    ImageButton btnAddPrevWork, btnEditImage, chatIcon;
    ImageView profileImage;
    TextView tvName, tvCost, tvBio, tvRegion, tvProfession, tvShowPortfolio, tvHidePortfolio;
    Uri selectedImage;
    FirebaseFirestore db;

    EditText etCost, etBio;
    RecyclerView recyclerViewPortfolio, reviewsRecyclerView;
    ArrayList<PortfolioPost> portfolioPostList = new ArrayList<>();;
    PortfolioAdapter adapter;
    private ReviewAdapter adapter2;
    private List<Review> reviewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_provider_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        db = FirebaseFirestore.getInstance();
        providerId = getIntent().getStringExtra("providerId");
        homeownerId = getIntent().getStringExtra("homeownerId");
        boolean isOwner = getIntent().getBooleanExtra("isOwner", false);

        NotificationHelper.createNotificationChannel(this);

        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter2 = new ReviewAdapter(reviewList);
        reviewsRecyclerView.setAdapter(adapter2);

        recyclerViewPortfolio = findViewById(R.id.portfolioRecycler);
        adapter = new PortfolioAdapter(this, portfolioPostList);
        recyclerViewPortfolio.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPortfolio.setAdapter(adapter);

        tvBio = findViewById(R.id.providerBio);
        tvCost = findViewById(R.id.providerPricing);
        tvName = findViewById(R.id.providerName);
        tvRegion = findViewById(R.id.tvRegion);
        tvProfession = findViewById(R.id.tvprofession);
        tvShowPortfolio = findViewById(R.id.tvShowPortfolio);
        tvHidePortfolio = findViewById(R.id.tvHidePortfolio);
        etBio = findViewById(R.id.etProviderBio);
        etCost = findViewById(R.id.etProviderPricing);

        btnAddPrevWork =  findViewById(R.id.addProjectBtn);
        btnEditProfile =  findViewById(R.id.btnEditProfile);
        btnEditImage = findViewById(R.id.editImageBtn);
        profileImage = findViewById(R.id.profileImage);
        btnRequests = findViewById(R.id.btnRequests);
        btnSaveChanges = findViewById(R.id.btnSaveEditedProfile);
        btnAddRequest = findViewById(R.id.btnAddRequest);
        btnSetAvailability = findViewById(R.id.btnSetAvailability);
        chatIcon = findViewById(R.id.chatIcon);

        if (isOwner) {
            btnSetAvailability.setVisibility(View.VISIBLE);
            btnAddPrevWork.setVisibility(View.VISIBLE);
            btnEditProfile.setVisibility(View.VISIBLE);
            btnRequests.setVisibility(View.VISIBLE);
            btnAddRequest.setVisibility(View.GONE);
            chatIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ProviderProfile.this, ChatHomeowners.class);
                    intent.putExtra("providerId", providerId);
                    startActivity(intent);
                }
            });

            //refresh portfolio if modified
            db.collection("portfolioPost")
                    .whereEqualTo("providerId", providerId)
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null || snapshots == null) return;

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                loadPortfolio();
                            }
                        }
                    });

            //Receive notifications for new message


            //show notification when request added or deleted
            db.collection("repairRequests")
                    .whereEqualTo("providerId", providerId)
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null || snapshots == null) return;

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.MODIFIED) {
                                //Boolean isNotified = dc.getDocument().getBoolean("isNotified");
                                String state = dc.getDocument().getString("state");
                                String requestId = dc.getDocument().getId();
                                String date = dc.getDocument().getString("date");
                                String time = dc.getDocument().getString("time");
                                //when deleted
                                if (state.equals("deleted")){
                                    Intent intent = new Intent(this, RequestDetailsActivity.class);
                                    intent.putExtra("requestId", requestId);
                                    intent.putExtra("isDeleted", true);
                                    db.collection("repairRequests")
                                            .document(requestId)
                                            .update("isNotified", true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                }
                                            });
                                    NotificationHelper.showNotification(
                                            this,
                                            "Repair Request Deleted",
                                            "the appointment on " + date + " at " + time +
                                                    " has been deleted.",
                                            intent
                                    );
                                    //when added
                                }
                            }else if(dc.getType() == DocumentChange.Type.ADDED){
                                Boolean isNotified = dc.getDocument().getBoolean("isNotified");
                                String state = dc.getDocument().getString("state");
                                String requestId = dc.getDocument().getId();
                                String date = dc.getDocument().getString("date");
                                String time = dc.getDocument().getString("time");
                                if (state.equals("pending") && !isNotified.booleanValue()){
                                    Intent intent = new Intent(this, RequestDetailsActivity.class);
                                    intent.putExtra("requestId", requestId);
                                    db.collection("repairRequests")
                                            .document(requestId)
                                            .update("isNotified", true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                }
                                            });
                                    NotificationHelper.showNotification(
                                            this,
                                            "New Requests Added",
                                            "Appointment is added on " + date + " at " + time +
                                                    "\n tap to view",
                                            intent
                                    );
                                }
                            }
                        }
                    });
        } else {
            btnRequests.setVisibility(View.GONE);
            btnEditImage.setVisibility(View.GONE);
            btnEditProfile.setVisibility(View.GONE);
            btnAddPrevWork.setVisibility(View.GONE);
            btnAddRequest.setVisibility(View.VISIBLE);
            btnSetAvailability.setVisibility(View.GONE);

            chatIcon.setOnClickListener(view -> {
                Intent intent = new Intent(ProviderProfile.this, ChatActivity.class);
                intent.putExtra("homeownerId", homeownerId);
                intent.putExtra("providerId", providerId);
                startActivity(intent);
            });
        }
        //calculate rating of the provider
        db.collection("Reviews")
                .whereEqualTo("providerId", providerId)
                .whereEqualTo("state", "complete") // Only include valid reviews
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double total = 0;
                    int count = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Long rating = doc.getLong("rating");
                       // Toast.makeText(ProviderProfile.this, rating.toString(), Toast.LENGTH_LONG).show();
                        if (rating != null) {
                            total += rating;
                            count++;
                        }
                    }
                    if (count > 0) {
                        double average = total / count;

                        // Update provider's rating field
                        db.collection("providers")
                                .document(providerId)
                                .update("rating", average);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RatingUpdate", "Failed to calculate average rating", e);
                });

        btnSetAvailability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProviderProfile.this, ProviderAvailability.class);
                intent.putExtra("providerId", providerId);
                startActivity(intent);
            }
        });

        tvShowPortfolio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewPortfolio.setVisibility(View.VISIBLE);
                tvHidePortfolio.setVisibility(View.VISIBLE);
                tvShowPortfolio.setVisibility(View.GONE);

            }
        });

        tvHidePortfolio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewPortfolio.setVisibility(View.GONE);
                tvHidePortfolio.setVisibility(View.GONE);
                tvShowPortfolio.setVisibility(View.VISIBLE);
            }
        });

        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvCost.setText(etCost.getText().toString());
                tvBio.setText(etBio.getText().toString());
                Map<String, Object> edit = new HashMap<>();
                edit.put("costPerHour", Integer.parseInt(etCost.getText().toString()));
                edit.put("bio", etBio.getText().toString());
                tvCost.append(" $/h");
                etCost.setVisibility(View.GONE);
                etBio.setVisibility(View.GONE);
                btnSaveChanges.setVisibility(View.GONE);
                btnEditProfile.setVisibility(View.VISIBLE);
                tvCost.setVisibility(View.VISIBLE);
                tvBio.setVisibility(View.VISIBLE);
                btnRequests.setVisibility(View.VISIBLE);
                btnEditImage.setVisibility(View.GONE);
                btnAddPrevWork.setVisibility(View.VISIBLE);
                btnSetAvailability.setVisibility(View.VISIBLE);
                chatIcon.setVisibility(View.VISIBLE);

                db.collection("providers")
                        .document(providerId)
                        .update(edit)
                        .addOnSuccessListener(documentReference ->
                                Toast.makeText(ProviderProfile.this, "Profile Updated!!", Toast.LENGTH_LONG).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(ProviderProfile.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());

            }
        });

        btnRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProviderProfile.this, ProviderRequestsActivity.class);
                intent.putExtra("providerId", providerId);
                startActivity(intent);//change to edit page
            }
        });

        //Edit Profile
        btnEditProfile.setOnClickListener(v -> {
            etCost.setVisibility(View.VISIBLE);
            etBio.setVisibility(View.VISIBLE);
            etBio.setText(tvBio.getText().toString());
            btnSaveChanges.setVisibility(View.VISIBLE);
            btnEditImage.setVisibility(View.VISIBLE);
            btnEditProfile.setVisibility(View.GONE);
            tvCost.setVisibility(View.GONE);
            tvBio.setVisibility(View.GONE);
            btnRequests.setVisibility(View.GONE);
            btnAddPrevWork.setVisibility(View.GONE);
            btnSetAvailability.setVisibility(View.GONE);
            chatIcon.setVisibility(View.GONE);
        });
        //Change profile image
        btnEditImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        btnAddPrevWork.setOnClickListener(v ->{
            Intent intent = new Intent(ProviderProfile.this, AddToPortfolio.class);
            intent.putExtra("providerId", providerId);
            startActivity(intent);
        });

        fetchProfile();
        loadPortfolio();
        loadReviews();

    }

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

    //String name = "";
    private void loadReviews() {
        db.collection("Reviews")
                .whereEqualTo("providerId", providerId)
                .whereEqualTo("state", "complete") // Optional: filter approved only
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reviewList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                       String comment = doc.getString("review");
                        Long rating = doc.getLong("rating");
                        String homeownerId = doc.getString("homeownerId");

                        db.collection("users")
                                .document(homeownerId)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                String name = document.getString("name");
                                                reviewList.add(new Review(comment, rating, name));
                                                Toast.makeText(ProviderProfile.this, name, Toast.LENGTH_SHORT).show();
                                                adapter2.notifyDataSetChanged();
                                            } else {
                                                Log.d("USER_NAME", "No such document");
                                            }
                                        } else {
                                            Log.e("USER_NAME", "get failed with ", task.getException());
                                        }
                                    }
                                });
                       // reviewList.add(new Review(comment, rating, name));
                    }
                    //adapter2.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load reviews", Toast.LENGTH_SHORT).show()
                );
    }
    private void loadPortfolio() {
        db.collection("portfolioPost")
                .whereEqualTo("providerId",providerId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    portfolioPostList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        String desc = doc.getString("description");
                        List<String> images = (List<String>) doc.get("images");
                        //Date date = doc.getDate("timestamp");
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        Date date = timestamp != null ? timestamp.toDate() : null;
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
                        //sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Optional: to match Firestore Console format
                        String formattedDate = date != null ? sdf.format(date) : "N/A";
                       // String date = doc.getString("timestamp");
                        portfolioPostList.add(new PortfolioPost(desc, images, formattedDate));
                    }
                    adapter.notifyDataSetChanged();
                }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error fetching posts", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }


    ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        Glide.with(this)
                                .load(imageUri)
                                .circleCrop()
                                .placeholder(R.drawable.img)
                                .into(profileImage);
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            //uploadImage(bitmap);// 👈 upload it here
                            uploadProfilePicture(bitmap, providerId);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // Same logic to load image & upload
                    }
                }
            }
    );

    public void fetchProfile(){
        db.collection("providers").document(providerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String name = documentSnapshot.getString("name");
                    String bio = documentSnapshot.getString("bio");
                    Double costValue = documentSnapshot.getDouble("costPerHour");
                    String cost = (costValue != null) ? costValue.toString() : "";
                    String region = documentSnapshot.getString("region");
                    String profession ="";
                    ArrayList<String> professions = (ArrayList<String>) documentSnapshot.get("profession");
                    if (professions != null) {
                        for (String prof : professions) {
                            tvProfession.append(prof+" || ");
                        }
                    }

                    String filename = documentSnapshot.getString("profilePicture");
                    String imageUrl = ImageUtils.getImageUrl(filename);

                    Glide.with(this)
                            .load(imageUrl)
                            .circleCrop()
                            .into(profileImage);

                    tvBio.setText(bio);
                    tvName.setText(name);
                    tvCost.setText(cost);
                    tvCost.append(" $/h");
                    tvRegion.append(region);
                });
    }
    public void uploadProfilePicture(Bitmap bitmap, String providerId) {
        String url = ImageUtils.getUrl();
        String filename = bitmap.toString() + "_profile.jpg";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("image", encodedImage);
            jsonBody.put("filename", filename);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {

                    //Save URL to Firestore
                    db.collection("providers")
                            .document(providerId)
                            .update("profilePicture", filename)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(getApplicationContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(getApplicationContext(), "Failed to update Firestore", Toast.LENGTH_SHORT).show()
                            );
                },
                error -> Toast.makeText(getApplicationContext(), "Upload failed: " + error.toString(), Toast.LENGTH_LONG).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
    public void addRequest(View view) {
        Intent intent = new Intent(this, RepairRequestForm.class);
        intent.putExtra("providerId", providerId);
        intent.putExtra("homeownerId", homeownerId);
        startActivity(intent);
    }
}
