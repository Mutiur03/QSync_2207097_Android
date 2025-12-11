package com.example.qsync_2207097_android;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView rv = findViewById(R.id.rvQueues);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<QueueItem> sample = new ArrayList<>();
        sample.add(new QueueItem("Dr. Sultana - Pediatrics", "ER", "Token: C11", "Position: 1st", "Estimated Wait: 3 min", 5));
        sample.add(new QueueItem("Dr. Rahman - ENT", "OPD", "Token: B02", "Position: 2nd", "Estimated Wait: 8 min", 20));
        sample.add(new QueueItem("Dr. Khan - Cardiology", "OPD", "Token: A15", "Position: 5th", "Estimated Wait: 25 min", 60));

        QueueAdapter adapter = new QueueAdapter(sample);
        rv.setAdapter(adapter);
    }
}