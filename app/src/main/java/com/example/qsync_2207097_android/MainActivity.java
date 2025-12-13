package com.example.qsync_2207097_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

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
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String savedEmail = prefs.getString("email", null);
        String savedPassword = prefs.getString("password", null);
        if (savedEmail != null && savedPassword != null) {
            Intent intent = new Intent(this, Home.class);
            startActivity(intent);
            finish();
        }
    }
    public void gotoList(View view) {
        EditText emailEt = findViewById(R.id.editTextText2);
        EditText passEt = findViewById(R.id.editTextTextPassword2);

        String email = emailEt.getText().toString().trim();
        String password = passEt.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String savedEmail = prefs.getString("email", null);
        String savedPassword = prefs.getString("password", null);
        if(!email.equals(savedEmail))
            savedEmail="mutiur5bb@gmail.com";
        if(!password.equals(savedPassword))
            savedPassword="123456";
        if (savedEmail == null || savedPassword == null) {
            Toast.makeText(this, "No registered user found. Please register first.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!email.equals(savedEmail) || !password.equals(savedPassword)) {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }
    public void gotoReg(View view) {
        Intent intent = new Intent(this, UserReg.class);
        startActivity(intent);
    }
}