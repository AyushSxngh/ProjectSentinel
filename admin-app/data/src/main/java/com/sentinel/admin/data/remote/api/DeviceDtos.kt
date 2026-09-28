package com.sentinel.admin.data.remote.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO for GET /devices response wrapper.
 */
@JsonClass(generateAdapter = true)
data class DevicesResponse(
    @Json(name = "devices") val devices: List<DeviceDto>
)

/**
 * DTO for a single device from GET /devices and GET /devices/{deviceId}.
 * Maps to the backend JSON response. Not exposed outside :data.
 */
@JsonClass(generateAdapter = true)
data class DeviceDto(
    @Json(name = "deviceId") val deviceId: String = "",
    @Json(name = "connectionId") val connectionId: String = "",
    @Json(name = "authenticated") val authenticated: Boolean = false,
    @Json(name = "registered") val registered: Boolean = false,
    @Json(name = "registrationState") val registrationState: String = "",
    @Json(name = "heartbeatStatus") val heartbeatStatus: String = "",
    @Json(name = "connectedAt") val connectedAt: String = "",
    @Json(name = "lastHeartbeat") val lastHeartbeat: String = "",
    @Json(name = "deviceName") val deviceName: String = "",
    @Json(name = "appVersion") val appVersion: String = "",
    @Json(name = "model") val model: String = "",
    @Json(name = "latestLocation") val latestLocation: DeviceLocationDto? = null,
    @Json(name = "latestSync") val latestSync: DeviceSyncDto? = null
)

/**
 * DTO for nested location data within a device response.
 */
@JsonClass(generateAdapter = true)
data class DeviceLocationDto(
    @Json(name = "deviceId") val deviceId: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "accuracy") val accuracy: Double,
    @Json(name = "battery") val battery: Int,
    @Json(name = "network") val network: String,
    @Json(name = "recordedAt") val recordedAt: String
)

@JsonClass(generateAdapter = true)
data class BatterySyncDto(
    @Json(name = "level") val level: Int = 0,
    @Json(name = "isCharging") val isCharging: Boolean = false,
    @Json(name = "temperatureC") val temperatureC: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class NetworkSyncDto(
    @Json(name = "networkType") val networkType: String = "",
    @Json(name = "wifiSsid") val wifiSsid: String? = null,
    @Json(name = "carrierName") val carrierName: String? = null
)

@JsonClass(generateAdapter = true)
data class LocationSyncDto(
    @Json(name = "latitude") val latitude: Double = 0.0,
    @Json(name = "longitude") val longitude: Double = 0.0,
    @Json(name = "accuracy") val accuracy: Float = 0f,
    @Json(name = "recordedAt") val recordedAt: Long = 0L
)

@JsonClass(generateAdapter = true)
data class PermissionStatusDto(
    @Json(name = "name") val name: String = "",
    @Json(name = "permission") val permission: String = "",
    @Json(name = "state") val state: String = "",
    @Json(name = "lastUpdated") val lastUpdated: Long = 0L,
    @Json(name = "syncStatus") val syncStatus: String = ""
)

@JsonClass(generateAdapter = true)
data class CallLogDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "phoneNumber") val phoneNumber: String = "",
    @Json(name = "callType") val callType: String = "",
    @Json(name = "timestamp") val timestamp: Long = 0L,
    @Json(name = "durationSeconds") val durationSeconds: Long = 0L
)

@JsonClass(generateAdapter = true)
data class MetadataSyncDto(
    @Json(name = "contactCount") val contactCount: Int? = null,
    @Json(name = "callCount") val callCount: Int? = null,
    @Json(name = "lastCallTimestamp") val lastCallTimestamp: Long? = null,
    @Json(name = "adId") val adId: String? = null,
    @Json(name = "manufacturer") val manufacturer: String? = null,
    @Json(name = "model") val model: String? = null,
    @Json(name = "osVersion") val osVersion: String? = null,
    @Json(name = "storageAvailableGb") val storageAvailableGb: String? = null,
    @Json(name = "storageTotalGb") val storageTotalGb: String? = null
)

@JsonClass(generateAdapter = true)
data class DeviceSyncDto(
    @Json(name = "deviceId") val deviceId: String = "",
    @Json(name = "timestamp") val timestamp: Long = 0L,
    @Json(name = "syncEnabled") val syncEnabled: Boolean = false,
    @Json(name = "permissionStates") val permissionStates: Map<String, String> = emptyMap(),
    @Json(name = "permissions") val permissions: List<PermissionStatusDto> = emptyList(),
    @Json(name = "batteryStatus") val batteryStatus: BatterySyncDto? = null,
    @Json(name = "networkState") val networkState: NetworkSyncDto? = null,
    @Json(name = "location") val location: LocationSyncDto? = null,
    @Json(name = "approvedDeviceMetadata") val approvedDeviceMetadata: MetadataSyncDto? = null,
    @Json(name = "callLogs") val callLogs: List<CallLogDto> = emptyList(),
    @Json(name = "systemUptimeSeconds") val systemUptimeSeconds: Long? = null,
    @Json(name = "lastSyncSuccessTime") val lastSyncSuccessTime: Long? = null,
    @Json(name = "lastSyncFailureTime") val lastSyncFailureTime: Long? = null
)
