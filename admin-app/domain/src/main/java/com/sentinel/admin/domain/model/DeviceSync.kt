package com.sentinel.admin.domain.model

/**
 * Domain model for approved device telemetry synchronization.
 */
data class DeviceSync(
    val deviceId: String,
    val timestamp: Long,
    val syncEnabled: Boolean,
    val permissionStates: Map<String, String> = emptyMap(),
    val batteryStatus: BatterySync? = null,
    val networkState: NetworkSync? = null,
    val location: LocationSync? = null,
    val approvedDeviceMetadata: MetadataSync? = null
)

data class BatterySync(
    val level: Int,
    val isCharging: Boolean,
    val temperatureC: Double
)

data class NetworkSync(
    val networkType: String,
    val wifiSsid: String?,
    val carrierName: String?
)

data class LocationSync(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val recordedAt: Long
)

data class MetadataSync(
    val contactCount: Int?,
    val callCount: Int?,
    val lastCallTimestamp: Long?,
    val adId: String?,
    val manufacturer: String?,
    val model: String?,
    val osVersion: String?
)
