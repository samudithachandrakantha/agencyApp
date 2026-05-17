package com.hfad.agencyapp.utils;

import android.Manifest;
import android.content.Context;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class PermissionUtils {

    public static String[] getBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+: fine-grained Bluetooth permissions
            return new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
            };
        } else {
            // Android < 12
            return new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
            };
        }
    }

    public static String[] getNetworkPermissions() {
        return new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
        };
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasAllPermissions(Context context, String[] permissions) {
        for (String perm : permissions) {
            if (!hasPermission(context, perm)) return false;
        }
        return true;
    }
}

