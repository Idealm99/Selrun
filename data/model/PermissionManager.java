package com.example.running_app.data.model;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

import com.example.running_app.R;
import com.example.running_app.ui.fragments.RunFragment;

public class PermissionManager {

    public AlertDialog permissionDeniedDialog;

    public String[] requiredPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION
    };

    public boolean haveRequiredPermissions(Context context) {
        int fineLocationPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocationPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        int backgroundLocationPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        int recognitionPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION);

        return fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED && backgroundLocationPermission == PackageManager.PERMISSION_GRANTED && recognitionPermission == PackageManager.PERMISSION_GRANTED;
    }

    public boolean haveLocationPermissions(Context context) {
        int fineLocationPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocationPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);

        return fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasBackgroundPermission(Context context) {
        int backgroundLocationPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        return backgroundLocationPermission == PackageManager.PERMISSION_GRANTED;
    }
    private void backgroundPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{android.Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                2
        );
    }

    public void backgroundPermissionDeniedDialog(Activity activity, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(activity.getResources().getString(R.string.background_permission_denied_dialog_title));

        DialogInterface.OnClickListener listener = (dialogInterface, i) -> {
            if (i == DialogInterface.BUTTON_POSITIVE) {
                backgroundPermission(activity);
            }
        };

        builder.setPositiveButton(activity.getResources().getString(R.string.show_permission_denied_notification_positive_button), listener);
        builder.setNegativeButton(activity.getResources().getString(R.string.show_permission_denied_notification_negative_button), null);

        builder.show();
    }

    public void requestPermission(Context context, RunFragment runFragment) {
        // 이미 권한이 있으면 그냥 리턴
        if (haveRequiredPermissions(context)) {
            return;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (runFragment.requestPermissionLauncher != null) {
                    runFragment.requestPermissionLauncher.launch(requiredPermissions);
                }
            }
        }
    }

    public void showPermissionDeniedNotification(Activity activity, String message, String action, String setting_screen_type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getResources().getString(R.string.show_permission_denied_notification_title))
                .setMessage(message)
                .setPositiveButton(activity.getResources().getString(R.string.show_permission_denied_notification_positive_button), (dialog, which) -> {
                    // 설정 화면으로 이동
                    if ("app_settings".equals(setting_screen_type)) {
                        Intent intent = new Intent(action);
                        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                        intent.setData(uri);
                        activity.startActivity(intent);
                    } else if ("location_setting".equals(setting_screen_type)) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        activity.startActivity(intent);
                    }

                })
                .setNegativeButton(activity.getResources().getString(R.string.show_permission_denied_notification_negative_button), (dialog, which) -> permissionDeniedDialog.dismiss())
                .setCancelable(false);


        permissionDeniedDialog = builder.create();

        permissionDeniedDialog.show();
    }
}


