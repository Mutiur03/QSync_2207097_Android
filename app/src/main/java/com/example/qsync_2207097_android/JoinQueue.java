package com.example.qsync_2207097_android;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinQueue extends AppCompatActivity {

    private Spinner spinnerDepartments;
    private Button btnJoin;
    private TextView tvSummaryDepartment;
    private TextView tvSummaryDoctor;
    private TextView tvSummaryTime;
    private TextView tvSummaryPosition;
    private RecyclerView recyclerDoctors;

    private Map<String, List<Doctor>> data = new HashMap<>();
    private Doctor selectedDoctor = null;
    private String selectedDepartment = null;
    private DoctorAdapter doctorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_join_queue);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Join a Queue");
        }

        spinnerDepartments = findViewById(R.id.spinner_departments);
        btnJoin = findViewById(R.id.btn_join);
        tvSummaryDepartment = findViewById(R.id.tv_summary_department);
        tvSummaryDoctor = findViewById(R.id.tv_summary_doctor);
        tvSummaryTime = findViewById(R.id.tv_summary_time);
        tvSummaryPosition = findViewById(R.id.tv_summary_position);
        recyclerDoctors = findViewById(R.id.recycler_doctors);

        recyclerDoctors.setLayoutManager(new LinearLayoutManager(this));
        doctorAdapter = new DoctorAdapter((doctor, position) -> {
            selectedDoctor = doctor;
            tvSummaryDoctor.setText("Doctor: " + doctor.name);
            tvSummaryTime.setText("Est. wait: ~" + doctor.estimatedWaitMinutes + " min");
            tvSummaryPosition.setText("Preview position: #" + (doctor.queueLength + 1));
            btnJoin.setEnabled(true);
        });
        recyclerDoctors.setAdapter(doctorAdapter);


        populateSampleData();

        List<String> departments = new ArrayList<>();
        departments.add("");
        departments.addAll(data.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartments.setAdapter(adapter);

        spinnerDepartments.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String sel = (String) parent.getItemAtPosition(position);
                boolean valid = sel != null && !sel.trim().isEmpty();
                selectedDepartment = valid ? sel : null;
                tvSummaryDepartment.setText(valid ? ("Department: " + sel) : "Department: —");

                selectedDoctor = null;
                tvSummaryDoctor.setText("Doctor: —");
                tvSummaryTime.setText("Est. wait: —");
                tvSummaryPosition.setText("Preview position: —");

                populateDoctorsForDepartment(sel);
                btnJoin.setEnabled(false);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedDepartment = null;
                btnJoin.setEnabled(false);
            }
        });

        btnJoin.setOnClickListener(v -> {
            if (selectedDepartment == null) return;
            if (selectedDoctor == null) {
                new AlertDialog.Builder(this)
                        .setTitle("Select a doctor")
                        .setMessage("Please select a doctor from the list before joining the queue.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }
            showConfirmation();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void populateSampleData() {
        List<Doctor> cardiology = new ArrayList<>();
        cardiology.add(new Doctor("Dr. Amina Khan", "Cardiologist", 6, 25));
        cardiology.add(new Doctor("Dr. Sameer Patel", "Cardiologist", 3, 18));
        cardiology.add(new Doctor("Dr. Li Wei", "Cardiologist", 10, 30));

        List<Doctor> dermatology = new ArrayList<>();
        dermatology.add(new Doctor("Dr. Sara Gomez", "Dermatologist", 4, 10));
        dermatology.add(new Doctor("Dr. Hana Park", "Dermatologist", 2, 8));

        List<Doctor> orthopedics = new ArrayList<>();
        orthopedics.add(new Doctor("Dr. John Doe", "Orthopedics", 8, 20));
        orthopedics.add(new Doctor("Dr. Priya Singh", "Orthopedics", 5, 12));

        List<Doctor> pediatrics = new ArrayList<>();
        pediatrics.add(new Doctor("Dr. Maya Rao", "Pediatrics", 7, 15));
        pediatrics.add(new Doctor("Dr. Omar Aziz", "Pediatrics", 4, 9));

        data.put("Cardiology", cardiology);
        data.put("Dermatology", dermatology);
        data.put("Orthopedics", orthopedics);
        data.put("Pediatrics", pediatrics);
    }

    private void populateDoctorsForDepartment(String department) {
        if (department == null || !data.containsKey(department)) {
            doctorAdapter.setItems(null);
            return;
        }

        List<Doctor> docs = new ArrayList<>(data.get(department));
        Collections.sort(docs, Comparator.comparingInt(d -> d.estimatedWaitMinutes));
        doctorAdapter.setItems(docs);
        recyclerDoctors.scrollToPosition(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showConfirmation() {
        String dept = selectedDepartment != null ? selectedDepartment : "";
        String doc = selectedDoctor != null ? selectedDoctor.name : "";
        int est = selectedDoctor != null ? selectedDoctor.estimatedWaitMinutes : 0;
        int position = selectedDoctor != null ? (selectedDoctor.queueLength + 1) : 0;
        String message = "You're about to join the " + dept + " queue with " + doc + ".\n\nEstimated wait: ~" + est + " min\nPreview position: #" + position;
        new AlertDialog.Builder(this)
                .setTitle("Confirm Join")
                .setMessage(message)
                .setPositiveButton("Join", (dialog, which) -> performJoin())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performJoin() {
        new AlertDialog.Builder(this)
                .setTitle("Joined")
                .setMessage("You've successfully joined the queue. We will notify you when it's your turn.")
                .setPositiveButton("OK", (DialogInterface dialog, int which) -> finish())
                .show();
    }

    public static class Doctor {
        String name;
        String specialty;
        int queueLength;
        int estimatedWaitMinutes;
        int years;

        public Doctor(String name, String specialty, int queueLength, int estimatedWaitMinutes) {
            this.name = name;
            this.specialty = specialty;
            this.queueLength = queueLength;
            this.estimatedWaitMinutes = estimatedWaitMinutes;
            this.years = 0;
        }

        public Doctor(String name, String specialty, int queueLength, int estimatedWaitMinutes, int years) {
            this.name = name;
            this.specialty = specialty;
            this.queueLength = queueLength;
            this.estimatedWaitMinutes = estimatedWaitMinutes;
            this.years = years;
        }
    }
}