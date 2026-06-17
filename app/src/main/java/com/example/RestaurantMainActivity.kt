package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.KitchenSection
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PlatformViewModel

class RestaurantMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val requestBackgroundLocationLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            android.util.Log.d("RestaurantMainActivity", "Background location permission granted: $isGranted")
        }

        val requestPermissionsLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            val postNotificationsGranted = permissions[android.Manifest.permission.POST_NOTIFICATIONS] ?: false
            android.util.Log.d("RestaurantMainActivity", "Permissions callback: fine=$fineLocationGranted, coarse=$coarseLocationGranted, notify=$postNotificationsGranted")

            if (fineLocationGranted || coarseLocationGranted) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    val hasBackgroundPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (!hasBackgroundPermission) {
                        requestBackgroundLocationLauncher.launch(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                }
            }
        }

        val permissionsToRequest = mutableListOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())

        setContent {
            val viewModel: PlatformViewModel = viewModel()
            viewModel.switchRole("Restaurant")
            val isDark by viewModel.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KitchenSection(viewModel = viewModel)
                }
            }
        }
    }
}
