package com.jackscanner.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jackscanner.R
import com.jackscanner.data.preferences.PreferencesManager
import com.jackscanner.ui.navigation.BlueMeanieNavGraph
import com.jackscanner.ui.navigation.Screen
import com.jackscanner.ui.navigation.bottomNavItems
import com.jackscanner.service.BleScanService
import com.jackscanner.ui.theme.BlueMeanieTheme
import com.jackscanner.ui.theme.getThemeColors
import com.jackscanner.domain.model.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startScanning()
        } else {
            Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_LONG).show()
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (isBluetoothEnabled()) {
            checkPermissionsAndScan()
        } else {
            Toast.makeText(this, getString(R.string.bluetooth_required), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        setContent {
            var appTheme by remember { mutableStateOf(AppTheme.BLUE_MEANIE_CLASSIC) }
            
            LaunchedEffect(Unit) {
                preferencesManager.selectedTheme.collect { theme ->
                    appTheme = theme
                }
            }
            
            BlueMeanieTheme(appTheme = appTheme) {
                MainContent(
                    onStartScan = { checkBluetoothState() },
                    preferencesManager = preferencesManager
                )
            }
        }
        
        // Handle navigation from notification
        handleNotificationIntent()
    }

    override fun onResume() {
        super.onResume()
        handleNotificationIntent()
    }

    private fun handleNotificationIntent() {
        val navigateTo = intent.getStringExtra("navigate")
        // Handle navigation from notification actions
        when (navigateTo) {
            "heatmap" -> {
                // Will be handled in navigation
            }
            "feed" -> {
                // Will be handled in navigation
            }
        }
    }

    private fun checkBluetoothState() {
        if (!isBluetoothEnabled()) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableIntent)
        } else {
            checkPermissionsAndScan()
        }
    }

    private fun checkPermissionsAndScan() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            startScanning()
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter?.isEnabled == true
    }

    private fun startScanning() {
        val intent = Intent(this, BleScanService::class.java).apply {
            action = BleScanService.ACTION_START_SCANNING
        }
        startForegroundService(intent)
    }

    fun stopScanning() {
        val intent = Intent(this, BleScanService::class.java).apply {
            action = BleScanService.ACTION_STOP_SCANNING
        }
        startService(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(
    onStartScan: () -> Unit,
    preferencesManager: PreferencesManager
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    var showOnboarding by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        val onboardingCompleted = preferencesManager.onboardingCompleted.first()
        showOnboarding = !onboardingCompleted
    }
    
    LaunchedEffect(showOnboarding) {
        if (showOnboarding) {
            navController.navigate(Screen.Onboarding.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    val startDestination = if (showOnboarding) Screen.Onboarding.route else Screen.Home.route
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute in bottomNavItems.map { it.route }) {
                NavigationBar(
                    containerColor = com.jackscanner.ui.theme.BlueMeanieTheme.colors.surface,
                    contentColor = com.jackscanner.ui.theme.BlueMeanieTheme.colors.primary
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = com.jackscanner.ui.theme.BlueMeanieTheme.colors.primary,
                                selectedTextColor = com.jackscanner.ui.theme.BlueMeanieTheme.colors.primary,
                                unselectedIconColor = com.jackscanner.ui.theme.BlueMeanieTheme.colors.textTertiary,
                                unselectedTextColor = com.jackscanner.ui.theme.BlueMeanieTheme.colors.textTertiary,
                                indicatorColor = com.jackscanner.ui.theme.BlueMeanieTheme.colors.primary.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            BlueMeanieNavGraph(
                navController = navController,
                startDestination = startDestination
            )
        }
    }
}