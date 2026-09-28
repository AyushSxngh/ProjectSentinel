package com.sentinel.host.data.privacy

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sentinel.host.domain.privacy.PermissionItem
import com.sentinel.host.domain.privacy.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PermissionManagerImpl(
    private val context: Context
) : PermissionManager {

    private val _permissions = MutableStateFlow<List<PermissionItem>>(emptyList())
    override val permissions: StateFlow<List<PermissionItem>> = _permissions.asStateFlow()

    init {
        refreshPermissions()
    }

    override fun refreshPermissions() {
        val list = mutableListOf<PermissionItem>()

        // 1. Location
        val locationGranted = isGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
                isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        list.add(
            PermissionItem(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                title = "Location",
                category = "Location",
                isGranted = locationGranted,
                purpose = "Required to share coordinates with Sentinel server.",
                syncUsage = "Synced to admin dashboard only when 'Sync with Admin' is ON."
            )
        )

        // 2. Contacts
        val contactsGranted = isGranted(Manifest.permission.READ_CONTACTS)
        list.add(
            PermissionItem(
                permission = Manifest.permission.READ_CONTACTS,
                title = "Contacts Summary",
                category = "Contacts",
                isGranted = contactsGranted,
                purpose = "Allows checking total contact counts for device diagnostics.",
                syncUsage = "Only total count is shared. Zero contact names, numbers, or records are ever uploaded."
            )
        )

        // 3. Call Log
        val callLogGranted = isGranted(Manifest.permission.READ_CALL_LOG)
        list.add(
            PermissionItem(
                permission = Manifest.permission.READ_CALL_LOG,
                title = "Call Log Summary",
                category = "Call Log",
                isGranted = callLogGranted,
                purpose = "Allows reading overall call activity metrics (e.g., total call counts).",
                syncUsage = "Only aggregate counts and last call timestamp are shared. Zero phone numbers or call history details are uploaded."
            )
        )

        // 4. Phone State
        val phoneStateGranted = isGranted(Manifest.permission.READ_PHONE_STATE)
        list.add(
            PermissionItem(
                permission = Manifest.permission.READ_PHONE_STATE,
                title = "Phone State",
                category = "Telephony",
                isGranted = phoneStateGranted,
                purpose = "Allows reading cellular carrier and network operator info.",
                syncUsage = "Carrier network name is shared in system diagnostics when enabled."
            )
        )

        // 5. Camera
        val cameraGranted = isGranted(Manifest.permission.CAMERA)
        list.add(
            PermissionItem(
                permission = Manifest.permission.CAMERA,
                title = "Camera",
                category = "Media",
                isGranted = cameraGranted,
                purpose = "Allows manual photo capture on explicit operator command.",
                syncUsage = "Never streams continuously. Used only when explicitly invoked by an authenticated admin."
            )
        )

        // 6. Microphone
        val micGranted = isGranted(Manifest.permission.RECORD_AUDIO)
        list.add(
            PermissionItem(
                permission = Manifest.permission.RECORD_AUDIO,
                title = "Microphone",
                category = "Media",
                isGranted = micGranted,
                purpose = "Allows audio monitoring sessions when requested by admin.",
                syncUsage = "Operates only during active authenticated audio sessions."
            )
        )

        // 7. Notifications (API 33+)
        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isGranted(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
        list.add(
            PermissionItem(
                permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.POST_NOTIFICATIONS
                } else "android.permission.POST_NOTIFICATIONS",
                title = "Notifications",
                category = "System",
                isGranted = notifGranted,
                purpose = "Displays persistent foreground service notification ensuring transparency.",
                syncUsage = "Notification status is reported in permission states.",
                isDangerous = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            )
        )

        // 8. Background Location
        val bgLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            locationGranted
        }
        list.add(
            PermissionItem(
                permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                title = "Background Location",
                category = "Location",
                isGranted = bgLocationGranted,
                purpose = "Allows continuous location tracking when app is minimized or device is locked.",
                syncUsage = "Enables location telemetry syncing while in background.",
                isDangerous = true
            )
        )

        // 9. Storage
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager() || isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) || isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        list.add(
            PermissionItem(
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
                title = "External Storage / All Files",
                category = "Storage",
                isGranted = storageGranted,
                purpose = "Allows saving diagnostics logs and files to device storage.",
                syncUsage = "Diagnostic export files are written only on explicit user or admin action."
            )
        )

        // 10. Battery Optimization Exemption
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val batteryOptIgnored = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
        list.add(
            PermissionItem(
                permission = Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                title = "Battery Optimization Exemption",
                category = "System",
                isGranted = batteryOptIgnored,
                purpose = "Prevents Android OS from killing background sync service during idle Doze mode.",
                syncUsage = "Enables reliable real-time telemetry streaming and heartbeat maintenance.",
                isDangerous = false
            )
        )

        // 11. Notification Listener Service
        val notifListenerGranted = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
        list.add(
            PermissionItem(
                permission = Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
                title = "Notification Listener Access",
                category = "Special System Access",
                isGranted = notifListenerGranted,
                purpose = "Special system access to capture and log notifications for device diagnostics.",
                syncUsage = "Enables remote notification log inspection from the Admin command console.",
                isDangerous = true
            )
        )

        // 12. Accessibility Helper Service
        val accessibilityGranted = isAccessibilityServiceEnabled()
        list.add(
            PermissionItem(
                permission = Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
                title = "Accessibility Helper Service",
                category = "Special System Access",
                isGranted = accessibilityGranted,
                purpose = "Special accessibility helper for observing user interactions and window events.",
                syncUsage = "Logs UI interaction events for remote device support.",
                isDangerous = true
            )
        )

        // 13. Package Inventory
        list.add(
            PermissionItem(
                permission = "android.permission.QUERY_ALL_PACKAGES",
                title = "Package Inventory Query",
                category = "Applications",
                isGranted = true,
                purpose = "Allows inspecting installed package inventory for compatibility.",
                syncUsage = "Package inventory is queried only for diagnostic verification.",
                isDangerous = false
            )
        )

        _permissions.value = list
    }

    override fun isPermissionGranted(permission: String): Boolean {
        return when (permission) {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    android.os.Environment.isExternalStorageManager() || isGranted(permission)
                } else {
                    isGranted(permission)
                }
            }
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
            }
            Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE -> {
                NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
            }
            Manifest.permission.BIND_ACCESSIBILITY_SERVICE -> {
                isAccessibilityServiceEnabled()
            }
            Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    isGranted(permission)
                } else {
                    isGranted(Manifest.permission.ACCESS_FINE_LOCATION) || isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            }
            else -> isGranted(permission)
        }
    }

    override fun getPermissionStatesMap(): Map<String, String> {
        val locationGranted = isGranted(Manifest.permission.ACCESS_FINE_LOCATION) || isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        val bgLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            locationGranted
        }
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager() || isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) || isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isGranted(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val batteryOptIgnored = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
        val notifListenerGranted = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
        val accessibilityGranted = isAccessibilityServiceEnabled()

        return mapOf(
            "location" to if (locationGranted) "granted" else "denied",
            "backgroundLocation" to if (bgLocationGranted) "granted" else "denied",
            "camera" to if (isGranted(Manifest.permission.CAMERA)) "granted" else "denied",
            "microphone" to if (isGranted(Manifest.permission.RECORD_AUDIO)) "granted" else "denied",
            "storage" to if (storageGranted) "granted" else "denied",
            "contacts" to if (isGranted(Manifest.permission.READ_CONTACTS)) "granted" else "denied",
            "callLog" to if (isGranted(Manifest.permission.READ_CALL_LOG)) "granted" else "denied",
            "phoneState" to if (isGranted(Manifest.permission.READ_PHONE_STATE)) "granted" else "denied",
            "notifications" to if (notifGranted) "granted" else "denied",
            "batteryOptimization" to if (batteryOptIgnored) "granted" else "denied",
            "notificationListener" to if (notifListenerGranted) "granted" else "denied",
            "accessibility" to if (accessibilityGranted) "granted" else "denied",
            "packageInventory" to "granted"
        )
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            val expectedServiceName = "${context.packageName}/com.sentinel.host.service.SentinelAccessibilityService"
            val enabledServices = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val colonSplitter = android.text.TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(expectedServiceName, ignoreCase = true) ||
                    componentName.contains(context.packageName, ignoreCase = true)
                ) {
                    return true
                }
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
