package com.example.rapidrestore;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class RepairRequestForm extends AppCompatActivity {

    private static final int PICK_IMAGES_CODE = 1000;
    ArrayList<Uri> imageUris = new ArrayList<>();
    LinearLayout imageContainer;
    Button buttonUploadPhotos, buttonSubmit;
    EditText editTextName, editTextPhone, editTextEmail, editTextAddress;
    EditText editTextIssueLocation, editTextDescription;
    FirebaseFirestore db;
    String providerId, homeownerId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_repair_request_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        providerId = getIntent().getStringExtra("providerId");
        homeownerId = getIntent().getStringExtra("homeownerId");

        Toast.makeText(RepairRequestForm.this, providerId, Toast.LENGTH_SHORT).show();
        Toast.makeText(RepairRequestForm.this, homeownerId, Toast.LENGTH_SHORT).show();

        imageContainer = findViewById(R.id.imageContainer);
        buttonUploadPhotos = findViewById(R.id.buttonUploadPhotos);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextIssueLocation = findViewById(R.id.editTextIssueLocation);
        editTextDescription = findViewById(R.id.editTextDescription);

        buttonUploadPhotos.setOnClickListener(v -> openGallery());
        buttonSubmit.setOnClickListener(v -> submitForm());

        db = FirebaseFirestore.getInstance();


    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES_CODE);
    }

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
        imageView.setLayoutParams(new LinearLayout.LayoutParams(400, 400));
        imageView.setPadding(5, 5, 5, 5);
        imageView.setImageURI(uri);
        imageContainer.addView(imageView);
    }

    private void submitForm() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String issueLocation = editTextIssueLocation.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("homeownerId", homeownerId);
        requestData.put("providerId", providerId);
        requestData.put("name", name);
        requestData.put("phone", phone);
        requestData.put("email", email);
        requestData.put("address", address);
        requestData.put("issueLocation", issueLocation);
        requestData.put("description", description);
        requestData.put("timestamp", FieldValue.serverTimestamp());
        requestData.put("state", "pending");

        // Placeholder for image URIs,find a way to upload theme somewhere DONT FORGET
        ArrayList<String> imagePaths = new ArrayList<>();
        for (Uri uri : imageUris) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                String filename = bitmap.toString() + "_picture.jpg";
                uploadRepairPicture(bitmap);
                imagePaths.add(filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        requestData.put("images", imagePaths);


        db.collection("repairRequests")
                .add(requestData)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Booked!!", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        finish();
    }
    public void uploadRepairPicture(Bitmap bitmap) {
        String url = "http://192.168.1.105:5000/upload"; // ← use your local IP
        String filename = bitmap.toString() + "_picture.jpg";

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
                response -> {//Toast.makeText(getApplicationContext(), "Uploaded!!", Toast.LENGTH_LONG).show();
                    },
                error -> Toast.makeText(getApplicationContext(), "Upload failed: " + error.toString(), Toast.LENGTH_LONG).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

}