package com.example.qsync_2207097_android;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static DatabaseManager instance;
    private final DatabaseReference database;

    private DatabaseManager() {
        database = FirebaseDatabase.getInstance().getReference();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public interface DepartmentLoadCallback {
        void onDepartmentsLoaded(List<Department> departments);
        void onError(String error);
    }

    public interface DoctorLoadCallback {
        void onDoctorsLoaded(List<Doctor> doctors);
        void onError(String error);
    }

    public interface QueueJoinCallback {
        void onQueueJoined(String queueId, int position);
        void onError(String error);
    }

    public interface QueuePositionCallback {
        void onPositionUpdated(int currentPosition);
        void onError(String error);
    }

    public interface QueueCancellationCallback {
        void onQueueCancelled();
        void onError(String error);
    }

    public void loadDepartments(DepartmentLoadCallback callback) {
        database.child("departments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Department> departments = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Department dept = child.getValue(Department.class);
                    if (dept != null && dept.isActive) {
                        dept.id = child.getKey();
                        departments.add(dept);
                    }
                }
                callback.onDepartmentsLoaded(departments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void loadDoctorsByDepartment(String departmentId, DoctorLoadCallback callback) {
        if (departmentId == null || departmentId.isEmpty()) {
            callback.onDoctorsLoaded(new ArrayList<>());
            return;
        }

        database.child("doctors").orderByChild("departmentId").equalTo(departmentId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Doctor> doctors = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Doctor doctor = child.getValue(Doctor.class);
                            if (doctor != null && doctor.isAvailable) {
                                doctor.id = child.getKey();
                                doctors.add(doctor);
                            }
                        }
                        callback.onDoctorsLoaded(doctors);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void joinQueue(String doctorId, String patientId, QueueJoinCallback callback) {
        if (doctorId == null || patientId == null) {
            callback.onError("Invalid doctor or patient ID");
            return;
        }

        database.child("doctors").child(doctorId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Doctor doctor = task.getResult().getValue(Doctor.class);
                if (doctor != null) {
                    int currentQueueLength = doctor.currentQueueLength;
                    int newPosition = currentQueueLength + 1;

                    database.child("doctors").child(doctorId).child("currentQueueLength")
                            .setValue(currentQueueLength + 1);

                    String queueId = database.child("queues").push().getKey();
                    if (queueId != null) {
                        QueueEntry queueEntry = new QueueEntry(
                            queueId,
                            patientId,
                            doctorId,
                            newPosition,
                            System.currentTimeMillis(),
                            "waiting"
                        );

                        database.child("queues").child(queueId).setValue(queueEntry)
                                .addOnSuccessListener(aVoid -> callback.onQueueJoined(queueId, newPosition))
                                .addOnFailureListener(e -> callback.onError("Failed to join queue: " + e.getMessage()));
                    } else {
                        callback.onError("Failed to generate queue ID");
                    }
                } else {
                    callback.onError("Doctor not found");
                }
            } else {
                callback.onError("Failed to get doctor information");
            }
        });
    }

    public void getCurrentQueuePosition(String doctorId, String patientId, QueuePositionCallback callback) {
        database.child("queues").orderByChild("doctorId").equalTo(doctorId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int currentPosition = 1;
                        boolean found = false;

                        for (DataSnapshot child : snapshot.getChildren()) {
                            QueueEntry entry = child.getValue(QueueEntry.class);
                            if (entry != null && "waiting".equals(entry.status)) {
                                if (entry.patientId.equals(patientId)) {
                                    found = true;
                                    break;
                                }
                                currentPosition++;
                            }
                        }

                        if (found) {
                            callback.onPositionUpdated(currentPosition);
                        } else {
                            callback.onError("Queue entry not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void cancelQueue(String queueId, QueueCancellationCallback callback) {
        if (queueId == null || queueId.isEmpty()) {
            callback.onError("Invalid queue ID");
            return;
        }

        database.child("queues").child(queueId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                QueueEntry entry = task.getResult().getValue(QueueEntry.class);
                if (entry != null) {
                    database.child("queues").child(queueId).child("status").setValue("cancelled")
                            .addOnSuccessListener(aVoid -> {
                                database.child("doctors").child(entry.doctorId).child("currentQueueLength")
                                        .get().addOnCompleteListener(lengthTask -> {
                                            if (lengthTask.isSuccessful()) {
                                                Integer currentLength = lengthTask.getResult().getValue(Integer.class);
                                                if (currentLength != null && currentLength > 0) {
                                                    database.child("doctors").child(entry.doctorId)
                                                            .child("currentQueueLength")
                                                            .setValue(currentLength - 1);
                                                }
                                            }
                                        });
                                callback.onQueueCancelled();
                            })
                            .addOnFailureListener(e -> callback.onError("Failed to cancel queue: " + e.getMessage()));
                } else {
                    callback.onError("Queue entry not found");
                }
            } else {
                callback.onError("Failed to get queue information");
            }
        });
    }

    public static class QueueEntry {
        public String queueId;
        public String patientId;
        public String doctorId;
        public int position;
        public long timestamp;
        public String status;

        public QueueEntry() {}

        public QueueEntry(String queueId, String patientId, String doctorId, int position, long timestamp, String status) {
            this.queueId = queueId;
            this.patientId = patientId;
            this.doctorId = doctorId;
            this.position = position;
            this.timestamp = timestamp;
            this.status = status;
        }
    }
}
