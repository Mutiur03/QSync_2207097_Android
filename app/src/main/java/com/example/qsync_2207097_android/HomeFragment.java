package com.example.qsync_2207097_android;

import android.os.Bundle;

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

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    TextView tvGreeting=null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BottomNavigationView bottomNav =
                requireActivity().findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.home).setChecked(true);
        }
        RecyclerView rv = view.findViewById(R.id.rvQueues);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<QueueItem> sample = new ArrayList<>();
        sample.add(new QueueItem("Dr. Sultana - Pediatrics", "ER", "Your Token: 2", "Current Token: 1", "Estimated Wait: 3 min", 50));
        sample.add(new QueueItem("Dr. Rahman - ENT", "OPD", "Your Token: 5", "Current Token: 2", "Estimated Wait: 8 min", 40));
        sample.add(new QueueItem("Dr. Khan - Cardiology", "OPD", "Your Token: 15", "Current Token: 5", "Estimated Wait: 25 min", 33));
        QueueAdapter adapter = new QueueAdapter(sample);
        rv.setAdapter(adapter);
        FloatingActionButton fab = view.findViewById(R.id.fabAddQueue);
        fab.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, new JoinFragment())
                    .commit();
            if (bottomNav != null) {
                bottomNav.getMenu().findItem(R.id.join).setChecked(true);
            }
        });
        tvGreeting=view.findViewById(R.id.tvGreeting);
        tvGreeting.setText("Hello "+user.getDisplayName());
    }

}
