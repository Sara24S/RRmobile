package com.example.rapidrestore;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class HomeownerNewAccount extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword, editTextName, editTextConfirmPassword;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homeowner_new_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        editTextName = findViewById(R.id.edit_text_name);
        editTextConfirmPassword = findViewById(R.id.edit_text_passwordConfirm);

    }
    public void signUp(View view) {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        String name = String.valueOf(editTextName.getText());
        String passwordConfirm = editTextConfirmPassword.getText().toString();


        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(getApplicationContext(), "Empty fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(passwordConfirm)){
            Toast.makeText(getApplicationContext(), "Passwords don't match!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("phone number", "for now");
        user.put("role", "homeowner");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "SignUp Successful", Toast.LENGTH_SHORT).show();
                            // Add a new document with a generated ID
                            db.collection("users")
                                    .document(mAuth.getCurrentUser().getUid()) // UID as document ID
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "User added with UID"))
                                    .addOnFailureListener(e -> Log.w("Firestore", "Error adding user", e));

                            startActivity(new Intent(HomeownerNewAccount.this, CareersPage.class));
                            editTextEmail.setText("");
                            editTextPassword.setText("");
                            editTextName.setText("");

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(HomeownerNewAccount.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }
}