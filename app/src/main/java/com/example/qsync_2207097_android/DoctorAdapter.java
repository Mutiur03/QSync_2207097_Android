package com.example.qsync_2207097_android;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    public interface Callback {
        void onDoctorSelected(JoinQueue.Doctor doctor, int position);
    }

    private List<JoinQueue.Doctor> items = new ArrayList<>();
    private int selectedIndex = RecyclerView.NO_POSITION;
    private final Callback callback;

    DoctorAdapter(Callback callback) {
        this.callback = callback;
    }

    void setItems(List<JoinQueue.Doctor> list) {
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
        JoinQueue.Doctor d = items.get(position);
        holder.name.setText(d.name);
        holder.specialty.setText(d.specialty + " • " + d.years + " yrs");
        holder.info.setText("Queue: " + d.queueLength + "  •  Est. " + d.estimatedWaitMinutes + " min");
        holder.itemView.setBackgroundColor(selectedIndex == position ? Color.parseColor("#E3F2FD") : Color.TRANSPARENT);
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
        TextView name, specialty, info;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.doc_name);
            specialty = itemView.findViewById(R.id.doc_specialty);
            info = itemView.findViewById(R.id.doc_queue_info);
        }
    }
}

