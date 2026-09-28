package com.sentinel.host.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.sentinel.host.domain.privacy.PermissionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val permissions by viewModel.permissions.collectAsState()
    val syncWithAdmin by viewModel.syncWithAdminEnabled.collectAsState()
    val syncLocation by viewModel.syncLocationEnabled.collectAsState()
    val syncContacts by viewModel.syncContactsEnabled.collectAsState()
    val syncCallLog by viewModel.syncCallLogEnabled.collectAsState()
    val syncPhoneState by viewModel.syncPhoneStateEnabled.collectAsState()

    LifecycleResumeEffect(Unit) {
        viewModel.refreshPermissions()
        onPauseOrDispose { }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.refreshPermissions()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permissions & Privacy Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Master Privacy Switch: Sync with Admin
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (syncWithAdmin) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sync with Admin",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (syncWithAdmin) "Status: ON (Consented)" else "Status: OFF (Default)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (syncWithAdmin) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                            )
                        }

                        Switch(
                            checked = syncWithAdmin,
                            onCheckedChange = { viewModel.setSyncWithAdmin(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (syncWithAdmin) {
                            "Approved telemetry will be synchronized with the authenticated Sentinel Admin. Data is gathered only when explicit permissions are granted."
                        } else {
                            "Synchronization is completely DISABLED. No personal, telemetry, or device metrics will be transmitted to the admin server."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AnimatedVisibility(visible = syncWithAdmin) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Button(
                                onClick = { viewModel.triggerSyncNow() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Sync Now")
                            }
                        }
                    }
                }
            }

            // 2. Granular Telemetry Controls (visible only when master sync is enabled)
            AnimatedVisibility(visible = syncWithAdmin) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Approved Telemetry Categories",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SyncCategoryToggle(
                                title = "Location Sharing",
                                description = "Shares GPS coordinates when permitted.",
                                enabled = syncLocation,
                                onCheckedChange = { viewModel.setSyncLocation(it) }
                            )
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            SyncCategoryToggle(
                                title = "Contacts Summary",
                                description = "Shares total count only. Zero contact details.",
                                enabled = syncContacts,
                                onCheckedChange = { viewModel.setSyncContacts(it) }
                            )
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            SyncCategoryToggle(
                                title = "Call Log Summary",
                                description = "Shares call volume & last timestamp. Zero numbers.",
                                enabled = syncCallLog,
                                onCheckedChange = { viewModel.setSyncCallLog(it) }
                            )
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            SyncCategoryToggle(
                                title = "Cellular & Carrier State",
                                description = "Shares network carrier name for diagnostics.",
                                enabled = syncPhoneState,
                                onCheckedChange = { viewModel.setSyncPhoneState(it) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Centralized Permission Audit Table / List
            Text(
                text = "Audited Android Permissions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            permissions.forEach { item ->
                PermissionItemCard(
                    item = item,
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                            (item.permission == Manifest.permission.WRITE_EXTERNAL_STORAGE || item.category == "Storage")
                        ) {
                            try {
                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                try {
                                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    permissionLauncher.launch(item.permission)
                                }
                            }
                        } else {
                            permissionLauncher.launch(item.permission)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SyncCategoryToggle(
    title: String,
    description: String,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = enabled, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PermissionItemCard(
    item: PermissionItem,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                if (item.isGranted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Granted",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.height(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Granted",
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Denied",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.height(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Denied",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Purpose: ${item.purpose}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Sync Usage: ${item.syncUsage}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!item.isGranted && item.isDangerous) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onRequestPermission,
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Text("Grant Permission", fontSize = 12.sp)
                }
            }
        }
    }
}
