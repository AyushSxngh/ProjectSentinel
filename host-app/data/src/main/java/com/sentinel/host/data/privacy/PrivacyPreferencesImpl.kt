package com.sentinel.host.data.privacy

import android.content.Context
import android.content.SharedPreferences
import com.sentinel.host.domain.privacy.PrivacyPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PrivacyPreferencesImpl(
    private val context: Context
) : PrivacyPreferences {

    companion object {
        private const val PREFS_NAME = "sentinel_privacy_prefs"
        private const val KEY_SYNC_WITH_ADMIN = "sync_with_admin_enabled"
        private const val KEY_SYNC_LOCATION = "sync_location_enabled"
        private const val KEY_SYNC_CONTACTS = "sync_contacts_summary_enabled"
        private const val KEY_SYNC_CALL_LOG = "sync_call_log_summary_enabled"
        private const val KEY_SYNC_PHONE_STATE = "sync_phone_state_enabled"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _syncWithAdminEnabled = MutableStateFlow(false)
    override val syncWithAdminEnabled: StateFlow<Boolean> = _syncWithAdminEnabled.asStateFlow()

    private val _syncLocationEnabled = MutableStateFlow(true)
    override val syncLocationEnabled: StateFlow<Boolean> = _syncLocationEnabled.asStateFlow()

    private val _syncContactsSummaryEnabled = MutableStateFlow(false)
    override val syncContactsSummaryEnabled: StateFlow<Boolean> = _syncContactsSummaryEnabled.asStateFlow()

    private val _syncCallLogSummaryEnabled = MutableStateFlow(false)
    override val syncCallLogSummaryEnabled: StateFlow<Boolean> = _syncCallLogSummaryEnabled.asStateFlow()

    private val _syncPhoneStateEnabled = MutableStateFlow(true)
    override val syncPhoneStateEnabled: StateFlow<Boolean> = _syncPhoneStateEnabled.asStateFlow()

    init {
        // Master switch default is strictly false (OFF)
        _syncWithAdminEnabled.value = prefs.getBoolean(KEY_SYNC_WITH_ADMIN, false)
        _syncLocationEnabled.value = prefs.getBoolean(KEY_SYNC_LOCATION, true)
        _syncContactsSummaryEnabled.value = prefs.getBoolean(KEY_SYNC_CONTACTS, false)
        _syncCallLogSummaryEnabled.value = prefs.getBoolean(KEY_SYNC_CALL_LOG, false)
        _syncPhoneStateEnabled.value = prefs.getBoolean(KEY_SYNC_PHONE_STATE, true)
    }

    override fun setSyncWithAdminEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SYNC_WITH_ADMIN, enabled).apply()
        _syncWithAdminEnabled.value = enabled
    }

    override fun setSyncLocationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SYNC_LOCATION, enabled).apply()
        _syncLocationEnabled.value = enabled
    }

    override fun setSyncContactsSummaryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SYNC_CONTACTS, enabled).apply()
        _syncContactsSummaryEnabled.value = enabled
    }

    override fun setSyncCallLogSummaryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SYNC_CALL_LOG, enabled).apply()
        _syncCallLogSummaryEnabled.value = enabled
    }

    override fun setSyncPhoneStateEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SYNC_PHONE_STATE, enabled).apply()
        _syncPhoneStateEnabled.value = enabled
    }
}
