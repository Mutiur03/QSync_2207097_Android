package com.example.qsync_2207097_android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ManageDoctorAdapter extends RecyclerView.Adapter<ManageDoctorAdapter.ViewHolder> {

    public interface Callback {
        void onDoctorSelected(Doctor doctor, int position);
    }

    private final Callback callback;
    private final List<Doctor> items = new ArrayList<>();
    private final List<Doctor> filteredItems = new ArrayList<>();
    private final List<Department> departments = new ArrayList<>();

    ManageDoctorAdapter(Callback cb) {
        this.callback = cb;
    }

    void setItems(List<Doctor> list) {
        items.clear();
        filteredItems.clear();
        if (list != null) {
            items.addAll(list);
            filteredItems.addAll(list);
        }
        notifyDataSetChanged();
    }

    void setDepartments(List<Department> list) {
        departments.clear();
        if (list != null) departments.addAll(list);
        notifyDataSetChanged();
    }

    private String getDepartmentName(String departmentId) {
        for (Department dept : departments) {
            if (dept.id != null && dept.id.equals(departmentId)) {
                return dept.name;
            }
        }
        return departmentId;
    }

    public void filter(String query) {
        filteredItems.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredItems.addAll(items);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Doctor doctor : items) {
                if (doctor == null) continue;

                String deptName = getDepartmentName(doctor.departmentId);
                if ((doctor.name != null && doctor.name.toLowerCase().contains(lowerQuery)) ||
                    (doctor.specialty != null && doctor.specialty.toLowerCase().contains(lowerQuery)) ||
                    (deptName != null && deptName.toLowerCase().contains(lowerQuery)) ||
                    (doctor.email != null && doctor.email.toLowerCase().contains(lowerQuery)) ||
                    (doctor.phone != null && doctor.phone.contains(lowerQuery))) {
                    filteredItems.add(doctor);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_doctor, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor d = filteredItems.get(position);
        holder.title.setText(d.name);
        String deptName = getDepartmentName(d.departmentId);
        holder.subtitle.setText(d.specialty + " (Dept: " + deptName + ")");
        holder.itemView.setOnClickListener(v -> {
            if (callback != null) {
                int originalPosition = items.indexOf(d);
                callback.onDoctorSelected(d, originalPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, subtitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            subtitle = itemView.findViewById(android.R.id.text2);
        }
    }
}
