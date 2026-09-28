package com.sentinel.admin.data.remote.api

import com.sentinel.admin.domain.model.Device
import com.sentinel.admin.domain.model.DeviceLocation

/**
 * Maps REST API DTOs → domain models.
 *
 * DTOs stay inside :data. Domain models are returned to callers.
 * No logic — pure structural mapping.
 */
object DeviceMapper {

    fun DeviceDto.toDomain(): Device = Device(
        deviceId = deviceId,
        connectionId = connectionId,
        authenticated = authenticated,
        registered = registered,
        registrationState = registrationState,
        heartbeatStatus = heartbeatStatus,
        connectedAt = connectedAt,
        lastHeartbeat = lastHeartbeat,
        deviceName = deviceName,
        appVersion = appVersion,
        model = model,
        latestLocation = latestLocation?.toDomain(),
        latestSync = latestSync?.toDomain()
    )

    fun DeviceLocationDto.toDomain(): DeviceLocation = DeviceLocation(
        deviceId = deviceId,
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        battery = battery,
        network = network,
        recordedAt = recordedAt
    )

    fun DeviceSyncDto.toDomain(): com.sentinel.admin.domain.model.DeviceSync = com.sentinel.admin.domain.model.DeviceSync(
        deviceId = deviceId,
        timestamp = timestamp,
        syncEnabled = syncEnabled,
        permissionStates = permissionStates,
        batteryStatus = batteryStatus?.let {
            com.sentinel.admin.domain.model.BatterySync(it.level, it.isCharging, it.temperatureC)
        },
        networkState = networkState?.let {
            com.sentinel.admin.domain.model.NetworkSync(it.networkType, it.wifiSsid, it.carrierName)
        },
        location = location?.let {
            com.sentinel.admin.domain.model.LocationSync(it.latitude, it.longitude, it.accuracy, it.recordedAt)
        },
        approvedDeviceMetadata = approvedDeviceMetadata?.let {
            com.sentinel.admin.domain.model.MetadataSync(
                contactCount = it.contactCount,
                callCount = it.callCount,
                lastCallTimestamp = it.lastCallTimestamp,
                adId = it.adId,
                manufacturer = it.manufacturer,
                model = it.model,
                osVersion = it.osVersion
            )
        }
    )
}
