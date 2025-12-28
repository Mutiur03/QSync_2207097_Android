package com.example.qsync_2207097_android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private ActivityResultLauncher<String> pickImageLauncher;
    private ShapeableImageView profileImage;
    private TextView profileName, profileSubtitle;
    private TextView valueFullName, valuePhone, valueEmail, valueDob, valueGender;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseDatabase rtdb;
    private DatabaseReference userRtRef;
    private ValueEventListener userRealtimeListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        rtdb = FirebaseDatabase.getInstance();
        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileSubtitle = view.findViewById(R.id.profile_subtitle);
        valueFullName = view.findViewById(R.id.value_fullname);
        valuePhone = view.findViewById(R.id.value_phone);
        valueEmail = view.findViewById(R.id.value_email);
        valueDob = view.findViewById(R.id.value_dob);
        valueGender = view.findViewById(R.id.value_gender);
        MaterialButton logoutButton = view.findViewById(R.id.button_logout);
        TextView changePassword = view.findViewById(R.id.setting_change_password);
        TextView notifications = view.findViewById(R.id.setting_notifications);
        TextView privacy = view.findViewById(R.id.setting_privacy);
        MaterialToolbar toolbar = view.findViewById(R.id.profile_toolbar);
        ImageButton editBtn = view.findViewById(R.id.profile_edit_button);
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                Glide.with(requireContext()).load(uri).centerCrop().into(profileImage);
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
                    user.updateProfile(request).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(), "Profile photo updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update profile photo", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) getActivity().onBackPressed();
            });
        }
        View.OnClickListener pickListener = v -> pickImageLauncher.launch("image/*");
        profileImage.setOnClickListener(pickListener);
        changePassword.setOnClickListener(v -> showChangePasswordDialog());
        notifications.setOnClickListener(v -> Toast.makeText(requireContext(), "Notification Preferences tapped", Toast.LENGTH_SHORT).show());
        privacy.setOnClickListener(v -> Toast.makeText(requireContext(), "Privacy Settings tapped", Toast.LENGTH_SHORT).show());
        logoutButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext()).setTitle("Logout").setMessage("Are you sure you want to logout?").setNegativeButton("Cancel", (d, w) -> d.dismiss()).setPositiveButton("Logout", (d, w) -> {
            auth.signOut();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
            Toast.makeText(requireContext(), "Logout successful", Toast.LENGTH_SHORT).show();
        }).show());
        if (auth.getCurrentUser() != null) {
            userRtRef = FirebaseDatabase.getInstance().getReference("users").child(auth.getCurrentUser().getUid());
            attachRealtimeListener();
        }
        loadProfile();
        if (editBtn != null) {
            editBtn.setOnClickListener(v -> showEditProfileDialog());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userRtRef != null && userRealtimeListener != null) {
            userRtRef.removeEventListener(userRealtimeListener);
        }
    }

    private void attachRealtimeListener() {
        if (userRtRef == null) return;
        if (userRealtimeListener != null) {
            userRtRef.removeEventListener(userRealtimeListener);
        }
        userRealtimeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("name").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String emailDb = snapshot.child("email").getValue(String.class);
                    String dob = snapshot.child("dob").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    if (fullName != null) valueFullName.setText(fullName);
                    if (phone != null) valuePhone.setText(phone);
                    if (emailDb != null) valueEmail.setText(emailDb);
                    if (dob != null) valueDob.setText(dob);
                    if (gender != null) valueGender.setText(gender);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Realtime load cancelled: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        userRtRef.addValueEventListener(userRealtimeListener);
    }

    private void loadProfile() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            Uri photo = currentUser.getPhotoUrl();
            profileName.setText(name != null && !name.isEmpty() ? name : "No display name");
            profileSubtitle.setText(email != null && !email.isEmpty() ? email : "No email");
            if (photo != null) {
                Glide.with(requireContext()).load(photo).centerCrop().into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.baseline_account_circle_24);
            }
            if (userRtRef == null) {
                userRtRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            }
            userRtRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean gotData = false;
                    if (snapshot.exists()) {
                        String fullName = snapshot.child("name").getValue(String.class);
                        String phone = snapshot.child("phone").getValue(String.class);
                        String emailDb = snapshot.child("email").getValue(String.class);
                        String dob = snapshot.child("dob").getValue(String.class);
                        String gender = snapshot.child("gender").getValue(String.class);
                        if (fullName != null) { valueFullName.setText(fullName); gotData = true; }
                        if (phone != null) { valuePhone.setText(phone); gotData = true; }
                        if (emailDb != null) { valueEmail.setText(emailDb); gotData = true; }
                        if (dob != null) { valueDob.setText(dob); gotData = true; }
                        if (gender != null) { valueGender.setText(gender); gotData = true; }
                    }
                    if (!gotData) {
                        DocumentReference doc = db.collection("users").document(currentUser.getUid());
                        doc.get().addOnSuccessListener(snapshotFs -> {
                            if (snapshotFs != null && snapshotFs.exists()) {
                                String fullName = snapshotFs.getString("name");
                                String phone = snapshotFs.getString("phone");
                                String fsEmail = snapshotFs.getString("email");
                                String dob = snapshotFs.getString("dob");
                                String gender = snapshotFs.getString("gender");
                                if (valueFullName.getText().length() == 0 && fullName != null) valueFullName.setText(fullName);
                                if (valuePhone.getText().length() == 0 && phone != null) valuePhone.setText(phone);
                                if (valueEmail.getText().length() == 0 && fsEmail != null) valueEmail.setText(fsEmail);
                                if (valueDob.getText().length() == 0 && dob != null) valueDob.setText(dob);
                                if (valueGender.getText().length() == 0 && gender != null) valueGender.setText(gender);
                            } else {
                                String email = currentUser.getEmail();
                                if (valueEmail.getText().length() == 0 && email != null) valueEmail.setText(email);
                            }
                        }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to load profile (FS)", Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(requireContext(), "Failed to load profile (RTDB)", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            profileName.setText("Guest");
            profileSubtitle.setText("Not signed in");
            profileImage.setImageResource(R.drawable.baseline_account_circle_24);
        }
    }

    private void showChangePasswordDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Please sign in first", Toast.LENGTH_SHORT).show();
            return;
        }
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        EditText currentPass = dialogView.findViewById(R.id.input_current_password);
        EditText newPass = dialogView.findViewById(R.id.input_new_password);
        EditText confirmPass = dialogView.findViewById(R.id.input_confirm_password);
        new MaterialAlertDialogBuilder(requireContext()).setTitle("Change Password").setView(dialogView).setNegativeButton("Cancel", (d, w) -> d.dismiss()).setPositiveButton("Update", (d, w) -> {
            String current = currentPass.getText().toString();
            String newP = newPass.getText().toString();
            String confirm = confirmPass.getText().toString();
            if (current.isEmpty() || newP.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newP.equals(confirm)) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            String email = user.getEmail();
            if (email == null) {
                Toast.makeText(requireContext(), "No email associated with account", Toast.LENGTH_SHORT).show();
                return;
            }
            auth.signInWithCredential(EmailAuthProvider.getCredential(email, current)).addOnSuccessListener(result -> user.updatePassword(newP).addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to update password", Toast.LENGTH_SHORT).show())).addOnFailureListener(e -> Toast.makeText(requireContext(), "Current password incorrect", Toast.LENGTH_SHORT).show());
        }).show();
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);
        EditText inputName = dialogView.findViewById(R.id.input_full_name);
        EditText inputPhone = dialogView.findViewById(R.id.input_phone);
        EditText inputEmail = dialogView.findViewById(R.id.input_email);
        EditText inputDob = dialogView.findViewById(R.id.input_dob);
        Spinner inputGender = dialogView.findViewById(R.id.input_gender);
        inputName.setText(valueFullName.getText().toString());
        inputPhone.setText(valuePhone.getText().toString());
        inputEmail.setText(valueEmail.getText().toString());
        inputDob.setText(valueDob.getText().toString());
        String[] genders = new String[]{"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputGender.setAdapter(adapter);
        String currentGender = valueGender.getText().toString();
        int sel = java.util.Arrays.asList(genders).indexOf(currentGender);
        if (sel >= 0) inputGender.setSelection(sel);
        new MaterialAlertDialogBuilder(requireContext()).setTitle("Edit Profile").setView(dialogView).setNegativeButton("Cancel", (d, w) -> d.dismiss()).setPositiveButton("Save", (d, w) -> {
            String name = inputName.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String dob = inputDob.getText().toString().trim();
            String gender = inputGender.getSelectedItem() != null ? inputGender.getSelectedItem().toString() : "";
            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || dob.isEmpty() || gender.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseUser user = auth.getCurrentUser();
            if (user == null) {
                Toast.makeText(requireContext(), "Please sign in first", Toast.LENGTH_SHORT).show();
                return;
            }
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("name", name);
            data.put("phone", phone);
            data.put("email", email);
            data.put("dob", dob);
            data.put("gender", gender);
            if (userRtRef == null) {
                userRtRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            }
            userRtRef.updateChildren(data).addOnFailureListener(e -> Toast.makeText(requireContext(), "RTDB save failed", Toast.LENGTH_SHORT).show());
            DocumentReference doc = db.collection("users").document(user.getUid());
            doc.set(data, com.google.firebase.firestore.SetOptions.merge()).addOnSuccessListener(aVoid -> {
                valueFullName.setText(name);
                valuePhone.setText(phone);
                valueEmail.setText(email);
                valueDob.setText(dob);
                valueGender.setText(gender);
                UserProfileChangeRequest req = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                user.updateProfile(req);
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(err -> Toast.makeText(requireContext(), "Failed to save (FS)", Toast.LENGTH_SHORT).show());
        }).show();
    }

}