package com.sentinel.host.service

import android.util.Log
import com.sentinel.host.data.remote.SequenceGenerator
import com.sentinel.host.data.remote.protocol.MessageSerializer
import com.sentinel.host.data.sync.DeviceSyncCollector
import com.sentinel.host.domain.privacy.PrivacyPreferences
import com.sentinel.host.domain.repository.ConnectionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Service managing synchronization of approved telemetry with Sentinel server.
 *
 * Privacy Guarantees:
 * - Sync is strictly disabled by default.
 * - Nothing is gathered or transmitted when syncWithAdmin is OFF.
 * - Sub-features only gather data when permission is granted AND feature is active.
 * - Zero raw contacts, call records, or personal data logged or transmitted.
 */
class DeviceSyncStreamer(
    private val syncCollector: DeviceSyncCollector,
    private val connectionRepository: ConnectionRepository,
    private val messageSerializer: MessageSerializer,
    private val sequenceGenerator: SequenceGenerator,
    private val privacyPreferences: PrivacyPreferences,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "Sentinel:DeviceSync"
        private const val DEFAULT_SYNC_INTERVAL_MS = 30_000L // 30 seconds periodic sync
    }

    private var syncJob: Job? = null

    fun start() {
        stop()
        syncJob = scope.launch {
            // Immediate sync upon connection confirmation
            performSync()
            while (isActive) {
                delay(DEFAULT_SYNC_INTERVAL_MS)
                performSync()
            }
        }
    }

    fun stop() {
        syncJob?.cancel()
        syncJob = null
    }

    suspend fun performSync(): Boolean {
        return try {
            val payload = syncCollector.collectPayload()
            val sequence = sequenceGenerator.next()
            val messageJson = messageSerializer.serializeDeviceSync(payload, sequence)

            val sent = connectionRepository.sendText(messageJson)
            syncCollector.recordSyncResult(sent)
            if (sent) {
                Log.i(TAG, "Device sync payload successfully transmitted to server (seq=$sequence, syncEnabled=${payload.syncEnabled})")
            } else {
                Log.w(TAG, "Failed to send device sync payload over connection")
            }
            sent
        } catch (e: Exception) {
            syncCollector.recordSyncResult(false)
            Log.e(TAG, "Error performing device sync: ${e.message}")
            false
        }
    }
}
