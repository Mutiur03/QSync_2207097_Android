package com.example.qsync_2207097_android;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import androidx.appcompat.app.AlertDialog;

public class HomeFragment extends Fragment {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    TextView tvGreeting, tvDate;
    RecyclerView rv;
    LinearLayout layoutEmptyState;
    QueueAdapter adapter;
    List<QueueItem> userQueues = new ArrayList<>();
    private final Set<String> notifiedQueueKeys = new HashSet<>();
    private final String today = getTodayDate();

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private String getDisplayDate() {
        return new SimpleDateFormat("EEEE, d MMM", Locale.US).format(new Date());
    }

    private String getGreetingMessage() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int hour = c.get(java.util.Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12)
            return "Good Morning,";
        if (hour >= 12 && hour < 17)
            return "Good Afternoon,";
        if (hour >= 17 && hour < 21)
            return "Good Evening,";
        return "Good Night,";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.home).setChecked(true);
        }
        rv = view.findViewById(R.id.rvQueues);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvDate = view.findViewById(R.id.tvDate);
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabAddQueue);
        tvDate.setText(getDisplayDate());
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            tvGreeting.setText(getGreetingMessage() + " " + user.getDisplayName());
        } else {
            tvGreeting.setText(getGreetingMessage() + " Mutiur");
        }
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new QueueAdapter(userQueues, item -> {
            QueueDetailsUserDialog dialog = new QueueDetailsUserDialog();
            dialog.setQueueItem(item);
            dialog.show(getParentFragmentManager(), "QueueDetailsUserDialog");
        });
        rv.setAdapter(adapter);
        loadUserQueues();
        fab.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame, new JoinFragment())
                    .commit();
            if (bottomNav != null) {
                bottomNav.getMenu().findItem(R.id.join).setChecked(true);
            }
        });
    }

    private void updateEmptyState() {
        if (userQueues.isEmpty()) {
            rv.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rv.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void loadUserQueues() {
        if (user == null) {
            updateEmptyState();
            return;
        }
        DatabaseReference queueRef = FirebaseDatabase.getInstance().getReference("queues");
        queueRef.orderByChild("patientId").equalTo(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userQueues.clear();
                if (!snapshot.exists()) {
                    updateEmptyState();
                    return;
                }
                final boolean[] hasCandidates = { false };
                for (DataSnapshot queueSnapshot : snapshot.getChildren()) {
                    Queue.QueueEntry entry = queueSnapshot.getValue(Queue.QueueEntry.class);
                    String date = queueSnapshot.child("date").getValue(String.class);
                    if (entry != null && ("waiting".equals(entry.status) || "in_progress".equals(entry.status))
                            && today.equals(date)) {
                        hasCandidates[0] = true;
                        loadDoctorInfo(entry);
                    }
                }
                if (!hasCandidates[0]) {
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateEmptyState();
            }
        });
    }

    private void loadDoctorInfo(Queue.QueueEntry queueEntry) {
        DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference("doctors").child(queueEntry.doctorId);
        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Doctor doctor = snapshot.getValue(Doctor.class);
                if (doctor != null) {
                    loadDepartmentInfo(queueEntry, doctor);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadDepartmentInfo(Queue.QueueEntry queueEntry, Doctor doctor) {
        DatabaseReference deptRef = FirebaseDatabase.getInstance().getReference("departments")
                .child(doctor.departmentId);
        deptRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Department dept = snapshot.getValue(Department.class);
                if (dept != null && isAdded()) {
                    String doctorInfo = doctor.name;
                    String tag = "OPD";

                    String doctorName = "Dr. " + doctor.name;
                    String departmentName = dept.name;

                    String token = String.valueOf(queueEntry.position); // Just the number

                    computeCurrentToken(queueEntry, (currentTokenValue, isEmergency) -> {
                        if (!isAdded())
                            return;
                        String uniqueKey = queueEntry.doctorId + "|" + queueEntry.position + "|" + today;
                        if (currentTokenValue == queueEntry.position && !notifiedQueueKeys.contains(uniqueKey)) {
                            notifiedQueueKeys.add(uniqueKey);
                            requireActivity().runOnUiThread(() -> {
                                new AlertDialog.Builder(requireContext())
                                        .setTitle("It's your turn!")
                                        .setMessage("Current token " + currentTokenValue + " matches your token "
                                                + queueEntry.position + ". Please proceed to the doctor.")
                                        .setPositiveButton("OK", null)
                                        .show();
                            });
                        }

                        String currentTokenStr = String.valueOf(currentTokenValue);

                        int avg = doctor.getAvgTimeMinutes();
                        computeWaitingMinutes(queueEntry, Math.max(1, avg), waitTimeMinutes -> {
                            if (!isAdded())
                                return;
                            String waitStr = (currentTokenValue == queueEntry.position ? "0"
                                    : String.valueOf(waitTimeMinutes)) + "m";
                            int totalToReachYou = Math.max(1, queueEntry.position - 1);
                            int progressed = Math.max(0, Math.min(queueEntry.position - 1, currentTokenValue - 1));
                            int progressPercent = (int) Math.round((progressed * 100.0) / totalToReachYou);
                            if (currentTokenValue == queueEntry.position)
                                progressPercent = 100;
                            QueueItem queueItem = new QueueItem(doctorName, departmentName, tag, token, currentTokenStr,
                                    waitStr, progressPercent);
                            userQueues.add(queueItem);
                            adapter.notifyDataSetChanged();
                            updateEmptyState();
                        });
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    interface TokenCallback {
        void onComputed(int currentToken, boolean isEmergency);
    }

    interface WaitTimeCallback {
        void onComputed(int waitTimeMinutes);
    }

    private void computeWaitingMinutes(Queue.QueueEntry queueEntry, int avgMinutesPerPatient,
            WaitTimeCallback callback) {
        DatabaseReference queuesRef = FirebaseDatabase.getInstance().getReference("queues");
        queuesRef.orderByChild("doctorId").equalTo(queueEntry.doctorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int currentToken = 0;
                        long startedAtMs = -1L;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String date = child.child("date").getValue(String.class);
                            String status = child.child("status").getValue(String.class);
                            Long posLong = child.child("position").getValue(Long.class);
                            int pos = (posLong != null) ? posLong.intValue() : -1;
                            if (!today.equals(date))
                                continue;
                            if (status != null && status.equals("in_progress") && pos > 0) {
                                Long sa = child.child("startedAt").getValue(Long.class);
                                if (sa == null) {
                                    child.getRef().child("startedAt").setValue(ServerValue.TIMESTAMP);
                                } else {
                                    startedAtMs = sa;
                                }
                                currentToken = Math.max(currentToken, pos);
                            }
                        }
                        if (currentToken == queueEntry.position) {
                            callback.onComputed(0);
                            return;
                        }
                        int remainingCurrent = 0;
                        if (startedAtMs > 0L) {
                            long now = System.currentTimeMillis();
                            long elapsedMin = Math.max(0L, (now - startedAtMs) / 60000L);
                            remainingCurrent = (int) Math.max(0L, (long) avgMinutesPerPatient - elapsedMin);
                        } else {
                            remainingCurrent = avgMinutesPerPatient;
                        }
                        int slotsBetween = Math.max(0, (queueEntry.position - currentToken) - 1);
                        int waitMinutes = Math.max(0,
                                remainingCurrent + (slotsBetween * Math.max(1, avgMinutesPerPatient)));
                        callback.onComputed(waitMinutes);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onComputed(0);
                    }
                });
    }

    private void computeCurrentToken(Queue.QueueEntry queueEntry, TokenCallback callback) {
        final int[] maxInProgressPos = { 0 };
        final boolean[] isEmergencyOfCurrent = { false };
        DatabaseReference queuesRef = FirebaseDatabase.getInstance().getReference("queues");
        queuesRef.orderByChild("doctorId").equalTo(queueEntry.doctorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String date = child.child("date").getValue(String.class);
                            String status = child.child("status").getValue(String.class);
                            Long posLong = child.child("position").getValue(Long.class);
                            int pos = (posLong != null) ? posLong.intValue() : -1;
                            String priority = child.child("priority").getValue(String.class);
                            boolean emergency = priority != null && priority.equalsIgnoreCase("Emergency");
                            if (today.equals(date) && status != null && status.equals("in_progress") && pos > 0) {
                                if (pos > maxInProgressPos[0]) {
                                    maxInProgressPos[0] = pos;
                                    isEmergencyOfCurrent[0] = emergency;
                                    Long sa = child.child("startedAt").getValue(Long.class);
                                    if (sa == null) {
                                        child.getRef().child("startedAt").setValue(ServerValue.TIMESTAMP);
                                    }
                                }
                            }
                        }
                        if (maxInProgressPos[0] == 0) {
                        }

                        callback.onComputed(maxInProgressPos[0], isEmergencyOfCurrent[0]);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onComputed(maxInProgressPos[0], isEmergencyOfCurrent[0]);
                    }
                });
    }
}
