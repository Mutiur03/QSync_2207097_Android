package com.example.qsync_2207097_android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfileFragment extends Fragment {

    private ActivityResultLauncher<String> pickImageLauncher;
    private ShapeableImageView profileImage;
    private TextView profileName, profileSubtitle;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();

        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileSubtitle = view.findViewById(R.id.profile_subtitle);

        MaterialButton logoutButton = view.findViewById(R.id.button_logout);
        TextView changePassword = view.findViewById(R.id.setting_change_password);
        TextView notifications = view.findViewById(R.id.setting_notifications);
        TextView privacy = view.findViewById(R.id.setting_privacy);
        MaterialToolbar toolbar = view.findViewById(R.id.profile_toolbar);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                Glide.with(requireContext()).load(uri).centerCrop().into(profileImage);

                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(uri)
                            .build();

                    user.updateProfile(request)
                            .addOnCompleteListener(task -> {
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

        changePassword.setOnClickListener(v -> Toast.makeText(requireContext(), "Change Password tapped", Toast.LENGTH_SHORT).show());
        notifications.setOnClickListener(v -> Toast.makeText(requireContext(), "Notification Preferences tapped", Toast.LENGTH_SHORT).show());
        privacy.setOnClickListener(v -> Toast.makeText(requireContext(), "Privacy Settings tapped", Toast.LENGTH_SHORT).show());

        logoutButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Logout", (d, w) -> {
                    auth.signOut();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    if (getActivity() != null) getActivity().finish();
                    Toast.makeText(requireContext(), "Logout successful", Toast.LENGTH_SHORT).show();
                })
                .show());

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            Uri photo = currentUser.getPhotoUrl();

            profileName.setText(name != null && !name.isEmpty() ? name : "No display name");
            profileSubtitle.setText(email != null && !email.isEmpty() ? email : "No email");

            if (photo != null) {
                Glide.with(requireContext())
                        .load(photo)
                        .centerCrop()
                        .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.baseline_account_circle_24);
            }

        } else {
            profileName.setText("Guest");
            profileSubtitle.setText("Not signed in");
            profileImage.setImageResource(R.drawable.baseline_account_circle_24);
        }
    }

}