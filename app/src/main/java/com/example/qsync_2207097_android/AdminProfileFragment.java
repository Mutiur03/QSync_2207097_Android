package com.example.qsync_2207097_android;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminProfileFragment extends Fragment {

    private TextView adminProfileName, adminProfileEmail;
    private TextView statTotalAppointments, statActiveQueues, statTotalDoctors, statTotalUsers;
    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        adminProfileName = view.findViewById(R.id.admin_profile_name);
        adminProfileEmail = view.findViewById(R.id.admin_profile_email);
        statTotalAppointments = view.findViewById(R.id.stat_total_appointments);
        statActiveQueues = view.findViewById(R.id.stat_active_queues);
        statTotalDoctors = view.findViewById(R.id.stat_total_doctors);
        statTotalUsers = view.findViewById(R.id.stat_total_users);

        MaterialButton logoutButton = view.findViewById(R.id.button_admin_logout);
        LinearLayout actionDoctors = view.findViewById(R.id.action_manage_doctors);
        LinearLayout actionDepartments = view.findViewById(R.id.action_manage_departments);
        LinearLayout actionSettings = view.findViewById(R.id.action_system_settings);
        LinearLayout controlReports = view.findViewById(R.id.control_view_reports);
        LinearLayout controlUsers = view.findViewById(R.id.control_user_management);
        LinearLayout controlBackup = view.findViewById(R.id.control_backup_data);
        loadAdminProfile();
        loadSystemStats();
        loadDoctorStats();
        loadUserStats();
        actionDoctors.setOnClickListener(v -> navigateToManage());
        actionDepartments.setOnClickListener(v -> navigateToManage());
        actionSettings.setOnClickListener(
                v -> Toast.makeText(requireContext(), "System Settings - Coming Soon", Toast.LENGTH_SHORT).show());
        controlReports.setOnClickListener(v -> navigateToCompleted());

        controlUsers.setOnClickListener(
                v -> Toast.makeText(requireContext(), "User Management - Coming Soon", Toast.LENGTH_SHORT).show());

        controlBackup.setOnClickListener(
                v -> Toast.makeText(requireContext(), "Backup & Data - Coming Soon", Toast.LENGTH_SHORT).show());

        logoutButton.setOnClickListener(v -> showLogoutDialog());
    }

    private void navigateToManage() {
        if (getActivity() != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame, new ManageFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void navigateToCompleted() {
        if (getActivity() != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame, new AdminCompletedFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void loadAdminProfile() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            String displayName = currentUser.getDisplayName();

            adminProfileEmail.setText(email != null ? email : "admin@qsync.com");
            adminProfileName.setText(displayName != null && !displayName.isEmpty() ? displayName : "Administrator");
        }
    }

    private void loadSystemStats() {
        String todayDate = getTodayDate();
        dbRef.child("queues").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalAppointments = snapshot.getChildrenCount();
                statTotalAppointments.setText(String.valueOf(totalAppointments));
                long activeQueues = 0;
                for (DataSnapshot queueSnapshot : snapshot.getChildren()) {
                    String date = queueSnapshot.child("date").getValue(String.class);
                    if (date == null || !date.equals(todayDate)) {
                        continue;
                    }

                    AdminQueueItem queueItem = queueSnapshot.getValue(AdminQueueItem.class);
                    if (queueItem != null && queueItem.status != null) {
                        if ("waiting".equals(queueItem.status) || "in_progress".equals(queueItem.status)) {
                            activeQueues++;
                        }
                    }
                }
                statActiveQueues.setText(String.valueOf(activeQueues));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load appointment stats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getTodayDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }

    private void loadDoctorStats() {
        dbRef.child("doctors").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalDoctors = snapshot.getChildrenCount();
                statTotalDoctors.setText(String.valueOf(totalDoctors));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load doctor stats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserStats() {
        dbRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalUsers = snapshot.getChildrenCount();
                statTotalUsers.setText(String.valueOf(totalUsers));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load user stats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out from admin panel?")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Sign Out", (d, w) -> {
                    auth.signOut();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    if (getActivity() != null)
                        getActivity().finish();
                    Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
