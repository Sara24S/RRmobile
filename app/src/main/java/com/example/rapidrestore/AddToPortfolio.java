package com.example.rapidrestore;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
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

public class AddToPortfolio extends AppCompatActivity {

    private static final int PICK_IMAGES_CODE = 1000;
    ArrayList<Uri> imageUris = new ArrayList<>();
    LinearLayout imageContainer;
    Button buttonUploadPhotos;
    EditText editTextDescription;
    FirebaseFirestore db;
    String providerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_to_portfolio);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        providerId = getIntent().getStringExtra("providerId");

        imageContainer = findViewById(R.id.imageContainer);
        buttonUploadPhotos = findViewById(R.id.buttonUploadPhotos);
        editTextDescription = findViewById(R.id.etdescription);


        buttonUploadPhotos.setOnClickListener(v -> openGallery());
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
    public void uploadPicture(Bitmap bitmap) {
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
                response -> {},
                error -> Toast.makeText(getApplicationContext(), "Upload failed: " + error.toString(), Toast.LENGTH_LONG).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
    private void addImageToContainer(Uri uri) {
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(400, 400));
        imageView.setPadding(5, 5, 5, 5);
        imageView.setImageURI(uri);
        imageContainer.addView(imageView);
    }

    public void add(View view) {

        String description = editTextDescription.getText().toString().trim();

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("providerId", providerId);
        requestData.put("description", description);
        requestData.put("timestamp", FieldValue.serverTimestamp());

        // Placeholder for image URIs,find a way to upload theme somewhere DONT FORGET
        ArrayList<String> imagePaths = new ArrayList<>();
        for (Uri uri : imageUris) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                String filename = bitmap.toString() + "_picture.jpg";
                uploadPicture(bitmap);
                imagePaths.add(filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        requestData.put("images", imagePaths);


        db.collection("portfolioPost")
                .add(requestData)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Request submitted successfully!", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}