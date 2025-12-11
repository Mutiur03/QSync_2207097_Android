package com.example.qsync_2207097_android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ListRVAdapter extends RecyclerView.Adapter<ListRVAdapter.ViewHolder> {
    ArrayList<WaitingList> items;

    public ListRVAdapter(ArrayList<WaitingList> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_queue_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WaitingList item = items.get(position);
        holder.listStart.setText(String.valueOf(item.getId()));
        holder.listSecond.setText(item.getName());
        holder.listWaitingTime.setText(item.getWaitingTime());
        String url = item.getImage();
        if (url != null && !url.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .centerCrop()
                    .into(holder.listImage);
        } else {
            holder.listImage.setImageResource(android.R.color.darker_gray);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView listImage;
        TextView listStart, listSecond, listWaitingTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            listImage = itemView.findViewById(R.id.list_image);
            listStart = itemView.findViewById(R.id.list_start);
            listSecond = itemView.findViewById(R.id.list_second);
            listWaitingTime = itemView.findViewById(R.id.list_waiting_time);
        }
    }
}
