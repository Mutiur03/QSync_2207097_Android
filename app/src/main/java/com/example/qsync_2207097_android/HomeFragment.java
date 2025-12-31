package com.example.qsync_2207097_android;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
public class HomeFragment extends Fragment {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    TextView tvGreeting = null;
    RecyclerView rv;
    QueueAdapter adapter;
    List<QueueItem> userQueues = new ArrayList<>();
    private final String today = getTodayDate();
    private String getTodayDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(new java.util.Date());
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
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new QueueAdapter(userQueues);
        rv.setAdapter(adapter);
        loadUserQueues();
        FloatingActionButton fab = view.findViewById(R.id.fabAddQueue);
        fab.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame, new JoinFragment()).commit();
            if (bottomNav != null) {
                bottomNav.getMenu().findItem(R.id.join).setChecked(true);
            }
        });
        tvGreeting = view.findViewById(R.id.tvGreeting);
    }
    private void loadUserQueues() {
        if (user == null) {
            return;
        }
        DatabaseReference queueRef = FirebaseDatabase.getInstance().getReference("queues");
        queueRef.orderByChild("patientId").equalTo(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userQueues.clear();
                for (DataSnapshot queueSnapshot : snapshot.getChildren()) {
                    Queue.QueueEntry entry = queueSnapshot.getValue(Queue.QueueEntry.class);
                    String date = queueSnapshot.child("date").getValue(String.class);
                    if (entry != null && ("waiting".equals(entry.status) || "in_progress".equals(entry.status)) && today.equals(date)) {
                        loadDoctorInfo(entry);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void loadDepartmentInfo(Queue.QueueEntry queueEntry, Doctor doctor) {
        DatabaseReference deptRef = FirebaseDatabase.getInstance().getReference("departments").child(doctor.departmentId);
        deptRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Department dept = snapshot.getValue(Department.class);
                if (dept != null && isAdded()) {
                    String doctorInfo = doctor.name + " - " + dept.name;
                    String tag = dept.name.toUpperCase();
                    String token = "Your Token: " + queueEntry.position;
                    computeCurrentToken(queueEntry, (currentTokenValue, isEmergency) -> {
                        if (!isAdded()) return;
                        String currentToken = "Current Token: " + currentTokenValue + (isEmergency ? " (Emergency)" : "");
                        int avg = doctor.getAvgTimeMinutes();
                        computeWaitingMinutes(queueEntry, Math.max(1, avg), waitTimeMinutes -> {
                            if (!isAdded()) return;
                            String wait = "Estimated Wait: " + (currentTokenValue == queueEntry.position ? 0 : waitTimeMinutes) + " min";
                            int totalToReachYou = Math.max(1, queueEntry.position - 1);
                            int progressed = Math.max(0, Math.min(queueEntry.position - 1, currentTokenValue - 1));
                            int progressPercent = (int) Math.round((progressed * 100.0) / totalToReachYou);
                            QueueItem queueItem = new QueueItem(doctorInfo, tag, token, currentToken, wait, progressPercent);
                            userQueues.add(queueItem);
                            adapter.notifyItemInserted(userQueues.size() - 1);
                        });
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    interface TokenCallback {
        void onComputed(int currentToken, boolean isEmergency);
    }
    interface WaitTimeCallback {
        void onComputed(int waitTimeMinutes);
    }
    private void computeWaitingMinutes(Queue.QueueEntry queueEntry, int avgMinutesPerPatient, WaitTimeCallback callback) {
        DatabaseReference queuesRef = FirebaseDatabase.getInstance().getReference("queues");
        queuesRef.orderByChild("doctorId").equalTo(queueEntry.doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int currentToken = 0;
                long startedAtMs = -1L;
                for (DataSnapshot child : snapshot.getChildren()) {
                    String date = child.child("date").getValue(String.class);
                    String status = child.child("status").getValue(String.class);
                    Long posLong = child.child("position").getValue(Long.class);
                    int pos = (posLong != null) ? posLong.intValue() : -1;
                    if (!today.equals(date)) continue;
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
                int waitMinutes = Math.max(0, remainingCurrent + (slotsBetween * Math.max(1, avgMinutesPerPatient)));
                callback.onComputed(waitMinutes);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onComputed(0);
            }
        });
    }
    private void computeCurrentToken(Queue.QueueEntry queueEntry, TokenCallback callback) {
        final int[] maxInProgressPos = {0};
        final boolean[] isEmergencyOfCurrent = {false};
        DatabaseReference queuesRef = FirebaseDatabase.getInstance().getReference("queues");
        queuesRef.orderByChild("doctorId").equalTo(queueEntry.doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
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
                if(maxInProgressPos[0] == -1){
                    maxInProgressPos[0] = queueEntry.position;
                    isEmergencyOfCurrent[0] = false;
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
