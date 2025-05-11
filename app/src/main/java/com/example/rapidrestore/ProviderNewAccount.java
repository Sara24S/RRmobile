package com.example.rapidrestore;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.google.firebase.auth.FirebaseUser;

public class ProviderNewAccount extends AppCompatActivity {


    TextInputEditText editTextEmail, editTextPassword;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_provider_new_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextEmail = findViewById(R.id.edit_text_numberOrEmail);
        editTextPassword = findViewById(R.id.edit_text_password);

        editTextEmail.setText("");
        editTextPassword.setText("");

        mAuth = FirebaseAuth.getInstance();

        ConnectivityManager cm = (ConnectivityManager) ProviderNewAccount.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            Toast.makeText(ProviderNewAccount.this, "No network connection", Toast.LENGTH_SHORT).show();
        }
    }

/*
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Toast.makeText(ProviderNewAccount.this, "Already have a user",
                    Toast.LENGTH_SHORT).show();
            Toast.makeText(ProviderNewAccount.this, currentUser.getUid(),
                    Toast.LENGTH_SHORT).show();
            //reload();
        }
    }

 */

    public void signUp(View view) {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(ProviderNewAccount.this, "Authentication Succeeded",
                                    Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(ProviderNewAccount.this, WorkRequestForm.class);
                            i.putExtra("email", email);
                            i.putExtra("UID", user.getUid().toString());
                            startActivity(i);
                            editTextEmail.setText("");
                            editTextPassword.setText("");
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(ProviderNewAccount.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }

}