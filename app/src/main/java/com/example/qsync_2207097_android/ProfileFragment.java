package com.example.qsync_2207097_android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

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

//        ImageButton editButton = view.findViewById(R.id.profile_edit_button);
        MaterialButton logoutButton = view.findViewById(R.id.button_logout);
        TextView changePassword = view.findViewById(R.id.setting_change_password);
        TextView notifications = view.findViewById(R.id.setting_notifications);
        TextView privacy = view.findViewById(R.id.setting_privacy);
        MaterialToolbar toolbar = view.findViewById(R.id.profile_toolbar);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) getActivity().onBackPressed();
            });
        }

        View.OnClickListener pickListener = v -> pickImageLauncher.launch("image/*");
        profileImage.setOnClickListener(pickListener);
//        editButton.setOnClickListener(pickListener);

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

        profileName.setText("Mutiur Rahman");
        profileSubtitle.setText("mutiur5bb@gmmail.com");
    }

}