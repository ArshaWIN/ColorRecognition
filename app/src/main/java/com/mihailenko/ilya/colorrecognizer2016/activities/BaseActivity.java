package com.mihailenko.ilya.colorrecognizer2016.activities;


import android.content.pm.PackageManager;
import android.support.annotation.CallSuper;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;

import com.mihailenko.ilya.colorrecognizer2016.utils.interfaces.PermissionCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;



/**
 * Implements initialization of Calligraphy and permissions checks.
 */
public class BaseActivity extends AppCompatActivity {
    private static Random random = new Random();
    private SparseArray<WeakReference<PermissionCallback>> permissionCallbacks = new SparseArray<>(1);

    @Override
    @CallSuper
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length == 0) return;

        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        WeakReference<PermissionCallback> callbackReference = permissionCallbacks.get(requestCode);
        if (callbackReference != null) {
            PermissionCallback permissionCallback = callbackReference.get();
            if (permissionCallback != null) {
                permissionCallback.onPermissionGranted();
                permissionCallbacks.remove(requestCode);
            }
        }
    }

    final public boolean checkPermissions(@NonNull String permissions[]) {
        return checkPermissions(permissions, null);
    }

    /**
     * This method is deprecated because request code is randomly generated.
     * This can lead to multiple entities of callbacks in collection.
     */
    @Deprecated
    final public boolean checkPermissions(@NonNull String permissions[], @Nullable PermissionCallback permissionCallback) {
        return checkPermissions(permissions, permissionCallback, getRequestCode());
    }

    final public boolean checkPermissions(@NonNull String permissions[],
                                          @Nullable PermissionCallback permissionCallback,
                                          @IntRange(from = 1, to = 255) int requestCode) {
        if (requestCode < 1 || requestCode > 255) {
            throw new IllegalArgumentException("Permission request code must be in range (1..255)");
        }

        ArrayList<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }

        boolean hasDeniedPermissions = deniedPermissions.size() > 0;

        if (hasDeniedPermissions && permissionCallback != null) {
            String permissionsToRequest[] = new String[deniedPermissions.size()];
            permissionCallbacks.put(requestCode, new WeakReference<>(permissionCallback));
            ActivityCompat.requestPermissions(this, deniedPermissions.toArray(permissionsToRequest), requestCode);
        }
        return !hasDeniedPermissions;
    }

    private int getRequestCode() {
        int reqCode = random.nextInt(254) + 1;

        return permissionCallbacks.get(reqCode) == null ? reqCode : getRequestCode();
    }
}
