package com.example.rapidrestore;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class WorkRequestForm extends AppCompatActivity {
    //add the images functions

    Spinner spinnerRegions;
    String spinnerSelection, experience, email, uID, profession, url;
    boolean checkedCarpenter, checkedElectrician, checkedRoofer, checkedGlassTechnican, checkedMason,
            checkedPlumber,checkedLocksmith;
    TextInputEditText editTextName, editTextCertification;

    TextView image;
    FirebaseFirestore db = FirebaseFirestore.getInstance();




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
        image = findViewById(R.id.certificationImage);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1000); // 1000 = request code

            }
        });


        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        uID = intent.getStringExtra("UID");
        Toast.makeText(this, uID,
                Toast.LENGTH_SHORT).show();

        editTextName = findViewById(R.id.edit_text_fullName);
        editTextCertification = findViewById(R.id.edit_text_certification);
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
    Uri imageUri;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            imageUri = data.getData(); // works for gallery

            // Show preview in an ImageView
            ImageView profileImage = findViewById(R.id.image);
            profileImage.setImageURI(imageUri);

            //uploadToFirebase(imageUri);
        }
    }

    private void uploadToFirebase(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_images");
        StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();

                    // Save URL to Firestore
                    FirebaseFirestore.getInstance().collection("workRequest")
                            .document(uID) // replace with actual ID
                            .update("certification image", imageUrl);

                    Toast.makeText(WorkRequestForm.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(WorkRequestForm.this, "Upload failed", Toast.LENGTH_SHORT).show();
                });
    }


    public void submit(View view) {
        profession ="";
        if(checkedCarpenter) profession+="carpenter,";
        if(checkedElectrician) profession+="electrician,";
        if(checkedLocksmith) profession+="locksmith,";
        if(checkedGlassTechnican) profession+="glass technican,";
        if(checkedMason) profession+="mason,";
        if(checkedRoofer) profession+="roofer,";
        if(checkedPlumber) profession+="plumber";

        //Toast.makeText(getApplicationContext(), profession, Toast.LENGTH_SHORT).show();

        String name = editTextName.getText().toString();
        String certification = editTextCertification.getText().toString();
        if(certification.isEmpty())
            certification="";
        String number="", address="", certificationImage="";

        String image = imageUri.toString();

        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("phone number", number);
        user.put("address", address);
        user.put("profession", profession);
        user.put("crtification", certification);
        user.put("certification image", image);
        user.put("experience", experience);
        user.put("region", spinnerSelection);
        user.put("provider ID", uID);
        user.put("createdAt", FieldValue.serverTimestamp());

        db.collection("workRequests")
                .add(user)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User added with UID"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding user", e));

        Toast.makeText(WorkRequestForm.this, "Request sent", Toast.LENGTH_SHORT).show();
        //uploadToFirebase(imageUri);

        editTextName.setText("");
        editTextCertification.setText("");

/*
        url = "http://192.168.242.1/RRmobile/addworkrequest.php?name="
                + name + "&email=" + email + "&phonenumber=" + number + "&address=" + address
                + "&profession=" + profession + "&certification=" + certification + "&certificationimage=" + certificationImage
                + "&experience=" + experience + "&region=" + spinnerSelection + "&firebaseid=" + uID;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(
                Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), "on response", Toast.LENGTH_SHORT).show();
                        editTextName.setText(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError) {// Handle timeout error
                    Toast.makeText(WorkRequestForm.this, "Request timed out", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Error:" + error.toString(), Toast.LENGTH_SHORT).show();
                    editTextName.setText(error.getMessage().toString());}
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
        Toast.makeText(getApplicationContext(),"Please wait to receive response", Toast.LENGTH_SHORT).show();
        //save as in database as work request.
        //Admin accepts/rejects
        //worker receives notification with admin decision
        //if accepted, he can login with his account
        String t="your request will be reviewed\nresponse in next 24h";

        //Toast.makeText(this, spinnerSelection, Toast.LENGTH_LONG).show();

 */



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