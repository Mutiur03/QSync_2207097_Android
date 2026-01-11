package com.example.qsync_2207097_android;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JoinFragment extends Fragment {
    private Button btnJoin;
    private TextView tvSummaryDepartment, tvSummaryDoctor, tvSummaryTime, tvSummaryPosition;
    private RecyclerView recyclerDoctors;
    private EditText etSymptoms;
    private Spinner spinnerDepartments;
    private ChipGroup chipGroupPriority;
    private final List<Department> departments = new ArrayList<>();
    private final List<Doctor> currentDoctors = new ArrayList<>();
    private Doctor selectedDoctor;
    private String selectedDepartmentId;
    private DoctorAdapter doctorAdapter;
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    AlertDialog loadingDialog;
    String today = getTodayDate();

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_join, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().hide(); // We have our own toolbar now
        }
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.join).setChecked(true);
        }

        spinnerDepartments = view.findViewById(R.id.spinner_departments);
        btnJoin = view.findViewById(R.id.btn_join);
        tvSummaryDepartment = view.findViewById(R.id.tv_summary_department);
        tvSummaryDoctor = view.findViewById(R.id.tv_summary_doctor);
        tvSummaryTime = view.findViewById(R.id.tv_summary_time);
        tvSummaryPosition = view.findViewById(R.id.tv_summary_position);
        recyclerDoctors = view.findViewById(R.id.recycler_doctors);
        etSymptoms = view.findViewById(R.id.et_symptoms);
        chipGroupPriority = view.findViewById(R.id.chip_group_priority);

        recyclerDoctors.setLayoutManager(new LinearLayoutManager(requireContext()));
        doctorAdapter = new DoctorAdapter((doctor, position) -> {
            selectedDoctor = doctor;
            tvSummaryDoctor.setText(doctor.name);
            tvSummaryTime.setText(doctor.getEstimatedWaitTime() + " min");
            tvSummaryPosition.setText("Queue Position: " + (doctor.currentQueueLength + 1));
            btnJoin.setEnabled(true);
        });
        recyclerDoctors.setAdapter(doctorAdapter);
        btnJoin.setEnabled(false);
        loadDepartments();
        List<String> initialDeptList = new ArrayList<>();
        initialDeptList.add("Loading departments...");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, initialDeptList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartments.setAdapter(adapter);

        spinnerDepartments.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                boolean valid = position > 0 && position <= departments.size();
                if (valid) {
                    Department selectedDept = departments.get(position - 1);
                    selectedDepartmentId = selectedDept.id;
                    tvSummaryDepartment.setText(selectedDept.name);
                    loadDoctorsForDepartment(selectedDept.id);
                } else {
                    selectedDepartmentId = null;
                    tvSummaryDepartment.setText("Department");
                    currentDoctors.clear();
                    doctorAdapter.setItems(currentDoctors);
                }
                selectedDoctor = null;
                tvSummaryDoctor.setText("Select a Doctor");
                tvSummaryTime.setText("-- min");
                tvSummaryPosition.setText("Wait Time");
                btnJoin.setEnabled(false);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedDepartmentId = null;
                btnJoin.setEnabled(false);
            }
        });

        btnJoin.setOnClickListener(v -> {
            if (selectedDoctor == null) {
                new AlertDialog.Builder(requireContext()).setTitle(R.string.select_a_doctor_title).setMessage(R.string.select_a_doctor_message).setPositiveButton(android.R.string.ok, null).show();
                return;
            }
            showConfirmation();
        });
    }

    private void loadDepartments() {
        database.child("departments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Department> departments = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Department dept = child.getValue(Department.class);
                    if (dept != null) {
                        dept.id = child.getKey();
                        departments.add(dept);
                    }
                }
                onDepartmentsLoaded(departments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onError(error.getMessage());
            }
        });
    }

    public void onDepartmentsLoaded(List<Department> loadedDepartments) {
        if (!isAdded()) return;
        departments.clear();
        departments.addAll(loadedDepartments);
        List<String> deptNames = new ArrayList<>();
        deptNames.add("Select Department");
        for (Department dept : departments) {
            deptNames.add(dept.name);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, deptNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (isAdded() && getView() != null) {
             Spinner spinner = getView().findViewById(R.id.spinner_departments);
             if (spinner != null) spinner.setAdapter(adapter);
        }
    }

    private void loadDoctorsForDepartment(String departmentId) {
        if (departmentId == null) {
            currentDoctors.clear();
            doctorAdapter.setItems(currentDoctors);
            return;
        }
        database.child("doctors").orderByChild("departmentId").equalTo(departmentId).addValueEventListener(new ValueEventListener() {
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
                onDoctorsLoaded(doctors);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onError("Failed to load doctors: " + error.getMessage());
            }
        });
    }

    public void onDoctorsLoaded(List<Doctor> loadedDoctors) {
        if (!isAdded()) return;
        currentDoctors.clear();
        currentDoctors.addAll(loadedDoctors);
        loadQueueLengthsForDoctors(loadedDoctors);
    }

    private void loadQueueLengthsForDoctors(List<Doctor> doctors) {
        if (doctors == null || doctors.isEmpty()) {
            doctorAdapter.setItems(currentDoctors);
            return;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = cal.getTimeInMillis();
        for (Doctor doctor : doctors) {
            database.child("queues").orderByChild("doctorId").equalTo(doctor.id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int waitingCount = 0;
                    for (DataSnapshot q : snapshot.getChildren()) {
                        String status = q.child("status").getValue(String.class);
                        Long ts = q.child("timestamp").getValue(Long.class);
                        if (ts == null || status == null) continue;
                        if ("waiting".equals(status) && ts >= startOfDay && ts < endOfDay) {
                            waitingCount++;
                        }
                    }
                    doctor.currentQueueLength = waitingCount;
                    currentDoctors.sort(Comparator.comparingInt(Doctor::getEstimatedWaitTime));
                    doctorAdapter.setItems(currentDoctors);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void showConfirmation() {
        String departmentName = "";
        for (Department dept : departments) {
            if (dept.id.equals(selectedDepartmentId)) {
                departmentName = dept.name;
                break;
            }
        }
        String symptoms = etSymptoms.getText().toString().trim();
        
        final String priority;
        int checkedChipId = chipGroupPriority.getCheckedChipId();
        if (checkedChipId != View.NO_ID) {
            Chip chip = requireView().findViewById(checkedChipId);
            priority = chip.getText().toString();
        } else {
            priority = "Normal";
        }

        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("Department: ").append(departmentName).append("\n");
        msgBuilder.append("Doctor: ").append(selectedDoctor == null ? "" : selectedDoctor.name).append("\n");
        msgBuilder.append("Est. Wait: ").append(selectedDoctor == null ? 0 : selectedDoctor.getEstimatedWaitTime()).append(" min");
        if (!symptoms.isEmpty()) {
            msgBuilder.append("\nSymptoms: ").append(symptoms);
        }
        msgBuilder.append("\nPriority: ").append(priority);
        String msg = msgBuilder.toString();
        new AlertDialog.Builder(requireContext()).setTitle(R.string.confirm_join_title).setMessage(msg).setPositiveButton("Join", (d, w) -> performJoin(priority)).setNegativeButton(android.R.string.cancel, null).show();
    }

    private void performJoin(String priority) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (selectedDoctor == null) {
            new AlertDialog.Builder(requireContext()).setTitle("Error").setMessage("Please select a doctor").setPositiveButton(android.R.string.ok, null).show();
            return;
        }
        String symptoms = etSymptoms.getText().toString().trim();
        if (symptoms.isEmpty()) {
            new AlertDialog.Builder(requireContext()).setTitle("Symptoms Required").setMessage("Please describe your symptoms before joining the queue").setPositiveButton(android.R.string.ok, null).show();
            return;
        }
        loadingDialog = new AlertDialog.Builder(requireContext()).setTitle("Joining Queue").setMessage("Please wait...").setCancelable(false).create();
        loadingDialog.show();
        
        DatabaseReference doctorDayRef = database.child("doctors").child(selectedDoctor.id).child("dailyQueue").child(today);
        doctorDayRef.child("count").get().addOnCompleteListener(task -> {
            int currentCount = 0;
            if (task.isSuccessful() && task.getResult().exists()) {
                Integer countValue = task.getResult().getValue(Integer.class);
                if (countValue != null) {
                    currentCount = countValue;
                }
            }
            int newPosition = currentCount + 1;
            doctorDayRef.child("count").setValue(newPosition);
            String queueId = database.child("queues").push().getKey();
            if (queueId != null) {
                Queue.QueueEntry queueEntry = new Queue.QueueEntry(queueId, currentUser.getUid(), selectedDoctor.id, newPosition, System.currentTimeMillis(), "waiting", today);
                Map<String, Object> queueData = new HashMap<>();
                queueData.put("id", queueId);
                queueData.put("patientId", queueEntry.patientId);
                queueData.put("doctorId", queueEntry.doctorId);
                queueData.put("position", queueEntry.position);
                queueData.put("timestamp", queueEntry.timestamp);
                queueData.put("status", queueEntry.status);
                queueData.put("date", today);
                queueData.put("symptoms", symptoms);
                queueData.put("priority", priority);
                long visitingTimeMillis = computeVisitingTimeMillis(selectedDoctor, newPosition, today);
                queueData.put("visitingTime", visitingTimeMillis);
                database.child("queues").child(queueId).setValue(queueData)
                        .addOnSuccessListener(aVoid -> onQueueJoined(newPosition))
                        .addOnFailureListener(e -> onError("Failed to join queue: " + e.getMessage()));
            } else {
                onError("Failed to generate queue ID");
            }
        });
    }

    private long computeVisitingTimeMillis(Doctor doctor, int position, String dateStr) {
        String start = doctor.startTime != null && !doctor.startTime.isEmpty() ? doctor.startTime : "09:00";
        int avgMin = Math.max(1, doctor.avgTimeMinutes);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date base = df.parse(dateStr + " " + start);
            if (base == null) return System.currentTimeMillis();
            Calendar cal = Calendar.getInstance();
            cal.setTime(base);
            cal.add(Calendar.MINUTE, (position - 1) * avgMin);
            return cal.getTimeInMillis();
        } catch (ParseException e) {
            return System.currentTimeMillis();
        }
    }

    private void onQueueJoined(int newPosition) {
        if (!isAdded()) return;
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Queue Joined")
                .setMessage("Your position is " + newPosition)
                .setPositiveButton(android.R.string.ok, null)
                .show()
                .setOnDismissListener(d -> requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame, new HomeFragment())
                        .commitNow());
    }

    private void onError(String error) {
        if (!isAdded()) return;
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Error")
                .setMessage("Failed to join queue: " + error)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
