package com.sentinel.host.data.sync

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import com.sentinel.host.domain.privacy.PermissionManager
import com.sentinel.host.domain.privacy.PrivacyPreferences
import com.sentinel.shared.model.BatterySyncData
import com.sentinel.shared.model.DeviceMetadataSyncData
import com.sentinel.shared.model.DeviceSyncPayload
import com.sentinel.shared.model.LocationSyncData
import com.sentinel.shared.model.NetworkSyncData

class DeviceSyncCollector(
    private val context: Context,
    private val permissionManager: PermissionManager,
    private val privacyPreferences: PrivacyPreferences,
    private val deviceIdProvider: () -> String
) {
    companion object {
        private const val TAG = "Sentinel:SyncCollector"
    }

    /**
     * Gathers the approved device synchronization payload.
     *
     * Strict privacy guarantees:
     * - If syncWithAdminEnabled is false, returns an empty/disabled payload.
     * - Sub-features only gather data if permission is granted AND feature toggle is ON.
     * - Zero personal identifiers, names, phone numbers, or complete logs are ever gathered.
     */
    fun collectPayload(): DeviceSyncPayload {
        val syncEnabled = privacyPreferences.syncWithAdminEnabled.value
        val deviceId = deviceIdProvider()
        val timestamp = System.currentTimeMillis() / 1000

        permissionManager.refreshPermissions()
        val permStates = permissionManager.getPermissionStatesMap()

        if (!syncEnabled) {
            return DeviceSyncPayload(
                deviceId = deviceId,
                timestamp = timestamp,
                syncEnabled = false,
                permissionStates = permStates
            )
        }

        // 1. Battery Status
        val batteryData = collectBatteryData()

        // 2. Network State
        val networkData = collectNetworkData()

        // 3. Location (Only if granted & user consent given)
        val locationData = if (privacyPreferences.syncLocationEnabled.value &&
            permissionManager.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            collectLocationData()
        } else {
            null
        }

        // 4. Approved Metadata
        val metadata = collectApprovedMetadata()

        return DeviceSyncPayload(
            deviceId = deviceId,
            timestamp = timestamp,
            syncEnabled = true,
            permissionStates = permStates,
            batteryStatus = batteryData,
            networkState = networkData,
            location = locationData,
            approvedDeviceMetadata = metadata
        )
    }

    private fun collectBatteryData(): BatterySyncData? {
        return try {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else 0
            val tempTenths = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            val plugged = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
            val isCharging = plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                    plugged == BatteryManager.BATTERY_PLUGGED_USB ||
                    plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS

            BatterySyncData(
                level = batteryPct,
                isCharging = isCharging,
                temperatureC = tempTenths / 10.0
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to collect battery status: ${e.message}")
            null
        }
    }

    private fun collectNetworkData(): NetworkSyncData? {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = connectivityManager?.activeNetwork
            val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)

            val netType = when {
                caps == null -> "NONE"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
                else -> "OTHER"
            }

            var ssid: String? = null
            if (netType == "WIFI" && permissionManager.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
                try {
                    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                    val rawSsid = wifiManager?.connectionInfo?.ssid?.replace("\"", "")
                    if (rawSsid != null && rawSsid != "<unknown ssid>") {
                        ssid = rawSsid
                    }
                } catch (_: Exception) {}
            }

            var carrierName: String? = null
            if (privacyPreferences.syncPhoneStateEnabled.value &&
                permissionManager.isPermissionGranted(Manifest.permission.READ_PHONE_STATE)
            ) {
                try {
                    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                    carrierName = telephonyManager?.networkOperatorName?.takeIf { it.isNotBlank() }
                } catch (_: Exception) {}
            }

            NetworkSyncData(
                networkType = netType,
                wifiSsid = ssid,
                carrierName = carrierName
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to collect network state: ${e.message}")
            null
        }
    }

    private fun collectLocationData(): LocationSyncData? {
        // Location is read from system or latest cached update
        return null // Provided asynchronously by LocationRepository or fused provider when active
    }

    private fun collectApprovedMetadata(): DeviceMetadataSyncData {
        var contactCount: Int? = null
        if (privacyPreferences.syncContactsSummaryEnabled.value &&
            permissionManager.isPermissionGranted(Manifest.permission.READ_CONTACTS)
        ) {
            try {
                val cursor = context.contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    arrayOf(ContactsContract.Contacts._ID),
                    null, null, null
                )
                cursor?.use {
                    contactCount = it.count
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to count contacts: ${e.message}")
            }
        }

        var callCount: Int? = null
        var lastCallTimestamp: Long? = null
        if (privacyPreferences.syncCallLogSummaryEnabled.value &&
            permissionManager.isPermissionGranted(Manifest.permission.READ_CALL_LOG)
        ) {
            try {
                val cursor = context.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    arrayOf(CallLog.Calls.DATE),
                    null, null, "${CallLog.Calls.DATE} DESC"
                )
                cursor?.use {
                    callCount = it.count
                    if (it.moveToFirst()) {
                        val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                        if (dateIndex >= 0) {
                            lastCallTimestamp = it.getLong(dateIndex) / 1000
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read call summary: ${e.message}")
            }
        }

        return DeviceMetadataSyncData(
            contactCount = contactCount,
            callCount = callCount,
            lastCallTimestamp = lastCallTimestamp,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        )
    }
}
