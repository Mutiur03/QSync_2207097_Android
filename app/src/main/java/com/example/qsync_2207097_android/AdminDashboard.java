package com.example.qsync_2207097_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        TextView welcome = findViewById(R.id.textViewAdminWelcome);
        Button logout = findViewById(R.id.buttonLogout);

        welcome.setText(getString(R.string.welcome_admin));

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(AdminDashboard.this, AdminLogin.class));
            finish();
        });
    }
}
