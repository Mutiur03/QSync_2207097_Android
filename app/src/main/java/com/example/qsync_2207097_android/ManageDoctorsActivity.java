package com.example.qsync_2207097_android;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageDoctorsActivity extends AppCompatActivity implements ManageDoctorAdapter.Callback {

    private ManageDoctorAdapter adapter;
    private DatabaseReference docRef;
    private DatabaseReference deptRef;
    private final List<Department> departments = new ArrayList<>();
    private final List<Doctor> doctors = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_doctors);

        Toolbar toolbar = findViewById(R.id.toolbar_manage_doctors);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        setTitle("Manage Doctors");

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        docRef = db.getReference("doctors");
        deptRef = db.getReference("departments");

        RecyclerView recyclerView = findViewById(R.id.doc_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManageDoctorAdapter(this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add_doctor);
        fab.setOnClickListener(v -> showAddDoctorDialog());

        listenDepartments();
        listenDoctors();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manage_doctors, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setQueryHint("Search doctors...");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (adapter != null) {
                        adapter.filter(newText);
                    }
                    return true;
                }
            });
        }

        return true;
    }

    private void listenDepartments() {
        deptRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                departments.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Department d = child.getValue(Department.class);
                    if (d != null) {
                        d.id = child.getKey();
                        departments.add(d);
                    }
                }
                adapter.setDepartments(departments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageDoctorsActivity.this, "Failed to load departments: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void listenDoctors() {
        docRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctors.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Doctor d = child.getValue(Doctor.class);
                    if (d != null) {
                        d.id = child.getKey();
                        doctors.add(d);
                    }
                }
                adapter.setItems(doctors);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageDoctorsActivity.this, "Failed to load doctors: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showAddDoctorDialog() {
        View root = getLayoutInflater().inflate(R.layout.dialog_add_doctor, null);
        EditText name = root.findViewById(R.id.input_doc_name);
        EditText specialty = root.findViewById(R.id.input_doc_specialty);
        EditText phone = root.findViewById(R.id.input_doc_phone);
        EditText email = root.findViewById(R.id.input_doc_email);
        EditText experience = root.findViewById(R.id.input_doc_experience);
        Spinner deptSpinner = root.findViewById(R.id.spinner_dept);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (Department d : departments) {
            spinnerAdapter.add(
                    d.name + (d.description != null && !d.description.isEmpty()
                            ? " (" + d.description + ")"
                            : "")
            );
        }
        deptSpinner.setAdapter(spinnerAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Add Doctor")
                .setView(root)
                .setPositiveButton("Add", (dialog, which) -> {
                    String n = name.getText().toString().trim();
                    String s = specialty.getText().toString().trim();
                    String p = phone.getText().toString().trim();
                    String e = email.getText().toString().trim();
                    String expStr = experience.getText().toString().trim();
                    int deptIndex = deptSpinner.getSelectedItemPosition();

                    if (n.isEmpty() || s.isEmpty() || deptIndex < 0 || departments.isEmpty()) {
                        Toast.makeText(this, "Name, specialty and department are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int exp = 0;
                    if (!expStr.isEmpty()) {

                            exp = Integer.parseInt(expStr);

                    }

                    String deptId = departments.get(deptIndex).id;
                    DatabaseReference newRef = docRef.push();
                    Doctor doc = new Doctor(newRef.getKey(), n, s, deptId, p, e, exp);
                    newRef.setValue(doc);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDoctorSelected(Doctor doctor, int position) {
        String[] items = {"Edit", "Delete"};
        new AlertDialog.Builder(this)
                .setTitle(doctor.name)
                .setItems(items, (dialog, which) -> {
                    if (which == 0) showEditDoctorDialog(doctor);
                    else {
                        showDeleteConfirmationDialog(doctor);
                    }
                })
                .show();
    }

    private void showEditDoctorDialog(Doctor doctor) {
        View root = getLayoutInflater().inflate(R.layout.dialog_add_doctor, null);
        EditText name = root.findViewById(R.id.input_doc_name);
        EditText specialty = root.findViewById(R.id.input_doc_specialty);
        EditText phone = root.findViewById(R.id.input_doc_phone);
        EditText email = root.findViewById(R.id.input_doc_email);
        EditText experience = root.findViewById(R.id.input_doc_experience);
        Spinner deptSpinner = root.findViewById(R.id.spinner_dept);

        name.setText(doctor.name);
        specialty.setText(doctor.specialty);
        phone.setText(doctor.phone != null ? doctor.phone : "");
        email.setText(doctor.email != null ? doctor.email : "");
        experience.setText(String.valueOf(doctor.yearsOfExperience));

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        int sel = 0;
        for (int i = 0; i < departments.size(); i++) {
            Department d = departments.get(i);
            spinnerAdapter.add(d.name + " (" + d.id + ")");
            if (d.id != null && d.id.equals(doctor.departmentId)) sel = i;
        }
        deptSpinner.setAdapter(spinnerAdapter);
        deptSpinner.setSelection(sel);

        new AlertDialog.Builder(this)
                .setTitle("Edit Doctor")
                .setView(root)
                .setPositiveButton("Save", (dialog, which) -> {
                    String n = name.getText().toString().trim();
                    String s = specialty.getText().toString().trim();
                    String p = phone.getText().toString().trim();
                    String e = email.getText().toString().trim();
                    String expStr = experience.getText().toString().trim();
                    int deptIndex = deptSpinner.getSelectedItemPosition();

                    if (n.isEmpty() || s.isEmpty()) {
                        Toast.makeText(this, "Name and specialty are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int exp = 0;
                    if (!expStr.isEmpty()) {

                            exp = Integer.parseInt(expStr);

                    }

                    doctor.name = n;
                    doctor.specialty = s;
                    doctor.phone = p;
                    doctor.email = e;
                    doctor.yearsOfExperience = exp;
                    doctor.departmentId = departments.get(deptIndex).id;
                    if (doctor.id != null) docRef.child(doctor.id).setValue(doctor);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void showDeleteConfirmationDialog(Doctor doctor) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Doctor")
                .setMessage("Are you sure you want to delete '" + doctor.name + "'? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (doctor.id != null) {
                        docRef.child(doctor.id).removeValue()
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(this, "Doctor deleted successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to delete doctor", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
