package com.example.qsync_2207097_android;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageDepartmentsActivity extends AppCompatActivity implements DepartmentAdapter.Callback {

    private RecyclerView recyclerView;
    private DepartmentAdapter adapter;
    private DatabaseReference ref;
    private final List<Department> departments = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_departments);

        Toolbar toolbar = findViewById(R.id.toolbar_manage_departments);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        setTitle("Manage Departments");

        ref = FirebaseDatabase.getInstance().getReference("departments");

        recyclerView = findViewById(R.id.dept_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DepartmentAdapter(this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add_department);
        fab.setOnClickListener(v -> showAddDepartmentDialog());

        listenDepartments();
    }

    private void listenDepartments() {
        ref.addValueEventListener(new ValueEventListener() {
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
                adapter.setItems(departments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageDepartmentsActivity.this, "Failed to load departments: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showAddDepartmentDialog() {
        final EditText nameInput = new EditText(this);
        nameInput.setHint("Department name");

        final EditText descInput = new EditText(this);
        descInput.setHint("Description (optional)");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.addView(nameInput);
        layout.addView(descInput);

        new AlertDialog.Builder(this)
                .setTitle("Add Department")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String description = descInput.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Enter a department name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    DatabaseReference newRef = ref.push();
                    Department d = new Department(name, description);
                    newRef.setValue(d);
                    Toast.makeText(this, "Department added successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDepartmentSelected(Department department, int position) {
        String[] items = {"Edit", "Delete"};
        new AlertDialog.Builder(this)
                .setTitle(department.name)
                .setItems(items, (dialog, which) -> {
                    if (which == 0) showEditDepartmentDialog(department);
                    else {
                        showDeleteConfirmationDialog(department);
                    }
                })
                .show();
    }



    private void showEditDepartmentDialog(Department department) {
        final EditText nameInput = new EditText(this);
        nameInput.setText(department.name);
        nameInput.setHint("Department name");

        final EditText descInput = new EditText(this);
        descInput.setText(department.description != null ? department.description : "");
        descInput.setHint("Description (optional)");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.addView(nameInput);
        layout.addView(descInput);

        new AlertDialog.Builder(this)
                .setTitle("Edit Department")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String description = descInput.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Enter a department name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (department.id != null) {
                        Task<Void> name1 = ref.child(department.id).child("name").setValue(name);
                        ref.child(department.id).child("description").setValue(description);
                        Toast.makeText(this, "Department updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmationDialog(Department department) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Department")
                .setMessage("Are you sure you want to delete '" + department.name + "'? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (department.id != null) {
                        ref.child(department.id).removeValue()
                                .addOnSuccessListener(aVoid ->
                                    Toast.makeText(this, "Department deleted successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to delete department", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
