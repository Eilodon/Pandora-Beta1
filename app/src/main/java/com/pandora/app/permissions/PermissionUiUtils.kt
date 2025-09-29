package com.pandora.app.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

// FIXED: show rationale only for declared permissions
object PermissionUiUtils {
    fun isDeclaredInManifest(context: Context, permission: String): Boolean {
        return try {
            val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            val declared = info.requestedPermissions?.toSet() ?: emptySet()
            declared.contains(permission)
        } catch (e: Exception) {
            false
        }
    }

    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return isDeclaredInManifest(activity, permission) &&
               ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
}
