package com.sentinel.admin.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sentinel.admin.domain.model.DeviceSync
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeviceSyncCard(
    sync: DeviceSync?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.height(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Device Sync & Privacy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                val syncEnabled = sync?.syncEnabled == true
                Text(
                    text = if (syncEnabled) "Sync: ON" else "Sync: OFF",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (syncEnabled) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (sync == null) {
                Text(
                    text = "No synchronization payload has been received from this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (!sync.syncEnabled) {
                Text(
                    text = "The device user has turned OFF 'Sync with Admin'. In compliance with user privacy controls, personal telemetry and metrics are not synchronized.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Last sync timestamp
                val formattedTime = SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.getDefault())
                    .format(Date(sync.timestamp * 1000))
                SyncDataRow("Last Sync Time", formattedTime)

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Permission States
                Text(
                    text = "Granted User Permissions",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                sync.permissionStates.forEach { (perm, state) ->
                    val granted = state.lowercase() == "granted"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = perm.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (granted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.height(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (granted) "Granted" else "Denied",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (granted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Battery Status
                sync.batteryStatus?.let { battery ->
                    SyncDataRow("Battery Level", "${battery.level}% (${if (battery.isCharging) "Charging" else "Discharging"})")
                    SyncDataRow("Battery Temp", "${battery.temperatureC} °C")
                }

                // Network State
                sync.networkState?.let { network ->
                    SyncDataRow("Network Type", network.networkType)
                    network.wifiSsid?.let { SyncDataRow("Wi-Fi SSID", it) }
                    network.carrierName?.let { SyncDataRow("Cellular Carrier", it) }
                }

                // Approved Metadata (Counts only — zero raw personal data)
                sync.approvedDeviceMetadata?.let { meta ->
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Approved Telemetry Summaries",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    meta.contactCount?.let {
                        SyncDataRow("Total Contacts", "$it (count only, details never uploaded)")
                    }
                    meta.callCount?.let {
                        SyncDataRow("Total Calls Logged", "$it (count only, numbers never uploaded)")
                    }
                    meta.lastCallTimestamp?.let {
                        val callTime = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
                            .format(Date(it * 1000))
                        SyncDataRow("Last Call Time", callTime)
                    }
                    meta.osVersion?.let {
                        SyncDataRow("Operating System", it)
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncDataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
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
