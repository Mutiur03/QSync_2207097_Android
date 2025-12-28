package com.example.qsync_2207097_android;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class AdminExpandableAdapter extends BaseExpandableListAdapter {
    private final Context context;
    private final List<Department> departments;
    private final HashMap<String, List<Doctor>> departmentDoctors;
    private final HashMap<String, List<AdminQueueItem>> doctorQueues;
    private OnQueueItemClickListener queueItemClickListener;
    public interface OnQueueItemClickListener {
        void onQueueItemClick(AdminQueueItem queueItem);
        void onStatusChangeClick(AdminQueueItem queueItem, String newStatus);
    }
    public AdminExpandableAdapter(Context context, List<Department> departments, HashMap<String, List<Doctor>> departmentDoctors, HashMap<String, List<AdminQueueItem>> doctorQueues) {
        this.context = context;
        this.departments = departments != null ? departments : new ArrayList<>();
        this.departmentDoctors = departmentDoctors != null ? departmentDoctors : new HashMap<>();
        this.doctorQueues = doctorQueues != null ? doctorQueues : new HashMap<>();
        android.util.Log.d("AdminExpandableAdapter", "Initialized with " + this.departments.size() + " departments");
    }
    public void setOnQueueItemClickListener(OnQueueItemClickListener listener) {
        this.queueItemClickListener = listener;
    }
    @Override
    public int getGroupCount() {
        return departments != null ? departments.size() : 0;
    }
    @Override
    public int getChildrenCount(int groupPosition) {
        if (departments == null || groupPosition >= departments.size()) {
            return 0;
        }
        String departmentId = departments.get(groupPosition).id;
        List<Doctor> doctors = departmentDoctors.get(departmentId);
        return doctors != null ? doctors.size() : 0;
    }
    @Override
    public Object getGroup(int groupPosition) {
        return departments.get(groupPosition);
    }
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String departmentId = departments.get(groupPosition).id;
        List<Doctor> doctors = departmentDoctors.get(departmentId);
        return doctors != null ? doctors.get(childPosition) : null;
    }
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
    @Override
    public boolean hasStableIds() {
        return false;
    }
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (departments == null || groupPosition >= departments.size() || context == null) {
            TextView errorView = new TextView(parent.getContext());
            errorView.setText("Invalid department data");
            return errorView;
        }
        Department department = departments.get(groupPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_department_item, parent, false);
        }
        TextView departmentName = convertView.findViewById(android.R.id.text1);
        TextView departmentStats = convertView.findViewById(android.R.id.text2);
        TextView activeQueues = convertView.findViewById(R.id.tvActiveQueues);
        if (departmentName != null) {
            departmentName.setText(department.name + (isExpanded ? " −" : " +"));
        }
        if (departmentStats != null) {
            departmentStats.setText(department.getStatsText());
        }
        if (activeQueues != null) {
            activeQueues.setText(String.valueOf(department.activeQueues));
        }
        return convertView;
    }
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String departmentId = departments.get(groupPosition).id;
        List<Doctor> doctors = departmentDoctors.get(departmentId);
        if (doctors == null || childPosition >= doctors.size()) {
            TextView emptyView = new TextView(context);
            emptyView.setText("No doctor data available");
            emptyView.setPadding(32, 16, 16, 16);
            emptyView.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            return emptyView;
        }
        Doctor doctor = doctors.get(childPosition);
        LinearLayout containerView;
        if (convertView instanceof LinearLayout) {
            containerView = (LinearLayout) convertView;
            containerView.removeAllViews();
        } else {
            containerView = new LinearLayout(context);
            containerView.setOrientation(LinearLayout.VERTICAL);
            containerView.setPadding(16, 8, 16, 8);
            containerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        View doctorHeaderView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        TextView doctorName = doctorHeaderView.findViewById(android.R.id.text1);
        TextView doctorInfo = doctorHeaderView.findViewById(android.R.id.text2);
        List<AdminQueueItem> queues = doctorQueues.get(doctor.id);
        int queueSize = 0;
        if (queues != null) {
            for (AdminQueueItem q : queues) {
                if (q != null && ("waiting".equals(q.status) || "in_progress".equals(q.status))) {
                    queueSize++;
                }
            }
        }
        android.util.Log.d("AdminExpandableAdapter", "Doctor " + doctor.id + " active queues=" + queueSize);
        doctorName.setText("Dr. " + doctor.name);
        doctorName.setTextSize(16f);
        doctorName.setTextColor(ContextCompat.getColor(context, R.color.textPrimary));
        doctorInfo.setText(doctor.specialty + " • " + queueSize + " patients in queue");
        doctorInfo.setTextColor(ContextCompat.getColor(context, R.color.textSecondary));
        doctorHeaderView.setBackgroundColor(ContextCompat.getColor(context, R.color.bgLight));
        doctorHeaderView.setPadding(16, 12, 16, 12);
        containerView.addView(doctorHeaderView);
        if (queues != null) {
            boolean addedAny = false;
            for (AdminQueueItem queueItem : queues) {
                if (queueItem == null) continue;
                if (!"waiting".equals(queueItem.status) && !"in_progress".equals(queueItem.status)) {
                    continue;
                }
                View queueView = createQueueItemView(queueItem, parent);
                containerView.addView(queueView);
                addedAny = true;
            }
            if (!addedAny) {
                TextView emptyQueueView = new TextView(context);
                emptyQueueView.setText("No patients in queue");
                emptyQueueView.setPadding(32, 16, 16, 16);
                emptyQueueView.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                emptyQueueView.setTextSize(14f);
                emptyQueueView.setBackgroundColor(ContextCompat.getColor(context, R.color.cardBackground));
                containerView.addView(emptyQueueView);
            }
        } else {
            TextView emptyQueueView = new TextView(context);
            emptyQueueView.setText("No patients in queue");
            emptyQueueView.setPadding(32, 16, 16, 16);
            emptyQueueView.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            emptyQueueView.setTextSize(14f);
            emptyQueueView.setBackgroundColor(ContextCompat.getColor(context, R.color.cardBackground));
            containerView.addView(emptyQueueView);
        }
        return containerView;
    }
    private View createQueueItemView(AdminQueueItem queueItem, ViewGroup parent) {
        View queueView = LayoutInflater.from(context).inflate(R.layout.admin_queue_item, parent, false);
        TextView tokenNumber = queueView.findViewById(R.id.textTokenNumber);
        TextView position = queueView.findViewById(R.id.textQueuePosition);
        TextView patientName = queueView.findViewById(R.id.textPatientName);
        TextView status = queueView.findViewById(R.id.textQueueStatus);
        View statusButton = queueView.findViewById(R.id.buttonChangeStatus);
        if (tokenNumber != null) {
            String token = queueItem.tokenNumber != null && !queueItem.tokenNumber.isEmpty() ? queueItem.tokenNumber : "T" + String.format("%03d", queueItem.position);
            tokenNumber.setText(token);
        }
        if (position != null) {
            position.setText("Position: " + queueItem.position);
        }
        if (patientName != null) {
            String name = queueItem.patientName != null && !queueItem.patientName.isEmpty() ? queueItem.patientName : "Patient #" + queueItem.patientId;
            patientName.setText(name);
        }
        if (status != null) {
            String statusText = getStatusDisplayText(queueItem.status);
            status.setText(statusText);
            int statusColor = getStatusColor(queueItem.status);
            status.setTextColor(ContextCompat.getColor(context, statusColor));
            switch (queueItem.status) {
                case "waiting":
                    status.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_light));
                    break;
                case "in_progress":
                    status.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_light));
                    break;
                case "completed":
                    status.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light));
                    break;
                case "cancelled":
                    status.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_light));
                    break;
                default:
                    status.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                    break;
            }
        }
        queueView.setOnClickListener(v -> {
            if (queueItemClickListener != null) {
                queueItemClickListener.onQueueItemClick(queueItem);
            }
        });
        if (statusButton != null) {
            statusButton.setOnClickListener(v -> {
                if (queueItemClickListener != null) {
                    queueItemClickListener.onQueueItemClick(queueItem);
                }
            });
        }
        return queueView;
    }
    private String getFormattedTimestamp(long timestamp) {
        if (timestamp <= 0) return "N/A";
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(new java.util.Date(timestamp));
    }
    private int getStatusColor(String status) {
        return android.R.color.white;
//        switch (status) {
//            case "waiting":
//                return android.R.color.holo_orange_dark;
//            case "in_progress":
//                return android.R.color.holo_blue_dark;
//            case "completed":
//                return android.R.color.holo_green_dark;
//            case "cancelled":
//                return android.R.color.holo_red_dark;
//            default:
//                return android.R.color.darker_gray;
//        }
    }
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    public String getStatusDisplayText(String status) {
        switch (status) {
            case "waiting":
                return "Waiting";
            case "in_progress":
                return "In Progress";
            case "completed":
                return "Completed";
            case "cancelled":
                return "Cancelled";
            default:
                return "Unknown";
        }
    }
}
