package com.sentinel.host.data.remote.protocol

import com.squareup.moshi.JsonClass

/**
 * Moshi-serializable protocol models.
 * These are internal transport types — never exposed to the domain layer.
 *
 * Outgoing payloads: serialized into the "data" field of the protocol envelope.
 * Incoming models: full envelope + typed data for deserialization.
 */

// ============================================================
// Envelope — used to extract "type" from any incoming message
// ============================================================

@JsonClass(generateAdapter = true)
internal data class EnvelopeJson(
    val type: String = "",
    val version: Int = 0,
    val timestamp: Long = 0,
    val sequence: Long = 0
)

// ============================================================
// Outgoing data payloads
// ============================================================

@JsonClass(generateAdapter = true)
internal data class AuthDataJson(
    val token: String
)

@JsonClass(generateAdapter = true)
internal data class RegisterDataJson(
    val deviceId: String,
    val deviceName: String,
    val appVersion: String,
    val model: String
)

@JsonClass(generateAdapter = true)
internal data class LocationDataJson(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val battery: Int,
    val network: String
)

// ============================================================
// Incoming full messages (envelope + typed data)
// ============================================================

@JsonClass(generateAdapter = true)
internal data class AckDataJson(
    val success: Boolean = false
)

@JsonClass(generateAdapter = true)
internal data class AckMessageJson(
    val type: String = "",
    val version: Int = 0,
    val timestamp: Long = 0,
    val sequence: Long = 0,
    val data: AckDataJson = AckDataJson()
)

@JsonClass(generateAdapter = true)
internal data class ErrorDataJson(
    val code: Int = 0,
    val message: String = ""
)

@JsonClass(generateAdapter = true)
internal data class ErrorMessageJson(
    val type: String = "",
    val version: Int = 0,
    val timestamp: Long = 0,
    val sequence: Long = 0,
    val data: ErrorDataJson = ErrorDataJson()
)

// ============================================================
// File Messages
// ============================================================

@JsonClass(generateAdapter = true)
internal data class FilesListReqDataJson(
    val path: String = ""
)

@JsonClass(generateAdapter = true)
internal data class FilesListReqJson(
    val type: String = "",
    val data: FilesListReqDataJson = FilesListReqDataJson()
)

@JsonClass(generateAdapter = true)
internal data class FileDownloadReqDataJson(
    val path: String = "",
    val offset: Long = 0L,
    val nonce: String = ""
)

@JsonClass(generateAdapter = true)
internal data class FileDownloadReqJson(
    val type: String = "",
    val data: FileDownloadReqDataJson = FileDownloadReqDataJson()
)

@JsonClass(generateAdapter = true)
internal data class FileChunkAckDataJson(
    val path: String = "",
    val sequence: Long = 0L
)

@JsonClass(generateAdapter = true)
internal data class FileChunkAckJson(
    val type: String = "",
    val data: FileChunkAckDataJson = FileChunkAckDataJson()
)

@JsonClass(generateAdapter = true)
internal data class FileStopReqDataJson(
    val path: String = ""
)

@JsonClass(generateAdapter = true)
internal data class FileStopReqJson(
    val type: String = "",
    val data: FileStopReqDataJson = FileStopReqDataJson()
)

// ============================================================
// Device Sync & Privacy Models
// ============================================================

@JsonClass(generateAdapter = true)
internal data class BatterySyncJson(
    val level: Int = 0,
    val isCharging: Boolean = false,
    val temperatureC: Double = 0.0
)

@JsonClass(generateAdapter = true)
internal data class NetworkSyncJson(
    val networkType: String = "",
    val wifiSsid: String? = null,
    val carrierName: String? = null
)

@JsonClass(generateAdapter = true)
internal data class LocationSyncJson(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0f,
    val recordedAt: Long = 0L
)

@JsonClass(generateAdapter = true)
internal data class MetadataSyncJson(
    val contactCount: Int? = null,
    val callCount: Int? = null,
    val lastCallTimestamp: Long? = null,
    val adId: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
    val osVersion: String? = null
)

@JsonClass(generateAdapter = true)
internal data class DeviceSyncDataJson(
    val deviceId: String = "",
    val timestamp: Long = 0L,
    val syncEnabled: Boolean = false,
    val permissionStates: Map<String, String> = emptyMap(),
    val batteryStatus: BatterySyncJson? = null,
    val networkState: NetworkSyncJson? = null,
    val location: LocationSyncJson? = null,
    val approvedDeviceMetadata: MetadataSyncJson? = null
)

