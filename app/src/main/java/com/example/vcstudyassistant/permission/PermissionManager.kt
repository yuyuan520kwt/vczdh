package com.example.vcstudyassistant.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager {
    companion object {
        const val REQUEST_PERMISSIONS = 1001
        const val REQUEST_ACCESSIBILITY = 1002
        const val REQUEST_OVERDRAW = 1003

        // 检查所有必要权限
        fun hasAllPermissions(context: Context): Boolean {
            return hasSelfPermissions(context) && hasAccessibilityPermission(context) && hasOverdrawPermission(context)
        }

        // 检查应用权限（麦克风、存储、相机等）
        fun hasSelfPermissions(context: Context): Boolean {
            val requiredPermissions = mutableListOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.CAMERA
            )

            // 根据Android版本添加不同的存储权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requiredPermissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
                requiredPermissions.add(android.Manifest.permission.READ_MEDIA_VIDEO)
                requiredPermissions.add(android.Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                requiredPermissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                requiredPermissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            for (permission in requiredPermissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
            return true
        }

        // 请求应用权限
        fun requestSelfPermissions(activity: Activity) {
            val requiredPermissions = mutableListOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.CAMERA
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requiredPermissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
                requiredPermissions.add(android.Manifest.permission.READ_MEDIA_VIDEO)
                requiredPermissions.add(android.Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                requiredPermissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                requiredPermissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            ActivityCompat.requestPermissions(
                activity,
                requiredPermissions.toTypedArray(),
                REQUEST_PERMISSIONS
            )
        }

        // 检查无障碍权限
        fun hasAccessibilityPermission(context: Context): Boolean {
            val accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED, 0
            )
            val ourAccessibilityService = "${context.packageName}/com.example.vcstudyassistant.accessibility.MyAccessibilityService"
            return accessibilityEnabled == 1 && Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ).contains(ourAccessibilityService)
        }

        // 请求无障碍权限
        fun requestAccessibilityPermission(activity: Activity) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            activity.startActivityForResult(intent, REQUEST_ACCESSIBILITY)
        }

        // 检查悬浮窗权限
        fun hasOverdrawPermission(context: Context): Boolean {
            return Settings.canDrawOverlays(context)
        }

        // 请求悬浮窗权限
        fun requestOverdrawPermission(activity: Activity) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${activity.packageName}"))
            activity.startActivityForResult(intent, REQUEST_OVERDRAW)
        }

        // 检查权限请求结果
        fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray): Boolean {
            if (requestCode == REQUEST_PERMISSIONS) {
                return grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            }
            return false
        }

        // 打开应用设置界面
        fun openAppSettings(activity: Activity) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
        }
    }
}