package com.example.rapidrestore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
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


    //private static final int PICK_IMAGES_CODE = 1000;
    //ArrayList<Uri> imageUris = new ArrayList<>();
    private static final int PICK_IMAGE_REQUEST = 1;
    String providerId, homeownerId, imageTitle;
    Button btnEditProfile, btnRequests, btnSaveChanges, btnAddRequest;
    ImageButton btnAddPrevWork, btnEditImage;
    ImageView profileImage;
    TextView tvName, tvCost, tvBio, tvRegion, tvProfession;
    Uri selectedImage;
    FirebaseFirestore db;

    EditText etCost, etBio;

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

        tvBio = findViewById(R.id.providerBio);
        tvCost = findViewById(R.id.providerPricing);
        tvName = findViewById(R.id.providerName);
        tvRegion = findViewById(R.id.tvRegion);
        tvProfession = findViewById(R.id.tvprofession);
        etBio = findViewById(R.id.etProviderBio);
        etCost = findViewById(R.id.etProviderPricing);

        btnAddPrevWork =  findViewById(R.id.addProjectBtn);
        btnEditProfile =  findViewById(R.id.btnEditProfile);
        btnEditImage = findViewById(R.id.editImageBtn);
        profileImage = findViewById(R.id.profileImage);
        btnRequests = findViewById(R.id.btnRequests);
        btnSaveChanges = findViewById(R.id.btnSaveEditedProfile);
        btnAddRequest = findViewById(R.id.btnAddRequest);

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

        ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String cost = data.getStringExtra("cost");
                        String bio = data.getStringExtra("bio");
                        String image = data.getStringExtra("image");

                        if (image != null) {
                            Glide.with(this)
                                    .load(Uri.parse(image))
                                    .circleCrop()
                                    .placeholder(R.drawable.img)
                                    .into(profileImage);
                        }

                        if (cost != null) {
                            tvCost.setText(cost);
                        }
                        if (bio != null) {
                            tvBio.setText(bio);
                        }
                    }
                }
        );

        //Edit Profile
        btnEditProfile.setOnClickListener(v -> {
            etCost.setVisibility(View.VISIBLE);
            etBio.setVisibility(View.VISIBLE);
            etBio.setText(tvBio.getText().toString());
            btnSaveChanges.setVisibility(View.VISIBLE);
            btnEditProfile.setVisibility(View.GONE);
            tvCost.setVisibility(View.GONE);
            tvBio.setVisibility(View.GONE);
            btnRequests.setVisibility(View.GONE);



          /*  Intent intent = new Intent(this, EditProfile.class);
            intent.putExtra("providerId", providerId);
            editProfileLauncher.launch(intent);

           */
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

        /*
        EditText etReview = findViewById(R.id.etReview);
        Button btnSubmitReview = findViewById(R.id.btnSubmitReview);
        RecyclerView reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);

        ArrayList<String> reviewsList = new ArrayList<>();
        ReviewsAdapter adapter = new ReviewsAdapter(reviewsList);

        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(adapter);

        btnSubmitReview.setOnClickListener(v -> {
            String review = etReview.getText().toString().trim();
            if (!review.isEmpty()) {
                reviewsList.add(review);
                adapter.notifyItemInserted(reviewsList.size() - 1);
                etReview.setText("");
                Toast.makeText(this, "Review Submitted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please write something first.", Toast.LENGTH_SHORT).show();
            }
        });

         */

        fetchProfile();

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
                    String imageUrl = "http://192.168.1.105:5000/uploads/" + filename;

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


/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_CODE && resultCode == RESULT_OK) {
            imageUris.clear();
            imageContainer.removeAllViews();

            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    imageUris.add(imageUri);
                    addImageToContainer(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                imageUris.add(imageUri);
                addImageToContainer(imageUri);
            }
        }
    }

    private void addImageToContainer(Uri uri) {
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        imageView.setPadding(8, 8, 8, 8);
        imageView.setImageURI(uri);
        imageContainer.addView(imageView);
    }

 */
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
            Uri imageUri = Uri.parse(selectedImage.toString());
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


            // You can now display this image in an ImageView, or upload it to your database
            Toast.makeText(this, "Image Selected!", Toast.LENGTH_SHORT).show();
        }
    }

 */

    public void uploadProfilePicture(Bitmap bitmap, String providerId) {
        String url = "http://192.168.1.105:5000/upload"; // ← use your local IP
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
                    // 1. Construct image URL
                    //String imageUrl = "http://192.168.1.105:5000/uploads/" + filename;

                    // 2. Save URL to Firestore
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
