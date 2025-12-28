package com.example.qsync_2207097_android;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
public class AdminCompletedFragment extends Fragment {
    private RecyclerView recycler;
    private ProgressBar progress;
    private TextView empty;
    private CompletedAdapter adapter;
    private final String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(new java.util.Date());
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_completed, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recycler = view.findViewById(R.id.recyclerCompleted);
        progress = view.findViewById(R.id.progressCompleted);
        empty = view.findViewById(R.id.textEmptyCompleted);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CompletedAdapter();
        recycler.setAdapter(adapter);
        loadCompleted();
    }
    private void loadCompleted() {
        progress.setVisibility(View.VISIBLE);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("queues");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<AdminQueueItem> list = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String date = ds.child("date").getValue(String.class);
                    if (!today.equals(date)) continue;
                    AdminQueueItem item = ds.getValue(AdminQueueItem.class);
                    if (item != null && "completed".equals(item.status)) {
                        item.id = ds.getKey();
                        list.add(item);
                    }
                }
                adapter.submit(list);
                progress.setVisibility(View.GONE);
                empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progress.setVisibility(View.GONE);
                empty.setVisibility(View.VISIBLE);
                empty.setText("Failed to load: " + error.getMessage());
            }
        });
    }
    static class CompletedAdapter extends RecyclerView.Adapter<CompletedAdapter.Holder> {
        private final List<AdminQueueItem> items = new ArrayList<>();
        private final HashMap<String, String> patientNameCache = new HashMap<>();
        private final HashMap<String, String> doctorNameCache = new HashMap<>();
        void submit(List<AdminQueueItem> list) {
            items.clear();
            if (list != null) items.addAll(list);
            notifyDataSetChanged();
        }
        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_completed, parent, false);
            return new Holder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            AdminQueueItem it = items.get(position);
            String token = it.tokenNumber != null && !it.tokenNumber.isEmpty() ? it.tokenNumber : ("T" + String.format(Locale.US, "%03d", it.position));
            String displayPatientName = it.patientName;
            if (displayPatientName == null || displayPatientName.trim().isEmpty()) {
                if (it.patientId != null && patientNameCache.containsKey(it.patientId)) {
                    displayPatientName = patientNameCache.get(it.patientId);
                } else if (it.patientId != null) {
                    h.title.setText("Patient • " + token);
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(it.patientId).child("name");
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.getValue(String.class);
                            if (name != null && !name.trim().isEmpty()) {
                                patientNameCache.put(it.patientId, name);
                                if (h.getBindingAdapterPosition() == position) {
                                    h.title.setText(name + " • " + token);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            if (displayPatientName == null || displayPatientName.trim().isEmpty()) {
                displayPatientName = "Patient";
            }
            h.title.setText(displayPatientName + " • " + token);
            String displayDoctorName = it.doctorName;
            if (displayDoctorName == null || displayDoctorName.trim().isEmpty()) {
                if (it.doctorId != null && doctorNameCache.containsKey(it.doctorId)) {
                    displayDoctorName = doctorNameCache.get(it.doctorId);
                } else if (it.doctorId != null) {
                    h.subtitle.setText("Dr: " + it.doctorId);
                    DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference("doctors").child(it.doctorId).child("name");
                    doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String dname = snapshot.getValue(String.class);
                            if (dname != null && !dname.trim().isEmpty()) {
                                doctorNameCache.put(it.doctorId, dname);
                                if (h.getBindingAdapterPosition() == position) {
                                    h.subtitle.setText("Dr: " + dname);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            if (displayDoctorName == null || displayDoctorName.trim().isEmpty()) {
                displayDoctorName = it.doctorId != null ? it.doctorId : "Unknown";
            }
            h.subtitle.setText("Dr: " + displayDoctorName);
        }
        @Override
        public int getItemCount() { return items.size(); }
        static class Holder extends RecyclerView.ViewHolder {
            TextView title, subtitle, trailing;
            Holder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.textTitle);
                subtitle = itemView.findViewById(R.id.textSubtitle);
                trailing = itemView.findViewById(R.id.textTrailing);
            }
        }
    }
}
