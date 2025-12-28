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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);
        databaseRef = FirebaseDatabase.getInstance().getReference();
        initViews(view);
        Button buttonCompleted = view.findViewById(R.id.buttonCompleted);
        if (buttonCompleted != null) {
            buttonCompleted.setOnClickListener(v -> openCompleted());
        }
        return view;
    }
    private void openCompleted() {
        if (!isAdded()) return;
        try {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame, new AdminCompletedFragment()).addToBackStack(null).commit();
        } catch (Exception e) {
            showError("Failed to open completed view: " + e.getMessage());
        }
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
            if (expandableListView == null || progressBar == null || textEmptyState == null || buttonRefresh == null) {
                throw new IllegalStateException("Required views not found in layout");
            }
            departments = new ArrayList<>();
            departmentDoctors = new HashMap<>();
            doctorQueues = new HashMap<>();
            usersMap = new HashMap<>();
            showLoading(true);
            loadUsers();
            loadDepartments();
            loadDoctorsForDepartments();
            loadQueuesForDoctors();
            updateStatistics();
            showLoading(false);
            buttonRefresh.setOnClickListener(v -> {
                if (isAdded() && getContext() != null) {
                    showSuccess("Refreshing data...");
                    departments.clear();
                    departmentDoctors.clear();
                    doctorQueues.clear();
                    usersMap.clear();
                    showLoading(true);
                    loadUsers();
                    loadDepartments();
                    loadDoctorsForDepartments();
                    loadQueuesForDoctors();
                    updateStatistics();
                    showLoading(false);
                }
            });
        } catch (Exception e) {
            showError("View initialization error: " + e.getMessage());
        }
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
                            queueItem.patientName = user.name;
                            queueItem.patientPhone = user.phone;
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
                int active = 0;
                if (queueList != null) {
                    for (AdminQueueItem q : queueList) {
                        if ("waiting".equals(q.status) || "in_progress".equals(q.status)) active++;
                    }
                }
                doctor.currentQueueLength = active;
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
            TextView queuePosition = dialogView.findViewById(R.id.textQueuePosition);
            TextView currentStatus = dialogView.findViewById(R.id.textCurrentStatus);
            TextView timestamp = dialogView.findViewById(R.id.textTimestamp);
            TextView textPriority = dialogView.findViewById(R.id.textPriority);
            patientName.setText("Name: " + (queueItem.patientName != null ? queueItem.patientName : "Patient ID: " + queueItem.patientId));
            patientPhone.setText("Phone: " + (queueItem.patientPhone != null ? queueItem.patientPhone : "N/A"));
            patientSymptoms.setText("Symptoms: " + (queueItem.symptoms != null ? queueItem.symptoms : "N/A"));
            queueId.setText("Queue ID: " + (queueItem.queueId != null ? queueItem.queueId : queueItem.id));
            queuePosition.setText("Position: " + queueItem.position);
            currentStatus.setText("Status: " + getStatusDisplayText(queueItem.status));
            timestamp.setText("Created: " + getFormattedTimestamp(queueItem.timestamp));
            textPriority.setText("Priority: " + queueItem.priority);
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
                    public void onNothingSelected(AdapterView<?> parent) {}
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
        databaseRef.child("queues").child(queueItem.id).child("status").setValue(newStatus).addOnSuccessListener(aVoid -> {
            if (isAdded() && getContext() != null) {
                showSuccess("Queue status updated successfully");
            }
            if ("completed".equals(newStatus)) {
                try {
                    List<AdminQueueItem> queueList = doctorQueues != null ? doctorQueues.get(queueItem.doctorId) : null;
                    if (queueList != null && !queueList.isEmpty()) {
                        queueList.sort(java.util.Comparator.comparingInt(q -> q.position));
                        AdminQueueItem nextWaiting = null;
                        for (AdminQueueItem q : queueList) {
                            if (q != null && "waiting".equals(q.status)) {
                                if (q.position > queueItem.position) {
                                    nextWaiting = q;
                                    break;
                                }
                            }
                        }
                        if (nextWaiting == null) {
                            for (AdminQueueItem q : queueList) {
                                if (q != null && "waiting".equals(q.status)) {
                                    nextWaiting = q;
                                    break;
                                }
                            }
                        }
                        if (nextWaiting != null && nextWaiting.id != null) {
                            final String nextId = nextWaiting.id;
                            final AdminQueueItem nextItem = nextWaiting;
                            databaseRef.child("queues").child(nextId).child("status").setValue("in_progress").addOnSuccessListener(v -> {
                                nextItem.status = "in_progress";
                                if (adapter != null) {
                                    adapter.notifyDataSetChanged();
                                }
                                showSuccess("Next token moved to In Progress");
                            }).addOnFailureListener(e -> showError("Failed to move next token to In Progress: " + e.getMessage()));
                        }
                    }
                } catch (Exception ex) {
                    showError("Auto-advance failed: " + ex.getMessage());
                }
            }
            try {
                updateQueueCounts();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                updateStatistics();
            } catch (Exception ignored) {}
        }).addOnFailureListener(e -> {
            if (isAdded() && getContext() != null) {
                showError("Failed to update queue status: " + e.getMessage());
            }
        });
    }
    private boolean isFragmentActive() {
        return isAdded() && getContext() != null;
    }
    private void showLoading(boolean show) {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        } catch (Exception ignored) {}
    }
    private void showEmptyState(boolean show) {
        try {
            if (textEmptyState != null) {
                textEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        } catch (Exception ignored) {}
    }
    private void showSuccess(String message) {
        try {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ignored) {}
    }
    private void showError(String message) {
        try {
            Log.e("AdminHome", message);
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        } catch (Exception ignored) {}
    }
    private String getTodayDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }
    private String getStatusDisplayText(String status) {
        if (status == null) return "Waiting";
        switch (status) {
            case "in_progress":
                return "In Progress";
            case "completed":
                return "Completed";
            case "cancelled":
                return "Cancelled";
            default:
                return "Waiting";
        }
    }
    private String getFormattedTimestamp(Long ts) {
        if (ts == null) return "N/A";
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, yyyy h:mm a", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date(ts));
        } catch (Exception e) {
            return String.valueOf(ts);
        }
    }
}
