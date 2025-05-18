package com.example.rapidrestore;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //startActivity(new Intent(MainActivity.this, RepairRequestForm.class));

        editTextEmail = findViewById(R.id.edit_text_numberOrEmail);
        editTextPassword = findViewById(R.id.edit_text_password);
        editTextEmail.setText("");
        editTextPassword.setText("");
    }

    public void logIN(View view) {
        String Email = String.valueOf(editTextEmail.getText());
        String Password = String.valueOf(editTextPassword.getText());

        if(TextUtils.isEmpty(Email)){
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(Password)){
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                            db.collection("users")
                                    .document(currentUser.getUid())  // your document ID
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            String role = documentSnapshot.getString("role");
                                            if ("homeowner".equals(role)) {

                                                //startActivity(new Intent(MainActivity.this, CareersPage.class));
                                                Intent intent = new Intent(MainActivity.this, CareersPage.class);
                                                intent.putExtra("homeownerId", currentUser.getUid()); // or document ID
                                                startActivity(intent);
                                                //finish();

                                                Toast.makeText(MainActivity.this, role, Toast.LENGTH_SHORT).show();
                                            }
                                            else if("provider".equals(role)){
                                                Toast.makeText(MainActivity.this, role, Toast.LENGTH_SHORT).show();
                                                //startActivity(new Intent(MainActivity.this, ProviderProfile.class));
                                                Intent intent = new Intent(MainActivity.this, ProviderProfile.class);//temp, open provider profile
                                                intent.putExtra("providerId", currentUser.getUid());
                                                intent.putExtra("isOwner", true);// or document ID
                                                startActivity(intent);

                                            }
                                            else {
                                                Intent intent = new Intent(MainActivity.this, AdminPage.class);
                                                intent.putExtra("adminId", currentUser.getUid()); // or document ID
                                                startActivity(intent);


                                                Toast.makeText(MainActivity.this, role, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.w("Firestore", "Error reading field", e));
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public void createAccount(View view) {
        Intent s = new Intent(MainActivity.this, CreateAccount.class);
        startActivity(s);
        editTextEmail.setText("");
        editTextPassword.setText("");
    }
}