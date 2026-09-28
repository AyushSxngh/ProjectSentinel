package com.sentinel.host.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.sentinel.host.domain.model.ConnectionState
import com.sentinel.host.ui.dashboard.DashboardScreen
import com.sentinel.host.ui.login.LoginViewModel
import com.sentinel.host.ui.settings.SettingsScreen
import com.sentinel.host.ui.theme.SentinelHostTheme

/**
 * Root composable for the Host app.
 * Auto-connect is handled by MainActivity.
 * Displays connection status, dashboard, and the permissions/privacy settings screen.
 */
@Composable
fun SentinelHostApp() {
    SentinelHostTheme {
        val viewModel: LoginViewModel = hiltViewModel()
        val connectionState by viewModel.connectionState.collectAsState()
        var currentScreen by remember { mutableStateOf("main") }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            if (currentScreen == "settings") {
                SettingsScreen(onNavigateBack = { currentScreen = "main" })
            } else {
                when (connectionState) {
                    is ConnectionState.Ready -> {
                        DashboardScreen(
                            onNavigateToSettings = { currentScreen = "settings" },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                    else -> {
                        StatusScreen(
                            state = connectionState,
                            onNavigateToSettings = { currentScreen = "settings" },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
