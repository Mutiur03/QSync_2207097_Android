package com.example.qsync_2207097_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLogin extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        EditText editTextEmail = findViewById(R.id.editTextEmail);
        EditText editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView textViewError = findViewById(R.id.textViewError);

        buttonLogin.setOnClickListener(v -> {
            textViewError.setVisibility(View.GONE);

            String email = editTextEmail.getText() != null ? editTextEmail.getText().toString().trim() : "";
            String password = editTextPassword.getText() != null ? editTextPassword.getText().toString() : "";

            if (TextUtils.isEmpty(email)) {
                textViewError.setText("Please enter email");
                textViewError.setVisibility(View.VISIBLE);
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                textViewError.setText("Please enter a valid email address");
                textViewError.setVisibility(View.VISIBLE);
                return;
            }

            if (TextUtils.isEmpty(password) || password.length() < 6) {
                textViewError.setText("Password must be at least 6 characters");
                textViewError.setVisibility(View.VISIBLE);
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            buttonLogin.setEnabled(false);
            editTextEmail.setEnabled(false);
            editTextPassword.setEnabled(false);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(AdminLogin.this, loginTask -> {
                        progressBar.setVisibility(View.GONE);
                        buttonLogin.setEnabled(true);
                        editTextEmail.setEnabled(true);
                        editTextPassword.setEnabled(true);

                        if (!loginTask.isSuccessful()) {
                            String message = loginTask.getException() != null
                                    ? loginTask.getException().getMessage()
                                    : "Authentication failed";
                            textViewError.setText(message);
                            textViewError.setVisibility(View.VISIBLE);
                            return;
                        }

                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            textViewError.setText("User is null");
                            textViewError.setVisibility(View.VISIBLE);
                            return;
                        }

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("admins")
                                .child(user.getUid());

                        ref.get().addOnCompleteListener(adminCheckTask -> {
                            if (!adminCheckTask.isSuccessful()) {
                                FirebaseAuth.getInstance().signOut();
                                textViewError.setText("Failed to verify admin access");
                                textViewError.setVisibility(View.VISIBLE);
                                return;
                            }

                            DataSnapshot snapshot = adminCheckTask.getResult();
                            if (snapshot.exists() && "admin".equals(snapshot.child("role").getValue(String.class))) {
                                startActivity(new Intent(AdminLogin.this, AdminDashboard.class));
                                finish();
                            } else {
                                FirebaseAuth.getInstance().signOut();
                                textViewError.setText("Access denied: Admins only");
                                textViewError.setVisibility(View.VISIBLE);
                            }
                        });
                    });
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, AdminDashboard.class));
            finish();
        }
    }
}