package com.sentinel.host.domain.privacy

import kotlinx.coroutines.flow.StateFlow

/**
 * Centralized manager for querying, observing, and validating Android runtime permissions.
 */
interface PermissionManager {
    /**
     * Flow of all audited permissions and their current grant status.
     */
    val permissions: StateFlow<List<PermissionItem>>

    /**
     * Re-checks Android runtime permissions against current system state.
     */
    fun refreshPermissions()

    /**
     * Returns true if the specific permission is currently granted.
     */
    fun isPermissionGranted(permission: String): Boolean

    /**
     * Returns a map of permission short-name to "granted" or "denied" for payload serialization.
     */
    fun getPermissionStatesMap(): Map<String, String>

    /**
     * Returns the full list of audited permissions with Android identifiers, state, and sync status.
     */
    fun getDetailedPermissionRecords(): List<com.sentinel.shared.model.PermissionStatusRecord>
}
