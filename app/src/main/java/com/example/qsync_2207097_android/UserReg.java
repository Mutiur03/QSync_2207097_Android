package com.example.qsync_2207097_android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UserReg extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_reg);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
    }

    public void gotoLogin(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void registerUser(View view) {
        auth = FirebaseAuth.getInstance();
        EditText nameEt = findViewById(R.id.editTextText);
        EditText emailEt = findViewById(R.id.editTextTextEmailAddress);
        EditText passEt = findViewById(R.id.editTextTextPassword);
        String name = nameEt.getText().toString().trim();
        String email = emailEt.getText().toString().trim();
        String password = passEt.getText().toString();

        if (name.isEmpty()) {
            nameEt.setError("Name required");
            nameEt.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            emailEt.setError("Email required");
            emailEt.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt.setError("Enter a valid email");
            emailEt.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passEt.setError("Password required");
            passEt.requestFocus();
            return;
        }
        if (password.length() < 6) {
            passEt.setError("Password must be at least 6 characters");
            passEt.requestFocus();
            return;
        }

        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setView(new ProgressBar(this))
                .setCancelable(false)
                .create();
        progressDialog.show();
        view.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        user.sendEmailVerification()
                                                .addOnCompleteListener(emailTask -> {
                                                    progressDialog.dismiss();
                                                    view.setEnabled(true);
                                                    if (emailTask.isSuccessful()) {
                                                        Toast.makeText(this, "Registration successful. Verification email sent. Please verify before logging in.", Toast.LENGTH_LONG).show();
                                                        Intent intent = new Intent(this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        String msg = emailTask.getException() != null ? emailTask.getException().getMessage() : "Failed to send verification email";
                                                        Toast.makeText(this, "Registered but failed to send verification email: " + msg, Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    });
                        } else {
                            progressDialog.dismiss();
                            view.setEnabled(true);
                            Toast.makeText(this, "Registration failed: unknown user", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        view.setEnabled(true);
                        String message = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                        Toast.makeText(this, "Registration failed: " + message, Toast.LENGTH_LONG).show();
                    }
                });
    }
}