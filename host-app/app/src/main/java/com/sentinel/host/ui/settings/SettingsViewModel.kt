package com.sentinel.host.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinel.host.domain.privacy.PermissionItem
import com.sentinel.host.domain.privacy.PermissionManager
import com.sentinel.host.domain.privacy.PrivacyPreferences
import com.sentinel.host.service.DeviceSyncStreamer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val permissionManager: PermissionManager,
    private val privacyPreferences: PrivacyPreferences,
    private val syncStreamer: DeviceSyncStreamer
) : ViewModel() {

    val permissions: StateFlow<List<PermissionItem>> = permissionManager.permissions
    val syncWithAdminEnabled: StateFlow<Boolean> = privacyPreferences.syncWithAdminEnabled
    val syncLocationEnabled: StateFlow<Boolean> = privacyPreferences.syncLocationEnabled
    val syncContactsEnabled: StateFlow<Boolean> = privacyPreferences.syncContactsSummaryEnabled
    val syncCallLogEnabled: StateFlow<Boolean> = privacyPreferences.syncCallLogSummaryEnabled
    val syncPhoneStateEnabled: StateFlow<Boolean> = privacyPreferences.syncPhoneStateEnabled

    fun setSyncWithAdmin(enabled: Boolean) {
        privacyPreferences.setSyncWithAdminEnabled(enabled)
    }

    fun setSyncLocation(enabled: Boolean) {
        privacyPreferences.setSyncLocationEnabled(enabled)
    }

    fun setSyncContacts(enabled: Boolean) {
        privacyPreferences.setSyncContactsSummaryEnabled(enabled)
    }

    fun setSyncCallLog(enabled: Boolean) {
        privacyPreferences.setSyncCallLogSummaryEnabled(enabled)
    }

    fun setSyncPhoneState(enabled: Boolean) {
        privacyPreferences.setSyncPhoneStateEnabled(enabled)
    }

    fun refreshPermissions() {
        permissionManager.refreshPermissions()
    }

    fun triggerSyncNow() {
        viewModelScope.launch {
            syncStreamer.performSync()
        }
    }
}
