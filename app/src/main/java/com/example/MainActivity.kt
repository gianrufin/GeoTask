package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.screens.QuickLogScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppNavTab(val label: String) {
    REMINDERS("Reminders"),
    QUICK_LOG("Quick Log"),
    SETTINGS("Settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }
            val context = LocalContext.current

            // Show snackbar notices
            LaunchedEffect(uiState.userNotice) {
                uiState.userNotice?.let { notice ->
                    snackbarHostState.showSnackbar(notice)
                    viewModel.clearNotice()
                }
            }

            // Permissions launcher
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                if (locationGranted) {
                    viewModel.fetchLocation()
                }
            }

            LaunchedEffect(Unit) {
                val permissionsToRequest = mutableListOf<String>()
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
                    permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }

                if (permissionsToRequest.isNotEmpty()) {
                    permissionLauncher.launch(permissionsToRequest.toTypedArray())
                }
            }

            MyApplicationTheme(
                palette = uiState.settings.colorPalette,
                darkModePreference = uiState.settings.darkMode
            ) {
                var currentTab by remember { mutableStateOf(AppNavTab.REMINDERS) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        GeoReminderTopAppBar(
                            currentTab = currentTab,
                            onTestGeofence = {
                                uiState.currentLocation?.let {
                                    viewModel.evaluateLocationTriggers(it.latitude, it.longitude)
                                } ?: viewModel.evaluateLocationTriggers(37.7749, -122.4194)
                            }
                        )
                    },
                    bottomBar = {
                        GeoReminderBottomBar(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            AppNavTab.REMINDERS -> {
                                RemindersScreen(
                                    state = uiState,
                                    onFilterChange = { viewModel.setFilter(it) },
                                    onRefreshLocation = { viewModel.fetchLocation() },
                                    onToggleActive = { viewModel.toggleReminderActive(it) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onSaveReminder = { viewModel.saveReminder(it) },
                                    onDeleteReminder = { viewModel.deleteReminder(it) },
                                    onOneTapLog = { reminder ->
                                        viewModel.logQuickAction(
                                            title = reminder.title,
                                            locationLabel = reminder.locationLabel,
                                            latitude = reminder.latitude,
                                            longitude = reminder.longitude,
                                            reminderId = reminder.id
                                        )
                                    },
                                    onTestTrigger = { reminder ->
                                        viewModel.evaluateLocationTriggers(
                                            reminder.latitude,
                                            reminder.longitude
                                        )
                                    }
                                )
                            }
                            AppNavTab.QUICK_LOG -> {
                                QuickLogScreen(
                                    state = uiState,
                                    onLogAction = { title, loc, note ->
                                        viewModel.logQuickAction(
                                            title = title,
                                            locationLabel = loc,
                                            note = note
                                        )
                                    },
                                    onDeleteLog = { viewModel.deleteQuickLog(it) },
                                    onClearAllLogs = { viewModel.clearAllLogs() }
                                )
                            }
                            AppNavTab.SETTINGS -> {
                                SettingsScreen(
                                    state = uiState,
                                    onDarkModeChange = { viewModel.updateDarkMode(it) },
                                    onPaletteChange = { viewModel.updateColorPalette(it) },
                                    onSoundStyleChange = { viewModel.updateSoundStyle(it) },
                                    onSoundVolumeChange = { viewModel.updateSoundVolume(it) },
                                    onBatteryIntervalChange = { viewModel.updateBatterySaverInterval(it) },
                                    onBackgroundMonitoringToggle = { viewModel.toggleBackgroundMonitoring(it) },
                                    onPreviewSound = { viewModel.previewSound(it) },
                                    onSendTestNotification = { viewModel.sendTestNotification() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoReminderTopAppBar(
    currentTab: AppNavTab,
    onTestGeofence: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "GeoReminder",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            if (currentTab == AppNavTab.REMINDERS) {
                IconButton(
                    onClick = onTestGeofence,
                    modifier = Modifier.testTag("test_geofence_action_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Test Geofence Triggers at Current Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun GeoReminderBottomBar(
    currentTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            selected = currentTab == AppNavTab.REMINDERS,
            onClick = { onTabSelected(AppNavTab.REMINDERS) },
            icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            label = { Text("Reminders") },
            modifier = Modifier.testTag("nav_reminders")
        )
        NavigationBarItem(
            selected = currentTab == AppNavTab.QUICK_LOG,
            onClick = { onTabSelected(AppNavTab.QUICK_LOG) },
            icon = { Icon(Icons.Default.Bolt, contentDescription = null) },
            label = { Text("Quick Log") },
            modifier = Modifier.testTag("nav_quick_log")
        )
        NavigationBarItem(
            selected = currentTab == AppNavTab.SETTINGS,
            onClick = { onTabSelected(AppNavTab.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Settings") },
            modifier = Modifier.testTag("nav_settings")
        )
    }
}

/**
 * Retained for backward-compatibility with screenshot tests
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
