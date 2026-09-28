package com.sentinel.admin.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Battery4Bar
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkChatRead
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sentinel.admin.domain.model.DeviceSync
import com.sentinel.shared.model.CallLogRecord
import com.sentinel.shared.model.PermissionStatusRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Detailed descriptor for audited Android permissions and their privacy boundaries.
 */
private data class PermissionDefinition(
    val key: String,
    val title: String,
    val technicalName: String,
    val category: String,
    val purpose: String,
    val syncUsage: String,
    val icon: ImageVector,
    val isDangerous: Boolean = true
)

private val PERMISSION_CATALOG = listOf(
    PermissionDefinition(
        key = "location",
        title = "Location (Fine & Coarse)",
        technicalName = "android.permission.ACCESS_FINE_LOCATION",
        category = "Sensors & Motion",
        purpose = "Allows device positioning via GPS, cellular networks, and Wi-Fi networks.",
        syncUsage = "Synced to admin dashboard only when 'Sync Location' is enabled.",
        icon = Icons.Default.LocationOn
    ),
    PermissionDefinition(
        key = "backgroundLocation",
        title = "Background Location",
        technicalName = "android.permission.ACCESS_BACKGROUND_LOCATION",
        category = "Sensors & Motion",
        purpose = "Allows continuous location tracking while app is minimized or screen is locked.",
        syncUsage = "Enables location telemetry syncing while operating in the background.",
        icon = Icons.Default.MyLocation
    ),
    PermissionDefinition(
        key = "camera",
        title = "Camera Snapshot",
        technicalName = "android.permission.CAMERA",
        category = "Sensors & Media",
        purpose = "Enables manual photo capture upon explicit remote admin operator instruction.",
        syncUsage = "Operates only during active authenticated snapshot sessions. Zero continuous video.",
        icon = Icons.Default.CameraAlt
    ),
    PermissionDefinition(
        key = "microphone",
        title = "Microphone (Live Audio)",
        technicalName = "android.permission.RECORD_AUDIO",
        category = "Sensors & Media",
        purpose = "Allows encrypted Opus live audio monitoring and local recording.",
        syncUsage = "Streams live audio only when explicitly initiated by an authenticated admin.",
        icon = Icons.Default.Mic
    ),
    PermissionDefinition(
        key = "storage",
        title = "External Storage / All Files",
        technicalName = "android.permission.MANAGE_EXTERNAL_STORAGE",
        category = "Storage & Files",
        purpose = "Permits device storage access to save and retrieve diagnostic logs.",
        syncUsage = "Enables remote file manager browsing and log downloads upon admin command.",
        icon = Icons.Default.Folder
    ),
    PermissionDefinition(
        key = "contacts",
        title = "Contacts Directory Summary",
        technicalName = "android.permission.READ_CONTACTS",
        category = "Telemetry & Data",
        purpose = "Checks total address book count for device diagnostic profiling.",
        syncUsage = "Total count only (e.g. 142). Zero names, numbers, or records are uploaded.",
        icon = Icons.Default.People
    ),
    PermissionDefinition(
        key = "callLog",
        title = "Call History Metrics",
        technicalName = "android.permission.READ_CALL_LOG",
        category = "Telemetry & Data",
        purpose = "Checks aggregate call frequency metrics and last call timestamp.",
        syncUsage = "Aggregate count only. Zero phone numbers or call details are uploaded.",
        icon = Icons.Default.Call
    ),
    PermissionDefinition(
        key = "phoneState",
        title = "Cellular & Phone State",
        technicalName = "android.permission.READ_PHONE_STATE",
        category = "Telemetry & Data",
        purpose = "Inspects cellular SIM state, network carrier, and operator information.",
        syncUsage = "Cellular carrier name is shared for network connectivity diagnostics.",
        icon = Icons.Default.PhoneAndroid
    ),
    PermissionDefinition(
        key = "notifications",
        title = "System Notifications",
        technicalName = "android.permission.POST_NOTIFICATIONS",
        category = "System & Security",
        purpose = "Displays persistent Android foreground service notification for transparency.",
        syncUsage = "Notification status is reported to ensure service visibility.",
        icon = Icons.Default.Notifications
    ),
    PermissionDefinition(
        key = "batteryOptimization",
        title = "Battery Optimization Exemption",
        technicalName = "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
        category = "System & Security",
        purpose = "Prevents Android OS from killing background sync service during Doze mode.",
        syncUsage = "Ensures background heartbeat and real-time command reception.",
        icon = Icons.Default.BatteryChargingFull,
        isDangerous = false
    ),
    PermissionDefinition(
        key = "notificationListener",
        title = "Notification Listener Access",
        technicalName = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
        category = "Special System Access",
        purpose = "Special system access to capture and buffer system notifications for audit logs.",
        syncUsage = "Enables remote notification log inspection from the Admin command console.",
        icon = Icons.Default.MarkChatRead
    ),
    PermissionDefinition(
        key = "accessibility",
        title = "Accessibility Helper Service",
        technicalName = "android.permission.BIND_ACCESSIBILITY_SERVICE",
        category = "Special System Access",
        purpose = "Special accessibility helper for observing user interactions and window events.",
        syncUsage = "Logs UI interaction events for remote device support.",
        icon = Icons.Default.AccessibilityNew
    ),
    PermissionDefinition(
        key = "packageInventory",
        title = "Package Inventory Query",
        technicalName = "android.permission.QUERY_ALL_PACKAGES",
        category = "Applications",
        purpose = "Allows inspecting installed package inventory for compatibility.",
        syncUsage = "Package inventory is queried only for diagnostic verification.",
        icon = Icons.Default.Apps,
        isDangerous = false
    ),
    PermissionDefinition(
        key = "internet",
        title = "Internet Access",
        technicalName = "android.permission.INTERNET",
        category = "Network & Web",
        purpose = "Allows communication with ProjectSentinel server backend.",
        syncUsage = "Enables encrypted WebSocket and HTTPS transport.",
        icon = Icons.Default.Wifi,
        isDangerous = false
    ),
    PermissionDefinition(
        key = "networkState",
        title = "Access Network State",
        technicalName = "android.permission.ACCESS_NETWORK_STATE",
        category = "Network & Web",
        purpose = "Detects network connectivity state changes (Wi-Fi vs Cellular).",
        syncUsage = "Monitors real-time connection status.",
        icon = Icons.Default.Wifi,
        isDangerous = false
    ),
    PermissionDefinition(
        key = "wifiState",
        title = "Access Wi-Fi State",
        technicalName = "android.permission.ACCESS_WIFI_STATE",
        category = "Network & Web",
        purpose = "Allows inspecting connected Wi-Fi state and link speed.",
        syncUsage = "Included in network telemetry when active.",
        icon = Icons.Default.Wifi,
        isDangerous = false
    ),
    PermissionDefinition(
        key = "wakeLock",
        title = "Wake Lock",
        technicalName = "android.permission.WAKE_LOCK",
        category = "System & Security",
        purpose = "Prevents CPU from sleeping during active synchronization tasks.",
        syncUsage = "Keeps telemetry transport active during background sync cycles.",
        icon = Icons.Default.Lock,
        isDangerous = false
    ),
    PermissionDefinition(
        key = "adId",
        title = "Advertising ID Permission",
        technicalName = "com.google.android.gms.permission.AD_ID",
        category = "Telemetry & Data",
        purpose = "Allows querying device advertising identifier if enabled.",
        syncUsage = "Synchronized solely for optional diagnostic profiling. Never used as device identity.",
        icon = Icons.Default.Info,
        isDangerous = false
    )
)

