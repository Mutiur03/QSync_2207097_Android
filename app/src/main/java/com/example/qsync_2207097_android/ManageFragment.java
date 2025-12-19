package com.example.qsync_2207097_android;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class ManageFragment extends Fragment {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_manage, container, false);
        View cardDept = root.findViewById(R.id.card_manage_departments);
        View cardDoc = root.findViewById(R.id.card_manage_doctors);
        cardDept.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManageDepartmentsActivity.class)));
        cardDoc.setOnClickListener(v -> startActivity(new Intent(getActivity(), ManageDoctorsActivity.class)));
        return root;
    }
}