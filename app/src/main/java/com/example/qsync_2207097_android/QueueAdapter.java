package com.example.qsync_2207097_android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {

    private final List<QueueItem> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(QueueItem item);
    }

    public QueueAdapter(List<QueueItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_queue, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QueueItem item = items.get(position);
        holder.tvDoctor.setText(item.doctor);
        holder.tvDepartment.setText(item.department);
        holder.tvTag.setText(item.tag);
        holder.tvToken.setText(item.token);
        holder.tvPosition.setText(item.position);
        holder.tvWait.setText(item.wait);
        holder.progress.setProgress(item.progress);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctor, tvDepartment, tvTag, tvToken, tvPosition, tvWait;
        ProgressBar progress;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctor = itemView.findViewById(R.id.tvDoctor);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvTag = itemView.findViewById(R.id.tvTag);
            tvToken = itemView.findViewById(R.id.tvToken);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvWait = itemView.findViewById(R.id.tvWait);
            progress = itemView.findViewById(R.id.progress);
        }
    }
}
