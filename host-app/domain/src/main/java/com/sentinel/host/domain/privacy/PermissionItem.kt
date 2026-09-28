package com.sentinel.host.domain.privacy

/**
 * Describes an audited permission, its grant state, and privacy usage disclosure.
 */
data class PermissionItem(
    val permission: String,
    val title: String,
    val category: String,
    val isGranted: Boolean,
    val purpose: String,
    val syncUsage: String,
    val isDangerous: Boolean = true
)
