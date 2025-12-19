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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
//    private static final int RC_SIGN_IN = 100;
//    private GoogleSignInClient mGoogleSignInClient;

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

        auth = FirebaseAuth.getInstance();

//        String clientId = getString(R.string.default_web_client_id).trim();
//
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(clientId)
//                .requestEmail()
//                .build();
//
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//
//        findViewById(R.id.btnGoogleSignIn).setOnClickListener(v -> signIn());
//
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            currentUser.getIdToken(true).addOnFailureListener(
                    e -> {
                        Toast.makeText(this, "Failed to refresh ID token", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    });
            AlertDialog progressDialog = new AlertDialog.Builder(this)
                    .setView(new ProgressBar(this))
                    .setCancelable(false)
                    .create();
            progressDialog.show();

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("admins")
                    .child(currentUser.getUid());
            ref.get()
                    .addOnCompleteListener(adminCheckTask -> {
                        progressDialog.dismiss();
                        if (!adminCheckTask.isSuccessful()) {
                            String msg = adminCheckTask.getException() != null ? adminCheckTask.getException().getMessage() : "Failed to check admin role";
                            Toast.makeText(this, "Database check failed: " + msg, Toast.LENGTH_LONG).show();
                            auth.signOut();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                            return;
                        }
                        DataSnapshot snapshot = adminCheckTask.getResult();
                        if (snapshot.exists() && "admin".equals(snapshot.child("role").getValue(String.class))) {
                            startActivity(new Intent(this, AdminDashboard.class));
                            finish();
                        }
                        else{
                            startActivity(new Intent(this, Menu.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        auth.signOut();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    });

        } else if (currentUser != null && !currentUser.isEmailVerified()) {
            Toast.makeText(this, "Please verify your email address before continuing", Toast.LENGTH_LONG).show();
        }
    }
//    private void signIn() {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                if (account == null) {
//                    Toast.makeText(this, "Google sign-in failed: account is null", Toast.LENGTH_LONG).show();
//                    return;
//                }
//                String idToken = account.getIdToken();
//                if (idToken == null) {
//                    Toast.makeText(this, "Google sign-in failed: idToken is null (check requestIdToken client ID and SHA settings)", Toast.LENGTH_LONG).show();
//                    return;
//                }
//                firebaseAuthWithGoogle(idToken);
//            } catch (ApiException e) {
//                String msg = "Google sign-in failed: " + e.getStatusCode() + " - " + e.getMessage();
//                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
//                e.printStackTrace();
//            }
//        }
//    }
//    private void firebaseAuthWithGoogle(String idToken) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        auth.signInWithCredential(credential)
//                .addOnCompleteListener(this, task -> {
//                    if (task.isSuccessful()) {
//                        FirebaseUser user = auth.getCurrentUser();
//                        String displayName = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "User";
//                        Toast.makeText(this,
//                                "Welcome " + displayName,
//                                Toast.LENGTH_LONG).show();
//                    } else {
//                        String err = task.getException() != null ? task.getException().getMessage() : "Authentication Failed";
//                        Toast.makeText(this, "Authentication Failed: " + err, Toast.LENGTH_LONG).show();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Firebase auth error: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                    e.printStackTrace();
//                });
//    }
    public void gotoList(View view) {
        auth = FirebaseAuth.getInstance();
        EditText emailEt = findViewById(R.id.editTextText2);
        EditText passEt = findViewById(R.id.editTextTextPassword2);
        String email = emailEt.getText().toString().trim();
        String password = passEt.getText().toString();

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

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    view.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("admins")
                                    .child(user.getUid());
                            ref.get().addOnCompleteListener(adminCheckTask -> {

                                DataSnapshot snapshot = adminCheckTask.getResult();
                                if (snapshot.exists() && "admin".equals(snapshot.child("role").getValue(String.class))) {
                                    Toast.makeText(this, "Admin login successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, AdminDashboard.class));
                                    finish();
                                }
                                else{
                                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, Menu.class));
                                    finish();
                                }
                            });

                        } else if (user != null && !user.isEmailVerified()) {
                            Toast.makeText(this, "Please verify your email before logging in. Check your inbox.", Toast.LENGTH_LONG).show();
                            auth.signOut();
                        } else {
                            Toast.makeText(this, "Login failed: unknown user state", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String message = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        Toast.makeText(this, "Login failed: " + message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void gotoReg(View view) {
        Intent intent = new Intent(this, UserReg.class);
        startActivity(intent);
    }
    public void gotoAdminLogin(View view) {
        Intent intent = new Intent(this, AdminLogin.class);
        startActivity(intent);
    }

}