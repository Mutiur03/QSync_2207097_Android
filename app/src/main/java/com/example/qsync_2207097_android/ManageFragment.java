package com.example.qsync_2207097_android;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ManageFragment extends Fragment {

    private TextView statDepartments, statDoctors;
    private DatabaseReference dbRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_manage, container, false);
        statDepartments = root.findViewById(R.id.stat_departments);
        statDoctors = root.findViewById(R.id.stat_doctors);

        View cardDept = root.findViewById(R.id.card_manage_departments);
        View cardDoc = root.findViewById(R.id.card_manage_doctors);
        cardDept.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManageDepartmentsActivity.class)));
        cardDoc.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManageDoctorsActivity.class)));
        loadStats();

        return root;
    }

    private void loadStats() {
        dbRef.child("departments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (statDepartments != null) {
                    statDepartments.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dbRef.child("doctors").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (statDoctors != null) {
                    statDoctors.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}