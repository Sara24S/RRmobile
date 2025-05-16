package com.example.rapidrestore;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class WorkRequestView extends AppCompatActivity {

    TextView tvname, tvprofession, tvexperience, tvregion;
    EditText etRejectionReason;
    FirebaseFirestore db;
    String userId;

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
        tvname = findViewById(R.id.tvname);
        tvprofession = findViewById(R.id.tvprofession);
        tvexperience = findViewById(R.id.tvexperience);
        tvregion = findViewById(R.id.tvregion);
        etRejectionReason = findViewById(R.id.etRejectionReason);

        userId = getIntent().getStringExtra("userId");
        Toast.makeText(WorkRequestView.this, userId, Toast.LENGTH_SHORT).show();

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

                        tvname.append(name);
                        tvexperience.append(experience);
                        tvprofession.append(prof);
                        tvregion.append(region);
                    }
                });


    }

    public void acceptRequest(View view) {
        Map<String, Object> newFields = new HashMap<>();
        newFields.put("status", "approved");
        newFields.put("admin ID", "admin1");//temp
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
                        String certification = documentSnapshot.getString("certification");
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
                        provider.put("crtification", certification);
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
        newFields.put("admin ID", "admin1");//temp
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