package com.example.rapidrestore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProviderProfile extends AppCompatActivity {

    ActivityResultLauncher<Intent> pickImageLauncher;
    String providerId, homeownerId, imageTitle;
    Button btnEditProfile, btnRequests, btnSaveChanges, btnAddRequest;
    ImageButton btnAddPrevWork, btnEditImage, chatIcon;
    ImageView profileImage;
    TextView tvName, tvCost, tvBio, tvRegion, tvProfession;
    Uri selectedImage;
    FirebaseFirestore db;
    EditText etCost, etBio;

    RecyclerView recyclerViewPortfolio;
    ArrayList<PortfolioPost> portfolioPostList = new ArrayList<>();
    PortfolioAdapter adapter;

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

        recyclerViewPortfolio = findViewById(R.id.portfolioRecycler);
        adapter = new PortfolioAdapter(this, portfolioPostList);
        recyclerViewPortfolio.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPortfolio.setAdapter(adapter);

        tvBio = findViewById(R.id.providerBio);
        tvCost = findViewById(R.id.providerPricing);
        tvName = findViewById(R.id.providerName);
        tvRegion = findViewById(R.id.tvRegion);
        tvProfession = findViewById(R.id.tvprofession);
        etBio = findViewById(R.id.etProviderBio);
        etCost = findViewById(R.id.etProviderPricing);

        btnAddPrevWork = findViewById(R.id.addProjectBtn);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditImage = findViewById(R.id.editImageBtn);
        profileImage = findViewById(R.id.profileImage);
        btnRequests = findViewById(R.id.btnRequests);
        btnSaveChanges = findViewById(R.id.btnSaveEditedProfile);
        btnAddRequest = findViewById(R.id.btnAddRequest);
        chatIcon = findViewById(R.id.chatIcon);

        chatIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ProviderProfile.this, ChatActivity.class);
            intent.putExtra("chatId", providerId + "_" + homeownerId);
            startActivity(intent);
        });

        if (isOwner) {
            btnAddPrevWork.setVisibility(View.VISIBLE);
            btnEditProfile.setVisibility(View.VISIBLE);
            btnEditImage.setVisibility(View.VISIBLE);
            btnRequests.setVisibility(View.VISIBLE);
            btnAddRequest.setVisibility(View.GONE);
        } else {
            btnEditImage.setVisibility(View.GONE);
            btnEditProfile.setVisibility(View.GONE);
            btnAddPrevWork.setVisibility(View.GONE);
            btnRequests.setVisibility(View.GONE);
            btnAddRequest.setVisibility(View.VISIBLE);
        }

        btnSaveChanges.setOnClickListener(view -> {
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

            db.collection("providers")
                    .document(providerId)
                    .update(edit)
                    .addOnSuccessListener(documentReference ->
                            Toast.makeText(ProviderProfile.this, "Profile Updated!!", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(ProviderProfile.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        btnRequests.setOnClickListener(view -> {
            Intent intent = new Intent(ProviderProfile.this, ProviderRequestsActivity.class);
            intent.putExtra("providerId", providerId);
            startActivity(intent);
        });

        btnEditProfile.setOnClickListener(v -> {
            etCost.setVisibility(View.VISIBLE);
            etBio.setVisibility(View.VISIBLE);
            etBio.setText(tvBio.getText().toString());
            btnSaveChanges.setVisibility(View.VISIBLE);
            btnEditProfile.setVisibility(View.GONE);
            tvCost.setVisibility(View.GONE);
            tvBio.setVisibility(View.GONE);
            btnRequests.setVisibility(View.GONE);
        });

        btnEditImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        btnAddPrevWork.setOnClickListener(v -> {
            Intent intent = new Intent(ProviderProfile.this, AddToPortfolio.class);
            intent.putExtra("providerId", providerId);
            startActivity(intent);
        });

        pickImageLauncher = registerForActivityResult(
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
                                uploadProfilePicture(bitmap, providerId);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );

        fetchProfile();
    }

    public void fetchProfile() {
        db.collection("providers").document(providerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String name = documentSnapshot.getString("name");
                    String bio = documentSnapshot.getString("bio");
                    Double costValue = documentSnapshot.getDouble("costPerHour");
                    String cost = (costValue != null) ? costValue.toString() : "";
                    String region = documentSnapshot.getString("region");
                    ArrayList<String> professions = (ArrayList<String>) documentSnapshot.get("profession");

                    if (professions != null) {
                        for (String prof : professions) {
                            tvProfession.append(prof + " || ");
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
        String url = "http://192.168.1.105:5000/upload";
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
                response -> db.collection("providers")
                        .document(providerId)
                        .update("profilePicture", filename)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(getApplicationContext(), "Profile picture updated", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(getApplicationContext(), "Failed to update Firestore", Toast.LENGTH_SHORT).show()),
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