/**
 * Device Sync & Audited Permissions Dashboard.
 *
 * Displays all permissions data from the host device in detail ("in manner"),
 * along with all synced telemetry metrics (battery, network, counts, hardware metadata).
 */
@Composable
fun DeviceSyncCard(
    sync: DeviceSync?,
    onRequestSync: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 1. Header with Title & Consent State Badge
            SyncHeader(sync = sync, onRequestSync = onRequestSync)

            Spacer(modifier = Modifier.height(12.dp))

            if (sync == null) {
                // Awaiting initial sync
                AwaitingSyncBanner(onRequestSync = onRequestSync)
            } else {
                // Last Sync Timestamp & Device ID Info Row
                SyncMetaBanner(sync = sync)

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Full Audited Permissions Center
                PermissionsAuditSection(sync = sync)

                // 3. User Privacy Notice if Sync with Admin is paused
                if (!sync.syncEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SyncPausedPrivacyBanner()
                }

                // 4. Complete Telemetry Details (Every single data point)
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                TelemetryDetailsSection(sync = sync)
            }
        }
    }
}

// ============================================================
// Sub-components
// ============================================================

@Composable
private fun SyncHeader(
    sync: DeviceSync?,
    onRequestSync: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Permissions & Live Telemetry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Audited host device consent & status",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            val syncEnabled = sync?.syncEnabled == true
            val isNull = sync == null

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when {
                    isNull -> MaterialTheme.colorScheme.surfaceVariant
                    syncEnabled -> Color(0xFFE8F5E9)
                    else -> Color(0xFFFFF3E0)
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isNull -> Color.Gray
                                    syncEnabled -> Color(0xFF2E7D32)
                                    else -> Color(0xFFEF6C00)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when {
                            isNull -> "NO DATA"
                            syncEnabled -> "SYNC ACTIVE"
                            else -> "SYNC PAUSED"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isNull -> MaterialTheme.colorScheme.onSurfaceVariant
                            syncEnabled -> Color(0xFF2E7D32)
                            else -> Color(0xFFE65100)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onRequestSync,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Request Sync",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SyncMetaBanner(sync: DeviceSync) {
    val formattedTime = if (sync.timestamp > 0) {
        val date = Date(sync.timestamp * 1000)
        SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.getDefault()).format(date)
    } else {
        "Unknown"
    }

    val elapsedSeconds = if (sync.timestamp > 0) {
        (System.currentTimeMillis() / 1000) - sync.timestamp
    } else null

    val relativeText = when {
        elapsedSeconds == null -> ""
        elapsedSeconds < 5 -> " (just now)"
        elapsedSeconds < 60 -> " (${elapsedSeconds}s ago)"
        elapsedSeconds < 3600 -> " (${elapsedSeconds / 60}m ago)"
        else -> " (${elapsedSeconds / 3600}h ago)"
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Last Synced: $formattedTime$relativeText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "ID: ${sync.deviceId}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PermissionsAuditSection(
    sync: DeviceSync
) {
    val permissionStates = sync.permissionStates
    val permissionRecords = sync.permissions

    // Merge catalog with any unknown dynamic keys from host
    val allDefinitions = remember(permissionStates, permissionRecords) {
        val catalogMap = PERMISSION_CATALOG.associateBy { it.key }.toMutableMap()
        permissionStates.forEach { (key, _) ->
            if (!catalogMap.containsKey(key)) {
                catalogMap[key] = PermissionDefinition(
                    key = key,
                    title = key.replaceFirstChar { it.uppercase() },
                    technicalName = "android.permission.$key",
                    category = "Additional",
                    purpose = "Audited host system permission.",
                    syncUsage = "Tracked in device sync payload.",
                    icon = Icons.Default.Security
                )
            }
        }
        permissionRecords.forEach { record ->
            val existing = catalogMap.values.find {
                it.technicalName.equals(record.permission, ignoreCase = true) ||
                it.title.equals(record.name, ignoreCase = true)
            }
            if (existing == null) {
                catalogMap[record.permission] = PermissionDefinition(
                    key = record.permission,
                    title = record.name,
                    technicalName = record.permission,
                    category = "Audited",
                    purpose = "Audited host system permission state.",
                    syncUsage = "Reported directly by host permission manager.",
                    icon = Icons.Default.Security
                )
            }
        }
        catalogMap.values.toList()
    }

    var selectedCategory by remember { mutableStateOf("All") }

    val categories = remember(allDefinitions) {
        listOf("All") + allDefinitions.map { it.category }.distinct()
    }

    val filteredList = remember(selectedCategory, allDefinitions) {
        if (selectedCategory == "All") allDefinitions
        else allDefinitions.filter { it.category == selectedCategory }
    }

    val totalTracked = allDefinitions.size
    val grantedCount = allDefinitions.count { def ->
        val record = permissionRecords.find {
            it.permission.equals(def.technicalName, ignoreCase = true) ||
            it.name.equals(def.title, ignoreCase = true)
        }
        record?.state?.equals("Granted", ignoreCase = true)
            ?: (permissionStates[def.key]?.lowercase() == "granted")
    }
    val deniedCount = totalTracked - grantedCount
    val compliancePct = if (totalTracked > 0) (grantedCount * 100) / totalTracked else 0

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Audited Android Permissions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "$grantedCount of $totalTracked Granted",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Summary Metric Badges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricPill(
                label = "Total Audited",
                value = "$totalTracked",
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            MetricPill(
                label = "Granted",
                value = "$grantedCount",
                containerColor = Color(0xFFE8F5E9),
                contentColor = Color(0xFF2E7D32),
                icon = Icons.Default.Check,
                modifier = Modifier.weight(1f)
            )
            MetricPill(
                label = "Denied",
                value = "$deniedCount",
                containerColor = Color(0xFFFFEBEE),
                contentColor = Color(0xFFC62828),
                icon = Icons.Default.Close,
                modifier = Modifier.weight(1f)
            )
            MetricPill(
                label = "Grant Rate",
                value = "$compliancePct%",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Compliance Progress Bar
        LinearProgressIndicator(
            progress = { compliancePct / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (compliancePct >= 70) Color(0xFF2E7D32) else Color(0xFFEF6C00),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.take(5).forEach { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Render each permission card with full details
        filteredList.forEach { def ->
            val record = permissionRecords.find {
                it.permission.equals(def.technicalName, ignoreCase = true) ||
                it.name.equals(def.title, ignoreCase = true)
            }
            val isGranted = record?.state?.equals("Granted", ignoreCase = true)
                ?: (permissionStates[def.key]?.lowercase() == "granted")
            DetailedPermissionCard(def = def, record = record, isGranted = isGranted, sync = sync)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DetailedPermissionCard(
    def: PermissionDefinition,
    record: PermissionStatusRecord?,
    isGranted: Boolean,
    sync: DeviceSync
) {
    val state = record?.state ?: if (isGranted) "Granted" else "Denied"
    val syncStatus = record?.syncStatus ?: if (isGranted && sync.syncEnabled) "Synchronized" else if (!sync.syncEnabled) "Disabled" else "Not synchronized"

    val isPermGranted = state.equals("Granted", ignoreCase = true)
    val isPermDenied = state.equals("Denied", ignoreCase = true)

    val stateColor = when {
        isPermGranted -> Color(0xFF2E7D32)
        isPermDenied -> Color(0xFFC62828)
        else -> Color(0xFFEF6C00)
    }
    val stateBg = when {
        isPermGranted -> Color(0xFFE8F5E9)
        isPermDenied -> Color(0xFFFFEBEE)
        else -> Color(0xFFFFF3E0)
    }

    val syncColor = when (syncStatus.lowercase()) {
        "synchronized" -> Color(0xFF2E7D32)
        "disabled" -> Color(0xFFE65100)
        else -> Color(0xFF757575)
    }
    val syncBg = when (syncStatus.lowercase()) {
        "synchronized" -> Color(0xFFE8F5E9)
        "disabled" -> Color(0xFFFFF3E0)
        else -> Color(0xFFF5F5F5)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                isPermGranted -> Color(0xFFC8E6C9)
                isPermDenied -> Color(0xFFFFCDD2)
                else -> Color(0xFFFFE082)
            }
        ),
        shadowElevation = 0.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Icon + Title + Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(stateBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = def.icon,
                            contentDescription = null,
                            tint = stateColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = def.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = def.technicalName,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    // Runtime State Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = stateBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when {
                                    isPermGranted -> Icons.Default.CheckCircle
                                    isPermDenied -> Icons.Default.Close
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = stateColor,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = state.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = stateColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Synchronization Status Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = syncBg
                    ) {
                        Text(
                            text = "Sync: $syncStatus",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = syncColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Purpose & Scope
            Text(
                text = def.purpose,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Privacy & Data Sync Disclosure
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = def.syncUsage,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Distinguish permission granted from contact data synchronized
            if (def.key == "contacts" && isPermGranted) {
                Spacer(modifier = Modifier.height(4.dp))
                val contactCount = sync.approvedDeviceMetadata?.contactCount
                Text(
                    text = if (contactCount != null) {
                        "✓ Contact data synchronized: $contactCount contacts"
                    } else {
                        "Notice: Permission granted, but contacts summary data is not synchronized."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = if (contactCount != null) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                )
            }

            // Camera explicit note
            if (def.key == "camera") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Camera snapshots operate only upon explicit administrator command. Zero continuous streaming or background capture.",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (record != null && record.lastUpdated > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                val updateDate = Date(record.lastUpdated)
                val formattedUpdate = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault()).format(updateDate)
                Text(
                    text = "Last audit update: $formattedUpdate",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = contentColor.copy(alpha = 0.8f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
private fun SyncPausedPrivacyBanner() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF8E1),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE082)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFFF57F17),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Live Telemetry Paused by Host User",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "The device user has switched OFF 'Sync with Admin' in Sentinel Settings. In compliance with explicit user consent, personal telemetry (battery, network, contact counts, location) is suppressed. The permissions table above reflects the current audited security state.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF795548)
                )
            }
        }
    }
}

@Composable
private fun AwaitingSyncBanner(
    onRequestSync: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Awaiting Initial Telemetry Snapshot",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No synchronization packet has been received from this device yet. Telemetry will appear automatically once the host application registers.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            FilledTonalButton(
                onClick = onRequestSync,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Request Sync from Host Now")
            }
        }
    }
}

// ============================================================
// Complete Telemetry Sections (Every single data point)
// ============================================================

@Composable
private fun TelemetryDetailsSection(sync: DeviceSync) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Device Hardware & Telemetry Details",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 1. Battery Telemetry
        sync.batteryStatus?.let { battery ->
            TelemetrySubCard(
                title = "Battery Telemetry",
                icon = if (battery.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Charge Level: ${battery.level}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (battery.isCharging) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (battery.isCharging) "CHARGING" else "DISCHARGING",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (battery.isCharging) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { battery.level / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        battery.level > 50 -> Color(0xFF2E7D32)
                        battery.level > 20 -> Color(0xFFEF6C00)
                        else -> Color(0xFFC62828)
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                val tempF = String.format(Locale.US, "%.1f", (battery.temperatureC * 9 / 5) + 32)
                TelemetryRow("Battery Temperature", "${battery.temperatureC} °C ($tempF °F)")
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // 2. Network Telemetry
        sync.networkState?.let { net ->
            TelemetrySubCard(
                title = "Network & Connectivity",
                icon = Icons.Default.Wifi
            ) {
                TelemetryRow("Transport Type", net.networkType)
                net.wifiSsid?.let { TelemetryRow("Connected Wi-Fi SSID", it) }
                net.carrierName?.let { TelemetryRow("Cellular Carrier Operator", it) }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // 3. Approved Diagnostics, Hardware & System Metadata
        sync.approvedDeviceMetadata?.let { meta ->
            TelemetrySubCard(
                title = "Approved Diagnostics & System Metadata",
                icon = Icons.Default.Info
            ) {
                // Storage metrics
                if (meta.storageAvailableGb != null && meta.storageTotalGb != null) {
                    TelemetryRow("Internal Storage", "${meta.storageAvailableGb} GB free of ${meta.storageTotalGb} GB")
                } else if (meta.storageAvailableGb != null) {
                    TelemetryRow("Internal Storage", "${meta.storageAvailableGb} GB available")
                }

                // System Uptime
                sync.systemUptimeSeconds?.let { uptime ->
                    TelemetryRow("System Uptime", formatUptime(uptime))
                }

                meta.contactCount?.let {
                    TelemetryRow("Address Book Contacts", "$it (count only, raw data safeguarded)")
                }
                meta.callCount?.let {
                    TelemetryRow("Total Calls Logged", "$it (summary count)")
                }
                meta.lastCallTimestamp?.let {
                    if (it > 0) {
                        val callDate = Date(it * 1000)
                        val formattedCall = SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.getDefault()).format(callDate)
                        TelemetryRow("Last Call Activity", formattedCall)
                    }
                }
                meta.osVersion?.let {
                    TelemetryRow("Operating System", it)
                }
                meta.manufacturer?.let {
                    TelemetryRow("Hardware Manufacturer", it)
                }
                meta.model?.let {
                    TelemetryRow("Device Model", it)
                }
                meta.adId?.let {
                    TelemetryRow("Advertising Identifier (AD_ID)", it)
                }
                sync.lastSyncSuccessTime?.let {
                    if (it > 0) {
                        TelemetryRow("Last Sync Success", formatSyncTime(it))
                    }
                }
                sync.lastSyncFailureTime?.let {
                    if (it > 0) {
                        TelemetryRow("Last Sync Failure", formatSyncTime(it))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // 4. GPS Location Coordinates (from sync)
        val locPerm = sync.permissions.find { it.permission == "android.permission.ACCESS_COARSE_LOCATION" }
        val isLocGranted = locPerm?.state == "granted" || sync.permissionStates["coarseLocation"] == "granted"
        val loc = sync.location

        if (loc != null) {
            TelemetrySubCard(
                title = "GPS Coordinates (Sync)",
                icon = Icons.Default.LocationOn
            ) {
                TelemetryRow("Latitude", String.format(Locale.US, "%.6f", loc.latitude))
                TelemetryRow("Longitude", String.format(Locale.US, "%.6f", loc.longitude))
                TelemetryRow("Accuracy Radius", "${loc.accuracy} m")
                if (loc.recordedAt > 0) {
                    val locDate = Date(loc.recordedAt * 1000)
                    val formattedLoc = SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.getDefault()).format(locDate)
                    TelemetryRow("Recorded At", formattedLoc)
                }
            }
        } else {
            TelemetrySubCard(
                title = "GPS Location (Sync)",
                icon = Icons.Default.LocationOff
            ) {
                TelemetryRow("Location Status", "Location unavailable")
                val reason = when {
                    !isLocGranted -> "ACCESS_COARSE_LOCATION permission is Denied or Not Requested."
                    !sync.syncEnabled -> "Sync with Admin is paused by host user."
                    else -> "No GPS/network location fix recorded by host device."
                }
                Text(
                    text = reason,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Call Logs Telemetry Section
        CallLogTelemetrySubCard(sync = sync)
    }
}

@Composable
private fun CallLogTelemetrySubCard(sync: DeviceSync) {
    val callLogPerm = sync.permissions.find { it.permission == "android.permission.READ_CALL_LOG" }
    val isCallLogGranted = callLogPerm?.state == "granted" || sync.permissionStates["callLog"] == "granted"

    val totalCalls = sync.callLogs.size
    val incomingCalls = sync.callLogs.count { it.callType.equals("Incoming", ignoreCase = true) }
    val outgoingCalls = sync.callLogs.count { it.callType.equals("Outgoing", ignoreCase = true) }
    val missedCalls = sync.callLogs.count { it.callType.equals("Missed", ignoreCase = true) }
    val rejectedCalls = sync.callLogs.count { it.callType.equals("Rejected", ignoreCase = true) }

    TelemetrySubCard(
        title = "Call Logs Telemetry",
        icon = Icons.Default.Call
    ) {
        if (!isCallLogGranted) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFEBEE),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "READ_CALL_LOG permission is Denied. Telemetry collection blocked by Android runtime policy.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFC62828)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!sync.syncEnabled && sync.callLogs.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFF3E0),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Live sync is paused by host user. Displaying previously retained call records.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE65100),
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Summary Counts Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MetricPill(
                label = "Total",
                value = "$totalCalls",
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            MetricPill(
                label = "Incoming",
                value = "$incomingCalls",
                containerColor = Color(0xFFE8F5E9),
                contentColor = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
            MetricPill(
                label = "Outgoing",
                value = "$outgoingCalls",
                containerColor = Color(0xFFE3F2FD),
                contentColor = Color(0xFF1565C0),
                modifier = Modifier.weight(1f)
            )
            MetricPill(
                label = "Missed",
                value = "$missedCalls",
                containerColor = Color(0xFFFFEBEE),
                contentColor = Color(0xFFC62828),
                modifier = Modifier.weight(1f)
            )
            MetricPill(
                label = "Rejected",
                value = "$rejectedCalls",
                containerColor = Color(0xFFFFF3E0),
                contentColor = Color(0xFFEF6C00),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Synchronized Call Records List
        if (sync.callLogs.isEmpty()) {
            Text(
                text = if (!isCallLogGranted) {
                    "No call records collected because READ_CALL_LOG permission is not granted on host device."
                } else {
                    "No synchronized call records available."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "Synchronized Records (${sync.callLogs.size}):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            sync.callLogs.forEach { record ->
                CallRecordRow(record = record)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun CallRecordRow(record: CallLogRecord) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.phoneNumber.ifBlank { "Private / Unknown" },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val callDate = if (record.timestamp > 1000000000000L) Date(record.timestamp) else Date(record.timestamp * 1000)
                val formattedDate = SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.getDefault()).format(callDate)
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                val (typeBg, typeColor) = when (record.callType.lowercase()) {
                    "incoming" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                    "outgoing" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
                    "missed" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
                    "rejected" -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
                    else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = typeBg
                ) {
                    Text(
                        text = record.callType.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = formatDuration(record.durationSeconds),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatUptime(uptimeSeconds: Long?): String {
    if (uptimeSeconds == null || uptimeSeconds <= 0) return "Unknown"
    val days = uptimeSeconds / 86400
    val hours = (uptimeSeconds % 86400) / 3600
    val mins = (uptimeSeconds % 3600) / 60
    val secs = uptimeSeconds % 60
    return when {
        days > 0 -> "${days}d ${hours}h ${mins}m"
        hours > 0 -> "${hours}h ${mins}m ${secs}s"
        else -> "${mins}m ${secs}s"
    }
}

private fun formatDuration(seconds: Long): String {
    if (seconds <= 0) return "0s"
    val mins = seconds / 60
    val secs = seconds % 60
    return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
}

private fun formatSyncTime(timestamp: Long): String {
    if (timestamp <= 0) return "Unknown"
    val date = if (timestamp > 1000000000000L) Date(timestamp) else Date(timestamp * 1000)
    return SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.getDefault()).format(date)
}

@Composable
private fun TelemetrySubCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
