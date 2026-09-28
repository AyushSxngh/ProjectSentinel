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
                PermissionsAuditSection(permissionStates = sync.permissionStates)

                // 3. User Privacy Notice if Sync with Admin is paused
                if (!sync.syncEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SyncPausedPrivacyBanner()
                } else {
                    // 4. Complete Telemetry Details (Every single data point)
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    TelemetryDetailsSection(sync = sync)
                }
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
    permissionStates: Map<String, String>
) {
    // Merge catalog with any unknown dynamic keys from host
    val allDefinitions = remember(permissionStates) {
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
    val grantedCount = allDefinitions.count {
        permissionStates[it.key]?.lowercase() == "granted"
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
            categories.take(4).forEach { cat ->
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
            val isGranted = permissionStates[def.key]?.lowercase() == "granted"
            DetailedPermissionCard(def = def, isGranted = isGranted)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DetailedPermissionCard(
    def: PermissionDefinition,
    isGranted: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)
        ),
        shadowElevation = 0.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Icon + Title + Status Badge
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
                            .background(
                                if (isGranted) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = def.icon,
                            contentDescription = null,
                            tint = if (isGranted) Color(0xFF2E7D32) else Color(0xFFC62828),
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

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isGranted) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isGranted) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGranted) "GRANTED" else "DENIED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isGranted) Color(0xFF2E7D32) else Color(0xFFC62828)
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

        // 3. Approved Telemetry Metrics
        sync.approvedDeviceMetadata?.let { meta ->
            TelemetrySubCard(
                title = "Approved Diagnostics & Metadata",
                icon = Icons.Default.Info
            ) {
                meta.contactCount?.let {
                    TelemetryRow("Total Address Book Contacts", "$it (count only, raw data safeguarded)")
                }
                meta.callCount?.let {
                    TelemetryRow("Total Calls Logged", "$it (count only, zero numbers stored)")
                }
                meta.lastCallTimestamp?.let {
                    val callDate = Date(it * 1000)
                    val formattedCall = SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.getDefault()).format(callDate)
                    TelemetryRow("Last Call Activity", formattedCall)
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
                    TelemetryRow("Advertising Identifier", it)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // 4. GPS Location Coordinates (from sync)
        sync.location?.let { loc ->
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
        }
    }
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
