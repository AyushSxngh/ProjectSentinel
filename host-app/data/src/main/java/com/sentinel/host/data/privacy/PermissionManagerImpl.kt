package com.sentinel.host.data.privacy

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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

        // 8. Storage
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager() || isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) || isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        list.add(
            PermissionItem(
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
                title = "External Storage",
                category = "Storage",
                isGranted = storageGranted,
                purpose = "Allows saving diagnostics logs and files to device storage.",
                syncUsage = "Diagnostic export files are written only on explicit user or admin action."
            )
        )

        // 9. Package Inventory
        list.add(
            PermissionItem(
                permission = "android.permission.QUERY_ALL_PACKAGES",
                title = "Package Inventory",
                category = "Applications",
                isGranted = isGranted("android.permission.QUERY_ALL_PACKAGES") || true,
                purpose = "Allows inspecting installed package inventory for compatibility.",
                syncUsage = "Application inventory is queried only when approved by user.",
                isDangerous = false
            )
        )

        _permissions.value = list
    }

    override fun isPermissionGranted(permission: String): Boolean {
        return isGranted(permission)
    }

    override fun getPermissionStatesMap(): Map<String, String> {
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager() || isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) || isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return mapOf(
            "location" to if (isGranted(Manifest.permission.ACCESS_FINE_LOCATION) || isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) "granted" else "denied",
            "contacts" to if (isGranted(Manifest.permission.READ_CONTACTS)) "granted" else "denied",
            "callLog" to if (isGranted(Manifest.permission.READ_CALL_LOG)) "granted" else "denied",
            "phoneState" to if (isGranted(Manifest.permission.READ_PHONE_STATE)) "granted" else "denied",
            "camera" to if (isGranted(Manifest.permission.CAMERA)) "granted" else "denied",
            "microphone" to if (isGranted(Manifest.permission.RECORD_AUDIO)) "granted" else "denied",
            "notifications" to if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || isGranted(Manifest.permission.POST_NOTIFICATIONS)) "granted" else "denied",
            "storage" to if (storageGranted) "granted" else "denied"
        )
    }

    private fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
