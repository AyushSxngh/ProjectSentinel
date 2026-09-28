package com.sentinel.host.domain.privacy

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface managing user consent and privacy controls for device synchronization.
 */
interface PrivacyPreferences {
    /**
     * Master switch for synchronizing approved telemetry with the Sentinel Admin backend.
     * Default state MUST be false (OFF).
     */
    val syncWithAdminEnabled: StateFlow<Boolean>

    /**
     * Updates the master sync consent state.
     */
    fun setSyncWithAdminEnabled(enabled: Boolean)

    /**
     * Per-category user consent controls.
     */
    val syncLocationEnabled: StateFlow<Boolean>
    fun setSyncLocationEnabled(enabled: Boolean)

    val syncContactsSummaryEnabled: StateFlow<Boolean>
    fun setSyncContactsSummaryEnabled(enabled: Boolean)

    val syncCallLogSummaryEnabled: StateFlow<Boolean>
    fun setSyncCallLogSummaryEnabled(enabled: Boolean)

    val syncPhoneStateEnabled: StateFlow<Boolean>
    fun setSyncPhoneStateEnabled(enabled: Boolean)
}
