package com.example.qsync_2207097_android;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class QueueDetailsUserDialog extends DialogFragment {

    private QueueItem queueItem;

    public void setQueueItem(QueueItem queueItem) {
        this.queueItem = queueItem;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_queue_details_user, null);

        TextView tvDoctorName = view.findViewById(R.id.tvDoctorName);
        TextView tvDepartment = view.findViewById(R.id.tvDepartment);
        TextView tvToken = view.findViewById(R.id.tvToken);
        TextView tvPosition = view.findViewById(R.id.tvPosition);
        TextView tvWait = view.findViewById(R.id.tvWait);
        Button btnClose = view.findViewById(R.id.btnClose);

        if (queueItem != null) {
            tvDoctorName.setText(queueItem.doctor);
            tvDepartment.setText(queueItem.department);
            tvToken.setText(queueItem.token);
            tvPosition.setText(queueItem.position);
            tvWait.setText(queueItem.wait);
        }

        btnClose.setOnClickListener(v -> dismiss());

        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }
}
