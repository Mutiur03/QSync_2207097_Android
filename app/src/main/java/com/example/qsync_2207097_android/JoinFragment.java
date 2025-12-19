package com.example.qsync_2207097_android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinFragment extends Fragment {

    private Button btnJoin;
    private TextView tvSummaryDepartment, tvSummaryDoctor, tvSummaryTime, tvSummaryPosition;
    private RecyclerView recyclerDoctors;

    private final List<Department> departments = new ArrayList<>();
    private final List<Doctor> currentDoctors = new ArrayList<>();
    private Doctor selectedDoctor;
    private String selectedDepartmentId;
    private DatabaseManager databaseManager;

    private DoctorAdapter doctorAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_join, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle(getString(R.string.join_title));
        }

        BottomNavigationView bottomNav =
                requireActivity().findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.join).setChecked(true);
        }

        Spinner spinnerDepartments = view.findViewById(R.id.spinner_departments);
        btnJoin = view.findViewById(R.id.btn_join);
        tvSummaryDepartment = view.findViewById(R.id.tv_summary_department);
        tvSummaryDoctor = view.findViewById(R.id.tv_summary_doctor);
        tvSummaryTime = view.findViewById(R.id.tv_summary_time);
        tvSummaryPosition = view.findViewById(R.id.tv_summary_position);
        recyclerDoctors = view.findViewById(R.id.recycler_doctors);

        recyclerDoctors.setLayoutManager(new LinearLayoutManager(requireContext()));

        doctorAdapter = new DoctorAdapter((doctor, position) -> {
            selectedDoctor = doctor;
            tvSummaryDoctor.setText(getString(R.string.doctor_label, doctor.name));
            tvSummaryTime.setText(getString(R.string.est_wait, doctor.getEstimatedWaitTime()));
            tvSummaryPosition.setText(getString(R.string.preview_position, doctor.currentQueueLength + 1));
            btnJoin.setEnabled(true);
        });

        recyclerDoctors.setAdapter(doctorAdapter);

        btnJoin.setEnabled(false);

        databaseManager = DatabaseManager.getInstance();

        loadDepartments();

        List<String> initialDeptList = new ArrayList<>();
        initialDeptList.add("Loading departments...");

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, initialDeptList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartments.setAdapter(adapter);

        spinnerDepartments.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                boolean valid = position > 0 && position <= departments.size();

                if (valid) {
                    Department selectedDept = departments.get(position - 1);
                    selectedDepartmentId = selectedDept.id;
                    tvSummaryDepartment.setText(getString(R.string.department_label, selectedDept.name));
                    loadDoctorsForDepartment(selectedDept.id);
                } else {
                    selectedDepartmentId = null;
                    tvSummaryDepartment.setText(getString(R.string.department_label_empty));
                    currentDoctors.clear();
                    doctorAdapter.setItems(currentDoctors);
                }

                selectedDoctor = null;
                tvSummaryDoctor.setText(getString(R.string.doctor_label_empty));
                tvSummaryTime.setText(getString(R.string.est_wait_empty));
                tvSummaryPosition.setText(getString(R.string.preview_position_empty));
                btnJoin.setEnabled(false);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedDepartmentId = null;
                btnJoin.setEnabled(false);
            }
        });

        btnJoin.setOnClickListener(v -> {
            if (selectedDoctor == null) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.select_a_doctor_title)
                        .setMessage(R.string.select_a_doctor_message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return;
            }
            showConfirmation();
        });
    }

    private void loadDepartments() {
        databaseManager.loadDepartments(new DatabaseManager.DepartmentLoadCallback() {
            @Override
            public void onDepartmentsLoaded(List<Department> loadedDepartments) {
                if (!isAdded()) return;

                departments.clear();
                departments.addAll(loadedDepartments);
                List<String> deptNames = new ArrayList<>();
                deptNames.add("Select Department");
                for (Department dept : departments) {
                    deptNames.add(dept.name);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    deptNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                Spinner spinnerDepartments = requireView().findViewById(R.id.spinner_departments);
                spinnerDepartments.setAdapter(adapter);
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;

                new AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("Failed to load departments: " + error)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });
    }

    private void loadDoctorsForDepartment(String departmentId) {
        if (departmentId == null) {
            currentDoctors.clear();
            doctorAdapter.setItems(currentDoctors);
            return;
        }

        databaseManager.loadDoctorsByDepartment(departmentId, new DatabaseManager.DoctorLoadCallback() {
            @Override
            public void onDoctorsLoaded(List<Doctor> loadedDoctors) {
                if (!isAdded()) return;

                currentDoctors.clear();
                currentDoctors.addAll(loadedDoctors);

                currentDoctors.sort(Comparator.comparingInt(Doctor::getEstimatedWaitTime));

                doctorAdapter.setItems(currentDoctors);
                recyclerDoctors.scrollToPosition(0);
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;

                new AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("Failed to load doctors: " + error)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });
    }

    private void showConfirmation() {
        String departmentName = "";
        for (Department dept : departments) {
            if (dept.id.equals(selectedDepartmentId)) {
                departmentName = dept.name;
                break;
            }
        }

        String msg = getString(R.string.department_label, departmentName) + "\n" +
                getString(R.string.doctor_label, selectedDoctor == null ? "" : selectedDoctor.name) + "\n" +
                getString(R.string.est_wait, selectedDoctor == null ? 0 : selectedDoctor.getEstimatedWaitTime());

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_join_title)
                .setMessage(msg)
                .setPositiveButton("Join", (d, w) -> performJoin())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void performJoin() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Authentication Required")
                    .setMessage("Please log in to join the queue")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }

        if (selectedDoctor == null) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Error")
                    .setMessage("Please select a doctor")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }

        AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Joining Queue")
                .setMessage("Please wait...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        databaseManager.joinQueue(selectedDoctor.id, currentUser.getUid(), new DatabaseManager.QueueJoinCallback() {
            @Override
            public void onQueueJoined(String queueId, int position) {
                if (!isAdded()) return;

                loadingDialog.dismiss();

                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.joined_title)
                        .setMessage("Successfully joined the queue!\nYour position: " + position)
                        .setPositiveButton(android.R.string.ok, (d, w) -> {
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.frame, new HomeFragment())
                                    .commit();

                            BottomNavigationView bottomNav =
                                    requireActivity().findViewById(R.id.bottomNav);
                            if (bottomNav != null) {
                                bottomNav.getMenu().findItem(R.id.home).setChecked(true);
                            }
                        })
                        .show();
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;

                loadingDialog.dismiss();

                new AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("Failed to join queue: " + error)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });
    }
}
