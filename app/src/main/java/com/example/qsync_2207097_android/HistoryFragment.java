package com.example.qsync_2207097_android;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.app.AlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
public class HistoryFragment extends Fragment {
    private HistoryAdapter adapter;
    private final List<QueueSummary> items = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HistoryAdapter(items, this::showDetails);
        recyclerView.setAdapter(adapter);
        loadMyQueues();
    }
    private void loadMyQueues() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        DatabaseReference queuesRef = FirebaseDatabase.getInstance().getReference("queues");
        queuesRef.orderByChild("patientId").equalTo(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Queue.QueueEntry entry = child.getValue(Queue.QueueEntry.class);
                    if (entry == null) continue;
                    String date = child.child("date").getValue(String.class);
                    String doctorId = entry.doctorId;
                    int position = entry.position;
                    String status = entry.status;
                    String queueId = child.getKey();
                    fetchDoctorAndAdd(queueId, doctorId, position, status, date);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void fetchDoctorAndAdd(String queueId, String doctorId, int position, String status, String date) {
        DatabaseReference docRef = FirebaseDatabase.getInstance().getReference("doctors").child(doctorId);
        docRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Doctor doctor = snapshot.getValue(Doctor.class);
                String doctorName = doctor != null ? doctor.name : "Unknown Doctor";
                QueueSummary summary = new QueueSummary(queueId, doctorName, position, status, date, doctorId);
                items.add(summary);
                adapter.notifyItemInserted(items.size() - 1);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void showDetails(QueueSummary summary) {
        String message = "Doctor: " + summary.doctorName + "\nDate: " + (summary.date != null ? summary.date : "-") + "\nToken: " + summary.position + "\nStatus: " + summary.status + "\nQueue ID: " + summary.queueId;
        new AlertDialog.Builder(requireContext()).setTitle("Queue Details").setMessage(message).setPositiveButton("OK", null).show();
    }
    static class QueueSummary {
        final String queueId;
        final String doctorName;
        final int position;
        final String status;
        final String date;
        final String doctorId;
        QueueSummary(String queueId, String doctorName, int position, String status, String date, String doctorId) {
            this.queueId = queueId;
            this.doctorName = doctorName;
            this.position = position;
            this.status = status;
            this.date = date;
            this.doctorId = doctorId;
        }
    }
    static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
        interface OnClick { void onClick(QueueSummary summary); }
        private final List<QueueSummary> data;
        private final OnClick onClick;
        HistoryAdapter(List<QueueSummary> data, OnClick onClick) { this.data = data; this.onClick = onClick; }
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_queue, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            QueueSummary s = data.get(position);
            holder.tvDoctorName.setText(s.doctorName);
            holder.tvToken.setText("Token: " + s.position);
            holder.tvStatus.setText("Status: " + s.status);
            holder.itemView.setOnClickListener(v -> onClick.onClick(s));
        }
        @Override
        public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView tvDoctorName, tvToken, tvStatus;
            VH(@NonNull View itemView) {
                super(itemView);
                tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
                tvToken = itemView.findViewById(R.id.tvToken);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }
    }
}