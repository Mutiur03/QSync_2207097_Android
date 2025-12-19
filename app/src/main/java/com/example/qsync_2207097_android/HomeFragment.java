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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BottomNavigationView bottomNav =
                requireActivity().findViewById(R.id.bottomNav);
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
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, new JoinFragment())
                    .commit();
            if (bottomNav != null) {
                bottomNav.getMenu().findItem(R.id.join).setChecked(true);
            }
        });

        tvGreeting = view.findViewById(R.id.tvGreeting);
        if (user != null) {
            tvGreeting.setText("Hello " + user.getDisplayName());
        } else {
            tvGreeting.setText("Hello Guest");
        }
    }

    private void loadUserQueues() {
        if (user == null) {
            return;
        }

        DatabaseReference queueRef = FirebaseDatabase.getInstance().getReference("queues");
        queueRef.orderByChild("patientId").equalTo(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userQueues.clear();

                        for (DataSnapshot queueSnapshot : snapshot.getChildren()) {
                            Queue.QueueEntry entry = queueSnapshot.getValue(Queue.QueueEntry.class);
                            if (entry != null && "waiting".equals(entry.status)) {
                                loadDoctorInfo(entry);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //
                    }
                });
    }

    private void loadDoctorInfo(Queue.QueueEntry queueEntry) {
        DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference("doctors")
                .child(queueEntry.doctorId);

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
                //
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
                    String doctorInfo = doctor.name + " - " + dept.name;
                    String tag = dept.name.toUpperCase();
                    String token = "Your Token: " + queueEntry.position;
                    String currentToken = "Current Token: 1";
                    int waitTime = doctor.getEstimatedWaitTime();
                    String wait = "Estimated Wait: " + waitTime + " min";
                    int progress = Math.max(10, 100 - (queueEntry.position * 10));

                    QueueItem queueItem = new QueueItem(doctorInfo, tag, token, currentToken, wait, progress);
                    userQueues.add(queueItem);

                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //
            }
        });
    }

}
