package com.example.rapidrestore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkRequestForm extends AppCompatActivity {
    //add the images functions

    Spinner spinnerRegions;
    String spinnerSelection, experience, email, uID, profession, url;
    boolean checkedCarpenter, checkedElectrician, checkedRoofer, checkedGlassTechnican, checkedMason,
            checkedPlumber,checkedLocksmith;
    TextInputEditText editTextName, editTextCertification , etNumber;

    TextView imageId, imageCertificant;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String idFilename, certificationFilename;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.work_request_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        idFilename = "";
        imageId = findViewById(R.id.IdCard);
        imageId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1000); // 1000 = request code

            }
        });
        imageCertificant = findViewById(R.id.certificationImage);
        imageCertificant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1001); // 1000 = request code

            }
        });


        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        uID = intent.getStringExtra("UID");

        editTextName = findViewById(R.id.edit_text_fullName);
        editTextCertification = findViewById(R.id.edit_text_certification);
        etNumber = findViewById(R.id.edit_text_Number);
        etNumber.setText("");
        editTextName.setText("");
        editTextCertification.setText("");

        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton radioButton = findViewById(checkedId);
            experience = radioButton.getText().toString();
            //Toast.makeText(this,experience , Toast.LENGTH_LONG).show();
        });

        checkedCarpenter = false;
        checkedElectrician = false;
        checkedLocksmith = false;
        checkedGlassTechnican = false;
        checkedMason = false;
        checkedRoofer = false;
        checkedPlumber = false;

        spinnerRegions = findViewById(R.id.spinner_regions);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(WorkRequestForm.this,
                R.array.regions_array, android.R.layout.simple_spinner_dropdown_item);
        spinnerRegions.setAdapter(adapter);
        spinnerRegions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spinnerSelection = adapterView.getItemAtPosition(i).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                spinnerSelection = "Nothing is selected";
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri imageUri = data.getData(); // works for gallery
            if(requestCode == 1000){
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);//uploadImage(bitmap);// 👈 upload it here
                    uploadIdCard(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Show preview in an ImageView
                ImageView IdCard = findViewById(R.id.image);
                IdCard.setImageURI(imageUri);
            }
            else if (requestCode == 1001){
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);//uploadImage(bitmap);// 👈 upload it here
                    uploadCertification(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void uploadIdCard(Bitmap bitmap) {
        String url = ImageUtils.getUrl(); // ← use your local IP
        idFilename = bitmap.toString() + "_IdCard.jpg";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("image", encodedImage);
            jsonBody.put("filename", idFilename);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                },
                error -> Toast.makeText(getApplicationContext(), "Upload failed: " + error.toString(), Toast.LENGTH_LONG).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    public void uploadCertification(Bitmap bitmap) {
        String url = ImageUtils.getUrl(); // ← use your local IP
        certificationFilename = bitmap.toString() + "_Certification.jpg";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("image", encodedImage);
            jsonBody.put("filename", certificationFilename);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                },
                error -> Toast.makeText(getApplicationContext(), "Upload failed: " + error.toString(), Toast.LENGTH_LONG).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
    public void submit(View view) {

        if(idFilename.isEmpty()){
            Toast.makeText(WorkRequestForm.this, "Id image is mandatory", Toast.LENGTH_SHORT).show();
            return;
        }


        ArrayList<String> profession = new ArrayList<>();
        if(checkedCarpenter) profession.add("carpenter");
        if(checkedElectrician) profession.add("electrician");
        if(checkedLocksmith) profession.add("locksmith");
        if(checkedGlassTechnican) profession.add("glass technician");
        if(checkedMason) profession.add("mason");
        if(checkedRoofer) profession.add("roofer");
        if(checkedPlumber) profession.add("plumber");

        String name = editTextName.getText().toString();
        String certification = editTextCertification.getText().toString();
        String number = etNumber.getText().toString();
        if(certification.isEmpty())
            certification="";
        String address="";
        
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("phone number", number);
        user.put("address", address);
        user.put("profession", profession);
        user.put("crtification", certification);
        user.put("certification image", certificationFilename);
        user.put("IdCard", idFilename);
        user.put("experience", experience);
        user.put("region", spinnerSelection);
        user.put("provider ID", uID);
        user.put("createdAt", FieldValue.serverTimestamp());
        user.put("status", "pending");
        user.put("isNotified", false);

        db.collection("workRequests")
                .add(user)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(WorkRequestForm.this, "Request sent", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(WorkRequestForm.this, e.getMessage(), Toast.LENGTH_SHORT).show());

        editTextName.setText("");
        editTextCertification.setText("");
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }


    public void checkBoxProfession(View view) {
        if (view.getId() == R.id.checkbox_carpenter)
            checkedCarpenter = ((CheckBox)view).isChecked();
        else if (view.getId() == R.id.checkbox_electrician)
            checkedElectrician = ((CheckBox)view).isChecked();
        else if (view.getId() == R.id.checkbox_locksmit)
            checkedLocksmith = ((CheckBox)view).isChecked();
        else if (view.getId() == R.id.checkbox_mason)
            checkedMason = ((CheckBox)view).isChecked();
        else if (view.getId() == R.id.checkbox_plumber)
            checkedPlumber = ((CheckBox)view).isChecked();
        else if (view.getId() == R.id.checkbox_glassTechnican)
            checkedGlassTechnican = ((CheckBox)view).isChecked();
        else if (view.getId() == R.id.checkbox_roofer)
            checkedRoofer = ((CheckBox)view).isChecked();
    }




}