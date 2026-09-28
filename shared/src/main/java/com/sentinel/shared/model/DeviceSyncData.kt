package com.sentinel.shared.model

/**
 * Shared DTO for user-consented device synchronization.
 *
 * Synchronization Guarantees:
 * - Only transmitted when syncEnabled is true and the user has explicitly consented.
 * - Sub-features only gather data when the corresponding Android permission
 *   is granted and the respective feature toggle is active.
 * - Adheres strictly to audited Android runtime permission states.
 */
data class DeviceSyncPayload(
    val deviceId: String,
    val timestamp: Long,
    val syncEnabled: Boolean,
    val permissionStates: Map<String, String> = emptyMap(),
    val permissions: List<PermissionStatusRecord> = emptyList(),
    val batteryStatus: BatterySyncData? = null,
    val networkState: NetworkSyncData? = null,
    val location: LocationSyncData? = null,
    val approvedDeviceMetadata: DeviceMetadataSyncData? = null,
    val callLogs: List<CallLogRecord> = emptyList(),
    val systemUptimeSeconds: Long? = null,
    val lastSyncSuccessTime: Long? = null,
    val lastSyncFailureTime: Long? = null
)

data class PermissionStatusRecord(
    val name: String,
    val permission: String,
    val state: String, // "Granted", "Denied", "Not requested"
    val lastUpdated: Long,
    val syncStatus: String // "Synchronized", "Not synchronized", "Disabled"
)

data class CallLogRecord(
    val id: String = "",
    val phoneNumber: String,
    val callType: String, // "Incoming", "Outgoing", "Missed", "Rejected"
    val timestamp: Long,
    val durationSeconds: Long
)

data class BatterySyncData(
    val level: Int,
    val isCharging: Boolean,
    val temperatureC: Double
)

data class NetworkSyncData(
    val networkType: String,
    val wifiSsid: String? = null,
    val carrierName: String? = null
)

data class LocationSyncData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val recordedAt: Long
)

data class DeviceMetadataSyncData(
    val contactCount: Int? = null,
    val callCount: Int? = null,
    val lastCallTimestamp: Long? = null,
    val adId: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
    val osVersion: String? = null,
    val storageAvailableGb: String? = null,
    val storageTotalGb: String? = null
)
