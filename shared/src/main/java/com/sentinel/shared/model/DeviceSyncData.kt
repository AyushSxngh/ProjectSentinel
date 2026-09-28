package com.sentinel.shared.model

/**
 * Shared DTO for optional, user-consented device synchronization.
 *
 * Privacy Guarantees:
 * - Only transmitted when syncEnabled is true and the user has explicitly consented.
 * - Sensitive sub-fields are only populated if the corresponding Android permission
 *   is granted and the respective feature toggle is active.
 * - Zero raw contact records, raw call logs, or camera streams are transmitted.
 */
data class DeviceSyncPayload(
    val deviceId: String,
    val timestamp: Long,
    val syncEnabled: Boolean,
    val permissionStates: Map<String, String> = emptyMap(),
    val batteryStatus: BatterySyncData? = null,
    val networkState: NetworkSyncData? = null,
    val location: LocationSyncData? = null,
    val approvedDeviceMetadata: DeviceMetadataSyncData? = null
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
    val osVersion: String? = null
)
