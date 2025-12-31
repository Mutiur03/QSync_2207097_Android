package com.example.qsync_2207097_android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.ViewHolder> {

    public interface Callback {
        void onDepartmentSelected(Department department, int position);

    }

    private final Callback callback;
    private final List<Department> items = new ArrayList<>();

    DepartmentAdapter(Callback cb) {
        this.callback = cb;
    }

    void setItems(List<Department> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_department, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Department d = items.get(position);

        holder.departmentName.setText(d.name != null ? d.name : "Unnamed Department");

        holder.departmentId.setText("ID: " + (d.id != null ? d.id : "Unknown"));

        holder.itemView.setOnClickListener(v -> {
            if (callback != null) {
                callback.onDepartmentSelected(d, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView departmentName;
        TextView departmentId;
        ImageView departmentIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            departmentName = itemView.findViewById(R.id.department_name);
            departmentId = itemView.findViewById(R.id.department_id);
            departmentIcon = itemView.findViewById(R.id.department_icon);
        }
    }
}
