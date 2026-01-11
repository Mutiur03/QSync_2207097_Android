package com.example.qsync_2207097_android;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    public interface Callback {
        void onDoctorSelected(Doctor doctor, int position);
    }

    private List<Doctor> items = new ArrayList<>();
    private int selectedIndex = RecyclerView.NO_POSITION;
    private final Callback callback;

    DoctorAdapter(Callback callback) {
        this.callback = callback;
    }

    void setItems(List<Doctor> list) {
        items.clear();
        if (list != null) items.addAll(list);
        selectedIndex = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor d = items.get(position);
        holder.name.setText(d.name);
        holder.specialty.setText(d.specialty + " • " + d.yearsOfExperience + " years");
        holder.queueInfo.setText("Queue: " + d.currentQueueLength + " patients");
        holder.waitTime.setText("Est. " + d.getEstimatedWaitTime() + " min");
        if (selectedIndex == position) {
            holder.cardView.setStrokeColor(Color.parseColor("#0B8B8B"));
            holder.cardView.setStrokeWidth(4); // Thicker stroke for selection
            holder.cardView.setCardElevation(8f);
        } else {
            holder.cardView.setStrokeColor(Color.parseColor("#E0E0E0"));
            holder.cardView.setStrokeWidth(2);
            holder.cardView.setCardElevation(2f);
        }

        holder.itemView.setOnClickListener(v -> {
            int previous = selectedIndex;
            selectedIndex = holder.getBindingAdapterPosition();
            notifyItemChanged(previous);
            notifyItemChanged(selectedIndex);
            if (callback != null) callback.onDoctorSelected(d, selectedIndex);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, specialty, queueInfo, waitTime;
        MaterialCardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            if (itemView instanceof MaterialCardView) {
                cardView = (MaterialCardView) itemView;
            } else {
                throw new IllegalStateException("Layout root must be MaterialCardView");
            }
            
            name = itemView.findViewById(R.id.doc_name);
            specialty = itemView.findViewById(R.id.doc_specialty);
            queueInfo = itemView.findViewById(R.id.doc_queue_info);
            waitTime = itemView.findViewById(R.id.doc_wait_time);
        }
    }
}
