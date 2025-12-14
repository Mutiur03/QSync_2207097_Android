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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinFragment extends Fragment {

    private Button btnJoin;
    private TextView tvSummaryDepartment, tvSummaryDoctor, tvSummaryTime, tvSummaryPosition;
    private RecyclerView recyclerDoctors;

    private final Map<String, List<Doctor>> data = new HashMap<>();
    private Doctor selectedDoctor;
    private String selectedDepartment;

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
            tvSummaryTime.setText(getString(R.string.est_wait, doctor.estimatedWaitMinutes));
            tvSummaryPosition.setText(getString(R.string.preview_position, doctor.queueLength + 1));
            btnJoin.setEnabled(true);
        });

        recyclerDoctors.setAdapter(doctorAdapter);

        btnJoin.setEnabled(false);

        populateSampleData();

        List<String> departments = new ArrayList<>();
        departments.add("Select Department");
        departments.addAll(data.keySet());

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, departments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartments.setAdapter(adapter);

        spinnerDepartments.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                boolean valid = position != 0;
                String sel = valid ? parent.getItemAtPosition(position).toString() : null;

                selectedDepartment = sel;

                tvSummaryDepartment.setText(valid
                        ? getString(R.string.department_label, sel)
                        : getString(R.string.department_label_empty));

                selectedDoctor = null;
                tvSummaryDoctor.setText(getString(R.string.doctor_label_empty));
                tvSummaryTime.setText(getString(R.string.est_wait_empty));
                tvSummaryPosition.setText(getString(R.string.preview_position_empty));

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

    private void populateSampleData() {
        List<Doctor> cardiology = new ArrayList<>();
        cardiology.add(new Doctor("Dr. Amina Khan", "Cardiologist", 6, 25));
        cardiology.add(new Doctor("Dr. Sameer Patel", "Cardiologist", 3, 18));

        List<Doctor> dermatology = new ArrayList<>();
        dermatology.add(new Doctor("Dr. Sara Gomez", "Dermatologist", 4, 10));
        dermatology.add(new Doctor("Dr. Hana Park", "Dermatologist", 2, 8));

        data.put("Cardiology", cardiology);
        data.put("Dermatology", dermatology);
    }

    private void populateDoctorsForDepartment(String department) {
        if (department == null) {
            doctorAdapter.setItems(new ArrayList<>());
            return;
        }

        List<Doctor> src = data.get(department);
        if (src == null) {
            doctorAdapter.setItems(new ArrayList<>());
            return;
        }

        List<Doctor> docs = new ArrayList<>(src);
        docs.sort(Comparator.comparingInt(d -> d.estimatedWaitMinutes));
        doctorAdapter.setItems(docs);
        recyclerDoctors.scrollToPosition(0);
    }

    private void showConfirmation() {
        String msg = getString(R.string.department_label, selectedDepartment == null ? "" : selectedDepartment) + "\n" +
                getString(R.string.doctor_label, selectedDoctor == null ? "" : selectedDoctor.name) + "\n" +
                getString(R.string.est_wait, selectedDoctor == null ? 0 : selectedDoctor.estimatedWaitMinutes);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_join_title)
                .setMessage(msg)
                .setPositiveButton("Join", (d, w) -> performJoin())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void performJoin() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.joined_title)
                .setMessage(R.string.joined_message)
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
    }
}
