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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    EditText  etCost, etBio;
    Button saveButton;
    ImageButton btnEditImage;
    ImageView profileImage;
    Uri selectedImage;
    FirebaseFirestore db;
    String providerId;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = FirebaseFirestore.getInstance();


        etCost = findViewById(R.id.edit_cost);
        etBio = findViewById(R.id.edit_bio);
        saveButton = findViewById(R.id.save_button);
        btnEditImage = findViewById(R.id.editImageBtn);
        profileImage = findViewById(R.id.profileImage);

        btnEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open gallery to pick images
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);

            }

        });

        Intent intent = getIntent();
        providerId = intent.getStringExtra("providerId");

        db.collection("providers").document(providerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String bio = documentSnapshot.getString("bio");
                    Double costValue = documentSnapshot.getDouble("costPerHour");
                    String cost = (costValue != null) ? costValue.toString() : "";
                    String filename = documentSnapshot.getString("profilePicture");
                    String imageUrl = ImageUtils.getImageUrl(filename);

                    Glide.with(this)
                            .load(imageUrl)
                            .circleCrop()
                            .into(profileImage);
                    etBio.setText(bio);
                    etCost.setText(cost);
                });


        saveButton.setOnClickListener(v -> {
            Map<String, Object> edit = new HashMap<>();
            edit.put("costPerHour", Integer.parseInt(etCost.getText().toString()));
            edit.put("bio", etBio.getText().toString());

            db.collection("providers")
                    .document(providerId)
                    .update(edit)
                    .addOnSuccessListener(documentReference ->
                            Toast.makeText(this, "Profile Updated!!", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            Intent resultIntent = new Intent();
            resultIntent.putExtra("cost", etCost.getText().toString());
            resultIntent.putExtra("bio", etBio.getText().toString());
            resultIntent.putExtra("image", selectedImage != null ? selectedImage.toString() : null);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

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
    public void uploadProfilePicture(Bitmap bitmap, String providerId) {
        String url = "http://192.168.1.105:5000/upload"; // ← use your local IP
        String filename = "provider_" + providerId + "_profile.jpg";

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
}