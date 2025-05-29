package com.example.rapidrestore;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkRequestView extends AppCompatActivity {

    private TextView tvName, tvProfession, tvExperience, tvRegion, tvCertification, tvNumber, tvRejectionReason, tvAdminName;
    private EditText etRejectionReason;
    private FirebaseFirestore db;
    private String userId, adminId, adminName;
    private ImageView ivId, ivCertification;
    private Button btnAccept, btnReject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_work_request_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        tvName = findViewById(R.id.tvname);
        tvProfession = findViewById(R.id.tvprofession);
        tvExperience = findViewById(R.id.tvexperience);
        tvRegion = findViewById(R.id.tvregion);
        tvCertification = findViewById(R.id.tvCertification);
        tvNumber = findViewById(R.id.tvNumber);
        etRejectionReason = findViewById(R.id.etRejectionReason);
        ivId = findViewById(R.id.providerIdCard);
        ivCertification = findViewById(R.id.ivCertificationImage);
        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);
        tvRejectionReason = findViewById(R.id.tvRejectionReason);
        tvAdminName = findViewById(R.id.tvAdmin);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity safely
            return;
        }
        adminId = auth.getCurrentUser().getUid();
     //   Toast.makeText(this, adminId, Toast.LENGTH_SHORT).show();

        userId = getIntent().getStringExtra("userId");
       // Toast.makeText(WorkRequestView.this, userId, Toast.LENGTH_SHORT).show();

        db.collection("users")
                .document(adminId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        adminName = doc.getString("name");
                        Toast.makeText(WorkRequestView.this, adminName, Toast.LENGTH_SHORT).show();
                    }
                });

        db.collection("workRequests")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String prof = "";
                        ArrayList<String> professions = (ArrayList<String>) documentSnapshot.get("profession");
                        if (professions != null) {
                            for (String profession : professions) {
                                prof+=profession+", ";
                            }
                        }
                        String experience = documentSnapshot.getString("experience");
                        String region = documentSnapshot.getString("region");
                        String number = documentSnapshot.getString("phone number");
                        String certification = documentSnapshot.getString("crtification");
                        String certificationImage = documentSnapshot.getString("certification image");
                        String idCard = documentSnapshot.getString("IdCard");
                        String admin = documentSnapshot.getString("adminName");
                        String status = documentSnapshot.getString("status");
                        if (status.equals("pending")){
                            btnReject.setVisibility(View.VISIBLE);
                            btnAccept.setVisibility(View.VISIBLE);
                            etRejectionReason.setVisibility(View.VISIBLE);
                        } else {
                            tvAdminName.setVisibility(View.VISIBLE);
                            tvAdminName.append(admin);
                            if (status.equals("rejected")){
                                String rejectionReason = documentSnapshot.getString("rejection reason");
                                tvRejectionReason.setVisibility(View.VISIBLE);
                                tvRejectionReason.append(rejectionReason);
                            }else if(status.equals("approved")) {
                                etRejectionReason.setVisibility(View.GONE);
                            }
                        }
                        tvName.append(name);
                        tvExperience.append(experience);
                        tvProfession.append(prof);
                        tvRegion.append(region);
                        tvNumber.append(number);
                        if (!certification.isEmpty()){
                            tvCertification.append(certification);
                        }
                        else {
                            tvCertification.append(" no certification");
                            ivCertification.setVisibility(View.GONE);
                        }

                        String imageCerUrl = ImageUtils.getImageUrl(certificationImage);
                        Glide.with(this)
                                .load(imageCerUrl)
                                //.centerCrop()
                                // .circleCrop()
                                .into(ivCertification);
                        String imageIdUrl = ImageUtils.getImageUrl(idCard);
                        Glide.with(this)
                                .load(imageIdUrl)
                                //.centerCrop()
                                // .circleCrop()
                                .into(ivId);
                    }
                });


    }

    public void acceptRequest(View view) {
        Map<String, Object> newFields = new HashMap<>();
        newFields.put("status", "approved");
        newFields.put("admin ID", adminId);//temp
        newFields.put("adminName", adminName);
        newFields.put("decisionDate", FieldValue.serverTimestamp()); // server time

        db.collection("workRequests")
                .document(userId)
                .update(newFields)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Field updated"))
                .addOnFailureListener(e -> Log.w("Firestore", "Update failed", e));

        db.collection("workRequests")
                .document(userId) // 👈 specific document ID
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String id = documentSnapshot.getString("provider ID");
                        String name = documentSnapshot.getString("name");
                        ArrayList<String> professions = (ArrayList<String>) documentSnapshot.get("profession");
                        String experience = documentSnapshot.getString("experience");
                        String certification = documentSnapshot.getString("crtification");
                        String certificationImage = documentSnapshot.getString("certification image");
                        String region = documentSnapshot.getString("region");
                        String phoneNumber = documentSnapshot.getString("phone number");
                        String address = documentSnapshot.getString("address");
                        String email = documentSnapshot.getString("email");

                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);
                        user.put("phone number", phoneNumber);
                        user.put("role", "provider");

                        Map<String, Object> provider = new HashMap<>();
                        provider.put("costPerHour", 0);//temp
                        provider.put("rating", 0);//temp
                        provider.put("profile image", "");//temp
                        provider.put("name", name);
                        provider.put("phone number", phoneNumber);
                        provider.put("address", address);
                        provider.put("profession", professions);
                        provider.put("certification", certification);
                        provider.put("certification image", certificationImage);
                        provider.put("experience", experience);
                        provider.put("region", region);


                        db.collection("users")
                                .document(id)
                                .set(user)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User added with UID"))
                                .addOnFailureListener(e -> Log.w("Firestore", "Error adding user", e));

                        db.collection("providers")
                                .document(id)
                                .set(provider)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User added with UID"))
                                .addOnFailureListener(e -> Log.w("Firestore", "Error adding user", e));



                    } else {
                        Toast.makeText(WorkRequestView.this, "not successful",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.w("Firestore", "Failed to fetch", e));
        //send notificaton to provider
    }

    public void rejectRequest(View view) {
        String rejectionReason = etRejectionReason.getText().toString();

        Map<String, Object> newFields = new HashMap<>();
        newFields.put("status", "rejected");
        newFields.put("admin ID", adminId);
        newFields.put("adminName", adminName);
        newFields.put("decisionDate", FieldValue.serverTimestamp()); // server time
        newFields.put("rejection reason", rejectionReason);


        db.collection("workRequests")
                .document(userId)
                .update(newFields)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Field updated"))
                .addOnFailureListener(e -> Log.w("Firestore", "Update failed", e));


        //send notificaton to provider


    }
}