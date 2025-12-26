package com.example.qsync_2207097_android;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class AdminHome extends Fragment implements AdminExpandableAdapter.OnQueueItemClickListener {

    private ExpandableListView expandableListView;
    private AdminExpandableAdapter adapter;
    private ProgressBar progressBar;
    private TextView textEmptyState;

    private List<Department> departments;
    private HashMap<String, List<Doctor>> departmentDoctors;
    private HashMap<String, List<AdminQueueItem>> doctorQueues;
    private HashMap<String, User> usersMap;

    private DatabaseReference databaseRef;
    private ValueEventListener departmentsListener;
    private ValueEventListener doctorsListener;
    private ValueEventListener queuesListener;
    private ValueEventListener usersListener;

    private final String today = getTodayDate();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        initViews(view);
        initFirebase();

        return view;
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getView() != null) {
            View headerView = getView().findViewById(R.id.tvTotalDepartments);
            if (headerView != null && headerView.getParent() != null) {
                ((View) headerView.getParent()).setOnLongClickListener(v -> {
                    showTestDataToggleDialog();
                    return true;
                });
            }
        }

        if (databaseRef != null && isAdded()) {
            loadData();
        }
    }

    private void showTestDataToggleDialog() {
        if (!isAdded() || getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Debug Mode");
        builder.setMessage("Choose data source for debugging:");

        builder.setPositiveButton("Test Data Only", (dialog, which) -> {
            departments.clear();
            departmentDoctors.clear();
            doctorQueues.clear();
            usersMap.clear();
            createTestData();
            showSuccess("Switched to test data only");
        });

        builder.setNegativeButton("Firebase Data", (dialog, which) -> {
            departments.clear();
            departmentDoctors.clear();
            doctorQueues.clear();
            usersMap.clear();
            if (databaseRef != null) {
                loadUsers();
                showSuccess("Loading Firebase data...");
            } else {
                showError("Firebase not available");
                createTestData();
            }
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void initViews(View view) {
        if (view == null) {
            showError("View initialization failed - view is null");
            return;
        }

        try {
            expandableListView = view.findViewById(R.id.expandableListView);
            progressBar = view.findViewById(R.id.progressBar);
            textEmptyState = view.findViewById(R.id.textEmptyState);
            Button buttonRefresh = view.findViewById(R.id.buttonRefresh);

            if (expandableListView == null || progressBar == null ||
                textEmptyState == null || buttonRefresh == null) {
                throw new IllegalStateException("Required views not found in layout");
            }

            departments = new ArrayList<>();
            departmentDoctors = new HashMap<>();
            doctorQueues = new HashMap<>();
            usersMap = new HashMap<>();

            buttonRefresh.setOnClickListener(v -> {
                if (isAdded() && getContext() != null) {
                    showSuccess("Refreshing data...");

                    departments.clear();
                    departmentDoctors.clear();
                    doctorQueues.clear();
                    usersMap.clear();

                    if (databaseRef != null) {
                        loadData();
                    } else {
                        createTestData();
                    }
                }
            });

        } catch (Exception e) {
            showError("View initialization error: " + e.getMessage());
        }
    }

    private void initFirebase() {
        try {
            databaseRef = FirebaseDatabase.getInstance().getReference();
        } catch (Exception e) {
            showError("Firebase initialization failed: " + e.getMessage());
        }
    }

    private void loadData() {
        if (databaseRef == null) {
            showError("Database not initialized. Please check your Firebase configuration.");
            showLoading(false);
            createTestData();
            return;
        }
        showLoading(true);
        loadUsers();
    }

    private void createTestData() {
        Department testDept = new Department("Cardiology", "Heart and cardiovascular care", "Cardiology");
        testDept.id = "test_dept_1";
        testDept.doctorCount = 1;
        testDept.activeQueues = 2;
        departments.add(testDept);

        Doctor testDoctor = new Doctor("test_doctor_1", "John Smith", "Cardiologist", "test_dept_1");
        testDoctor.currentQueueLength = 2;
        List<Doctor> doctorList = new ArrayList<>();
        doctorList.add(testDoctor);
        departmentDoctors.put("test_dept_1", doctorList);

        AdminQueueItem queue1 = new AdminQueueItem();
        queue1.id = "test_queue_1";
        queue1.patientId = "test_patient_1";
        queue1.patientName = "Alice Johnson";
        queue1.doctorId = "test_doctor_1";
        queue1.tokenNumber = "A001";
        queue1.position = 1;
        queue1.status = "waiting";
        queue1.timestamp = System.currentTimeMillis();

        AdminQueueItem queue2 = new AdminQueueItem();
        queue2.id = "test_queue_2";
        queue2.patientId = "test_patient_2";
        queue2.patientName = "Bob Wilson";
        queue2.doctorId = "test_doctor_1";
        queue2.tokenNumber = "A002";
        queue2.position = 2;
        queue2.status = "in_progress";
        queue2.timestamp = System.currentTimeMillis() - 30000;

        List<AdminQueueItem> queueList = new ArrayList<>();
        queueList.add(queue1);
        queueList.add(queue2);
        doctorQueues.put("test_doctor_1", queueList);

        User user1 = new User("Alice Johnson", "alice@example.com");
        User user2 = new User("Bob Wilson", "bob@example.com");
        usersMap.put("test_patient_1", user1);
        usersMap.put("test_patient_2", user2);

        showSuccess("Sample data loaded - Cardiology department with Dr. John Smith");
        setupAdapter();
        showLoading(false);
    }

    private void loadUsers() {
        usersListener = databaseRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isFragmentActive()) {
                    return;
                }
                usersMap.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        usersMap.put(userSnapshot.getKey(), user);
                    }
                }
                loadDepartments();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded() || getContext() == null) {
                    return;
                }
                showLoading(false);
                showError("Failed to load users: " + error.getMessage());
            }
        });
    }

    private void loadDepartments() {
        departmentsListener = databaseRef.child("departments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isFragmentActive()) {
                    return;
                }
                departments.clear();
                if (!snapshot.exists()) {
                    showEmptyState(true);
                    showLoading(false);
                    showError("No departments found in Firebase. Creating sample data for demonstration.");
                    createTestData();
                    return;
                }
                for (DataSnapshot departmentSnapshot : snapshot.getChildren()) {
                    Department department = departmentSnapshot.getValue(Department.class);
                    if (department != null && department.isActive) {
                        department.id = departmentSnapshot.getKey();
                        departments.add(department);
                    }
                }
                if (departments.isEmpty()) {
                    showEmptyState(true);
                    showLoading(false);
                    showError("No active departments found. Creating sample data for demonstration.");
                    createTestData();
                } else {
                    showEmptyState(false);
                    loadDoctorsForDepartments();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded() || getContext() == null) {
                    return;
                }
                showLoading(false);
                showError("Failed to load departments: " + error.getMessage());
            }
        });
    }

    private void loadDoctorsForDepartments() {
        doctorsListener = databaseRef.child("doctors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isFragmentActive()) {
                    return;
                }

                departmentDoctors.clear();

                for (Department department : departments) {
                    departmentDoctors.put(department.id, new ArrayList<>());
                }


                int doctorCount = 0;
                for (DataSnapshot doctorSnapshot : snapshot.getChildren()) {
                    Doctor doctor = doctorSnapshot.getValue(Doctor.class);
                    if (doctor != null) {
                        doctor.id = doctorSnapshot.getKey();
                        doctorCount++;
                        Log.d("DEBUG", "Doctor deptId = " + doctor.departmentId);
                        for (Department d : departments) {
                            Log.d("DEBUG", "Department id = " + d.id);
                        }
                        List<Doctor> doctorList = departmentDoctors.get(doctor.departmentId);
                        if (doctorList != null) {
                            doctorList.add(doctor);
                        }
                    }
                }

                for (Department department : departments) {
                    List<Doctor> doctorList = departmentDoctors.get(department.id);
                    department.doctorCount = doctorList != null ? doctorList.size() : 0;
                }

                if (doctorCount > 0) {
                    showSuccess("Loaded " + doctorCount + " doctors");
                }

                loadQueuesForDoctors();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded() || getContext() == null) {
                    return;
                }
                showLoading(false);
                showError("Failed to load doctors: " + error.getMessage());
            }
        });
    }

    private void loadQueuesForDoctors() {
        queuesListener = databaseRef.child("queues").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isFragmentActive()) {
                    return;
                }

                doctorQueues.clear();

                for (List<Doctor> doctorList : departmentDoctors.values()) {
                    for (Doctor doctor : doctorList) {
                        doctorQueues.put(doctor.id, new ArrayList<>());
                    }
                }

                int queueCount = 0;
                for (DataSnapshot queueSnapshot : snapshot.getChildren()) {
                    String date = queueSnapshot.child("date").getValue(String.class);
                    if (date == null || !date.equals(today)) {
                        continue;
                    }

                    AdminQueueItem queueItem = queueSnapshot.getValue(AdminQueueItem.class);
                    if (queueItem != null) {
                        queueItem.id = queueSnapshot.getKey();
                        queueCount++;

                        if (queueItem.patientId != null && usersMap.containsKey(queueItem.patientId)) {
                            User user = usersMap.get(queueItem.patientId);
                            if (user != null && user.name != null) {
                                queueItem.patientName = user.name;
                            }
                        }

                        List<AdminQueueItem> queueList = doctorQueues.get(queueItem.doctorId);
                        if (queueList != null) {
                            queueList.add(queueItem);
                        }
                    }
                }

                if (queueCount > 0) {
                    showSuccess("Loaded " + queueCount + " queue items for today");
                } else {
                    showSuccess("No queues for today");
                }

                updateQueueCounts();

                for (List<AdminQueueItem> queueList : doctorQueues.values()) {
                    queueList.sort(Comparator.comparingInt(q -> q.position));
                }

                setupAdapter();
                showLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded() || getContext() == null) {
                    return;
                }
                showLoading(false);
                showError("Failed to load queues: " + error.getMessage());
            }
        });
    }

    private void updateQueueCounts() {
        for (List<Doctor> doctorList : departmentDoctors.values()) {
            for (Doctor doctor : doctorList) {
                List<AdminQueueItem> queueList = doctorQueues.get(doctor.id);
                doctor.currentQueueLength = queueList != null ? queueList.size() : 0;
            }
        }

        for (Department department : departments) {
            int activeQueues = 0;
            List<Doctor> doctorList = departmentDoctors.get(department.id);
            if (doctorList != null) {
                for (Doctor doctor : doctorList) {
                    List<AdminQueueItem> queueList = doctorQueues.get(doctor.id);
                    if (queueList != null) {
                        for (AdminQueueItem queue : queueList) {
                            if ("waiting".equals(queue.status) || "in_progress".equals(queue.status)) {
                                activeQueues++;
                            }
                        }
                    }
                }
            }
            department.activeQueues = activeQueues;
        }
    }

    private void setupAdapter() {
        if (getContext() == null || !isAdded()) {
            return;
        }

        try {
            if (adapter == null) {
                adapter = new AdminExpandableAdapter(getContext(), departments, departmentDoctors, doctorQueues);
                adapter.setOnQueueItemClickListener(this);
                expandableListView.setAdapter(adapter);

                expandableListView.post(() -> {
                    if (departments != null && isAdded()) {
                        for (int i = 0; i < departments.size(); i++) {
                            expandableListView.collapseGroup(i);
                        }
                    }
                });
            } else {
                adapter.notifyDataSetChanged();

                expandableListView.post(() -> {
                    if (departments != null && isAdded()) {
                        for (int i = 0; i < departments.size(); i++) {
                            expandableListView.collapseGroup(i);
                        }
                    }
                });
            }

            updateStatistics();

        } catch (Exception e) {
            showError("Error setting up display: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        if (!isAdded() || getView() == null) return;

        try {
            TextView tvTotalDepartments = getView().findViewById(R.id.tvTotalDepartments);
            TextView tvTotalQueues = getView().findViewById(R.id.tvTotalQueues);
            TextView tvTotalUsers = getView().findViewById(R.id.tvTotalUsers);

            if (tvTotalDepartments != null) {
                tvTotalDepartments.setText(String.valueOf(departments.size()));
            }

            int totalActiveQueues = 0;
            for (Department department : departments) {
                totalActiveQueues += department.activeQueues;
            }

            if (tvTotalQueues != null) {
                tvTotalQueues.setText(String.valueOf(totalActiveQueues));
            }

            if (tvTotalUsers != null) {
                tvTotalUsers.setText(String.valueOf(usersMap.size()));
            }

        } catch (Exception e) {
            showError("Error updating statistics: " + e.getMessage());
        }
    }

    @Override
    public void onQueueItemClick(AdminQueueItem queueItem) {
        showQueueDetailsDialog(queueItem);
    }

    @Override
    public void onStatusChangeClick(AdminQueueItem queueItem, String newStatus) {
        updateQueueStatus(queueItem, newStatus);
    }

    private void showQueueDetailsDialog(AdminQueueItem queueItem) {
        if (!isFragmentActive() || queueItem == null) {
            return;
        }

        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_queue_details, null);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();

        TextView patientName = dialogView.findViewById(R.id.textPatientName);
        TextView patientPhone = dialogView.findViewById(R.id.textPatientPhone);
        TextView patientSymptoms = dialogView.findViewById(R.id.textPatientSymptoms);
        TextView queueId = dialogView.findViewById(R.id.textQueueId);
        TextView tokenNumber = dialogView.findViewById(R.id.textTokenNumber);
        TextView queuePosition = dialogView.findViewById(R.id.textQueuePosition);
        TextView currentStatus = dialogView.findViewById(R.id.textCurrentStatus);
        TextView timestamp = dialogView.findViewById(R.id.textTimestamp);
        TextView estimatedTime = dialogView.findViewById(R.id.textEstimatedTime);

        patientName.setText("Name: " + (queueItem.patientName != null ? queueItem.patientName : "Patient ID: " + queueItem.patientId));
        patientPhone.setText("Phone: " + (queueItem.patientPhone != null ? queueItem.patientPhone : "N/A"));
        patientSymptoms.setText("Symptoms: " + (queueItem.symptoms != null ? queueItem.symptoms : "N/A"));
        queueId.setText("Queue ID: " + (queueItem.queueId != null ? queueItem.queueId : queueItem.id));
        tokenNumber.setText("Token: " + (queueItem.tokenNumber != null ? queueItem.tokenNumber : "N/A"));
        queuePosition.setText("Position: " + queueItem.position);
        currentStatus.setText("Status: " + getStatusDisplayText(queueItem.status));
        timestamp.setText("Created: " + getFormattedTimestamp(queueItem.timestamp));
        estimatedTime.setText("Estimated Time: " + getFormattedTime(queueItem.estimatedTime));

        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);
        TextView textSelectedStatus = dialogView.findViewById(R.id.textSelectedStatus);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonSave = dialogView.findViewById(R.id.buttonSave);

        final String[] selectedStatus = {queueItem.status};

        int initialIndex = getStatusIndex(queueItem.status);
        if (spinnerStatus != null && initialIndex >= 0) {
            try {
                spinnerStatus.setSelection(initialIndex);
            } catch (Exception ignored) {}
        }

        if (textSelectedStatus != null) {
            String display = getStatusDisplayText(selectedStatus[0]);
            try {
                textSelectedStatus.setText(getString(R.string.selected_prefix, display));
            } catch (Exception e) {
                textSelectedStatus.setText("Selected: " + display);
            }
        }

        if (spinnerStatus != null) {
            spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 0:
                            selectedStatus[0] = "waiting";
                            break;
                        case 1:
                            selectedStatus[0] = "in_progress";
                            break;
                        case 2:
                            selectedStatus[0] = "completed";
                            break;
                        case 3:
                            selectedStatus[0] = "cancelled";
                            break;
                        default:
                            selectedStatus[0] = queueItem.status;
                    }
                    if (textSelectedStatus != null) {
                        String display = getStatusDisplayText(selectedStatus[0]);
                        try {
                            textSelectedStatus.setText(getString(R.string.selected_prefix, display));
                        } catch (Exception e) {
                            textSelectedStatus.setText("Selected: " + display);
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    //
                }
            });
        }

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonSave.setOnClickListener(v -> {
            updateQueueStatus(queueItem, selectedStatus[0]);
            dialog.dismiss();
        });

        try {
            dialog.show();
        } catch (Exception e) {
            showError("Failed to show dialog: " + e.getMessage());
        }
    } catch (Exception e) {
        showError("Failed to create dialog: " + e.getMessage());
    }
}

    private int getStatusIndex(String status) {
        if (status == null) return 0;
        switch (status) {
            case "waiting":
                return 0;
            case "in_progress":
                return 1;
            case "completed":
                return 2;
            case "cancelled":
                return 3;
            default:
                return 0;
        }
    }

    private void updateQueueStatus(AdminQueueItem queueItem, String newStatus) {
        if (queueItem == null || newStatus == null || databaseRef == null) {
            showError("Invalid data for status update");
            return;
        }

        if (newStatus.equals(queueItem.status)) {
            return;
        }
        if (!isAdded() || getContext() == null) {
            return;
        }

        queueItem.status = newStatus;
        queueItem.updatedAt = System.currentTimeMillis();

        databaseRef.child("queues").child(queueItem.id).setValue(queueItem)
            .addOnSuccessListener(aVoid -> {
                if (isAdded() && getContext() != null) {
                    showSuccess("Queue status updated successfully");
                }
            })
            .addOnFailureListener(e -> {
                if (isAdded() && getContext() != null) {
                    showError("Failed to update queue status: " + e.getMessage());
                }
            });
    }

    private boolean isFragmentActive() {
        return isAdded() && getContext() != null && !isDetached() && !isRemoving();
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (expandableListView != null) {
            expandableListView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showEmptyState(boolean show) {
        if (getView() == null) return;

        View layoutEmptyState = getView().findViewById(R.id.layoutEmptyState);
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        if (textEmptyState != null && layoutEmptyState == null) {
            textEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void showSuccess(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanupFirebaseListeners();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cleanupFirebaseListeners();
    }

    private void cleanupFirebaseListeners() {
        if (databaseRef != null) {
            if (departmentsListener != null) {
                databaseRef.child("departments").removeEventListener(departmentsListener);
                departmentsListener = null;
            }
            if (doctorsListener != null) {
                databaseRef.child("doctors").removeEventListener(doctorsListener);
                doctorsListener = null;
            }
            if (queuesListener != null) {
                databaseRef.child("queues").removeEventListener(queuesListener);
                queuesListener = null;
            }
            if (usersListener != null) {
                databaseRef.child("users").removeEventListener(usersListener);
                usersListener = null;
            }
        }
    }

    private String getFormattedTimestamp(long timestamp) {
        if (timestamp <= 0) return "N/A";
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                .format(new java.util.Date(timestamp));
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "waiting":
                return android.R.color.holo_orange_dark;
            case "in_progress":
                return android.R.color.holo_blue_dark;
            case "completed":
                return android.R.color.holo_green_dark;
            case "cancelled":
                return android.R.color.holo_red_dark;
            default:
                return android.R.color.darker_gray;
        }
    }

    public String getStatusDisplayText(String status) {
        switch (status) {
            case "waiting":
                return "Waiting";
            case "in_progress":
                return "In Progress";
            case "completed":
                return "Completed";
            case "cancelled":
                return "Cancelled";
            default:
                return "Unknown";
        }
    }
    public String getFormattedTime(long estimatedTime) {
        if (estimatedTime <= 0) return "N/A";
        long hours = estimatedTime / 60;
        long minutes = estimatedTime % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }

    // Added: helper to get today's date in yyyy-MM-dd format
    private String getTodayDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(new java.util.Date());
    }
}
